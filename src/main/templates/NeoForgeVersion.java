/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

public final class NeoForgeVersion {
    private NeoForgeVersion() {}

    private static final NeoForgeBuildType BUILD_TYPE = NeoForgeBuildType.valueOf("${build_type}");

    public static String getVersion() {
        return "${version}";
    }

    public static String getNeoFormVersion() {
        return "${minecraft_version}-${neoform_version}";
    }

    public static String getMinecraftVersion() {
        return "${minecraft_version}";
    }

    public static NeoForgeBuildType getBuildType() {
        return BUILD_TYPE;
    }
}
