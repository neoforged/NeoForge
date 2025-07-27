package net.neoforged.neodev.jcc;

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
 * Downloads an arbitrary NeoForge installer by version.
 * <p>Sometimes we cannot use Gradle configurations since Gradle would replace references to net.neoforged:neoforge
 * with a reference to the current project. That prevents downloading different versions.
 */
public abstract class DownloadInstaller extends DefaultTask {
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
        getLogger().lifecycle("Downloading installer from " + url);

        var destination = getDestination().getAsFile().get();

        try (var client = HttpClient.newHttpClient()) {
            client.send(HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .build(), HttpResponse.BodyHandlers.ofFile(destination.toPath()));
        } catch (IOException e) {
            // Delete partially downloaded file
            destination.delete();
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for download.");
        }
    }
}
