/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import net.minecraft.DetectedVersion;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.common.I18nExtension;
import net.neoforged.neoforge.forge.snapshots.ForgeSnapshotsMod;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

public class BrandingControl {
    private static List<String> brandings;
    private static List<String> brandingsNoMC;
    private static List<String> overCopyrightBrandings;

    private static void computeBranding() {
        if (brandings == null) {
            ImmutableList.Builder<String> brd = ImmutableList.builder();
            brd.add(ForgeSnapshotsMod.BRANDING_NAME + ' ' + NeoForgeVersion.getVersion());
            brd.add("Minecraft " + DetectedVersion.BUILT_IN.getName());
            int tModCount = ModList.get().size();
            brd.add(I18nExtension.parseMessage("fml.menu.loadingmods", tModCount));
            brandings = brd.build();
            brandingsNoMC = brandings.subList(1, brandings.size());
        }
    }

    private static List<String> getBrandings(boolean includeMC, boolean reverse) {
        computeBranding();
        if (includeMC) {
            return reverse ? Lists.reverse(brandings) : brandings;
        } else {
            return reverse ? Lists.reverse(brandingsNoMC) : brandingsNoMC;
        }
    }

    private static void computeOverCopyrightBrandings() {
        if (overCopyrightBrandings == null) {
            ImmutableList.Builder<String> brd = ImmutableList.builder();
            if (ClientHooks.forgeStatusLine != null) brd.add(ClientHooks.forgeStatusLine);
            overCopyrightBrandings = brd.build();
        }
    }

    public static void forEachLine(boolean includeMC, boolean reverse, BiConsumer<Integer, String> lineConsumer) {
        final List<String> brandings = getBrandings(includeMC, reverse);
        IntStream.range(0, brandings.size()).boxed().forEachOrdered(idx -> lineConsumer.accept(idx, brandings.get(idx)));
    }

    public static void forEachAboveCopyrightLine(BiConsumer<Integer, String> lineConsumer) {
        computeOverCopyrightBrandings();
        IntStream.range(0, overCopyrightBrandings.size()).boxed().forEachOrdered(idx -> lineConsumer.accept(idx, overCopyrightBrandings.get(idx)));
    }

    public static String getClientBranding() {
        return ForgeSnapshotsMod.BRANDING_ID;
    }

    public static String getServerBranding() {
        return ForgeSnapshotsMod.BRANDING_ID;
    }

    public static ResourceManagerReloadListener resourceManagerReloadListener() {
        return BrandingControl::onResourceManagerReload;
    }

    private static void onResourceManagerReload(ResourceManager resourceManager) {
        brandings = null;
        brandingsNoMC = null;
    }
}
