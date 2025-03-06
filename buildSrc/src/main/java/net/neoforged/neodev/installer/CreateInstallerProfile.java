package net.neoforged.neodev.installer;

import com.google.gson.GsonBuilder;
import net.neoforged.neodev.utils.FileUtils;
import net.neoforged.neodev.utils.MavenIdentifier;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Creates the JSON profile used by legacyinstaller for installing the client into the vanilla launcher,
 * or installing a dedicated server.
 */
public abstract class CreateInstallerProfile extends DefaultTask {
    @Inject
    public CreateInstallerProfile() {}

    @Input
    public abstract Property<String> getMinecraftVersion();

    @Input
    public abstract Property<String> getNeoForgeVersion();

    @Input
    public abstract Property<String> getMcAndNeoFormVersion();

    @InputFile
    public abstract RegularFileProperty getIcon();

    @Nested
    protected abstract ListProperty<IdentifiedFile> getLibraryFiles();

    public void addLibraries(Configuration libraries) {
        getLibraryFiles().addAll(IdentifiedFile.listFromConfiguration(getProject(), libraries));
    }

    @Input
    public abstract ListProperty<URI> getRepositoryURLs();

    @Input
    public abstract MapProperty<InstallerProcessor, List<String>> getProcessorClasspaths();

    @Input
    public abstract MapProperty<InstallerProcessor, String> getProcessorGavs();

    @InputFile
    public abstract RegularFileProperty getUniversalJar();

    @OutputFile
    public abstract RegularFileProperty getInstallerProfile();

    private void addProcessor(List<ProcessorEntry> processors, @Nullable List<String> sides, InstallerProcessor processor, List<String> args) {
        var classpath = getProcessorClasspaths().get().get(processor);
        var mainJar = getProcessorGavs().get().get(processor);
        if (!classpath.contains(mainJar)) {
            throw new IllegalStateException("Processor %s is not included in its own classpath %s".formatted(mainJar, classpath));
        }
        processors.add(new ProcessorEntry(sides, mainJar, classpath, args));
    }

