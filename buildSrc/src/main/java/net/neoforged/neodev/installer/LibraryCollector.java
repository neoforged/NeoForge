package net.neoforged.neodev.installer;

import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

class LibraryCollector extends ModuleIdentificationVisitor {
    private static final Logger LOGGER = Logging.getLogger(LibraryCollector.class);
    /**
     * Hosts from which we allow the installer to download.
     * We whitelist here to avoid redirecting player download traffic to anyone not affiliated with Mojang or us.
     */
    private static final List<String> HOST_WHITELIST = List.of(
            "minecraft.net",
            "neoforged.net",
            "mojang.com"
    );

    private static final URI MOJANG_MAVEN = URI.create("https://libraries.minecraft.net");
    private static final URI NEOFORGED_MAVEN = URI.create("https://maven.neoforged.net/releases");

    private final List<URI> repositoryUrls;

    private final List<Future<Library>> libraries = new ArrayList<>();

    private final HttpClient httpClient = HttpClient.newBuilder().build();

    LibraryCollector(List<URI> repoUrl) {
        this.repositoryUrls = new ArrayList<>(repoUrl);

        // Only remote repositories make sense (no maven local)
        repositoryUrls.removeIf(it -> {
            var lowercaseScheme = it.getScheme().toLowerCase(Locale.ROOT);
            return !lowercaseScheme.equals("https") && !lowercaseScheme.equals("http");
        });
        // Allow only URLs from whitelisted hosts
        repositoryUrls.removeIf(uri -> {
            var lowercaseHost = uri.getHost().toLowerCase(Locale.ROOT);
            return HOST_WHITELIST.stream().noneMatch(it -> lowercaseHost.equals(it) || lowercaseHost.endsWith("." + it));
        });
        // Always try Mojang Maven first, then our installer Maven
        repositoryUrls.removeIf(it -> it.getHost().equals(MOJANG_MAVEN.getHost()));
        repositoryUrls.removeIf(it -> it.getHost().equals(NEOFORGED_MAVEN.getHost()) && it.getPath().startsWith(NEOFORGED_MAVEN.getPath()));
        repositoryUrls.add(0, NEOFORGED_MAVEN);
        repositoryUrls.add(0, MOJANG_MAVEN);

        LOGGER.info("Collecting libraries from:");
        for (var repo : repositoryUrls) {
            LOGGER.info(" - " + repo);
        }
    }

    void visit(ResolvedArtifactResult artifactResult) throws IOException {
        var componentId = artifactResult.getId().getComponentIdentifier();
        if (componentId instanceof ModuleComponentIdentifier moduleComponentId) {
            visitModule(
                    artifactResult.getFile(),
                    moduleComponentId.getGroup(),
                    moduleComponentId.getModule(),
                    moduleComponentId.getVersion(),
                    guessMavenClassifier(artifactResult.getFile(), moduleComponentId),
                    getExtension(artifactResult.getFile().getName())
            );
        } else {
            LOGGER.warn("Cannot handle component: " + componentId);
        }
    }

    private static String guessMavenClassifier(File file, ModuleComponentIdentifier id) {
        var artifact = id.getModule();
        var version = id.getVersion();
        var expectedBasename = artifact + "-" + version;
        var filename = file.getName();
        var startOfExt = filename.lastIndexOf('.');
        if (startOfExt != -1) {
            filename = filename.substring(0, startOfExt);
        }

        if (filename.startsWith(expectedBasename + "-")) {
            return filename.substring((expectedBasename + "-").length());
        }
        return "";
    }

    /**
     * The filename includes the period.
     */
    private static String getExtension(String path) {
        var lastSep = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));

        var potentialExtension = path.lastIndexOf('.');
        if (potentialExtension > lastSep) {
            // Check for a double extension like .tar.gz heuristically
            var doubleExtensionStart = path.lastIndexOf('.', potentialExtension - 1);
            // We only allow 3 chars maximum for the double extension
            if (doubleExtensionStart > lastSep && potentialExtension - doubleExtensionStart <= 4) {
                return path.substring(doubleExtensionStart);
            }

            return path.substring(potentialExtension);
        } else {
            return "";
        }
    }

    @Override
    protected void visitModule(File file, String group, String module, String version, String classifier, final String extension) throws IOException {
        final String name = group + ":" + module + ":" + version + (classifier.isEmpty() ? "" : ":" + classifier) + "@" + extension;
        final String path = group.replace(".", "/") + "/" + module + "/" + version + "/" + module + "-" + version + (classifier.isEmpty() ? "" : "-" + classifier) + "." + extension;

        var sha1 = sha1Hash(file.toPath());
        var fileSize = Files.size(file.toPath());

        // Try each configured repository in-order to find the file
        CompletableFuture<Library> libraryFuture = null;
        for (var repositoryUrl : repositoryUrls) {
            var artifactUri = joinUris(repositoryUrl, path);
            var request = HttpRequest.newBuilder(artifactUri)
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();

            Function<String, CompletableFuture<Library>> makeRequest = (String previousError) -> {
                return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                        .thenApply(response -> {
                            if (response.statusCode() != 200) {
                                LOGGER.info("  Got %d for %s".formatted(response.statusCode(), artifactUri));
                                String message = "Could not find %s: %d".formatted(artifactUri, response.statusCode());
                                // Prepend error message from previous repo if they all fail
                                if (previousError != null) {
                                    message = previousError + "\n" + message;
                                }
                                throw new RuntimeException(message);
                            }
                            LOGGER.info("  Found $name -> $artifactUri");
                            return new Library(
                                    name,
                                    new LibraryDownload(new LibraryArtifact(
                                            sha1,
                                            fileSize,
                                            artifactUri.toString(),
                                            path)));
                        });
            };

            if (libraryFuture == null) {
                libraryFuture = makeRequest.apply(null);
            } else {
                libraryFuture = libraryFuture.exceptionallyCompose(error -> {
                    return makeRequest.apply(error.getMessage());
                });
            }
        }

        libraries.add(libraryFuture);
    }

    static String sha1Hash(Path path) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        try (var in = Files.newInputStream(path);
                var din = new DigestInputStream(in, digest)) {
            byte[] buffer = new byte[8192];
            while (din.read(buffer) != -1) {
            }
        }

        return HexFormat.of().formatHex(digest.digest());
    }

    private static URI joinUris(URI repositoryUrl, String path) {
        var baseUrl = repositoryUrl.toString();
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            while (path.startsWith("/")) {
                path = path.substring(1);
            }
            return URI.create(baseUrl + path);
        } else if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
            return URI.create(baseUrl + "/" + path);
        } else {
            return URI.create(baseUrl + path);
        }
    }

    List<Library> getLibraries() {
        var result = libraries.stream().map(future -> {
            try {
                return future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).toList();
        LOGGER.info("Collected %d libraries".formatted(result.size()));
        return result;
    }
}
