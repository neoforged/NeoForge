package net.neoforged.neodev;

import org.gradle.api.Project;

public enum Tools {
    JST("net.neoforged.jst:jst-cli-bundle:%s", "jst_version", "toolJstClasspath", true),
    LEGACYINSTALLER("net.neoforged:legacyinstaller:%s:shrunk", "legacyinstaller_version", "toolLegacyinstallerClasspath", true),
    AUTO_RENAMING_TOOL("net.neoforged:AutoRenamingTool:%s:all", "art_version", "toolAutoRenamingToolClasspath", true),
    INSTALLERTOOLS("net.neoforged.installertools:installertools:%s", "installertools_version", "toolInstallertoolsClasspath", true),
    JARSPLITTER("net.neoforged.installertools:jarsplitter:%s", "installertools_version", "toolJarsplitterClasspath", true),
    BINPATCHER("net.neoforged.installertools:binarypatcher:%s:fatjar", "installertools_version", "toolBinpatcherClasspath", true);

    private final String gavPattern;
    private final String versionProperty;
    private final String gradleConfigurationName;
    private final boolean requestFatJar;

    Tools(String gavPattern, String versionProperty, String gradleConfigurationName, boolean requestFatJar) {
        this.gavPattern = gavPattern;
        this.versionProperty = versionProperty;
        this.gradleConfigurationName = gradleConfigurationName;
        this.requestFatJar = requestFatJar;
    }

    /**
     * The name of the Gradle {@link org.gradle.api.artifacts.Configuration} used to resolve this particular tool.
     */
    public String getGradleConfigurationName() {
        return gradleConfigurationName;
    }

    /**
     * Some tools may be incorrectly packaged and declare transitive dependencies even for their "fatjar" variants.
     * Gradle will not run these, so we ignore them.
     */
    public boolean isRequestFatJar() {
        return requestFatJar;
    }

    public String asGav(Project project) {
        var version = project.property(versionProperty);
        if (version == null) {
            throw new IllegalStateException("Could not find property " + versionProperty);
        }
        return gavPattern.formatted(version);
    }
}
