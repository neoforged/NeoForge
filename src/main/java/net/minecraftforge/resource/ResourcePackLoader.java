/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.resource;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraftforge.fml.*;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ResourcePackLoader {
    private static Map<IModFile, PathPackResources> modResourcePacks;
    private static final Logger LOGGER = LogManager.getLogger();

    public static Optional<PathPackResources> getPackFor(String modId) {
        return Optional.ofNullable(ModList.get().getModFileById(modId)).
                map(IModFileInfo::getFile).map(mf->modResourcePacks.get(mf));
    }

    public static void loadResourcePacks(PackRepository resourcePacks, Function<Map<IModFile, ? extends PathPackResources>, ? extends RepositorySource> packFinder, PackType packType) {
        modResourcePacks = ModList.get().getModFiles().stream()
                .filter(mf->mf.requiredLanguageLoaders().stream().noneMatch(ls->ls.languageName().equals("minecraft")))
                .map(mf -> Pair.of(mf, createPackForMod(mf, packType)))
                .collect(Collectors.toMap(p -> p.getFirst().getFile(), Pair::getSecond, (u,v) -> { throw new IllegalStateException(String.format(Locale.ENGLISH, "Duplicate key %s", u)); },  LinkedHashMap::new));
        resourcePacks.addPackFinder(packFinder.apply(modResourcePacks));
    }

    public static RepositorySource buildPackFinder(Map<IModFile, ? extends PathPackResources> modResourcePacks, PackType packType) {
        return packAcceptor -> packFinder(modResourcePacks, packAcceptor, packType);
    }

    private static void packFinder(Map<IModFile, ? extends PathPackResources> modResourcePacks, Consumer<Pack> packAcceptor, PackType packType) {
        var hiddenPacks = new ArrayList<Pack>();
        for (Map.Entry<IModFile, ? extends PathPackResources> e : modResourcePacks.entrySet())
        {
            IModInfo mod = e.getKey().getModInfos().get(0);
            if (Objects.equals(mod.getModId(), "minecraft")) continue; // skip the minecraft "mod"
            final String name = "mod:" + mod.getModId();
            final Pack modPack = Pack.readMetaAndCreate(name, Component.literal(e.getValue().packId()), false, id -> e.getValue(), packType, Pack.Position.BOTTOM, PackSource.DEFAULT);
            if (modPack == null) {
                // Vanilla only logs an error, instead of propagating, so handle null and warn that something went wrong
                ModLoader.get().addWarning(new ModLoadingWarning(mod, ModLoadingStage.ERROR, "fml.modloading.brokenresources", e.getKey()));
                continue;
            }
            LOGGER.debug(Logging.CORE, "Generating PackInfo named {} for mod file {}", name, e.getKey().getFilePath());
            // TODO: make different option for data pack hiding! Would require forgespi change
            if (mod.getOwningFile().showAsResourcePack()) {
                packAcceptor.accept(modPack);
            } else {
                hiddenPacks.add(modPack);
            }
        }

        final Pack modMarkerPack = packType == PackType.CLIENT_RESOURCES ? makeClientPack(hiddenPacks) : makeServerPack(hiddenPacks);

        packAcceptor.accept(modMarkerPack);
    }

    private static Pack makeServerPack(ArrayList<Pack> hiddenPacks) {
        return Pack.readMetaAndCreate("mod_data", Component.literal("Mod Data"), true,
                id -> new EmptyPackResources(id, false, new PackMetadataSection(Component.translatable("fml.resources.moddata", hiddenPacks.size()),
                        SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA))),
                PackType.SERVER_DATA, Pack.Position.BOTTOM, PackSource.DEFAULT).withChildren(hiddenPacks);
    }

    public static Pack makeClientPack(ArrayList<Pack> hiddenPacks) {
        final Pack modMarkerPack = Pack.readMetaAndCreate("mod_resources", Component.literal("Mod Resources"), true,
                id -> new EmptyPackResources(id, false, new PackMetadataSection(Component.translatable("fml.resources.modresources", hiddenPacks.size()),
                        SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES))),
                PackType.CLIENT_RESOURCES, Pack.Position.BOTTOM, PackSource.DEFAULT).withChildren(hiddenPacks);
        return modMarkerPack;
    }

    @NotNull
    public static PathPackResources createPackForMod(IModFileInfo mf, PackType packType)
    {
        String name = mf.getFile().getFileName();
        return new PathPackResources(name.isEmpty() ? "[unnamed]" : name, true, mf.getFile().getFilePath())
        {
            private final IModFile modFile = mf.getFile();

            @NotNull
            @Override
            protected Path resolve(@NotNull String... paths)
            {
                return this.modFile.findResource(paths);
            }

            @Override
            public boolean isHidden() {
                // TODO: use different flags per pack type
                return !mf.showAsResourcePack();
            }
        };
    }

    public static List<String> getPackNames() {
        return ModList.get().applyForEachModFile(mf->"mod:"+mf.getModInfos().get(0).getModId()).filter(n->!n.equals("mod:minecraft")).collect(Collectors.toList());
    }

    public static <V> Comparator<Map.Entry<String,V>> getSorter() {
        List<String> order = new ArrayList<>();
        order.add("vanilla");
        order.add("mod_resources");

        ModList.get().getModFiles().stream()
                .filter(mf -> mf.requiredLanguageLoaders().stream().noneMatch(ls->ls.languageName().equals("minecraft")))
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
