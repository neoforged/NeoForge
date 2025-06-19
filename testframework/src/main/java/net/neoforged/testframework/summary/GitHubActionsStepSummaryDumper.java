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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.impl.test.AbstractTest;
import net.neoforged.testframework.summary.md.Alignment;
import net.neoforged.testframework.summary.md.Table;
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

        // Generate check run annotations for failed tests that are @TestHolder methods
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

                // Try to find the method's class file and read its bytecode to figure out the name of the source file and the line number range of the test method
                try (var is = declaring.getClassLoader().getResourceAsStream(declaring.getName().replace(".", "/") + ".class")) {
                    if (is == null) continue;

                    AtomicReference<String> source = new AtomicReference<>();
                    // We collect both the first and the last line of the method to be able to find stack track elements included within the method's bounds
                    AtomicInteger firstLine = new AtomicInteger(-1), lastLine = new AtomicInteger();

                    var desc = Type.getMethodDescriptor(method);
                    new ClassReader(is).accept(new ClassVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitSource(String s, String debug) {
                            source.set(s);
                        }

                        @Override
                        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                            if (source.get() != null && name.equals(method.getName()) && desc.equals(descriptor)) {
                                return new MethodVisitor(Opcodes.ASM9) {
                                    private int lastFoundLine;

                                    @Override
                                    public void visitLineNumber(int line, Label start) {
                                        if (firstLine.get() == -1) {
                                            firstLine.set(line);
                                        }
                                        lastFoundLine = line;
                                    }

                                    @Override
                                    public void visitEnd() {
                                        lastLine.set(lastFoundLine);
                                    }
                                };
                            }
                            return super.visitMethod(access, name, descriptor, signature, exceptions);
                        }
                    }, ClassReader.SKIP_FRAMES);

                    // If we cannot find the method within the class file or if it doesn't have line number information we can't emit any annotation
                    if (firstLine.get() == -1) continue;

                    var relativeClassPath = declaring.getPackageName().replace(".", "/") + "/" + source.get();

                    for (Path root : roots) {

                        // Try to find the first source root folder where a source file with the name found in the bytecode and corresponding package exists
                        var possibleFile = root.resolve(relativeClassPath);
                        if (Files.exists(possibleFile)) {
                            int line = firstLine.get();

                            var exception = testInfo.status().exception();
                            if (exception != null) {
                                // If we have an exception, try to point the annotation at the first line of the exception within the same source file
                                // and within the lines of the method, otherwise, we point it at the first line of the method test that failed
                                for (StackTraceElement element : exception.getStackTrace()) {
                                    if (Objects.equals(element.getFileName(), source.get()) && firstLine.get() <= element.getLineNumber() && element.getLineNumber() <= lastLine.get()) {
                                        line = element.getLineNumber();
                                        break;
                                    }
                                }
                            }

                            locations.add(new TestLocation(possibleFile.toAbsolutePath(), method, testInfo.message(), line));
                            break;
                        }
                    }
                } catch (Exception ex) {
                    logger.error("Failed to read class declaring method {}", method, ex);
                }
            }

            if (!locations.isEmpty()) {
                // Finally, emit the annotations but make sure to first relativise all paths to the workspace folder
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
