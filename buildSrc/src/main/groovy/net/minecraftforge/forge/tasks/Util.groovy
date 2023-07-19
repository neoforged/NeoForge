package net.minecraftforge.forge.tasks

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.stream.Stream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

public class Util {
    static final ASM_LEVEL = Opcodes.ASM9

	static void init() {
		File.metaClass.sha1 = { ->
			MessageDigest md = MessageDigest.getInstance('SHA-1')
			delegate.eachByte 4096, {bytes, size ->
				md.update(bytes, 0, size)
			}
			return md.digest().collect {String.format "%02x", it}.join()
		}
        File.metaClass.getSha1 = { !delegate.exists() ? null : delegate.sha1() }

		File.metaClass.json = { -> new JsonSlurper().parseText(delegate.text) }
        File.metaClass.getJson = { return delegate.exists() ? new JsonSlurper().parse(delegate) : [:] }
        File.metaClass.setJson = { json -> delegate.text = new JsonBuilder(json).toPrettyString() }

		Date.metaClass.iso8601 = { ->
			def format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
			def result = format.format(delegate)
			return result[0..21] + ':' + result[22..-1]
		}

        String.metaClass.rsplit = { String del, int limit = -1 ->
            def lst = new ArrayList()
            def x = 0, idx
            def tmp = delegate
            while ((idx = tmp.lastIndexOf(del)) != -1 && (limit == -1 || x++ < limit)) {
                lst.add(0, tmp.substring(idx + del.length(), tmp.length()))
                tmp = tmp.substring(0, idx)
            }
            lst.add(0, tmp)
            return lst
        }
	}

	public static String[] getClasspath(project, libs, artifact) {
		def ret = []
		artifactTree(project, artifact).each { key, lib ->
			libs[lib.name] = lib
			if (lib.name != artifact)
				ret.add(lib.name)
		}
		return ret
	}

	public static def getArtifacts(project, config, classifiers) {
		def ret = [:]
		config.resolvedConfiguration.resolvedArtifacts.each {
			def art = [
				group: it.moduleVersion.id.group,
				name: it.moduleVersion.id.name,
				version: it.moduleVersion.id.version,
				classifier: it.classifier,
				extension: it.extension,
				file: it.file
			]
			def key = art.group + ':' + art.name
			def folder = "${art.group.replace('.', '/')}/${art.name}/${art.version}/"
			def filename = "${art.name}-${art.version}"
			if (art.classifier != null)
				filename += "-${art.classifier}"
			filename += ".${art.extension}"
			def path = "${folder}${filename}"
			def url = "https://libraries.minecraft.net/${path}"
			if (!checkExists(url)) {
				url = "https://maven.neoforged.net/releases/${path}"
			}
			ret[key] = [
				name: "${art.group}:${art.name}:${art.version}" + (art.classifier == null ? '' : ":${art.classifier}") + (art.extension == 'jar' ? '' : "@${art.extension}"),
				downloads: [
					artifact: [
						path: path,
						url: url,
						sha1: sha1(art.file),
						size: art.file.length()
					]
				]
			]
		}
		return ret
	}

	public static String getMavenPath(Task task) {
		def classifier = task.archiveClassifier.get()
		def dep = "${task.project.group}:${task.project.name}:${task.project.version}" + (classifier == '' ? '' : ':' + classifier)
		return "${task.project.group.replace('.', '/')}/${task.project.name}/${task.project.version}/${task.project.name}-${task.project.version}".toString() + (classifier == '' ? '' : '-' + classifier) + '.jar'
	}

	public static String getMavenDep(Task task) {
		def classifier = task.archiveClassifier.get()
		return "${task.project.group}:${task.project.name}:${task.project.version}" + (classifier == '' ? '' : ':' + classifier)
	}

    public static String getMavenPath(Project project, String notation) {
		def config = createConfiguration(project, notation);
		def resolvedConfig = config.resolvedConfiguration
		if (resolvedConfig.hasError()) {
			resolvedConfig.rethrowFailure()
		}

		def firstLevelModuleDependencies = resolvedConfig.firstLevelModuleDependencies
		if (firstLevelModuleDependencies.size() != 1) {
			throw new RuntimeException("Expected 1 dependency, got ${firstLevelModuleDependencies.size()}")
		}

		def dependency = firstLevelModuleDependencies.iterator().next()
		def artifacts = dependency.moduleArtifacts
		if (artifacts.size() != 1) {
			throw new RuntimeException("Expected 1 artifact, got ${artifacts.size()}")
		}

		def artifact = artifacts.iterator().next()

		return "${artifact.moduleVersion.id.group.replace('.', '/')}/${artifact.moduleVersion.id.name}/${artifact.moduleVersion.id.version}/${artifact.moduleVersion.id.name}-${artifact.moduleVersion.id.version}".toString() + (artifact.classifier == '' || artifact.classifier == null ? '' : '-' + artifact.classifier).toString() + '.' + artifact.extension
    }

