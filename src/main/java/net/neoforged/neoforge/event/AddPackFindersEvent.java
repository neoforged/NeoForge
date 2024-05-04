/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired on {@link PackRepository} creation to allow mods to add new pack finders.
 */
public class AddPackFindersEvent extends Event implements IModBusEvent {
    private final PackType packType;
    private final Consumer<RepositorySource> sources;

    public AddPackFindersEvent(PackType packType, Consumer<RepositorySource> sources) {
        this.packType = packType;
        this.sources = sources;
    }

    /**
     * Adds a new source to the list of pack finders.
     *
     * <p>Sources are processed in the order that they are added to the event.
     * Use {@link Pack.Position#TOP} to add high priority packs,
     * and {@link Pack.Position#BOTTOM} to add low priority packs.
     * 
     * @param source the pack finder
     */
    public void addRepositorySource(RepositorySource source) {
        sources.accept(source);
    }

    /**
     * @return the {@link PackType} of the pack repository being constructed.
     */
    public PackType getPackType() {
        return packType;
    }

    /**
     * Helper method to register a pack found under the `resources/` folder.
     *
     * @param packLocation    Location of the pack to load. Namespace should be the modid of the pack owner and path is the location under `resources/` folder
     * @param packType        Whether pack is a resourcepack or datapack
     * @param packNameDisplay The text that shows for the pack on the pack selection screen
     * @param alwaysActive    Whether the pack is forced active always. If false, players have to manually activate the pack themselves
     * @param packPosition    Where the pack goes for determining pack applying order
     */
    public void addPackFinders(ResourceLocation packLocation, PackType packType, Component packNameDisplay, boolean alwaysActive, Pack.Position packPosition) {
        if (getPackType() == packType) {
            var resourcePath = ModList.get().getModFileById(packLocation.getNamespace()).getFile().findResource(packLocation.getPath());

            var pack = Pack.readMetaAndCreate(
                    new PackLocationInfo("builtin/" + packLocation.getNamespace(), packNameDisplay, PackSource.BUILT_IN, Optional.empty()),
                    BuiltInPackSource.fromName((path) -> new PathPackResources(path, resourcePath)),
                    packType,
                    new PackSelectionConfig(alwaysActive, packPosition, false));

            addRepositorySource((packConsumer) -> packConsumer.accept(pack));
        }
    }
}
