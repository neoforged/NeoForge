package net.neoforged.neodev;

import org.gradle.api.Project;

// If a GAV is changed, make sure to change the corresponding renovate comment in gradle.properties.
public enum Tools {
    // Fatjar jst-cli-bundle instead of jst-cli because publication of the latter is currently broken.
    JST("net.neoforged.jst:jst-cli-bundle:%s", "jst_version", "toolJstClasspath", true),
    // Fatjar because the contents are copy/pasted into the installer jar which must be standalone.
    LEGACYINSTALLER("net.neoforged:legacyinstaller:%s:shrunk", "legacyinstaller_version", "toolLegacyinstallerClasspath", true),
    // Fatjar because the slim jar currently does not have the main class set in its manifest.
    AUTO_RENAMING_TOOL("net.neoforged:AutoRenamingTool:%s:all", "art_version", "toolAutoRenamingToolClasspath", true),
    INSTALLERTOOLS("net.neoforged.installertools:installertools:%s", "installertools_version", "toolInstallertoolsClasspath", false),
    JARSPLITTER("net.neoforged.installertools:jarsplitter:%s", "installertools_version", "toolJarsplitterClasspath", false),
    // Fatjar because it was like that in the userdev json in the past.
    // To reconsider, we need to get in touch with 3rd party plugin developers or wait for a BC window.
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
