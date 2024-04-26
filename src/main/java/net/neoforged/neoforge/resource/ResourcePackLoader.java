/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.resource;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import net.neoforged.fml.ModLoadingWarning;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

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
            final String name = "mod/" + mod.getModId();
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
                        ModLoader.addWarning(new ModLoadingWarning(mod, "fml.modloading.brokenresources", e.getKey()));
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
                ModLoader.addWarning(new ModLoadingWarning(mod, "fml.modloading.brokenresources", e.getKey()));
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

    public static List<String> getPackNames(PackType packType) {
        List<String> ids = new ArrayList<>();
        ids.addAll(ModList.get().getModFiles().stream().filter(packType == PackType.CLIENT_RESOURCES ? IModFileInfo::showAsResourcePack : IModFileInfo::showAsDataPack)
                .filter(mf -> mf.requiredLanguageLoaders().stream().noneMatch(ls -> ls.languageName().equals("minecraft")))
                .map(IModFileInfo::getFile)
                .map(mf -> "mod/" + mf.getModInfos().get(0).getModId())
                .toList());
        ids.add(packType == PackType.CLIENT_RESOURCES ? MOD_RESOURCES_ID : MOD_DATA_ID);
        return ids;
    }

    /*
     To work with the new KnownPack system, a PackRepository should be able to construct a set of specific packs
     given a list of names, and be guaranteed that the final list contains all of those name. At the same time, any
     child packs detected at the root level, while their parent pack is also being added, ought to be included only
     as a child of the parent. This method implements this filtering in an efficient manner.
     */
    @ApiStatus.Internal
    public static List<Pack> expandAndRemoveRootChildren(Stream<Pack> packs, Collection<Pack> availablePacks) {
        Set<Pack> hiddenSubPacks = Sets.newHashSet();
        for (var pack : availablePacks) {
            if (pack.isRequired()) {
                // Remove all children of required packs -- as these will always be present but may not be added till after this method is called
                hiddenSubPacks.addAll(pack.getChildren());
            }
        }
        SequencedSet<Pack> orderedPackSet = new LinkedHashSet<>();
        var iterator = packs.iterator();
        // We iterate the root packs
        while (iterator.hasNext()) {
            var rootPack = iterator.next();
            if (rootPack.isHidden()) {
                // If we've already found this as a child, remove it
                if (orderedPackSet.contains(rootPack) || hiddenSubPacks.contains(rootPack)) {
                    continue;
                }
            }
            // Now we actually add the pack and its children, moving them to the end of the ordering so that if a hidden pack was added earlier, it is moved
            orderedPackSet.addLast(rootPack);
            for (var pack : rootPack.getChildren()) {
                orderedPackSet.addLast(pack);
            }
        }

        return new ArrayList<>(orderedPackSet);
    }

    @ApiStatus.Internal
    public static void reorderNewlyDiscoveredPacks(Collection<String> set, Collection<String> old, PackRepository packRepository) {
        Set<String> added = Sets.newLinkedHashSet(set);
        Set<String> oldSet = Sets.newLinkedHashSet(old);
        set.clear();
        List<String> newOrder = new ArrayList<>();
        for (String s : added) {
            Pack pack = packRepository.getPack(s);
            if (!oldSet.contains(s) && pack != null && pack.getDefaultPosition() == Pack.Position.BOTTOM) {
                newOrder.add(0, s);
            } else {
                newOrder.add(s);
            }
        }
        set.addAll(newOrder);
    }
}
