package net.neoforged.neodev;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;

public enum Tools {
    JST("net.neoforged.jst:jst-cli-bundle:%s", "jst_version"),
    LEGACYINSTALLER("net.neoforged:legacyinstaller:%s:shrunk", "legacyinstaller_version"),
    AUTO_RENAMING_TOOL("net.neoforged:AutoRenamingTool:%s:all", "art_version"),
    INSTALLERTOOLS("net.neoforged.installertools:installertools:%s", "installertools_version"),
    JARSPLITTER("net.neoforged.installertools:jarsplitter:%s", "installertools_version"),
    BINPATCHER("net.neoforged.installertools:binarypatcher:%s:fatjar", "installertools_version");

    private final String gavPattern;
    private final String versionProperty;

    Tools(String gavPattern, String versionProperty) {
        this.gavPattern = gavPattern;
        this.versionProperty = versionProperty;
    }

    public String asGav(Project project) {
        var version = project.property(versionProperty);
        if (version == null) {
            throw new IllegalStateException("Could not find property " + versionProperty);
        }
        return gavPattern.formatted(version);
    }

    public Dependency asDependency(Project project) {
        return project.getDependencyFactory().create(asGav(project));
    }
}
