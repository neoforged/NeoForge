package net.neoforged.neodev;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Downloads a file from a given URL.
 * <p>Sometimes we cannot use Gradle configurations to resolve remote files, since Gradle would replace references
 * to net.neoforged:neoforge with a reference to the current project. That prevents downloading different versions.
 */
public abstract class DownloadFile extends DefaultTask {
    /**
     * The URL to download.
     */
    @Input
    public abstract Property<String> getUrl();

    /**
     * The path to download to.
     */
    @OutputFile
    public abstract RegularFileProperty getDestination();

    @TaskAction
    public void exec() throws IOException {
        var url = getUrl().get();
        getLogger().lifecycle("Downloading " + url);

        var destination = getDestination().getAsFile().get();

        var client = HttpClient.newHttpClient();
        try {
            client.send(HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .build(), HttpResponse.BodyHandlers.ofFile(destination.toPath()));
        } catch (IOException e) {
            destination.delete(); // Delete partially downloaded file
            throw e;
        } catch (InterruptedException e) {
            destination.delete(); // Delete partially downloaded file
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for download.");
        } finally {
            // HttpClient was only made AutoCloseable in Java 21. This is for compat with Java 17.
            if (client instanceof AutoCloseable autoCloseable) {
                try {
                    autoCloseable.close();
                } catch (Exception e) {
                    getLogger().error("Failed to close HTTP client.", e);
                }
            }
        }
    }
}
