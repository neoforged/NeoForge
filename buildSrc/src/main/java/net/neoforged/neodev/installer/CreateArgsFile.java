package net.neoforged.neodev.installer;

import net.neoforged.neodev.utils.DependencyUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ArchiveOperations;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Creates the JVM/program argument files used by the dedicated server launcher.
 */
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

    @Input
    protected abstract ListProperty<String> getModules();

    @Input
    protected abstract ListProperty<String> getJavaAgents();

    @Input
    public abstract ListProperty<String> getIgnoreList();

    @Input
    protected abstract ListProperty<String> getClasspath();

    public void setLibraries(Configuration classpath, Configuration modulePath, Configuration javaAgents) {
        getClasspath().set(DependencyUtils.configurationToClasspathItems(classpath, "libraries/"));
        getModules().set(DependencyUtils.configurationToClasspathItems(modulePath, "libraries/"));
        getJavaAgents().set(DependencyUtils.configurationToClasspathItems(javaAgents, "libraries/"));
    }

    @InputFile
    public abstract RegularFileProperty getRawServerJar();

    @OutputFile
    public abstract RegularFileProperty getArgsFile();

    @Inject
    protected abstract ArchiveOperations getArchiveOperations();

    private String resolveClasspath() throws IOException {
        String pathSeparator = getPathSeparator().get();
        var classpathItems = new ArrayList<>(getClasspath().get());
        classpathItems.add("libraries/net/minecraft/server/%s/server-%s-extra.jar".formatted(
                getRawNeoFormVersion().get(), getRawNeoFormVersion().get()));

        // Remove any java agents, since they automatically are on the classpath
        classpathItems.removeAll(getJavaAgents().get());

        // The raw server jar also contains its own classpath.
        // We want to make sure that our versions of the libraries are used when there is a conflict.
        var ourClasspathEntries = classpathItems.stream()
                .map(CreateArgsFile::stripVersionSuffix)
                .collect(Collectors.toSet());

        var serverClasspath = getArchiveOperations().zipTree(getRawServerJar())
                .filter(spec -> spec.getPath().endsWith("META-INF" + File.separator + "classpath-joined"))
                .getSingleFile();

        var filteredServerClasspath = Stream.of(Files.readString(serverClasspath.toPath()).split(";"))
                .filter(path -> !ourClasspathEntries.contains(stripVersionSuffix(path)))
                // Exclude the actual MC server jar, which is under versions/
                .filter(path -> path.startsWith("libraries/"))
                .collect(Collectors.joining(pathSeparator));

        classpathItems.add(filteredServerClasspath);
        return String.join(pathSeparator, classpathItems);
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
        var pathSeparator = getPathSeparator().get();

        var jvmOptions = new ArrayList<String>();
        for (var javaAgent : getJavaAgents().get()) {
            jvmOptions.add("-javaagent:" + javaAgent);
        }

        jvmOptions.add("-cp");
        jvmOptions.add(resolveClasspath());

        var replacements = new HashMap<String, String>();
        replacements.put("@JVM_OPTIONS@", String.join("\n", jvmOptions));
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
