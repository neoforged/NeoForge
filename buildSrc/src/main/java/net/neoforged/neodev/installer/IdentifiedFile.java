package net.neoforged.neodev.installer;

import net.neoforged.neodev.utils.DependencyUtils;
import net.neoforged.neodev.utils.MavenIdentifier;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

/**
 * Combines a {@link File} and its {@link MavenIdentifier maven identifier},
 * for usage as task inputs that will be passed to {@link LibraryCollector}.
 */
public abstract class IdentifiedFile {
    public static Provider<List<IdentifiedFile>> listFromConfiguration(Project project, Configuration configuration) {
        return configuration.getIncoming().getArtifacts().getResolvedArtifacts().map(
                artifacts -> artifacts.stream()
                        .map(artifact -> IdentifiedFile.of(project, artifact))
                        .toList());
    }

    private static IdentifiedFile of(Project project, ResolvedArtifactResult resolvedArtifact) {
        var identifiedFile = project.getObjects().newInstance(IdentifiedFile.class);
        identifiedFile.getFile().set(resolvedArtifact.getFile());
        identifiedFile.getIdentifier().set(DependencyUtils.guessMavenIdentifier(resolvedArtifact));
        return identifiedFile;
    }

    @Inject
    public IdentifiedFile() {}

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getFile();

    @Input
    public abstract Property<MavenIdentifier> getIdentifier();
}
