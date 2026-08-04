/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.neoforged.neoforge.loading.LoadingConfig;
import net.neoforged.neoforge.loading.adapt.BreakingChangesDatabase;
import net.neoforged.neoforge.loading.adapt.CompatPrecheck;
import net.neoforged.neoforge.loading.adapt.CompatPrecheck.CheckStatus;
import net.neoforged.neoforge.loading.adapt.CompatPrecheck.Dep;
import net.neoforged.neoforge.loading.adapt.CompatPrecheck.ModCheck;
import net.neoforged.neoforge.loading.adapt.CompatPrecheck.Report;
import net.neoforged.neoforge.loading.cache.ModIndexCache;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class CompatPrecheckTest {
    private static final String NONE_BIOME_MODIFIER = "net/neoforged/neoforge/common/world/NoneBiomeModifier";
    private static final String ENTITY_INTERACT_SPECIFIC = "net/neoforged/neoforge/event/entity/player/PlayerInteractEvent$EntityInteractSpecific";

    @TempDir
    Path tempDir;

    private static DefaultArtifactVersion version(String version) {
        return new DefaultArtifactVersion(version);
    }

    private LoadingConfig config() {
        return LoadingConfig.load(tempDir, false);
    }

    private static VersionRange range(String spec) {
        try {
            return VersionRange.createFromVersionSpec(spec);
        } catch (InvalidVersionSpecificationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Dep dep(String modId, String range, IModInfo.DependencyType type) {
        return new Dep(modId, range == null ? null : range(range), type);
    }

    private static Report run(List<ModCheck> checks, ModIndexCache cache, LoadingConfig config, Path gameDir) {
        return CompatPrecheck.run(checks, Map.of(), cache, BreakingChangesDatabase.load(gameDir), config, gameDir);
    }

    private static CompatPrecheck.ModResult resultBy(Report report, String modId) {
        return report.results().stream().filter(r -> r.modId().equals(modId)).findFirst().orElseThrow();
    }

    @Test
    void missingRequiredDependencyBlocks() {
        var mod = new ModCheck("a", version("1.0"), List.of(dep("missing", null, IModInfo.DependencyType.REQUIRED)), null);
        Report report = run(List.of(mod), null, config(), tempDir);
        Assertions.assertEquals(CheckStatus.BLOCK, resultBy(report, "a").status());
        Assertions.assertTrue(report.hasBlocks());
    }

    @Test
    void outOfRangeRequiredDependencyBlocks() {
        var b = new ModCheck("b", version("1.0"), List.of(), null);
        var a = new ModCheck("a", version("1.0"), List.of(dep("b", "[2.0,3.0)", IModInfo.DependencyType.REQUIRED)), null);
        Report report = run(List.of(a, b), null, config(), tempDir);
        Assertions.assertEquals(CheckStatus.BLOCK, resultBy(report, "a").status());
    }

    @Test
    void satisfiedDependencyPasses() {
        var b = new ModCheck("b", version("2.5"), List.of(), null);
        var a = new ModCheck("a", version("1.0"), List.of(dep("b", "[2.0,3.0)", IModInfo.DependencyType.REQUIRED)), null);
        Report report = run(List.of(a, b), null, config(), tempDir);
        Assertions.assertEquals(CheckStatus.PASS, resultBy(report, "a").status());
    }

    @Test
    void incompatiblePresentDependencyBlocks() {
        var b = new ModCheck("b", version("1.0"), List.of(), null);
        var a = new ModCheck("a", version("1.0"), List.of(dep("b", null, IModInfo.DependencyType.INCOMPATIBLE)), null);
        Report report = run(List.of(a, b), null, config(), tempDir);
        Assertions.assertEquals(CheckStatus.BLOCK, resultBy(report, "a").status());
    }

    @Test
    void optionalOutOfRangeWarns() {
        var b = new ModCheck("b", version("1.0"), List.of(), null);
        var a = new ModCheck("a", version("1.0"), List.of(dep("b", "[2.0,3.0)", IModInfo.DependencyType.OPTIONAL)), null);
        Report report = run(List.of(a, b), null, config(), tempDir);
        Assertions.assertEquals(CheckStatus.WARN, resultBy(report, "a").status());
    }

    @Test
    void discouragedPresentDependencyWarns() {
        var b = new ModCheck("b", version("1.0"), List.of(), null);
        var a = new ModCheck("a", version("1.0"), List.of(dep("b", null, IModInfo.DependencyType.DISCOURAGED)), null);
        Report report = run(List.of(a, b), null, config(), tempDir);
        Assertions.assertEquals(CheckStatus.WARN, resultBy(report, "a").status());
    }

    @Test
    void removedSymbolReferenceBlocks() throws Exception {
        Path jar = writeModJar("broken", NONE_BIOME_MODIFIER);
        var mod = new ModCheck("broken", version("1.0"), List.of(), jar);
        Report report = run(List.of(mod), null, config(), tempDir);
        var result = resultBy(report, "broken");
        Assertions.assertEquals(CheckStatus.BLOCK, result.status());
        Assertions.assertTrue(result.issues().stream().anyMatch(i -> i.message().contains("NoneBiomeModifier")));
        Assertions.assertTrue(Files.isRegularFile(tempDir.resolve("neoforge-compat-report.json")), "A machine-readable report must be written");
    }

    @Test
    void safeRenameReferenceWarns() throws Exception {
        Path jar = writeModJar("renamed", ENTITY_INTERACT_SPECIFIC);
        var mod = new ModCheck("renamed", version("1.0"), List.of(), jar);
        Report report = run(List.of(mod), null, config(), tempDir);
        var result = resultBy(report, "renamed");
        Assertions.assertEquals(CheckStatus.WARN, result.status());
        Assertions.assertTrue(result.adaptableSymbols().contains("net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$EntityInteractSpecific"));
    }

    @Test
    void analysisIsCachedPerFingerprint() throws Exception {
        Path jar = writeModJar("cached", NONE_BIOME_MODIFIER);
        var mod = new ModCheck("cached", version("1.0"), List.of(), jar);
        ModIndexCache cache = ModIndexCache.createForTest(tempDir.resolve("index.json"));

        Report first = run(List.of(mod), cache, config(), tempDir);
        Assertions.assertEquals(0, first.cacheHits());
        Assertions.assertEquals(1, first.cacheMisses());

        Report second = run(List.of(mod), cache, config(), tempDir);
        Assertions.assertEquals(1, second.cacheHits(), "Unchanged file must reuse the cached analysis");
        Assertions.assertEquals(0, second.cacheMisses());
        Assertions.assertEquals(first.results(), second.results(), "Cached and fresh analyses must agree");
    }

    private Path writeModJar(String name, String referencedInternalName) throws Exception {
        Path jar = tempDir.resolve(name + ".jar");
        byte[] bytes = buildClass("com/example/" + name + "/Main", referencedInternalName);
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(jar)))) {
            zos.putNextEntry(new ZipEntry("com/example/" + name + "/Main.class"));
            zos.write(bytes);
            zos.closeEntry();
        }
        return jar;
    }

    private static byte[] buildClass(String internalName, String referencedInternalName) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, internalName, null, "java/lang/Object", null);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "touch", "()V", null, null);
        mv.visitCode();
        mv.visitTypeInsn(Opcodes.NEW, referencedInternalName);
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }
}
