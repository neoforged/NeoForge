package net.neoforged.neodev;

import net.neoforged.minecraftdependencies.MinecraftDependenciesPlugin;
import net.neoforged.moddevgradle.internal.NeoDevFacade;
import net.neoforged.nfrtgradle.CreateMinecraftArtifacts;
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

        var rawNeoFormVersion = project.getProviders().gradleProperty("neoform_version");
        var minecraftVersion = project.getProviders().gradleProperty("minecraft_version");
        var mcAndNeoFormVersion = minecraftVersion.zip(rawNeoFormVersion, (mc, nf) -> mc + "-" + nf);

        // TODO: this is temporary
        var modulesConfiguration = project.getConfigurations().create("moduleOnly", spec -> {
            spec.getDependencies().add(projectDep(dependencyFactory, neoForgeProject, "moduleOnly"));
        });

        var downloadAssets = neoForgeProject.getTasks().named("downloadAssets", DownloadAssets.class);
        var createArtifacts = neoForgeProject.getTasks().named("createSourceArtifacts", CreateMinecraftArtifacts.class);
        var writeNeoDevConfig = neoForgeProject.getTasks().named("writeNeoDevConfig", WriteUserDevConfig.class);

        Consumer<Configuration> configureLegacyClasspath = spec -> {
            spec.getDependencies().addLater(mcAndNeoFormVersion.map(v -> dependencyFactory.create("net.neoforged:neoform:" + v).capabilities(caps -> {
                caps.requireCapability("net.neoforged:neoform-dependencies");
            })));
            spec.getDependencies().add(projectDep(dependencyFactory, neoForgeProject, "installer"));
            spec.getDependencies().add(projectDep(dependencyFactory, neoForgeProject, "moduleOnly"));
            spec.getDependencies().add(projectDep(dependencyFactory, neoForgeProject, "userdevCompileOnly"));
            // TODO: Convert into a cross-project dependency too
            spec.getDependencies().add(
                    dependencyFactory.create(
                            project.files(createArtifacts.flatMap(CreateMinecraftArtifacts::getResourcesArtifact))
                    )
            );
        };

        extension.getRuns().configureEach(run -> {
            configureLegacyClasspath.accept(run.getAdditionalRuntimeClasspathConfiguration());
        });
        NeoDevFacade.setupRuns(
                project,
                neoDevBuildDir,
                extension.getRuns(),
                writeNeoDevConfig,
                modulePath -> modulePath.extendsFrom(modulesConfiguration),
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
                writeNeoDevConfig,
                testExtension.getLoadedMods(),
                testExtension.getTestedMod(),
                modulePath -> modulePath.extendsFrom(modulesConfiguration),
                configureLegacyClasspath,
                downloadAssets.flatMap(DownloadAssets::getAssetPropertiesFile)
        );
    }

    private static ProjectDependency projectDep(DependencyFactory dependencyFactory, Project project, String configurationName) {
        var dep = dependencyFactory.create(project);
        dep.setTargetConfiguration(configurationName);
        return dep;
    }
}
