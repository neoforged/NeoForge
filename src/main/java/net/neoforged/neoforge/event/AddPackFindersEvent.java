/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import java.util.function.Consumer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired on {@link PackRepository} creation to allow mods to add new pack finders.
 */
public class AddPackFindersEvent extends Event implements IModBusEvent {
    private final PackType packType;
    private final Consumer<RepositorySource> sources;
    private final boolean trusted;

    public AddPackFindersEvent(PackType packType, Consumer<RepositorySource> sources, boolean trusted) {
        this.packType = packType;
        this.sources = sources;
        this.trusted = trusted;
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
     * {@return whether or not the pack repository being assembled is the one used to provide known packs to the client to avoid syncing from the server}
     */
    public boolean isTrusted() {
        return trusted;
    }
}
