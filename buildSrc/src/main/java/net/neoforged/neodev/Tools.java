package net.neoforged.neodev;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;

public enum Tools {
    JST("net.neoforged.jst:jst-cli-bundle:%s", "jst_version", "toolJstClasspath"),
    LEGACYINSTALLER("net.neoforged:legacyinstaller:%s:shrunk", "legacyinstaller_version", "toolLegacyinstallerClasspath"),
    AUTO_RENAMING_TOOL("net.neoforged:AutoRenamingTool:%s:all", "art_version", "toolAutoRenamingToolClasspath"),
    INSTALLERTOOLS("net.neoforged.installertools:installertools:%s", "installertools_version", "toolInstallertoolsClasspath"),
    JARSPLITTER("net.neoforged.installertools:jarsplitter:%s", "installertools_version", "toolJarsplitterClasspath"),
    BINPATCHER("net.neoforged.installertools:binarypatcher:%s:fatjar", "installertools_version", "toolBinpatcherClasspath");

    private final String gavPattern;
    private final String versionProperty;
    private final String gradleConfigurationName;

    Tools(String gavPattern, String versionProperty, String gradleConfigurationName) {
        this.gavPattern = gavPattern;
        this.versionProperty = versionProperty;
        this.gradleConfigurationName = gradleConfigurationName;
    }

    /**
     * The name of the Gradle {@link org.gradle.api.artifacts.Configuration} used to resolve this particular tool.
     */
    public String getGradleConfigurationName() {
        return gradleConfigurationName;
    }

    public String asGav(Project project) {
        var version = project.property(versionProperty);
        if (version == null) {
            throw new IllegalStateException("Could not find property " + versionProperty);
        }
        return gavPattern.formatted(version);
    }
}
