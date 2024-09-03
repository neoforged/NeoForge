package net.neoforged.neodev.installer;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ArchiveOperations;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class CreateArgsFile extends DefaultTask {
    @Inject
    public CreateArgsFile() {}

    @InputFile
    public abstract RegularFileProperty getTemplate();

    @Input
    public abstract Property<String> getFmlVersion();

    @Input
    public abstract Property<String> getMinecraftVersion();

    @Input
    public abstract Property<String> getNeoForgeVersion();

    @Input
    public abstract Property<String> getRawNeoFormVersion();

    @Input
    public abstract Property<String> getPathSeparator();

    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    public abstract ConfigurableFileCollection getModules();

    @Input
    public abstract ListProperty<String> getIgnoreList();

    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    public abstract ConfigurableFileCollection getClasspath();

    @InputFile
    public abstract RegularFileProperty getRawServerJar();

    @OutputFile
    public abstract RegularFileProperty getArgsFile();

    @Inject
    protected abstract ArchiveOperations getArchiveOperations();

    private String resolveClasspath() throws IOException {
        ArtifactPathsCollector classpathCollector = new ArtifactPathsCollector(getPathSeparator().get(), "libraries/");
        getClasspath().getAsFileTree().visit(classpathCollector);

        var ourClasspath = classpathCollector + getPathSeparator().get()
                + "libraries/net/minecraft/server/%s/server-%s-extra.jar".formatted(
                        getRawNeoFormVersion().get(), getRawNeoFormVersion().get());

        // The raw server jar also contains its own classpath.
        // We want to make sure that our versions of the libraries are used when there is a conflict.
        var ourClasspathEntries = Stream.of(ourClasspath.split(getPathSeparator().get()))
                .map(CreateArgsFile::stripVersionSuffix)
                .collect(Collectors.toSet());

        var serverClasspath = getArchiveOperations().zipTree(getRawServerJar())
                .filter(spec -> spec.getPath().endsWith("META-INF" + File.separator + "classpath-joined"))
                .getSingleFile();

        var filteredServerClasspath = Stream.of(Files.readString(serverClasspath.toPath()).split(";"))
                .filter(path -> !ourClasspathEntries.contains(stripVersionSuffix(path)))
                // Exclude the actual MC server jar, which is under versions/
                .filter(path -> path.startsWith("libraries/"))
                .collect(Collectors.joining(getPathSeparator().get()));

        return ourClasspath + getPathSeparator().get() + filteredServerClasspath;
    }

    // Example:
    // Convert "libraries/com/github/oshi/oshi-core/6.4.10/oshi-core-6.4.10.jar"
    // to "libraries/com/github/oshi/oshi-core".
    private static String stripVersionSuffix(String classpathEntry) {
        var parts = classpathEntry.split("/");
        return String.join("/", List.of(parts).subList(0, parts.length - 2));
    }

    @TaskAction
    public void createArgsFile() throws IOException {
        ArtifactPathsCollector modulePathCollector = new ArtifactPathsCollector(getPathSeparator().get(), "libraries/");

        getModules().getAsFileTree().visit(modulePathCollector);

        var replacements = new HashMap<String, String>();
        replacements.put("@MODULE_PATH@", modulePathCollector.toString());
        replacements.put("@MODULES@", "ALL-MODULE-PATH");
        replacements.put("@IGNORE_LIST@", String.join(",", getIgnoreList().get()));
        replacements.put("@PLUGIN_LAYER_LIBRARIES@", "");
        replacements.put("@GAME_LAYER_LIBRARIES@", "");
        replacements.put("@CLASS_PATH@", resolveClasspath());
        replacements.put("@TASK@", "forgeserver");
        replacements.put("@FORGE_VERSION@", getNeoForgeVersion().get());
        replacements.put("@FML_VERSION@", getFmlVersion().get());
        replacements.put("@MC_VERSION@", getMinecraftVersion().get());
        replacements.put("@MCP_VERSION@", getRawNeoFormVersion().get());

        var contents = Files.readString(getTemplate().get().getAsFile().toPath());
        for (var entry : replacements.entrySet()) {
            contents = contents.replaceAll(entry.getKey(), entry.getValue());
        }
        Files.writeString(getArgsFile().get().getAsFile().toPath(), contents);
    }
}
