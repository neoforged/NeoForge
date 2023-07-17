package net.minecraftforge.forge.tasks

import groovy.transform.CompileStatic
import org.eclipse.jgit.api.Git
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

@CompileStatic
abstract class FailGitChanges extends DefaultTask {
    @TaskAction
    void run() {
        try (var git = Git.open(project.rootProject.rootDir)) {
            var statusResult = git.status().call()
            if (statusResult.hasUncommittedChanges()) {
                git.add().addFilepattern('.').call()
                git.diff().setCached(true).setOutputStream(System.out).call()
                throw new IllegalStateException("Uncommitted changes found: ${statusResult.uncommittedChanges}")
            }
        }
    }
}
