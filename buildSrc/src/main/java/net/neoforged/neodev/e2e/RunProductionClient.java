package net.neoforged.neodev.e2e;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.neoforged.neodev.installer.IdentifiedFile;
import net.neoforged.neodev.utils.MavenIdentifier;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.gradle.process.JavaExecSpec;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runs a production client previously installed by {@link InstallProductionClient}.
 * <p>
 * This task has to extend from  {@link JavaExec} instead of using {@link org.gradle.process.ExecOperations} internally
 * to allow debugging it via IntelliJ directly.
 * (Technically, implementing {@link org.gradle.process.JavaForkOptions} would suffice).
 * <p>
 * The main complication of this task is evaluating the Vanilla version manifest and building a libraries
 * directory and classpath as the Vanilla launcher would.
 */
public abstract class RunProductionClient extends JavaExec {
    private final ExecOperations execOperations;

    /**
     * The folder where the game was installed.
     */
    @InputDirectory
    public abstract DirectoryProperty getInstallationDir();

    /**
     * The pre-processed libraries as a file collection.
     */
    @Nested
    public abstract ListProperty<IdentifiedFile> getLibraryFiles();

    /**
     * The asset properties file produced by {@link net.neoforged.nfrtgradle.DownloadAssets}.
     */
    @InputFile
    public abstract RegularFileProperty getAssetPropertiesFile();

    /**
     * The Minecraft version matching the NeoForge version to install.
     */
    @Input
    public abstract Property<String> getMinecraftVersion();

    /**
     * The NeoForge version, used for placeholders when launching the game.
     * It needs to match the installer used.
     */
    @Input
    public abstract Property<String> getNeoForgeVersion();

    /**
     * The original, unmodified client jar.
     * The Vanilla launcher puts this on the classpath when it launches the game.
     */
    @InputFile
    public abstract RegularFileProperty getOriginalClientJar();

    @Inject
    public RunProductionClient(ExecOperations execOperations) {
        this.execOperations = execOperations;
    }

    @TaskAction
    @Override
    public void exec() {
        var installDir = getInstallationDir().getAsFile().get().toPath();
        var nativesDir = installDir.resolve("natives");
        try {
            Files.createDirectories(nativesDir);
        } catch (IOException e) {
            throw new GradleException("Failed to pre-create natives directory " + nativesDir, e);
        }
        var librariesDir = installDir.resolve("libraries");

        var minecraftVersion = getMinecraftVersion().get();
        var versionId = "neoforge-" + getNeoForgeVersion().get();

        var assetProperties = new Properties();
        try (var in = new FileInputStream(getAssetPropertiesFile().getAsFile().get())) {
            assetProperties.load(in);
        } catch (IOException e) {
            throw new GradleException("Failed to read asset properties " + getAssetPropertiesFile(), e);
        }

        var assetIndex = Objects.requireNonNull(assetProperties.getProperty("asset_index"), "asset_index");
        var assetsRoot = Objects.requireNonNull(assetProperties.getProperty("assets_root"), "assets_root");

        // Set up the placeholders generally used by Vanilla profiles in their argument definitions.
        var placeholders = new HashMap<String, String>();
        placeholders.put("auth_player_name", "Dev");
        placeholders.put("version_name", minecraftVersion);
        placeholders.put("game_directory", installDir.toAbsolutePath().toString());
        placeholders.put("auth_uuid", "00000000-0000-4000-8000-000000000000");
        placeholders.put("auth_access_token", "0");
        placeholders.put("clientid", "0");
        placeholders.put("auth_xuid", "0");
        placeholders.put("user_type", "legacy");
        placeholders.put("version_type", "release");
        placeholders.put("assets_index_name", assetIndex);
        placeholders.put("assets_root", assetsRoot);
        placeholders.put("launcher_name", "NeoForgeProdInstallation");
        placeholders.put("launcher_version", "1.0");
        placeholders.put("natives_directory", nativesDir.toAbsolutePath().toString());
        // These are used by NF but provided by the launcher
        placeholders.put("library_directory", librariesDir.toAbsolutePath().toString());
        placeholders.put("classpath_separator", File.pathSeparator);

        execOperations.javaexec(spec -> {
            // The JVM args at this point may include debugging options when started through IntelliJ
            spec.jvmArgs(getJvmArguments().get());
            spec.workingDir(installDir);

            spec.environment(getEnvironment());
            applyVersionManifest(installDir, versionId, placeholders, librariesDir, spec);
        });
    }

