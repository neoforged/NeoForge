package net.neoforged.neodev.e2e;

import org.gradle.api.GradleException;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Runs a production server using {@link RunProductionServer} and passes the environment variable
 * to enable the {@link net.neoforged.neoforge.common.util.SelfTest self test}.
 * <p>
 * Once the server exits, it validates that the self-test file was created, indicating the server successfully
 * launched and started ticking.
 */
public abstract class TestProductionServer extends RunProductionServer {
    @Inject
    public TestProductionServer(ExecOperations execOperations) {
        super(execOperations);

        getTimeout().set(Duration.of(5, ChronoUnit.MINUTES));
    }

    @Override
    public void exec() {
        var selfTestReport = new File(getTemporaryDir(), "server_self_test.txt");

        environment("NEOFORGE_DEDICATED_SERVER_SELFTEST", selfTestReport.getAbsolutePath());

        var eulaFile = getInstallationDir().file("eula.txt").get().getAsFile().toPath();
        try {
            Files.writeString(eulaFile, "eula=true", StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new GradleException("Failed writing eula acceptable to eula.txt", e);
        }

        try {
            super.exec();
        } finally {
            try {
                Files.deleteIfExists(eulaFile);
            } catch (IOException ignored) {
            }
        }

        if (!selfTestReport.exists()) {
            throw new GradleException("Missing self test report file after running server: " + selfTestReport);
        }
    }
}
