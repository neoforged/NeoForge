package net.neoforged.neodev;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.inject.Inject;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputFile;

/**
 * A debugging task for helping with analyzing how the patches were de-duped, and what is being patched.
 */
abstract class ListBinaryPatches extends JavaExec {
    @Inject
    public ListBinaryPatches() {}

    /**
     * The patch bundle to report on.
     */
    @InputFile
    public abstract RegularFileProperty getPatchBundle();

    /**
     * Where the created patch bundle report should be written to.
     */
    @OutputFile
    abstract RegularFileProperty getOutputFile();

    @Override
    public void exec() {
        args("--list", "--patches", getPatchBundle().get().getAsFile().getAbsolutePath());

        File reportFile = getOutputFile().getAsFile().get();
        try (var out = new BufferedOutputStream(new FileOutputStream(reportFile))) {
            setStandardOutput(out);
            super.exec();
        } catch (IOException e) {
            throw new GradleException("Failed to list binary patches.", e);
        }

        getLogger().lifecycle("Wrote contents of patch bundle to " + reportFile.getAbsolutePath());
    }
}
