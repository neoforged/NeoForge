/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.loading.adapt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.neoforged.neoforge.loading.adapt.CompatPrecheck.CheckIssue;
import net.neoforged.neoforge.loading.adapt.CompatPrecheck.CheckStatus;
import net.neoforged.neoforge.loading.adapt.CompatPrecheck.Dep;
import net.neoforged.neoforge.loading.adapt.CompatPrecheck.ModCheck;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.jetbrains.annotations.ApiStatus;

/**
 * Evaluates a mod's declared dependencies against the set of installed mods (<em>PR-ADAPT-1</em>).
 *
 * <p>Missing or out-of-range required dependencies and present {@code INCOMPATIBLE} dependencies
 * block the mod; out-of-range optional dependencies and present {@code DISCOURAGED} dependencies
 * only warn. Pure data in, pure data out — no I/O, no FML state — so the rules are trivially
 * unit-testable.</p>
 */
@ApiStatus.Internal
final class DependencyChecker {
    private DependencyChecker() {}

    static List<CheckIssue> check(ModCheck check, Map<String, ArtifactVersion> versionsById) {
        List<CheckIssue> issues = new ArrayList<>();
        for (Dep dep : check.dependencies()) {
            ArtifactVersion target = versionsById.get(dep.modId());
            boolean inRange = target != null && (dep.range() == null || dep.range().containsVersion(target));
            switch (dep.type()) {
                case REQUIRED -> {
                    if (target == null) {
                        issues.add(new CheckIssue(CheckStatus.BLOCK,
                                "requires missing mod \"" + dep.modId() + "\"",
                                "Install " + dep.modId() + " or remove " + check.modId() + " from the mods folder."));
                    } else if (!inRange) {
                        issues.add(new CheckIssue(CheckStatus.BLOCK,
                                "requires " + dep.modId() + " version " + dep.range() + " but " + target + " is installed",
                                "Update " + dep.modId() + " to a compatible version."));
                    }
                }
                case OPTIONAL -> {
                    if (target != null && !inRange) {
                        issues.add(new CheckIssue(CheckStatus.WARN,
                                "optionally requires " + dep.modId() + " version " + dep.range() + " but " + target + " is installed",
                                "Optional integration with " + dep.modId() + " may be disabled."));
                    }
                }
                case INCOMPATIBLE -> {
                    if (target != null) {
                        issues.add(new CheckIssue(CheckStatus.BLOCK,
                                "is incompatible with installed mod \"" + dep.modId() + "\"",
                                "Remove " + dep.modId() + " or " + check.modId() + "."));
                    }
                }
                case DISCOURAGED -> {
                    if (target != null) {
                        issues.add(new CheckIssue(CheckStatus.WARN,
                                "discourages use with installed mod \"" + dep.modId() + "\"",
                                "Using both mods together is not recommended."));
                    }
                }
            }
        }
        return issues;
    }
}
