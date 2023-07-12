package net.minecraftforge.forge.tasks

import groovy.json.JsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*

import java.nio.file.Files

import static net.minecraftforge.forge.tasks.Util.getArtifacts
import static net.minecraftforge.forge.tasks.Util.iso8601Now

abstract class LauncherJson extends DefaultTask {
    @OutputFile abstract RegularFileProperty getOutput()
    @InputFiles abstract ConfigurableFileCollection getInput()
    @Input Map<String, Object> json = new LinkedHashMap<>()

    @Internal final vanilla = project.project(':mcp').file('build/mcp/downloadJson/version.json')
    @Internal final timestamp = iso8601Now()
    @Internal final id = "${project.rootProject.ext.MC_VERSION}-${project.name}${project.version.substring(project.rootProject.ext.MC_VERSION.length())}"

    LauncherJson() {
        getOutput().convention(project.layout.buildDirectory.file('version.json'))

        dependsOn('universalJar')
        getInput().from(project.tasks.universalJar.archiveFile,
                vanilla)
    }

    @TaskAction
    protected void exec() {
        if (!json.libraries)
            json.libraries = []
        def libs = [:]
        getArtifacts(project, project.configurations.installer, false).each { key, lib -> libs[key] = lib }
        getArtifacts(project, project.configurations.moduleonly, false).each { key, lib -> libs[key] = lib }
        getArtifacts(project, project.configurations.gameLayerLibrary, false).each { key, lib -> libs[key] = lib }
        getArtifacts(project, project.configurations.pluginLayerLibrary, false).each { key, lib -> libs[key] = lib }

        libs.each { key, lib -> json.libraries.add(lib) }
        Files.writeString(output.get().asFile.toPath(), new JsonBuilder(json).toPrettyString())
    }
}
