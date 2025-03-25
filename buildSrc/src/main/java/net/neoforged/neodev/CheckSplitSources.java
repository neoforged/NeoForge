package net.neoforged.neodev;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.Arrays;

/**
 * This task fails the build if client classes are accidentally put into the common source-set
 * and vice-versa.
 */
public abstract class CheckSplitSources extends DefaultTask {
    /**
     * Configures the folder in the server source-set where someone might accidentally place client classes. Any files placed in this location will fail the build.
     */
    @Optional
    @InputDirectory
    public abstract DirectoryProperty getServerClientFolder();

    /**
     * Configures the root folder of the package structure that should be exclusive to the client source-set. If this folder has any siblings, the build fails since it
     * assumes someone accidentally added common code to the client source-set.
     */
    @Optional
    @InputDirectory
    public abstract DirectoryProperty getClientClientFolder();

    @TaskAction
    public void run() {
        if (getServerClientFolder().isPresent()) {
            var filesInServer = getServerClientFolder()
                    .getAsFileTree()
                    .filter(File::isFile)
                    .getFiles()
                    .stream()
                    .map(File::toString)
                    .sorted()
                    .toList();

            if (!filesInServer.isEmpty()) {
                throw new GradleException("Found classes under the client package that are placed in the common sourceset: " + String.join(", ", filesInServer));
            }
        }

        // Check that the client package has no sibling packages in the client source-set
        if (getClientClientFolder().isPresent()) {
            final File clientFolder = getClientClientFolder().getAsFile().get();
            final File[] folders = clientFolder.getParentFile().listFiles();
            if (folders != null && (folders.length != 1 || !folders[0].equals(clientFolder))) {
                var siblings = Arrays.stream(folders).filter(f -> !f.equals(clientFolder)).map(File::getAbsolutePath).toList();

                throw new GradleException("Found common files under the client sourceset: " + String.join(", ", siblings));
            }

        }

    }

}
