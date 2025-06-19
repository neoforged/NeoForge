/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.summary;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.impl.test.AbstractTest;
import net.neoforged.testframework.summary.md.Alignment;
import net.neoforged.testframework.summary.md.Table;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;

public class GitHubActionsStepSummaryDumper implements FileSummaryDumper {
    private static final String SOURCE_FILE_ROOTS_PROPERTY = "net.neoforged.testframework.sourceFileRoots";

    private final Function<TestSummary, String> heading;

    public GitHubActionsStepSummaryDumper() {
        this("Test Summary");
    }

    public GitHubActionsStepSummaryDumper(String heading) {
        this(summary -> heading);
    }

    public GitHubActionsStepSummaryDumper(Function<TestSummary, String> heading) {
        this.heading = heading;
    }

    @Override
    public Path outputPath(ResourceLocation frameworkId) {
        return Path.of(System.getenv("GITHUB_STEP_SUMMARY"));
    }

    @Override
    public boolean enabled(TestSummary summary) {
        return summary.isGameTestRun() && System.getenv().containsKey("GITHUB_STEP_SUMMARY");
    }

    @Override
    public void write(TestSummary summary, Logger logger, PrintWriter writer) {
        writer.println("# " + this.heading.apply(summary));
        Map<Test.Result, List<TestSummary.TestInfo>> testsByStatus = summary.testInfos()
                .stream()
                .collect(Collectors.groupingBy(test -> test.status().result(), () -> new EnumMap<>(Test.Result.class), Collectors.toList()));
        List<TestSummary.TestInfo> failedTests = testsByStatus.getOrDefault(Test.Result.FAILED, List.of());
        List<TestSummary.TestInfo> passedTests = testsByStatus.getOrDefault(Test.Result.PASSED, List.of());
        Table.Builder builder = Table.builder()
                .withAlignments(Alignment.LEFT, Alignment.CENTER, Alignment.LEFT, Alignment.LEFT)
                .addRow("Test Id", "Test Result", "Status message", "Test description");
        if (!failedTests.isEmpty()) {
            for (TestSummary.TestInfo failedTest : failedTests) {
                builder.addRow(
                        failedTest.testId(),
                        formatStatus(failedTest.result(), !failedTest.manual() && !failedTest.required()),
                        failedTest.status().message(),
                        getDescription(failedTest));
            }
        }
        if (!passedTests.isEmpty()) {
            for (TestSummary.TestInfo passedTest : passedTests) {
                builder.addRow(
                        passedTest.testId(),
                        formatStatus(passedTest.status().result(), false),
                        passedTest.status().message(),
                        getDescription(passedTest));
            }
        }
        if (!passedTests.isEmpty() && failedTests.isEmpty()) {
            writer.println("All tests passed");
        }
        writer.println();
        writer.println(builder.build());

        if (!failedTests.isEmpty() && System.getProperty(SOURCE_FILE_ROOTS_PROPERTY) != null) {
            var roots = Arrays.stream(System.getProperty(SOURCE_FILE_ROOTS_PROPERTY).split(",")).map(Path::of).toList();

            record TestLocation(Path path, Method method, String message, int line) {}
            List<TestLocation> locations = new ArrayList<>();

            for (var testInfo : failedTests) {
                var test = summary.framework().tests().byId(testInfo.testId()).orElseThrow();
                if (!(test instanceof AbstractTest.Dynamic dynamic)) continue;
                var method = dynamic.getMethod();
                if (method == null) continue;

                var declaring = method.getDeclaringClass();
                try (var is = declaring.getClassLoader().getResourceAsStream(declaring.getName().replace(".", "/") + ".class")) {
                    if (is == null) continue;

                    var desc = Type.getMethodDescriptor(method);
                    new ClassReader(is).accept(new ClassVisitor(Opcodes.ASM9) {
                        @Nullable
                        private String source;

                        @Override
                        public void visitSource(String source, String debug) {
                            this.source = source;
                        }

                        @Override
                        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                            if (source != null && name.equals(method.getName()) && desc.equals(descriptor)) {
                                return new MethodVisitor(Opcodes.ASM9) {
                                    private boolean foundLine = false;

                                    @Override
                                    public void visitLineNumber(int line, Label start) {
                                        if (foundLine) return;

                                        foundLine = true;

                                        var relativeClassPath = declaring.getPackageName().replace(".", "/") + source;

                                        for (Path root : roots) {
                                            var possibleFile = root.resolve(relativeClassPath);
                                            if (Files.exists(possibleFile)) {
                                                locations.add(new TestLocation(possibleFile.toAbsolutePath(), method, testInfo.message(), line));
                                                break;
                                            }
                                        }
                                    }
                                };
                            }
                            return super.visitMethod(access, name, descriptor, signature, exceptions);
                        }
                    }, ClassReader.SKIP_FRAMES);
                } catch (Exception ex) {
                    logger.error("Failed to read class declaring method {}", method, ex);
                }
            }

            if (!locations.isEmpty()) {
                var workspace = Path.of(System.getenv("GITHUB_WORKSPACE")).toAbsolutePath();
                var errorMessage = locations.stream()
                        .map(loc -> "::error file=" + workspace.relativize(loc.path())
                                + ",line=" + loc.line() + ",title=Test " + loc.method() + " failed::" + loc.message())
                        .collect(Collectors.joining("\n"));
                // Print an empty line before to flush any dangling ANSI modifiers
                System.out.println();
                System.out.println(errorMessage);
                // And an empty line after for symmetry
                System.out.println();
            }
        }
    }

    protected String formatStatus(Test.Result result, boolean optional) {
        if (result.failed() && !optional) {
            return "❌";
        } else if (result.passed()) {
            return "✅";
        }
        return "⚠️";
    }

    private static String getDescription(TestSummary.TestInfo failedTest) {
        return failedTest.description().stream().filter(c -> !c.getString().equals("GameTest-only")).map(FormattingUtil::componentToPlainString).collect(Collectors.joining("<br/>"));
    }
}
