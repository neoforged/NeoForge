package net.neoforged.neodev;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.JavaPlugin;

/**
 * Helper class to keep track of many {@link Configuration}s used for the {@code neoforge} project.
 *
 * <p>Self-contained configurations (e.g. used to resolve tools) are not included here.
 */
class NeoDevConfigurations {
    static NeoDevConfigurations createAndSetup(Project project) {
        return new NeoDevConfigurations(project.getConfigurations());
    }

    //
    // Configurations against which dependencies should be declared ("dependency scopes").
    //

    /**
     * Only the NeoForm data zip and the dependencies to run NeoForm.
     * Does not contain the dependencies to run vanilla Minecraft.
     */
    final Configuration neoFormData;
    /**
     * Only the NeoForm dependencies.
     * These are the dependencies required to run NeoForm-decompiled Minecraft.
     * Does not contain the dependencies to run the NeoForm process itself.
     */
    final Configuration neoFormDependencies;
    /**
     * Libraries used by NeoForge at compilation and runtime.
     * These will end up on the MC-BOOTSTRAP module layer.
     */
    final Configuration libraries;
    /**
     * Libraries used by NeoForge at compilation and runtime that need to be placed on the jvm's module path to end up in the boot layer.
     * Currently, this only contains the few dependencies that are needed to create the MC-BOOTSTRAP module layer.
     * (i.e. BootstrapLauncher and its dependencies).
     */
    final Configuration moduleLibraries;
    /**
     * Libraries that should be accessible in mod development environments at compilation time only.
     * Currently, this is only used for MixinExtras, which is already available at runtime via JiJ in the NeoForge universal jar.
     */
    final Configuration userdevCompileOnly;
    /**
     * Libraries that should be accessible at runtime in unit tests.
     * Currently, this only contains the fml-junit test fixtures.
     */
    final Configuration userdevTestFixtures;

    //
    // Resolvable configurations.
    //

    /**
     * Resolvable {@link #neoFormDependencies}.
     */
    final Configuration neoFormClasspath;
    /**
     * Resolvable {@link #moduleLibraries}.
     */
    final Configuration modulePath;
    /**
     * Userdev dependencies (written to a json file in the userdev jar).
     * This should contain all of NeoForge's dependencies for userdev and NeoForm,
     * but does not need to include all of Minecraft's libraries.
     */
    final Configuration userdevClasspath;
    /**
     * Resolvable {@link #userdevCompileOnly}, to add these entries to the ignore list of BootstrapLauncher.
     */
    final Configuration userdevCompileOnlyClasspath;
    /**
     * Resolvable {@link #userdevTestFixtures}, to write it in the userdev jar.
     */
    final Configuration userdevTestClasspath;
    /**
     * Libraries that need to be added to the classpath when launching NeoForge through the launcher.
     * This contains all dependencies added by NeoForge, but does not include all of Minecraft's libraries.
     * This is also used to produce the legacy classpath file for server installs.
     */
    final Configuration launcherProfileClasspath;
    /**
     * Libraries that need to be downloaded and installed by the installer.
     * This includes all dependencies added by NeoForge, the NeoForm archive,
     * and all dependencies needed by the various tools that the installer runs.
     */
    final Configuration installerProfileLibraries;

    //
    // The configurations for resolution only are declared in the build.gradle file.
    //

    private static Configuration dependencyScope(ConfigurationContainer configurations, String name) {
        return configurations.create(name, configuration -> {
            configuration.setCanBeConsumed(false);
            configuration.setCanBeResolved(false);
        });
    }

    private static Configuration resolvable(ConfigurationContainer configurations, String name) {
        return configurations.create(name, configuration -> {
            configuration.setCanBeConsumed(false);
            configuration.setCanBeDeclared(false);
        });
    }

    private NeoDevConfigurations(ConfigurationContainer configurations) {
        neoFormData = dependencyScope(configurations, "neoFormData");
        neoFormDependencies = dependencyScope(configurations, "neoFormDependencies");
        libraries = dependencyScope(configurations, "libraries");
        moduleLibraries = dependencyScope(configurations, "moduleLibraries");
        userdevCompileOnly = dependencyScope(configurations, "userdevCompileOnly");
        userdevTestFixtures = dependencyScope(configurations, "userdevTestFixtures");

        neoFormClasspath = resolvable(configurations, "neoFormClasspath");
        modulePath = resolvable(configurations, "modulePath");
        userdevClasspath = resolvable(configurations, "userdevClasspath");
        userdevCompileOnlyClasspath = resolvable(configurations, "userdevCompileOnlyClasspath");
        userdevTestClasspath = resolvable(configurations, "userdevTestClasspath");
        launcherProfileClasspath = resolvable(configurations, "launcherProfileClasspath");
        installerProfileLibraries = resolvable(configurations, "installerProfileLibraries");

        // Libraries & module libraries & MC dependencies need to be available when compiling in NeoDev,
        // and on the runtime classpath too for IDE debugging support.
        configurations.getByName("implementation").extendsFrom(libraries, moduleLibraries, neoFormDependencies);

        // runtimeClasspath is our reference for all dependency versions.
        // Make sure that any configuration we resolve are consistent with it.
        var runtimeClasspath = configurations.getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME);

        modulePath.extendsFrom(moduleLibraries);
        modulePath.shouldResolveConsistentlyWith(runtimeClasspath);

        userdevClasspath.extendsFrom(libraries, moduleLibraries, userdevCompileOnly, neoFormData); // TODO: is neoFormData necessary here?
        userdevClasspath.shouldResolveConsistentlyWith(runtimeClasspath);

        userdevCompileOnlyClasspath.extendsFrom(userdevCompileOnly);
        userdevCompileOnlyClasspath.shouldResolveConsistentlyWith(runtimeClasspath);

        userdevTestClasspath.extendsFrom(userdevTestFixtures);
        userdevTestClasspath.shouldResolveConsistentlyWith(runtimeClasspath);

        launcherProfileClasspath.extendsFrom(libraries, moduleLibraries);
        launcherProfileClasspath.shouldResolveConsistentlyWith(runtimeClasspath);

        installerProfileLibraries.extendsFrom(libraries, moduleLibraries, neoFormData);
        installerProfileLibraries.shouldResolveConsistentlyWith(runtimeClasspath);
        // Installer tools are also added to `installerProfileLibraries`, from NeoDevPlugin.
    }
}
