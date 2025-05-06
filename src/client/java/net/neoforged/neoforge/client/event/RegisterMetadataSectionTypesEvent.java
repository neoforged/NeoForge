/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.function.Consumer;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Fired to allow mods to register their own {@linkplain MetadataSectionType metadata section types}.
 * This event is fired once on startup during the initial resource reload.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterMetadataSectionTypesEvent extends Event implements IModBusEvent {
    private final Consumer<MetadataSectionType<?>> typeConsumer;

    @Internal
    public RegisterMetadataSectionTypesEvent(Consumer<MetadataSectionType<?>> typeConsumer) {
        this.typeConsumer = typeConsumer;
    }

    public void register(MetadataSectionType<?> sectionType) {
        typeConsumer.accept(sectionType);
    }
}
