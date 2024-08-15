package net.neoforged.neodev.installer;

import com.google.gson.GsonBuilder;
import net.neoforged.moddevgradle.internal.utils.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class CreateLauncherProfile extends DefaultTask {
    @Inject
    public CreateLauncherProfile() {}

    @Input
    public abstract Property<String> getFmlVersion();

    @Input
    public abstract Property<String> getMinecraftVersion();

    @Input
    public abstract Property<String> getNeoForgeVersion();

    @Input
    public abstract Property<String> getRawNeoFormVersion();

    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    public abstract ConfigurableFileCollection getLibraries();

    @Input
    public abstract ListProperty<URI> getRepositoryURLs();

    @Input
    public abstract ListProperty<String> getIgnoreList();

    @InputFiles
    @PathSensitive(PathSensitivity.NAME_ONLY)
    public abstract ConfigurableFileCollection getModulePath();

    @OutputFile
    public abstract RegularFileProperty getLauncherProfile();

    @TaskAction
    public void createLauncherProfile() throws IOException {
        var time = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

        getLogger().info("Collecting libraries for Launcher Profile");
        var profileFiller = new LibraryCollector(getRepositoryURLs().get());
        getLibraries().getAsFileTree().visit(profileFiller);
        var libraries = profileFiller.getLibraries();

        var gameArguments = new ArrayList<>(List.of(
                "--fml.neoForgeVersion", getNeoForgeVersion().get(),
                "--fml.fmlVersion", getFmlVersion().get(),
                "--fml.mcVersion", getMinecraftVersion().get(),
                "--fml.neoFormVersion", getRawNeoFormVersion().get(),
                "--launchTarget", "forgeclient"));

        var jvmArguments = new ArrayList<>(List.of(
                "-Djava.net.preferIPv6Addresses=system",
                "-DignoreList=" + String.join(",", getIgnoreList().get()),
                // TODO: is this still relevant in any way?
                "-DmergeModules=jna-5.10.0.jar,jna-platform-5.10.0.jar",
                "-DlibraryDirectory=${library_directory}"));

        var modulePathCollector = new ArtifactPathsCollector("${classpath_separator}", "${library_directory}/");
        getModulePath().getAsFileTree().visit(modulePathCollector);
        jvmArguments.add("-p");
        jvmArguments.add(modulePathCollector.toString());

        jvmArguments.addAll(List.of(
                "--add-modules", "ALL-MODULE-PATH",
                "--add-opens", "java.base/java.util.jar=cpw.mods.securejarhandler",
                "--add-opens", "java.base/java.lang.invoke=cpw.mods.securejarhandler",
                "--add-exports", "java.base/sun.security.util=cpw.mods.securejarhandler",
                "--add-exports", "jdk.naming.dns/com.sun.jndi.dns=java.naming"));

        var arguments = new LinkedHashMap<String, List<String>>();
        arguments.put("game", gameArguments);
        arguments.put("jvm", jvmArguments);

        var profile = new LauncherProfile(
                "neoforge-%s".formatted(getNeoForgeVersion().get()),
                time,
                time,
                "release",
                "cpw.mods.bootstraplauncher.BootstrapLauncher",
                getMinecraftVersion().get(),
                arguments,
                libraries
        );

        FileUtils.writeStringSafe(
                getLauncherProfile().getAsFile().get().toPath(),
                new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(profile),
                // TODO: Not sure what this should be? Most likely the file is ASCII.
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

