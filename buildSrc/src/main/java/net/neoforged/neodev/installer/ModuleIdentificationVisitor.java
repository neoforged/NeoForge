package net.neoforged.neodev.installer;

import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: simplify this class and subclasses if possible
abstract class ModuleIdentificationVisitor implements FileVisitor {

    // The following regex detects file path patterns, in gradle cache format. Like:  /net.neoforged.fancymodloader/earlydisplay/47.1.47/46509b19504a71e25b115383e900aade5088598a/earlydisplay-47.1.47.jar
    private static final Pattern GRADLE_CACHE_PATTERN = Pattern.compile("/(?<group>[^/]+)/(?<module>[^/]+)/(?<version>[^/]+)/(?<hash>[a-z0-9]+)/\\k<module>-\\k<version>(-(?<classifier>[^/]+))?\\.(?<extension>(jar)|(zip))$");

    // The following regex detects file path patterns, in maven local cache format. Like:  /.m2/repository/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar
    private static final Pattern MAVEN_LOCAL_PATTERN = Pattern.compile("/.m2/repository/(?<group>.+)/(?<module>[^/]+)/(?<version>[^/]+)/\\k<module>-\\k<version>(-(?<classifier>[^/]+))?\\.(?<extension>(jar)|(zip))$");

    @Override
    public void visitDir(FileVisitDetails dirDetails) {
        //Noop
    }

    @Override
    public void visitFile(FileVisitDetails fileDetails) {
        File file = fileDetails.getFile();
        String absolutePath = file.getAbsolutePath().replace("\\", "/");

        Matcher matcher = GRADLE_CACHE_PATTERN.matcher(absolutePath);
        if (!matcher.find()) {
            matcher = MAVEN_LOCAL_PATTERN.matcher(absolutePath);
            if (!matcher.find()) {
                throw new IllegalStateException("Cannot determine the GAV of " + file + ", since it is neither a remote nor a Maven local dependency!");
            }
        }

        String group = matcher.group("group").replace("/", "."); //In case we match the maven way.
        String module = matcher.group("module");
        String version = matcher.group("version");
        String classifier = matcher.group("classifier") == null ? "" : matcher.group("classifier");
        String extension = matcher.group("extension");

        try {
            visitModule(file, group, module, version, classifier, extension);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void visitModule(File file, String group, String module, String version, String classifier, String extension) throws Exception;
}
