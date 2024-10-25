package net.neoforged.neodev.utils;

import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.internal.component.external.model.ModuleComponentArtifactIdentifier;

public final class DependencyUtils {
    private DependencyUtils() {
    }

    /**
     * Given a resolved artifact, try to guess which Maven GAV it was resolved from.
     */
    public static String guessMavenGav(ResolvedArtifactResult result) {
        String artifactId;
        String ext = "";
        String classifier = null;

        var filename = result.getFile().getName();
        var startOfExt = filename.lastIndexOf('.');
        if (startOfExt != -1) {
            ext = filename.substring(startOfExt + 1);
            filename = filename.substring(0, startOfExt);
        }

        if (result.getId() instanceof ModuleComponentArtifactIdentifier moduleId) {
            var artifact = moduleId.getComponentIdentifier().getModule();
            var version = moduleId.getComponentIdentifier().getVersion();
            var expectedBasename = artifact + "-" + version;

            if (filename.startsWith(expectedBasename + "-")) {
                classifier = filename.substring((expectedBasename + "-").length());
            }
            artifactId = moduleId.getComponentIdentifier().getGroup() + ":" + artifact + ":" + version;
        } else {
            // When we encounter a project reference, the component identifier does not expose the group or module name.
            // But we can access the list of capabilities associated with the published variant the artifact originates from.
            // If the capability was not overridden, this will be the project GAV. If it is *not* the project GAV,
            // it will be at least in valid GAV format, not crashing NFRT when it parses the manifest. It will just be ignored.
            var capabilities = result.getVariant().getCapabilities();
            if (capabilities.size() == 1) {
                var capability = capabilities.get(0);
                artifactId = capability.getGroup() + ":" + capability.getName() + ":" + capability.getVersion();
            } else {
                artifactId = result.getId().getComponentIdentifier().toString();
            }
        }
        String gav = artifactId;
        if (classifier != null) {
            gav += ":" + classifier;
        }
        if (!"jar".equals(ext)) {
            gav += "@" + ext;
        }
        return gav;
    }
}
