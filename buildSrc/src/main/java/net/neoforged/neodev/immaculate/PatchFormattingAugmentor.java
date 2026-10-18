package net.neoforged.neodev.immaculate;

import dev.lukebemish.immaculate.FileAugmentor;
import dev.lukebemish.immaculate.FileParams;
import dev.lukebemish.immaculate.FormattingAugmentor;
import dev.lukebemish.immaculate.HandledException;
import dev.lukebemish.immaculate.SharedParams;
import io.codechicken.diffpatch.patch.Patcher;
import io.codechicken.diffpatch.util.Diff;
import io.codechicken.diffpatch.util.Operation;
import io.codechicken.diffpatch.util.PatchFile;
import io.codechicken.diffpatch.util.PatchMode;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.problems.ProblemGroup;
import org.gradle.api.problems.ProblemId;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.jar.JarFile;

public abstract class PatchFormattingAugmentor extends FormattingAugmentor {
    public static final ProblemGroup PROBLEM_GROUP = ProblemGroup.create("net.neoforged.patch-formatting", "Patch Formatting Errors");
    private static final ProblemId MISSING_TAG =
            ProblemId.create("missing-tag", "Missing inserted tag from processed patched source", PROBLEM_GROUP);
    
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getOriginalJar();
    
    @Input
    public abstract Property<String> getBasePrefix();
    
    private record Data(PatchFile original) {}

    @Override
    public FileAugmentor<?> augmentor() {
        try {
            var jarFile = new JarFile(getOriginalJar().getAsFile().get());
            var aPrefix = getBasePrefix().get();

            return new FileAugmentor<Data>() {
                @Override
                public String augment(FileParams params, Consumer<Data> store) {
                    // Store a fresh version of the patch file, to be updated on deaugment
                    store.accept(new Data(PatchFile.fromLines(params.fileName(), params.text().lines().toList(), true)));
                    
                    var patchFile = PatchFile.fromLines(params.fileName(), params.text().lines().toList(), true);

                    // We surround each patch with flags; this will allow us to recover the formatted version of the original patched lines later
                    var idx = 0;
                    var totalOffset = 0;
                    for (var patch : patchFile.patches) {
                        patch.start2 += totalOffset;
                        if (patch.diffs.stream().anyMatch(diff -> diff.op == Operation.INSERT && diff.text.contains("immaculate:neo:ignore"))) {
                            continue;
                        }
                        var offset = 0;
                        var newDiffs = new ArrayList<Diff>();
                        var inserting = false;
                        for (var diff : patch.diffs) {
                            if (diff.op == Operation.INSERT) {
                                if (!inserting) {
                                    inserting = true;
                                    newDiffs.add(new Diff(Operation.INSERT, "//immaculate:neo:start:" + idx));
                                    offset++;
                                    totalOffset++;
                                }
                            } else {
                                if (inserting) {
                                    inserting = false;
                                    newDiffs.add(new Diff(Operation.INSERT, "//immaculate:neo:end:" + idx));
                                    offset++;
                                    totalOffset++;
                                    idx++;
                                }
                            }
                            newDiffs.add(diff);
                        }
                        if (inserting) {
                            newDiffs.add(new Diff(Operation.INSERT, "//immaculate:neo:end:" + idx));
                            offset++;
                            totalOffset++;
                            idx++;
                        }
                        patch.diffs = newDiffs;
                        patch.length2 += offset;
                    }
                    
                    // Find the original file in the base jar, and apply the modified patches to it
                    if (patchFile.basePath == null || !patchFile.basePath.startsWith(aPrefix)) {
                        throw new IllegalArgumentException("Patch file base path expected to start with '"+aPrefix+"' but was '"+patchFile.basePath+"'");
                    }
                    var jarEntry = jarFile.getEntry(patchFile.basePath.substring(aPrefix.length()));
                    List<String> lines;
                    try (
                            var is = jarFile.getInputStream(jarEntry);
                            var reader = new InputStreamReader(is, StandardCharsets.UTF_8)
                    ) {
                        lines = reader.readAllLines();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    
                    var patcher = new Patcher(patchFile, lines);
                    for (var result : patcher.patch(PatchMode.EXACT)) {
                        if (!result.success) {
                            throw new RuntimeException(result.summary());
                        }
                    }

                    return String.join("\n", patcher.lines);
                }

                @Override
                public String deAugment(FileParams params, Data value) {
                    var patchFile = value.original();
                    
                    var textLines = params.text().lines().iterator();

                    // Take the original patch file, iterate it the same as on augment
                    // Then in each hunk of added lines -- instead find the corresponding set of lines in the input text
                    var idx = 0;
                    for (var patch : patchFile.patches) {
                        if (patch.diffs.stream().anyMatch(diff -> diff.op == Operation.INSERT && diff.text.contains("immaculate:neo:ignore"))) {
                            continue;
                        }
                        var offset = 0;
                        var newDiffs = new ArrayList<Diff>();
                        var inserting = false;
                        for (var diff : patch.diffs) {
                            if (diff.op == Operation.INSERT) {
                                if (!inserting) {
                                    inserting = true;
                                    var startTag = "immaculate:neo:start:" + idx;
                                    var endTag = "immaculate:neo:end:" + idx;
                                    var started = false;
                                    while (true) {
                                        if (!textLines.hasNext()) {
                                            final var fStarted = started;
                                            throw params.reporter().throwing(new RuntimeException(), MISSING_TAG, spec -> {
                                                spec.fileLocation(params.fileDisplayPath());
                                                spec.contextualLabel("Expected inserted tag '"+(fStarted ? endTag : startTag)+"' which is missing from the formatted source");
                                                spec.details(patch.toString());
                                                spec.solution("Look for formatters that may be reordering lines or reformatting comments");
                                            });
                                        }
                                        var line = textLines.next();
                                        if (started) {
                                            if (line.contains(endTag)) {
                                                break;
                                            } else {
                                                newDiffs.add(new Diff(Operation.INSERT, line));
                                                offset++;
                                            }
                                        } else {
                                            if (line.contains(startTag)) {
                                                started = true;
                                            }
                                        }
                                    }
                                }
                                offset--;
                            } else {
                                if (inserting) {
                                    inserting = false;
                                    idx++;
                                }
                                newDiffs.add(diff);
                            }
                        }
                        if (inserting) {
                            idx++;
                        }
                        patch.diffs = newDiffs;
                        patch.length2 += offset;
                    }
                    
                    return String.join("\n", patchFile.toLines(true)) + "\n";
                }

                @Override
                public void close(SharedParams params) {
                    try {
                        jarFile.close();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            };
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
