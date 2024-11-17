package net.neoforged.neodev;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;

public enum Tools {
    JST("net.neoforged.jst:jst-cli-bundle:%s", "jst_version", "toolJstClasspath", false),
    LEGACYINSTALLER("net.neoforged:legacyinstaller:%s:shrunk", "legacyinstaller_version", "toolLegacyinstallerClasspath", false),
    AUTO_RENAMING_TOOL("net.neoforged:AutoRenamingTool:%s:all", "art_version", "toolAutoRenamingToolClasspath", false),
    INSTALLERTOOLS("net.neoforged.installertools:installertools:%s", "installertools_version", "toolInstallertoolsClasspath", false),
    JARSPLITTER("net.neoforged.installertools:jarsplitter:%s", "installertools_version", "toolJarsplitterClasspath", false),
    BINPATCHER("net.neoforged.installertools:binarypatcher:%s:fatjar", "installertools_version", "toolBinpatcherClasspath", false);

    private final String gavPattern;
    private final String versionProperty;
    private final String gradleConfigurationName;
    private final boolean ignoreTransitiveDependencies;

    Tools(String gavPattern, String versionProperty, String gradleConfigurationName, boolean ignoreTransitiveDependencies) {
        this.gavPattern = gavPattern;
        this.versionProperty = versionProperty;
        this.gradleConfigurationName = gradleConfigurationName;
        this.ignoreTransitiveDependencies = ignoreTransitiveDependencies;
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
    public boolean isIgnoreTransitiveDependencies() {
        return ignoreTransitiveDependencies;
    }

    public String asGav(Project project) {
        var version = project.property(versionProperty);
        if (version == null) {
            throw new IllegalStateException("Could not find property " + versionProperty);
        }
        return gavPattern.formatted(version);
    }
}
