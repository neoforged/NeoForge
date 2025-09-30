package net.neoforged.neodev.utils;

import java.io.Serializable;

public record MavenIdentifier(String group, String artifact, String version, String classifier, String extension) implements Serializable {
    public String artifactNotation() {
        return group + ":" + artifact + ":" + version + (classifier.isEmpty() ? "" : ":" + classifier) + ("jar".equals(extension) ? "" : "@" + extension);
    }

    public String repositoryPath() {
        return group.replace(".", "/") + "/" + artifact + "/" + version + "/" + artifact + "-" + version + (classifier.isEmpty() ? "" : "-" + classifier) + "." + extension;
    }

    public MavenIdentifier withVersion(String version) {
        return new MavenIdentifier(group, artifact, version, classifier, extension);
    }

    /**
     * Valid forms:
     * <ul>
     * <li>{@code groupId:artifactId:version}</li>
     * <li>{@code groupId:artifactId:version:classifier}</li>
     * <li>{@code groupId:artifactId:version:classifier@extension}</li>
     * <li>{@code groupId:artifactId:version@extension}</li>
     * </ul>
     */
    public static MavenIdentifier parse(String coordinate) {
        var coordinateAndExt = coordinate.split("@");
        String extension = "jar";
        if (coordinateAndExt.length > 2) {
            throw new IllegalArgumentException("Malformed Maven coordinate: " + coordinate);
        } else if (coordinateAndExt.length == 2) {
            extension = coordinateAndExt[1];
            coordinate = coordinateAndExt[0];
        }

        var parts = coordinate.split(":");
        if (parts.length != 3 && parts.length != 4) {
            throw new IllegalArgumentException("Malformed Maven coordinate: " + coordinate);
        }

        var groupId = parts[0];
        var artifactId = parts[1];
        var version = parts[2];
        var classifier = parts.length == 4 ? parts[3] : "";
        return new MavenIdentifier(groupId, artifactId, version, classifier, extension);
    }

    @Override
    public String toString() {
        if (classifier != null) {
            return group + ":" + artifact + ":" + version + ":" + classifier + "@" + extension;
        } else {
            return group + ":" + artifact + ":" + version + "@" + extension;
        }
    }
}
