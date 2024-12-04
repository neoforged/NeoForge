package net.neoforged.neodev;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.plugins.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Helper class to keep track of the many {@link Configuration}s used for the {@code neoforge} project.
 */
class NeoDevConfigurations {
    static NeoDevConfigurations createAndSetup(Project project) {
        return new NeoDevConfigurations(project);
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
     * Resolved {@link #neoFormData}.
     * This is used to add NeoForm to the installer libraries.
     * Only the zip is used (for the mappings), not the NeoForm tools, so it's not transitive.
     */
    final Configuration neoFormDataOnly;
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
     * This should contain all of NeoForge's additional dependencies for userdev,
     * but does not need to include Minecraft or NeoForm's libraries.
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

    //
    // The configurations for resolution only are declared in the build.gradle file.
    //

    /**
     * To download each executable tool, we use a resolvable configuration.
     * These configurations support both declaration and resolution.
     */
    final Map<Tools, Configuration> toolClasspaths;

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

    private NeoDevConfigurations(Project project) {
        var configurations = project.getConfigurations();

        neoFormData = dependencyScope(configurations, "neoFormData");
        neoFormDependencies = dependencyScope(configurations, "neoFormDependencies");
        libraries = dependencyScope(configurations, "libraries");
        moduleLibraries = dependencyScope(configurations, "moduleLibraries");
        userdevCompileOnly = dependencyScope(configurations, "userdevCompileOnly");
        userdevTestFixtures = dependencyScope(configurations, "userdevTestFixtures");

        neoFormDataOnly = resolvable(configurations, "neoFormDataOnly");
        neoFormClasspath = resolvable(configurations, "neoFormClasspath");
        modulePath = resolvable(configurations, "modulePath");
        userdevClasspath = resolvable(configurations, "userdevClasspath");
        userdevCompileOnlyClasspath = resolvable(configurations, "userdevCompileOnlyClasspath");
        userdevTestClasspath = resolvable(configurations, "userdevTestClasspath");
        launcherProfileClasspath = resolvable(configurations, "launcherProfileClasspath");

        // Libraries & module libraries & MC dependencies need to be available when compiling in NeoDev,
        // and on the runtime classpath too for IDE debugging support.
        configurations.getByName("implementation").extendsFrom(libraries, moduleLibraries, neoFormDependencies);

        // runtimeClasspath is our reference for all MC dependency versions.
        // Make sure that any classpath we resolve is consistent with it.
        var runtimeClasspath = configurations.getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME);

        neoFormDataOnly.setTransitive(false);
        neoFormDataOnly.extendsFrom(neoFormData);

        neoFormClasspath.extendsFrom(neoFormDependencies);

        modulePath.extendsFrom(moduleLibraries);
        modulePath.shouldResolveConsistentlyWith(runtimeClasspath);

        userdevClasspath.extendsFrom(libraries, moduleLibraries, userdevCompileOnly);
        userdevClasspath.shouldResolveConsistentlyWith(runtimeClasspath);

        userdevCompileOnlyClasspath.extendsFrom(userdevCompileOnly);
        userdevCompileOnlyClasspath.shouldResolveConsistentlyWith(runtimeClasspath);

        userdevTestClasspath.extendsFrom(userdevTestFixtures);
        userdevTestClasspath.shouldResolveConsistentlyWith(runtimeClasspath);

        launcherProfileClasspath.extendsFrom(libraries, moduleLibraries);
        launcherProfileClasspath.shouldResolveConsistentlyWith(runtimeClasspath);

        toolClasspaths = createToolClasspaths(project);
    }

    private static Map<Tools, Configuration> createToolClasspaths(Project project) {
        var configurations = project.getConfigurations();
        var dependencyFactory = project.getDependencyFactory();

        var result = new HashMap<Tools, Configuration>();

        for (var tool : Tools.values()) {
            var configuration = configurations.create(tool.getGradleConfigurationName(), spec -> {
                spec.setDescription("Resolves the executable for tool " + tool.name());
                spec.setCanBeConsumed(false);
                // Tools are considered to be executable jars.
                // Gradle requires the classpath for JavaExec to only contain a single file for these.
                if (tool.isRequestFatJar()) {
                    spec.attributes(attr -> {
                        attr.attribute(Bundling.BUNDLING_ATTRIBUTE, project.getObjects().named(Bundling.class, Bundling.SHADOWED));
                    });
                }

                var gav = tool.asGav(project);
                spec.getDependencies().add(dependencyFactory.create(gav));
            });
            result.put(tool, configuration);
        }

        return Map.copyOf(result);
    }

    /**
     * Gets a configuration representing the classpath for an executable tool.
     * Some tools are assumed to be executable jars, and their configurations only contain a single file.
     */
    public Configuration getExecutableTool(Tools tool) {
        return Objects.requireNonNull(toolClasspaths.get(tool));
    }
}
