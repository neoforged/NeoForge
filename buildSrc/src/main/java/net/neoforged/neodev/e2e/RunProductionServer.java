package net.neoforged.neodev.e2e;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;

/**
 * Runs the {@code server.jar} installed by our installer using {@link InstallProductionServer}.
 * <p>
 * This task has to extend from  {@link JavaExec} instead of using {@link ExecOperations} internally
 * to allow debugging the launched server with IntelliJ.
 * (Technically, implementing {@link org.gradle.process.JavaForkOptions} would suffice).
 */
public abstract class RunProductionServer extends JavaExec {
    private final ExecOperations execOperations;

    /**
     * The folder where the game was installed.
     */
    @InputDirectory
    public abstract DirectoryProperty getInstallationDir();

    @Inject
    public RunProductionServer(ExecOperations execOperations) {
        this.execOperations = execOperations;
    }

    @TaskAction
    @Override
    public void exec() {
        var installDir = getInstallationDir().getAsFile().get().toPath();

        execOperations.javaexec(spec -> {
            // The JVM args at this point may include debugging options when started through IntelliJ
            spec.jvmArgs(getJvmArguments().get());
            spec.workingDir(installDir);

            spec.environment(getEnvironment());
            spec.classpath(installDir.resolve("server.jar"));
        });

    }
}
