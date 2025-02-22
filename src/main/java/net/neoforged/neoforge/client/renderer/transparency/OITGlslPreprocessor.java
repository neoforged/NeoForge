package net.neoforged.neoforge.client.renderer.transparency;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.shaders.CompiledShader;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.renderer.INeoForgeGlslPreprocessor;

import java.util.List;

public class OITGlslPreprocessor implements INeoForgeGlslPreprocessor
{

    @Override
    public List<String> process(String shaderSource, CompiledShader.Type type)
    {
        if (type != CompiledShader.Type.FRAGMENT)
        {
            return List.of(shaderSource.split("\n"));
        }

        final List<String> lines = Lists.newArrayList(shaderSource.split("\n"));

        if (!isAdaptable(lines))
        {
            return lines;
        }

        updateVersion(lines);
        injectTransparentRenderUniform(lines);
        renameMainMethod(lines);
        injectShaderPrefix(lines);
        injectShaderSuffix(lines);

        return lines;
    }

    private static boolean isAdaptable(final List<String> lines) {
        return lines.stream().anyMatch(s -> s.equals("in float vertexDistance;")) &&
                lines.stream().filter(s -> s.startsWith("out vec4")).count() == 1;
    }

    private static void updateVersion(List<String> lines)
    {
        //We inject the extension so we can handle this for now like this.
        final int versionLine = findLastLineIndexStartingWith(lines, "#version");
        if (versionLine == -1) {
            throw new IllegalStateException("Failed to find #version line in shader");
        }

        final String[] versionParts = OITShaders.WBOIT_FRAGMENT_SHADER_VERSION_HEADER.split("\n");

        lines.remove(versionLine);
        for (int i = 0; i < versionParts.length; i++)
        {
            String versionPart = versionParts[i];
            lines.add(versionLine + i, versionPart);
        }
    }

    private static void injectTransparentRenderUniform(List<String> lines)
    {
        injectUniform(lines, ClientHooks.UNIFORM_OIT_TYPE, ClientHooks.UNIFORM_OIT_NAME);
        injectSampler2D(lines, ClientHooks.SAMPLER_OIT_NAME);
    }

    private static void injectSampler2D(final List<String> lines, final String name) {
        injectUniform(lines, "sampler2D", name);
    }

    private static void injectUniform(final List<String> lines, final String type, final String name) {
        int injectionIndex = findLastLineIndexStartingWith(lines, "uniform");
        if (injectionIndex == -1) {
            injectionIndex = findLastLineIndexStartingWith(lines, "#extension");
            if (injectionIndex == -1) {
                injectionIndex = findLastLineIndexStartingWith(lines, "#version");
                if (injectionIndex == -1) {
                    throw new IllegalStateException("Failed to find #version, or #extension or existing uniform lines in shader");
                }
            }
        }
        lines.add(injectionIndex + 1, "uniform " + type + " " + name + ";");
    }

    private static int findLastLineIndexStartingWith(final List<String> lines, final String prefix) {
        for (int i = lines.size() - 1; i >= 0; --i) {
            if (lines.get(i).trim().startsWith(prefix)) {
                return i;
            }
        }
        return -1;
    }

    private static void renameMainMethod(final List<String> lines) {
        final int mainMethodLine = findLastLineIndexStartingWith(lines, "void main()");

        final String mainMethod = lines.get(mainMethodLine);
        lines.set(mainMethodLine, mainMethod.replace("main", "mainShader"));
    }

    private static void injectShaderPrefix(final List<String> lines) {
        int versionLine = findLastLineIndexStartingWith(lines, "#extension");
        if (versionLine == -1) {
            versionLine = findLastLineIndexStartingWith(lines, "#version");
            if (versionLine == -1) {
                throw new IllegalStateException("Failed to find #version or #extension line in shader");
            }
        }

        final List<String> toInject = List.of(OITShaders.WBOIT_FRAGMENT_SHADER_PREFIX.split("\n"));
        for (int i = 0; i < toInject.size(); i++)
        {
            String line = toInject.get(i);
            lines.add(versionLine + 1 + i, line);
        }
    }

    private static void injectShaderSuffix(final List<String> lines) {
        final int outLine = findLastLineIndexStartingWith(lines, "out vec4");
        final String outVariable = lines.get(outLine).split(" ")[2].replace(";", "");

        lines.remove(outLine);
        lines.add(outLine, "vec4 " + outVariable + ";");

        //TODO: Dynamically determine the depth variable name
        final String depthVariable = "vertexDistance";

        final String code = OITShaders.WBOIT_FRAGMENT_SHADER_SUFFIX
                .replace("%OUT_COLOR%", outVariable)
                .replace("%Z_VALUE%", depthVariable);

        final List<String> toInject = List.of(code.split("\n"));
        lines.add("\n");
        lines.addAll(toInject);
    }
}