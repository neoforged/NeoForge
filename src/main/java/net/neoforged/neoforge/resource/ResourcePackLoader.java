/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.resource;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.neoforged.fml.*;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ResourcePackLoader {
    public static final String MOD_DATA_ID = "mod_data";
    public static final String MOD_RESOURCES_ID = "mod_resources";
    private static Map<IModFile, Pack.ResourcesSupplier> modResourcePacks;
    private static final Logger LOGGER = LogManager.getLogger();

    public static Optional<Pack.ResourcesSupplier> getPackFor(String modId) {
        return Optional.ofNullable(ModList.get().getModFileById(modId)).map(IModFileInfo::getFile).map(mf -> modResourcePacks.get(mf));
    }

    public static void loadResourcePacks(PackRepository resourcePacks, Function<Map<IModFile, Pack.ResourcesSupplier>, RepositorySource> packFinder) {
        findResourcePacks();
        resourcePacks.addPackFinder(packFinder.apply(modResourcePacks));
    }

    private synchronized static void findResourcePacks() {
        if (modResourcePacks == null) {
            modResourcePacks = ModList.get().getModFiles().stream()
                    .filter(mf -> mf.requiredLanguageLoaders().stream().noneMatch(ls -> ls.languageName().equals("minecraft")))
                    .map(mf -> Pair.of(mf, createPackForMod(mf)))
                    .collect(Collectors.toMap(p -> p.getFirst().getFile(), Pair::getSecond, (u, v) -> {
                        throw new IllegalStateException(String.format(Locale.ENGLISH, "Duplicate key %s", u));
                    }, LinkedHashMap::new));
        }
    }

    public static RepositorySource buildPackFinder(Map<IModFile, Pack.ResourcesSupplier> modResourcePacks, PackType packType) {
        return packAcceptor -> packFinder(modResourcePacks, packAcceptor, packType);
    }

    private static void packFinder(Map<IModFile, Pack.ResourcesSupplier> modResourcePacks, Consumer<Pack> packAcceptor, PackType packType) {
        var hiddenPacks = new ArrayList<Pack>();
        for (Map.Entry<IModFile, Pack.ResourcesSupplier> e : modResourcePacks.entrySet()) {
            IModInfo mod = e.getKey().getModInfos().get(0);
            if ("minecraft".equals(mod.getModId())) continue; // skip the minecraft "mod"
            final String name = "mod:" + mod.getModId();
            final String packName = mod.getOwningFile().getFile().getFileName();
            Pack modPack = Pack.readMetaAndCreate(
                    name,
                    Component.literal(packName.isEmpty() ? "[unnamed]" : packName),
                    false,
                    e.getValue(),
                    packType,
                    Pack.Position.BOTTOM,
                    PackSource.DEFAULT);
            if (modPack == null) {
                // Vanilla only logs an error, instead of propagating, so handle null and warn that something went wrong
                ModLoader.get().addWarning(new ModLoadingWarning(mod, ModLoadingStage.ERROR, "fml.modloading.brokenresources", e.getKey()));
                continue;
            }
            LOGGER.debug(Logging.CORE, "Generating PackInfo named {} for mod file {}", name, e.getKey().getFilePath());
            if ((packType == PackType.CLIENT_RESOURCES && mod.getOwningFile().showAsResourcePack()) || (packType == PackType.SERVER_DATA && mod.getOwningFile().showAsDataPack())) {
                packAcceptor.accept(modPack);
            } else {
                hiddenPacks.add(modPack.hidden());
            }
        }

        packAcceptor.accept(makePack(packType, hiddenPacks));
    }

    private static Pack makePack(PackType packType, ArrayList<Pack> hiddenPacks) {
        final String id = packType == PackType.CLIENT_RESOURCES ? MOD_RESOURCES_ID : MOD_DATA_ID;
        final String name = packType == PackType.CLIENT_RESOURCES ? "Mod Resources" : "Mod Data";
        final String descriptionKey = packType == PackType.CLIENT_RESOURCES ? "fml.resources.modresources" : "fml.resources.moddata";
        return Pack.readMetaAndCreate(id, Component.literal(name), true,
                new EmptyPackResources.EmptyResourcesSupplier(new PackMetadataSection(Component.translatable(descriptionKey, hiddenPacks.size()),
                        SharedConstants.getCurrentVersion().getPackVersion(packType)), false),
                packType, Pack.Position.BOTTOM, PackSource.DEFAULT).withChildren(hiddenPacks);
    }

    @NotNull
    public static Pack.ResourcesSupplier createPackForMod(IModFileInfo mf) {
        return new PathPackResources.PathResourcesSupplier(mf.getFile().getSecureJar().getRootPath(), true);
    }

    public static List<String> getDataPackNames() {
        List<String> ids = new ArrayList<>(ModList.get().getModFiles().stream().filter(IModFileInfo::showAsDataPack)
                .map(IModFileInfo::getFile)
                .map(mf -> "mod:" + mf.getModInfos().get(0).getModId()).filter(n -> !n.equals("mod:minecraft"))
                .toList());
        ids.add(MOD_DATA_ID);
        return ids;
    }

    public static <V> Comparator<Map.Entry<String, V>> getSorter(PackType packType) {
        List<String> order = new ArrayList<>();
        order.add("vanilla");
        if (packType == PackType.CLIENT_RESOURCES) {
            order.add(MOD_RESOURCES_ID);
        } else {
            order.add(MOD_DATA_ID);
        }

        ModList.get().getModFiles().stream()
                .filter(mf -> mf.requiredLanguageLoaders().stream().noneMatch(ls -> ls.languageName().equals("minecraft")))
                .map(e -> e.getMods().get(0).getModId())
                .map(e -> "mod:" + e)
                .forEach(order::add);

        final Object2IntMap<String> order_f = new Object2IntOpenHashMap<>(order.size());
        for (int x = 0; x < order.size(); x++)
            order_f.put(order.get(x), x);

        return (e1, e2) -> {
            final String s1 = e1.getKey();
            final String s2 = e2.getKey();
            final int i1 = order_f.getOrDefault(s1, -1);
            final int i2 = order_f.getOrDefault(s2, -1);

            if (i1 == i2 && i1 == -1)
                return s1.compareTo(s2);
            if (i1 == -1) return 1;
            if (i2 == -1) return -1;
            return i2 - i1;
        };
    }

}
