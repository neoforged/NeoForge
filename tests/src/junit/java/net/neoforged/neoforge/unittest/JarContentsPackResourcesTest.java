/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.neoforge.resource.JarContentsPackResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class JarContentsPackResourcesTest {
    @TempDir
    Path tempDir;
    JarContents contents;

    @BeforeEach
    void setup() throws IOException {
        contents = JarContents.ofPath(tempDir);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            CLIENT_RESOURCES,assets/namespace,,namespace
            CLIENT_RESOURCES,assets/namespace/subdir,,namespace
            # With prefix
            CLIENT_RESOURCES,other/assets/namespace/subdir,,
            CLIENT_RESOURCES,other/assets/namespace/subdir,other,namespace
            CLIENT_RESOURCES,other/assets/namespace/subdir,other/,namespace
            CLIENT_RESOURCES,data/namespace,,
            # Only scan dirs
            CLIENT_RESOURCES,assets,,
            # Same with server resources
            SERVER_DATA,data/namespace,,namespace
            SERVER_DATA,data/namespace/subdir,,namespace
            # With prefix
            SERVER_DATA,other/data/namespace/subdir,,
            SERVER_DATA,other/data/namespace/subdir,other,namespace
            SERVER_DATA,other/data/namespace/subdir,other/,namespace
            SERVER_DATA,assets/namespace,,
            # Only scan dirs
            SERVER_DATA,data,,
            """)
    void testGetNamespaces(PackType packType, String path, String prefix, String expected) throws IOException {
        Files.createDirectories(tempDir.resolve(path));
        Files.createFile(tempDir.resolve(path).resolve("dummy"));

        var namespaces = getResources(prefix).getNamespaces(packType);
        Set<String> expectedSet = expected == null ? Set.of() : Set.of(expected.split(";"));
        assertEquals(expectedSet, namespaces);
    }

    private PackResources getResources(String prefix) {
        prefix = Objects.requireNonNullElse(prefix, "");

        return new JarContentsPackResources.JarContentsResourcesSupplier(contents, prefix).openPrimary(
                new PackLocationInfo(
                        "x", Component.literal("x"), PackSource.BUILT_IN, Optional.empty()));
    }
}
