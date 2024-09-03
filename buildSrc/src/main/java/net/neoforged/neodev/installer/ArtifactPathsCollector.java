package net.neoforged.neodev.installer;

import java.io.File;

class ArtifactPathsCollector extends ModuleIdentificationVisitor {

    private final StringBuilder builder = new StringBuilder();
    private final String separator;
    private final String prefix;

    public ArtifactPathsCollector(String separator, String prefix) {
        this.separator = separator;
        this.prefix = prefix;
    }

    @Override
    protected void visitModule(File file, String group, String module, String version, String classifier, final String extension) throws Exception {
        builder.append(prefix);
        builder.append(group.replace(".", "/"));
        builder.append("/");
        builder.append(module);
        builder.append("/");
        builder.append(version);
        builder.append("/");
        builder.append(module);
        builder.append("-");
        builder.append(version);

        if (classifier != null && !classifier.isEmpty()) {
            builder.append("-");
            builder.append(classifier);
        }

        builder.append(".").append(extension);
        builder.append(separator);
    }

    @Override
    public String toString() {
        String result = builder.toString();
        if (result.endsWith(separator)) {
            return result.substring(0, result.length() - separator.length());
        } else {
            return result;
        }
    }
}