    /**
     * Applies a Vanilla Launcher version manifest to the JavaForkOptions.
     */
    private void applyVersionManifest(Path installDir,
                                      String versionId,
                                      Map<String, String> placeholders,
                                      Path librariesDir,
                                      JavaExecSpec spec) {
        var manifests = loadVersionManifests(installDir, versionId);

        var mergedProgramArgs = new ArrayList<String>();
        var mergedJvmArgs = new ArrayList<String>();

        for (var manifest : manifests) {
            var mainClass = manifest.getAsJsonPrimitive("mainClass");
            if (mainClass != null) {
                spec.getMainClass().set(mainClass.getAsString());
            }

            mergedProgramArgs.addAll(getArguments(manifest, "game"));
            mergedJvmArgs.addAll(getArguments(manifest, "jvm"));
        }

        // Index all available libraries
        var availableLibraries = new HashMap<MavenIdentifier, Path>();
        for (var identifiedFile : getLibraryFiles().get()) {
            availableLibraries.put(
                    identifiedFile.getIdentifier().get(),
                    identifiedFile.getFile().get().getAsFile().toPath()
            );
        }

        // The libraries are built in reverse, and libraries already added are not added again from parent manifests
        var librariesAdded = new HashSet<MavenIdentifier>();
        var classpathItems = new ArrayList<String>();
        for (var i = manifests.size() - 1; i >= 0; i--) {
            var manifest = manifests.get(i);

            var libraries = manifest.getAsJsonArray("libraries");
            for (var library : libraries) {
                var libraryObj = library.getAsJsonObject();

                // Skip if disabled by rule
                if (isDisabledByRules(libraryObj)) {
                    getLogger().info("Skipping library {} since it's condition is not met.", libraryObj);
                    continue;
                }

                var id = MavenIdentifier.parse(libraryObj.get("name").getAsString());

                // We use this to deduplicate the same library in different versions across manifests
                var idWithoutVersion = new MavenIdentifier(
                        id.group(),
                        id.artifact(),
                        "",
                        id.classifier(),
                        id.extension()
                );

                if (!librariesAdded.add(idWithoutVersion)) {
                    continue; // The library was overridden by a child profile
                }

                // Try finding the library in the classpath we got from Gradle
                var availableLibrary = availableLibraries.get(id);
                if (availableLibrary == null) {
                    throw new GradleException("Version manifest asks for " + id + " but this library is not available through Gradle.");
                }

                // Copy over the library to the libraries directory, since our loader only deduplicates class-path
                // items with module-path items when they are at the same location (and the module-path is defined
                // relative to the libraries directory).
                Path destination = librariesDir.resolve(id.repositoryPath());
                copyIfNeeded(availableLibrary, destination);
                classpathItems.add(destination.toAbsolutePath().toString());
            }
        }

        // The Vanilla launcher adds the actual game jar (obfuscated) as the last classpath item
        var gameJar = installDir.resolve("versions").resolve(versionId).resolve(versionId + ".jar");
        copyIfNeeded(getOriginalClientJar().get().getAsFile().toPath(), gameJar);
        classpathItems.add(gameJar.toAbsolutePath().toString());
        placeholders.put("version_name", versionId);

        var classpath = String.join(File.pathSeparator, classpathItems);
        placeholders.putIfAbsent("classpath", classpath);

        expandPlaceholders(mergedProgramArgs, placeholders);
        spec.args(mergedProgramArgs);
        expandPlaceholders(mergedJvmArgs, placeholders);
        spec.jvmArgs(mergedJvmArgs);
    }

