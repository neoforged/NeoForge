package net.neoforged.neodev;

import net.neoforged.minecraftdependencies.MinecraftDependenciesPlugin;
import net.neoforged.moddevgradle.internal.NeoDevFacade;
import net.neoforged.nfrtgradle.DownloadAssets;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.dsl.DependencyFactory;
import org.gradle.api.tasks.testing.Test;

import java.util.function.Consumer;

// TODO: the only point of this is to configure runs that depend on neoforge. Maybe this could be done with less code duplication...
// TODO: Gradle says "thou shalt not referenceth otherth projects" yet here we are
// TODO: depend on neoforge configurations that the moddev plugin also uses
public class NeoDevExtraPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPlugins().apply(MinecraftDependenciesPlugin.class);

        var neoForgeProject = project.getRootProject().getChildProjects().get("neoforge");

        var dependencyFactory = project.getDependencyFactory();
        var tasks = project.getTasks();
        var neoDevBuildDir = project.getLayout().getBuildDirectory().dir("neodev");

        var extension = project.getExtensions().create(NeoDevExtension.NAME, NeoDevExtension.class);

        var modulePathDependency = projectDep(dependencyFactory, neoForgeProject, "net.neoforged:neoforge-moddev-module-path");

        // TODO: this is temporary
        var downloadAssets = neoForgeProject.getTasks().named("downloadAssets", DownloadAssets.class);

        var neoForgeConfigOnly = project.getConfigurations().create("neoForgeConfigOnly", spec -> {
            spec.getDependencies().add(projectDep(dependencyFactory, neoForgeProject, "net.neoforged:neoforge-moddev-config"));
        });

        Consumer<Configuration> configureLegacyClasspath = spec -> {
            spec.getDependencies().add(projectDep(dependencyFactory, neoForgeProject, "net.neoforged:neoforge-dependencies"));
        };

        extension.getRuns().configureEach(run -> {
            configureLegacyClasspath.accept(run.getAdditionalRuntimeClasspathConfiguration());
        });
        NeoDevFacade.setupRuns(
                project,
                neoDevBuildDir,
                extension.getRuns(),
                neoForgeConfigOnly,
                modulePath -> modulePath.getDependencies().add(modulePathDependency),
                configureLegacyClasspath,
                downloadAssets.flatMap(DownloadAssets::getAssetPropertiesFile)
        );

        var testExtension = project.getExtensions().create(NeoDevTestExtension.NAME, NeoDevTestExtension.class);
        var testTask = tasks.register("junitTest", Test.class, test -> test.setGroup("verification"));
        tasks.named("check").configure(task -> task.dependsOn(testTask));

        NeoDevFacade.setupTestTask(
                project,
                neoDevBuildDir,
                testTask,
                neoForgeConfigOnly,
                testExtension.getLoadedMods(),
                testExtension.getTestedMod(),
                modulePath -> modulePath.getDependencies().add(modulePathDependency),
                configureLegacyClasspath,
                downloadAssets.flatMap(DownloadAssets::getAssetPropertiesFile)
        );
    }

    private static ProjectDependency projectDep(DependencyFactory dependencyFactory, Project project, String capabilityNotation) {
        var dep = dependencyFactory.create(project);
        dep.capabilities(caps -> {
            caps.requireCapability(capabilityNotation);
        });
        return dep;
    }
}
