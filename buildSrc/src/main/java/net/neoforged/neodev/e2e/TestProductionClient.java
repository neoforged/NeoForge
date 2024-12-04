package net.neoforged.neodev.e2e;

import org.gradle.api.GradleException;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.File;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Runs a production client using {@link RunProductionClient} and passes the environment variable
 * to enable the {@link net.neoforged.neoforge.common.util.SelfTest self test}.
 * <p>
 * Once the client exits, it validates that the self-test file was created, indicating the client successfully
 * launched and started ticking.
 */
public abstract class TestProductionClient extends RunProductionClient {
    @Inject
    public TestProductionClient(ExecOperations execOperations) {
        super(execOperations);

        getTimeout().set(Duration.of(5, ChronoUnit.MINUTES));
    }

    @Override
    public void exec() {
        var selfTestReport = new File(getTemporaryDir(), "client_self_test.txt");

        environment("NEOFORGE_CLIENT_SELFTEST", selfTestReport.getAbsolutePath());

        super.exec();

        if (!selfTestReport.exists()) {
            throw new GradleException("Missing self test report file after running client: " + selfTestReport);
        }
    }
}
