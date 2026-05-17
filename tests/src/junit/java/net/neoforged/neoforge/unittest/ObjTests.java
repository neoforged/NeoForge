/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.obj.ObjGeometry;
import net.neoforged.neoforge.client.model.obj.ObjTokenizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ObjTests {
    public static final ObjGeometry.Settings SETTINGS = new ObjGeometry.Settings(Identifier.fromNamespaceAndPath("unused", "unused"), false, false, false, false, null);

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            object_inside_groups.obj|group1,group2|group1,group1/group1cube1,group1/group1cube2,group2,group2/group2cube
            root_groups.obj|group1cube1,group1cube2,group2cube|group1cube1,group1cube2,group2cube
            root_objects.obj|group1cube1,group1cube2,group2cube|group1cube1,group1cube2,group2cube
            object_outside_group.obj|group1cube1,group1cube2,group2cube|group1cube1,group1cube1/theGroup,group1cube2,group2cube
            """)
    void testObj(String objFile, String expectedGroupNamesStr, String expectedComponentsStr) throws IOException {
        List<String> expectedGroupNames = List.of(expectedGroupNamesStr.split(","));
        List<String> expectedComponents = List.of(expectedComponentsStr.split(","));

        try (InputStream objStream = ObjTests.class.getResourceAsStream("/obj/" + objFile)) {
            Assertions.assertNotNull(objStream, "failed to find obj resource");
            ObjGeometry objGeometry = ObjGeometry.parse(new ObjTokenizer(objStream), SETTINGS);

            List<String> rootComponentNames = new ArrayList<>(objGeometry.getRootComponentNames());
            rootComponentNames.sort(Comparator.naturalOrder());
            Assertions.assertIterableEquals(expectedGroupNames, rootComponentNames, "expected root components not found");

            List<String> configurableComponentNames = new ArrayList<>(objGeometry.getConfigurableComponentNames());
            configurableComponentNames.sort(Comparator.naturalOrder());
            Assertions.assertIterableEquals(expectedComponents, configurableComponentNames, "configurableComponentNames contains objects with parent group prefix");
        }
    }
}
