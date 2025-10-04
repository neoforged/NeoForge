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
import net.minecraft.SharedConstants;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.fml.ModList;
import net.neoforged.fml.i18n.FMLTranslations;
import net.neoforged.neoforge.forge.snapshots.ForgeSnapshotsMod;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.Nullable;

public class BrandingControl {
    @Nullable
    private static String forgeStatusLine;

    @Nullable
    private static List<String> brandings;
    @Nullable
    private static List<String> brandingsNoMC;
    @Nullable
    private static List<String> overCopyrightBrandings;

    private static void computeBranding() {
        if (brandings == null) {
            ImmutableList.Builder<String> brd = ImmutableList.builder();
            brd.add("Minecraft " + SharedConstants.getCurrentVersion().name());
            int modCount = ModList.get().size();
            brd.add(FMLTranslations.parseMessage("fml.menu.branding", ForgeSnapshotsMod.BRANDING_NAME + ' ' + NeoForgeVersion.getVersion(), modCount));
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

    public static void setForgeStatusLine(@Nullable String forgeStatusLine) {
        BrandingControl.forgeStatusLine = forgeStatusLine;
    }

    private static void computeOverCopyrightBrandings() {
        if (overCopyrightBrandings == null) {
            ImmutableList.Builder<String> brd = ImmutableList.builder();
            if (forgeStatusLine != null) brd.add(forgeStatusLine);
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
