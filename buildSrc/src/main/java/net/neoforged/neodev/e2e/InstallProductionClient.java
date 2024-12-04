package net.neoforged.neodev.e2e;

import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

/**
 * Downloads and installs a production NeoForge client.
 * By extending this task from {@link JavaExec}, it's possible to debug the actual legacy installer
 * via IntelliJ directly.
 */
public abstract class InstallProductionClient extends JavaExec {
    /**
     * This file collection should contain exactly one file:
     * The NeoForge Installer Jar-File.
     */
    @InputFiles
    public abstract ConfigurableFileCollection getInstaller();

    /**
     * Where NeoForge should be installed.
     */
    @OutputDirectory
    public abstract DirectoryProperty getInstallationDir();

    @Inject
    public InstallProductionClient() {
        classpath(getInstaller());
    }

    @TaskAction
    @Override
    public void exec() {
        var installDir = getInstallationDir().getAsFile().get().toPath().toAbsolutePath();

        // Installer looks for this file
        var profilesJsonPath = installDir.resolve("launcher_profiles.json");
        try {
            Files.writeString(profilesJsonPath, "{}");
        } catch (IOException e) {
            throw new GradleException("Failed to write fake launcher profiles file.", e);
        }

        setWorkingDir(installDir.toFile());
        args("--install-client", installDir.toString());
        try {
            setStandardOutput(new BufferedOutputStream(Files.newOutputStream(installDir.resolve("install.log"))));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        super.exec();
    }
}
