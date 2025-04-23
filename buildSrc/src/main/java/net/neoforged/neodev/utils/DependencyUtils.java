package net.neoforged.neodev.utils;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.provider.Provider;
import org.gradle.internal.component.external.model.ModuleComponentArtifactIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class DependencyUtils {
    private DependencyUtils() {
    }

    /**
     * Given a resolved artifact, try to guess which Maven GAV it was resolved from.
     */
    public static MavenIdentifier guessMavenIdentifier(ResolvedArtifactResult result) {
        String group;
        String artifact;
        String version;
        String ext = "";
        String classifier = "";

        var filename = result.getFile().getName();
        var startOfExt = filename.lastIndexOf('.');
        if (startOfExt != -1) {
            ext = filename.substring(startOfExt + 1);
            filename = filename.substring(0, startOfExt);
        }

        if (result.getId() instanceof ModuleComponentArtifactIdentifier moduleId) {
            group = moduleId.getComponentIdentifier().getGroup();
            artifact = moduleId.getComponentIdentifier().getModule();
            version = moduleId.getComponentIdentifier().getVersion();
            var expectedBasename = artifact + "-" + version;

            if (filename.startsWith(expectedBasename + "-")) {
                classifier = filename.substring((expectedBasename + "-").length());
            }
        } else {
            // When we encounter a project reference, the component identifier does not expose the group or module name.
            // But we can access the list of capabilities associated with the published variant the artifact originates from.
            // If the capability was not overridden, this will be the project GAV. If it is *not* the project GAV,
            // it will be at least in valid GAV format, not crashing NFRT when it parses the manifest. It will just be ignored.
            var capabilities = result.getVariant().getCapabilities();
            if (capabilities.size() == 1) {
                var capability = capabilities.get(0);
                group = capability.getGroup();
                artifact = capability.getName();
                version = capability.getVersion();
            } else {
                throw new IllegalArgumentException("Don't know how to break " + result.getId().getComponentIdentifier() + " into Maven components.");
            }
        }
        return new MavenIdentifier(group, artifact, version, classifier, ext);
    }

    /**
     * Turns a configuration into a list of GAV entries.
     */
    public static Provider<List<String>> configurationToGavList(Configuration configuration) {
        return configuration.getIncoming().getArtifacts().getResolvedArtifacts().map(results -> {
            // Using .toList() fails with the configuration cache - looks like Gradle can't deserialize the resulting list?
            return results.stream().map(artifact -> guessMavenIdentifier(artifact).artifactNotation()).collect(Collectors.toCollection(ArrayList::new));
        });
    }

    /**
     * Turns a configuration into a classpath string,
     * assuming that the contents of the configuration are installed following the Maven directory layout.
     *
     * @param prefix string to add in front of each classpath entry
     * @param separator separator to add between each classpath entry
     */
    public static Provider<String> configurationToClasspath(Configuration configuration, String prefix, String separator) {
        return configuration.getIncoming().getArtifacts().getResolvedArtifacts().map(
                results -> results.stream()
                    .map(artifact -> prefix + guessMavenIdentifier(artifact).repositoryPath())
                    .collect(Collectors.joining(separator))
        );
    }
}
