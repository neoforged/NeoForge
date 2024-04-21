/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.resource;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.OverlayMetadataSection;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResourcePackLoader {
    public static final String MOD_DATA_ID = "mod_data";
    public static final String MOD_RESOURCES_ID = "mod_resources";
    private static Map<IModFile, Pack.ResourcesSupplier> modResourcePacks;
    private static final Logger LOGGER = LogManager.getLogger();
    private static final PackSelectionConfig MOD_PACK_SELECTION_CONFIG = new PackSelectionConfig(false, Pack.Position.BOTTOM, false);

    public static Optional<Pack.ResourcesSupplier> getPackFor(String modId) {
        return Optional.ofNullable(ModList.get().getModFileById(modId)).map(IModFileInfo::getFile).map(mf -> modResourcePacks.get(mf));
    }

    public static void populatePackRepository(PackRepository resourcePacks, PackType packType) {
        findResourcePacks();
        // First add the mod's builtin packs
        resourcePacks.addPackFinder(buildPackFinder(modResourcePacks, packType));
        // Then fire the event to add more packs
        ModLoader.postEvent(new AddPackFindersEvent(packType, resourcePacks::addPackFinder));
    }

    @Deprecated(forRemoval = true, since = "1.20.4")
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

            try {
                var locationInfo = new PackLocationInfo(
                        name,
                        Component.literal(packName.isEmpty() ? "[unnamed]" : packName),
                        PackSource.DEFAULT,
                        Optional.empty());

                final boolean isRequired = (packType == PackType.CLIENT_RESOURCES && mod.getOwningFile().showAsResourcePack()) || (packType == PackType.SERVER_DATA && mod.getOwningFile().showAsDataPack());
                final Pack modPack;
                // Packs displayed separately must be valid
                if (isRequired) {
                    modPack = Pack.readMetaAndCreate(
                            locationInfo,
                            e.getValue(),
                            packType,
                            MOD_PACK_SELECTION_CONFIG);

                    if (modPack == null) {
                        ModLoader.addLoadingIssue(ModLoadingIssue.warning("fml.modloading.brokenresources", e.getKey()).withAffectedMod(mod));
                        continue;
                    }
                } else {
                    modPack = readWithOptionalMeta(
                            locationInfo,
                            e.getValue(),
                            packType,
                            MOD_PACK_SELECTION_CONFIG);
                }

                if (isRequired) {
                    packAcceptor.accept(modPack);
                } else {
                    hiddenPacks.add(modPack.hidden());
                }
            } catch (IOException exception) {
                LOGGER.error("Failed to read pack.mcmeta file of mod {}", mod.getModId(), exception);
                ModLoader.addLoadingIssue(ModLoadingIssue.warning("fml.modloading.brokenresources", e.getKey()).withAffectedMod(mod).withCause(exception));
            }
        }

        packAcceptor.accept(makePack(packType, hiddenPacks));
    }

    public static final MetadataSectionType<PackMetadataSection> OPTIONAL_FORMAT = MetadataSectionType.fromCodec("pack", RecordCodecBuilder.create(
            in -> in.group(
                    ComponentSerialization.CODEC.optionalFieldOf("description", Component.empty()).forGetter(PackMetadataSection::description),
                    Codec.INT.optionalFieldOf("pack_format", -1).forGetter(PackMetadataSection::packFormat),
                    InclusiveRange.codec(Codec.INT).optionalFieldOf("supported_formats").forGetter(PackMetadataSection::supportedFormats))
                    .apply(in, PackMetadataSection::new)));

    public static Pack readWithOptionalMeta(
            PackLocationInfo location,
            Pack.ResourcesSupplier resources,
            PackType type,
            PackSelectionConfig selectionConfig) throws IOException {
        final Pack.Metadata packInfo = readMeta(type, location, resources);
        return new Pack(location, resources, packInfo, selectionConfig);
    }

    private static Pack.Metadata readMeta(PackType type, PackLocationInfo location, Pack.ResourcesSupplier resources) throws IOException {
        final int currentVersion = SharedConstants.getCurrentVersion().getPackVersion(type);
        try (final PackResources primaryResources = resources.openPrimary(location)) {
            final PackMetadataSection metadata = primaryResources.getMetadataSection(OPTIONAL_FORMAT);

            final FeatureFlagSet flags = Optional.ofNullable(primaryResources.getMetadataSection(FeatureFlagsMetadataSection.TYPE))
                    .map(FeatureFlagsMetadataSection::flags)
                    .orElse(FeatureFlagSet.of());

            final List<String> overlays = Optional.ofNullable(primaryResources.getMetadataSection(OverlayMetadataSection.TYPE))
                    .map(section -> section.overlaysForVersion(currentVersion))
                    .orElse(List.of());

            if (metadata == null) {
                return new Pack.Metadata(location.title(), PackCompatibility.COMPATIBLE, flags, overlays, primaryResources.isHidden());
            }

            final PackCompatibility compatibility;
            if (metadata.packFormat() == -1 && metadata.supportedFormats().isEmpty()) {
                compatibility = PackCompatibility.COMPATIBLE;
            } else {
                compatibility = PackCompatibility.forVersion(Pack.getDeclaredPackVersions(location.id(), metadata), currentVersion);
            }
            return new Pack.Metadata(metadata.description(), compatibility, flags, overlays, primaryResources.isHidden());
        }
    }

    private static Pack makePack(PackType packType, ArrayList<Pack> hiddenPacks) {
        final String id = packType == PackType.CLIENT_RESOURCES ? MOD_RESOURCES_ID : MOD_DATA_ID;
        final String name = packType == PackType.CLIENT_RESOURCES ? "Mod Resources" : "Mod Data";
        final String descriptionKey = packType == PackType.CLIENT_RESOURCES ? "fml.resources.modresources" : "fml.resources.moddata";
        return Pack.readMetaAndCreate(
                new PackLocationInfo(id, Component.literal(name), PackSource.DEFAULT, Optional.empty()),
                new EmptyPackResources.EmptyResourcesSupplier(new PackMetadataSection(Component.translatable(descriptionKey, hiddenPacks.size()),
                        SharedConstants.getCurrentVersion().getPackVersion(packType))),
                packType,
                new PackSelectionConfig(true, Pack.Position.BOTTOM, false)).withChildren(hiddenPacks);
    }

    public static Pack.ResourcesSupplier createPackForMod(IModFileInfo mf) {
        return new PathPackResources.PathResourcesSupplier(mf.getFile().getSecureJar().getRootPath());
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