    @TaskAction
    public void createInstallerProfile() throws IOException {
        var icon = "data:image/png;base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(getIcon().getAsFile().get().toPath()));

        var data = new LinkedHashMap<String, LauncherDataEntry>();
        var neoFormVersion = getMcAndNeoFormVersion().get();
        data.put("MAPPINGS", new LauncherDataEntry(String.format("[net.neoforged:neoform:%s:mappings@txt]", neoFormVersion), String.format("[net.neoforged:neoform:%s:mappings@txt]", neoFormVersion)));
        data.put("MOJMAPS", new LauncherDataEntry(String.format("[net.minecraft:client:%s:mappings@txt]", neoFormVersion), String.format("[net.minecraft:server:%s:mappings@txt]", neoFormVersion)));
        data.put("MERGED_MAPPINGS", new LauncherDataEntry(String.format("[net.neoforged:neoform:%s:mappings-merged@txt]", neoFormVersion), String.format("[net.neoforged:neoform:%s:mappings-merged@txt]", neoFormVersion)));
        data.put("BINPATCH", new LauncherDataEntry("/data/client.lzma", "/data/server.lzma"));
        data.put("MC_UNPACKED", new LauncherDataEntry(String.format("[net.minecraft:client:%s:unpacked]", neoFormVersion), String.format("[net.minecraft:server:%s:unpacked]", neoFormVersion)));
        data.put("MC_SLIM", new LauncherDataEntry(String.format("[net.minecraft:client:%s:slim]", neoFormVersion), String.format("[net.minecraft:server:%s:slim]", neoFormVersion)));
        data.put("MC_EXTRA", new LauncherDataEntry(String.format("[net.minecraft:client:%s:extra]", neoFormVersion), String.format("[net.minecraft:server:%s:extra]", neoFormVersion)));
        data.put("MC_SRG", new LauncherDataEntry(String.format("[net.minecraft:client:%s:srg]", neoFormVersion), String.format("[net.minecraft:server:%s:srg]", neoFormVersion)));
        data.put("PATCHED", new LauncherDataEntry(String.format("[%s:%s:%s:client]", "net.neoforged", "neoforge", getNeoForgeVersion().get()), String.format("[%s:%s:%s:server]", "net.neoforged", "neoforge", getNeoForgeVersion().get())));
        data.put("MCP_VERSION", new LauncherDataEntry(String.format("'%s'", neoFormVersion), String.format("'%s'", neoFormVersion)));

        var processors = new ArrayList<ProcessorEntry>();
        BiConsumer<InstallerProcessor, List<String>> commonProcessor = (processor, args) -> addProcessor(processors, null, processor, args);
        BiConsumer<InstallerProcessor, List<String>> clientProcessor = (processor, args) -> addProcessor(processors, List.of("client"), processor, args);
        BiConsumer<InstallerProcessor, List<String>> serverProcessor = (processor, args) -> addProcessor(processors, List.of("server"), processor, args);

        serverProcessor.accept(InstallerProcessor.INSTALLERTOOLS,
                List.of("--task", "EXTRACT_FILES", "--archive", "{INSTALLER}",

                        "--from", "data/run.sh", "--to", "{ROOT}/run.sh", "--exec", "{ROOT}/run.sh",

                        "--from", "data/run.bat", "--to", "{ROOT}/run.bat",

                        "--from", "data/user_jvm_args.txt", "--to", "{ROOT}/user_jvm_args.txt", "--optional", "{ROOT}/user_jvm_args.txt",

                        "--from", "data/win_args.txt", "--to", "{ROOT}/libraries/net/neoforged/neoforge/%s/win_args.txt".formatted(getNeoForgeVersion().get()),

                        "--from", "data/unix_args.txt", "--to", "{ROOT}/libraries/net/neoforged/neoforge/%s/unix_args.txt".formatted(getNeoForgeVersion().get()))
        );
        serverProcessor.accept(InstallerProcessor.INSTALLERTOOLS,
                List.of("--task", "BUNDLER_EXTRACT", "--input", "{MINECRAFT_JAR}", "--output", "{ROOT}/libraries/", "--libraries")
        );
        serverProcessor.accept(InstallerProcessor.INSTALLERTOOLS,
                List.of("--task", "BUNDLER_EXTRACT", "--input", "{MINECRAFT_JAR}", "--output", "{MC_UNPACKED}", "--jar-only")
        );
        var neoformDependency = "net.neoforged:neoform:" + getMcAndNeoFormVersion().get() + "@zip";;
        commonProcessor.accept(InstallerProcessor.INSTALLERTOOLS,
                List.of("--task", "MCP_DATA", "--input", String.format("[%s]", neoformDependency), "--output", "{MAPPINGS}", "--key", "mappings")
        );
        commonProcessor.accept(InstallerProcessor.INSTALLERTOOLS,
                List.of("--task", "DOWNLOAD_MOJMAPS", "--version", getMinecraftVersion().get(), "--side", "{SIDE}", "--output", "{MOJMAPS}")
        );
        commonProcessor.accept(InstallerProcessor.INSTALLERTOOLS,
                List.of("--task", "MERGE_MAPPING", "--left", "{MAPPINGS}", "--right", "{MOJMAPS}", "--output", "{MERGED_MAPPINGS}", "--classes", "--fields", "--methods", "--reverse-right")
        );
        clientProcessor.accept(InstallerProcessor.JARSPLITTER,
                List.of("--input", "{MINECRAFT_JAR}", "--slim", "{MC_SLIM}", "--extra", "{MC_EXTRA}", "--srg", "{MERGED_MAPPINGS}")
        );
        serverProcessor.accept(InstallerProcessor.JARSPLITTER,
                List.of("--input", "{MC_UNPACKED}", "--slim", "{MC_SLIM}", "--extra", "{MC_EXTRA}", "--srg", "{MERGED_MAPPINGS}")
        );
        // Note that the options supplied here have to match the ones used in the RemapJar task used to generate the binary patches
        commonProcessor.accept(InstallerProcessor.FART,
                List.of("--input", "{MC_SLIM}", "--output", "{MC_SRG}", "--names", "{MERGED_MAPPINGS}", "--ann-fix", "--ids-fix", "--src-fix", "--record-fix")
        );
        commonProcessor.accept(InstallerProcessor.BINPATCHER,
                List.of("--clean", "{MC_SRG}", "--output", "{PATCHED}", "--apply", "{BINPATCH}")
        );

        getLogger().info("Collecting libraries for Installer Profile");
        // Remove potential duplicates.
        var libraryFilesToResolve = new LinkedHashMap<MavenIdentifier, IdentifiedFile>(getLibraryFiles().get().size());
        for (var libraryFile : getLibraryFiles().get()) {
            var existingFile = libraryFilesToResolve.putIfAbsent(libraryFile.getIdentifier().get(), libraryFile);
            if (existingFile != null) {
                var existing = existingFile.getFile().getAsFile().get();
                var duplicate = libraryFile.getFile().getAsFile().get();
                if (!existing.equals(duplicate)) {
                    throw new IllegalArgumentException("Cannot resolve installer profile! Library %s has different files: %s and %s.".formatted(
                            libraryFile.getIdentifier().get(),
                            existing,
                            duplicate));
                }
            }
        }
        var libraries = new ArrayList<>(
                LibraryCollector.resolveLibraries(getRepositoryURLs().get(), libraryFilesToResolve.values()));

        var universalJar = getUniversalJar().getAsFile().get().toPath();
        libraries.add(new Library(
                "net.neoforged:neoforge:%s:universal".formatted(getNeoForgeVersion().get()),
                new LibraryDownload(new LibraryArtifact(
                        LibraryCollector.sha1Hash(universalJar),
                        Files.size(universalJar),
                        "https://maven.neoforged.net/releases/net/neoforged/neoforge/%s/neoforge-%s-universal.jar".formatted(
                                getNeoForgeVersion().get(),
                                getNeoForgeVersion().get()),
                        "net/neoforged/neoforge/%s/neoforge-%s-universal.jar".formatted(
                                getNeoForgeVersion().get(),
                                getNeoForgeVersion().get())
                ))));

        var profile = new InstallerProfile(
                1,
                "NeoForge",
                "neoforge-%s".formatted(getNeoForgeVersion().get()),
                icon,
                getMinecraftVersion().get(),
                "/version.json",
                "/big_logo.png",
                "Welcome to the simple NeoForge installer",
                "https://mirrors.neoforged.net",
                true,
                data,
                processors,
                libraries,
                "{LIBRARY_DIR}/net/minecraft/server/{MINECRAFT_VERSION}/server-{MINECRAFT_VERSION}.jar"
        );

        FileUtils.writeStringSafe(
                getInstallerProfile().getAsFile().get().toPath(),
                new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(profile),
                StandardCharsets.UTF_8
        );
    }
}

record InstallerProfile(
        int spec,
        String profile,
        String version,
        String icon,
        String minecraft,
        String json,
        String logo,
        String welcome,
        String mirrorList,
        boolean hideExtract,
        Map<String, LauncherDataEntry> data,
        List<ProcessorEntry> processors,
        List<Library> libraries,
        String serverJarPath) {}

record LauncherDataEntry(
        String client,
        String server) {}

record ProcessorEntry(
        @Nullable
        List<String> sides,
        String jar,
        List<String> classpath,
        List<String> args) {}
