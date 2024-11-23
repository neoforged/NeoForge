package net.neoforged.neodev.utils;

import java.io.Serializable;

public record MavenIdentifier(String group, String artifact, String version, String classifier, String extension) implements Serializable {
    public String artifactNotation() {
        return group + ":" + artifact + ":" + version + (classifier.isEmpty() ? "" : ":" + classifier) + ("jar".equals(extension) ? "" : "@" + extension);
    }

    public String repositoryPath() {
        return group.replace(".", "/") + "/" + artifact + "/" + version + "/" + artifact + "-" + version + (classifier.isEmpty() ? "" : "-" + classifier) + "." + extension;
    }
}
