package net.neoforged.neodev.e2e;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

/**
 * Runs the installer produced by the main build to install a dedicated server in a chosen directory.
 */
public abstract class InstallProductionServer extends JavaExec {
    /**
     * The NeoForge installer jar is expected to be the only file in this file collection.
     */
    @InputFiles
    public abstract ConfigurableFileCollection getInstaller();

    /**
     * Where the server should be installed.
     */
    @OutputDirectory
    public abstract DirectoryProperty getInstallationDir();

    /**
     * Points to the server.jar produced by the installer.
     */
    @OutputFile
    public abstract RegularFileProperty getServerLauncher();

    @Inject
    public InstallProductionServer() {
        classpath(getInstaller());
        getServerLauncher().set(getInstallationDir().map(id -> id.file("server.jar")));
        getServerLauncher().finalizeValueOnRead();
    }

    @TaskAction
    @Override
    public void exec() {
        var installDir = getInstallationDir().getAsFile().get().toPath().toAbsolutePath();

        setWorkingDir(installDir.toFile());
        args("--install-server", installDir.toString());
        args("--server.jar");
        try {
            setStandardOutput(new BufferedOutputStream(Files.newOutputStream(installDir.resolve("install.log"))));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        super.exec();
    }
}