    public static String getMavenDep(Project project, String notation) {
		def config = createConfiguration(project, notation);
		def resolvedConfig = config.resolvedConfiguration
		if (resolvedConfig.hasError()) {
			resolvedConfig.rethrowFailure()
		}

		def firstLevelModuleDependencies = resolvedConfig.firstLevelModuleDependencies
		if (firstLevelModuleDependencies.size() != 1) {
			throw new RuntimeException("Expected 1 dependency, got ${firstLevelModuleDependencies.size()}")
		}

		def dependency = firstLevelModuleDependencies.iterator().next()
		def artifacts = dependency.moduleArtifacts
		if (artifacts.size() != 1) {
			throw new RuntimeException("Expected 1 artifact, got ${artifacts.size()}")
		}

		def artifact = artifacts.iterator().next()

		return "${dependency.moduleGroup}:${dependency.moduleName}:${dependency.moduleVersion}".toString() + (artifact.classifier == null || artifact.classifier == "" ? "" : ":${artifact.classifier}").toString() + (artifact.extension == null || artifact.extension == "jar" ? "" : "@${artifact.extension}").toString()
    }

	public static File getMavenFile(Project project, String notation) {
		def config = createConfiguration(project, notation);
		def resolvedConfig = config.resolvedConfiguration
		if (resolvedConfig.hasError()) {
			resolvedConfig.rethrowFailure()
		}

		def firstLevelModuleDependencies = resolvedConfig.firstLevelModuleDependencies
		if (firstLevelModuleDependencies.size() != 1) {
			throw new RuntimeException("Expected 1 dependency, got ${firstLevelModuleDependencies.size()}")
		}

		def dependency = firstLevelModuleDependencies.iterator().next()
		def artifacts = dependency.moduleArtifacts
		if (artifacts.size() != 1) {
			throw new RuntimeException("Expected 1 artifact, got ${artifacts.size()}")
		}

		def artifact = artifacts.iterator().next()

		return artifact.file
	}

	public static def iso8601Now() { new Date().iso8601() }

	public static def sha1(file) {
		MessageDigest md = MessageDigest.getInstance('SHA-1')
		file.eachByte 4096, {bytes, size ->
			md.update(bytes, 0, size)
		}
		return md.digest().collect {String.format "%02x", it}.join()
	}

	private static def artifactTree(project, artifact) {
		if (!project.ext.has('tree_resolver'))
			project.ext.tree_resolver = 1
		def cfg = project.configurations.create('tree_resolver_' + project.ext.tree_resolver++)
		def dep = project.dependencies.create(artifact)
		cfg.dependencies.add(dep)
		def files = cfg.resolve()
		return getArtifacts(project, cfg, true)
	}

	private static boolean checkExists(url) {
		try {
			def code = new URL(url).openConnection().with {
				requestMethod = 'HEAD'
				connect()
				responseCode
			}
			return code == 200
		} catch (Exception e) {
			if (e.toString().contains('unable to find valid certification path to requested target'))
				throw new RuntimeException('Failed to connect to ' + url + ': Missing certificate root authority, try updating java')
			throw e
		}
	}

    static String getLatestForgeVersion(mcVersion) {
        final url = "https://maven.neoforged.net/api/maven/latest/version/releases/net/neoforged/forge?classifier=universal&filter=$mcVersion-"
        final json = new JsonSlurper().parseText(new URL(url).getText('UTF-8'))
        return json.version
    }

    static void processClassNodes(File file, Closure process) {
        file.withInputStream { i ->
            new ZipInputStream(i).withCloseable { zin ->
                ZipEntry zein
                while ((zein = zin.nextEntry) != null) {
                    if (zein.name.endsWith('.class')) {
                        def node = new ClassNode(ASM_LEVEL)
                        new ClassReader(zin).accept(node, 0)
                        process(node)
                    }
                }
            }
        }
    }

	public static Configuration createConfiguration(final Project project, String... depSpecs) {
		final Dependency[] dependencyNotations = Arrays.stream(depSpecs).map { project.getDependencies().create(it) }.toArray(Dependency[]::new)
		return project.getConfigurations().detachedConfiguration(dependencyNotations)
	}
}