    // Returns the inherited manifests first
    private static List<JsonObject> loadVersionManifests(Path installDir, String versionId) {
        // Read back the version manifest and get the startup arguments
        var manifestPath = installDir.resolve("versions").resolve(versionId).resolve(versionId + ".json");
        JsonObject manifest;
        try {
            manifest = readJson(manifestPath);
        } catch (IOException e) {
            throw new GradleException("Failed to read launcher profile " + manifestPath, e);
        }

        var result = new ArrayList<JsonObject>();
        var inheritsFrom = manifest.getAsJsonPrimitive("inheritsFrom");
        if (inheritsFrom != null) {
            result.addAll(loadVersionManifests(installDir, inheritsFrom.getAsString()));
        }

        result.add(manifest);

        return result;
    }

    private static void expandPlaceholders(List<String> args, Map<String, String> variables) {
        var pattern = Pattern.compile("\\$\\{([^}]+)}");

        args.replaceAll(s -> {
            var matcher = pattern.matcher(s);
            return matcher.replaceAll(match -> {
                var variable = match.group(1);
                return Matcher.quoteReplacement(variables.getOrDefault(variable, matcher.group()));
            });
        });
    }

    private static List<String> getArguments(JsonObject manifest, String kind) {
        var result = new ArrayList<String>();

        var gameArgs = manifest.getAsJsonObject("arguments").getAsJsonArray(kind);
        for (var gameArg : gameArgs) {
            if (gameArg.isJsonObject()) {
                var conditionalArgument = gameArg.getAsJsonObject();
                if (!isDisabledByRules(conditionalArgument)) {
                    var value = conditionalArgument.get("value");
                    if (value.isJsonPrimitive()) {
                        result.add(value.getAsString());
                    } else {
                        for (var valueEl : value.getAsJsonArray()) {
                            result.add(valueEl.getAsString());
                        }
                    }
                }
            } else {
                result.add(gameArg.getAsString());
            }
        }

        return result;
    }

    private static boolean isDisabledByRules(JsonObject ruleObject) {
        var rules = ruleObject.getAsJsonArray("rules");
        if (rules == null) {
            return false;
        }

        for (var ruleEl : rules) {
            var rule = ruleEl.getAsJsonObject();
            boolean allow = "allow".equals(rule.getAsJsonPrimitive("action").getAsString());
            // We only care about "os" rules
            if (rule.has("os")) {
                var os = rule.getAsJsonObject("os");
                var name = os.getAsJsonPrimitive("name");
                var arch = os.getAsJsonPrimitive("arch");
                boolean ruleMatches = (name == null || isCurrentOsName(name.getAsString())) && (arch == null || isCurrentOsArch(arch.getAsString()));
                if (ruleMatches != allow) {
                    return true;
                }
            } else {
                // We assume unknown rules do not apply
                return true;
            }
        }
        return false;
    }

    private static boolean isCurrentOsName(String os) {
        return switch (os) {
            case "windows" -> Os.isFamily(Os.FAMILY_WINDOWS);
            case "osx" -> Os.isFamily(Os.FAMILY_MAC);
            case "linux" -> Os.isFamily(Os.FAMILY_UNIX);
            default -> false;
        };
    }

    private static boolean isCurrentOsArch(String arch) {
        return switch (arch) {
            case "x86" -> System.getProperty("os.arch").equals("x86");
            default -> false;
        };
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, JsonObject.class);
        }
    }

    private static void copyIfNeeded(Path source, Path destination) {
        try {
            if (!Files.exists(destination)
                || !Objects.equals(Files.getLastModifiedTime(destination), Files.getLastModifiedTime(source))
                || Files.size(destination) != Files.size(source)) {
                Files.createDirectories(destination.getParent());
                Files.copy(source, destination, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new GradleException("Failed to copy " + source + " to " + destination + ": " + e, e);
        }
    }
}
