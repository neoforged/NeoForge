package net.neoforged.neodev.installer;

import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import net.neoforged.neodev.utils.DependencyUtils;
import net.neoforged.neodev.utils.FileUtils;
import net.neoforged.neodev.utils.MavenIdentifier;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

/**
 * Creates the JSON file for running NeoForge via the Vanilla launcher.
 */
public abstract class CreateLauncherProfile extends DefaultTask {
    @Inject
    public CreateLauncherProfile() {}

    @Input
    public abstract Property<String> getMinecraftVersion();

    @Input
    public abstract Property<String> getNeoForgeVersion();

    @Input
    public abstract Property<String> getRawNeoFormVersion();

    @Inject
    public abstract ObjectFactory getObjectFactory();

    @Nested
    public abstract ListProperty<Library> getAdditionalLibraries();

    @InputFile
    public abstract RegularFileProperty getUniversalJar();

    @Nested
    protected abstract ListProperty<IdentifiedFile> getLibraryFiles();

    @Nested
    protected abstract ListProperty<IdentifiedFile> getProjectLibraryFiles();

    /**
     * The libraries that the Minecraft version we target already has as dependencies.
     */
    @Input
    protected abstract ListProperty<MavenIdentifier> getMinecraftLibraryIds();

    public void addLibraries(Configuration libraries) {
        getLibraryFiles().addAll(IdentifiedFile.listFromConfiguration(getObjectFactory(), libraries, this));
    }

    public void addProjectLibraries(Configuration libraries) {
        getProjectLibraryFiles().addAll(IdentifiedFile.listFromConfiguration(getObjectFactory(), libraries, this));
    }

    public void setMinecraftLibraries(Configuration configuration) {
        getMinecraftLibraryIds().set(configuration.getIncoming().getArtifacts().getResolvedArtifacts().map(
                artifacts -> artifacts.stream()
                        .map(DependencyUtils::guessMavenIdentifier)
                        .toList()));
    }

    @Input
    public abstract ListProperty<URI> getRepositoryURLs();

    @OutputFile
    public abstract RegularFileProperty getLauncherProfile();

    @Inject
    public abstract ProviderFactory getProviderFactory();

    @TaskAction
    public void createLauncherProfile() throws IOException {
        var time = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

        getLogger().info("Collecting libraries for Launcher Profile");

        // We have to filter out any libraries that are already part of Minecraft, since
        // the Vanilla Launcher overrides not only the classifier we add, but also all other
        // classifiers of the same library. Example: if we add lwjgl, Vanilla removes all lwjgl natives, unless
        // we also add them.
        var minecraftLibraryVersions = getMinecraftLibraryIds().get().stream()
                .collect(Collectors.toMap(mavenId -> mavenId.withVersion("*").artifactNotation(), mavenId -> new DefaultArtifactVersion(mavenId.version())));
        var libraryFiles = new ArrayList<>(getLibraryFiles().get());
        libraryFiles.removeIf(identifiedFile -> {
            var libraryId = identifiedFile.getIdentifier().get();
            var idWithoutVersion = libraryId.withVersion("*");
            var minecraftVersion = minecraftLibraryVersions.get(idWithoutVersion.artifactNotation());
            if (minecraftVersion == null) {
                return false; // If Minecraft doesn't have the library at all, we add it ourselves.
            }
            // Otherwise we have to compare versions
            var libraryVersion = new DefaultArtifactVersion(libraryId.version());
            if (libraryVersion.compareTo(minecraftVersion) < 0) {
                throw new GradleException("Downgrading library " + libraryId + " from the Minecraft version " + minecraftVersion);
            } else if (libraryVersion.compareTo(minecraftVersion) == 0) {
                getLogger().info("Removing library {} since Minecraft already ships version {}", libraryId, minecraftVersion);
                return true; // Remove, if it's older or equal
            } else {
                return false;
            }
        });

        var libraries = new ArrayList<>(LibraryCollector.resolveLibraries(getRepositoryURLs().get(), libraryFiles));
        libraries.addAll(LibraryCollector.expectedPublishedJar(getProjectLibraryFiles().get(), getProviderFactory()));
        libraries.addAll(getAdditionalLibraries().get());

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
                                getNeoForgeVersion().get())))));

        var gameArguments = new ArrayList<>(List.of(
                "--fml.neoForgeVersion", getNeoForgeVersion().get(),
                "--fml.mcVersion", getMinecraftVersion().get(),
                "--fml.neoFormVersion", getRawNeoFormVersion().get()));

        var jvmArguments = new ArrayList<>(List.of(
                "-Djava.net.preferIPv6Addresses=system",
                "-DlibraryDirectory=${library_directory}"));

        jvmArguments.addAll(List.of(
                "--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED",
                "--add-exports", "jdk.naming.dns/com.sun.jndi.dns=java.naming"));

        var arguments = new LinkedHashMap<String, List<String>>();
        arguments.put("game", gameArguments);
        arguments.put("jvm", jvmArguments);

        var profile = new LauncherProfile(
                "neoforge-%s".formatted(getNeoForgeVersion().get()),
                time,
                time,
                "release",
                "net.neoforged.fml.startup.Client",
                getMinecraftVersion().get(),
                arguments,
                libraries);

        FileUtils.writeStringSafe(
                getLauncherProfile().getAsFile().get().toPath(),
                new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(profile),
                StandardCharsets.UTF_8);
    }
}

record LauncherProfile(
        String id,
        String time,
        String releaseTime,
        String type,
        String mainClass,
        String inheritsFrom,
        Map<String, List<String>> arguments,
        List<Library> libraries) {}
