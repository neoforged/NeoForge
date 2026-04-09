/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

public abstract sealed class RegisterCauldronInteractionEvent extends Event implements IModBusEvent {
    protected final ExtraCodecs.LateBoundIdMapper<String, CauldronInteraction.Dispatcher> idMapper;

    protected RegisterCauldronInteractionEvent(ExtraCodecs.LateBoundIdMapper<String, CauldronInteraction.Dispatcher> idMapper) {
        this.idMapper = idMapper;
    }

    /// Fired to allow mods to register their own cauldron interaction dispatchers.
    /// 
    /// The event is fired on the mod bus
    public static final class Dispatcher extends RegisterCauldronInteractionEvent {
        @ApiStatus.Internal
        public Dispatcher(ExtraCodecs.LateBoundIdMapper<String, CauldronInteraction.Dispatcher> idMapper) {
            super(idMapper);
        }

        public void register(Identifier id, CauldronInteraction.Dispatcher dispatcher) {
            idMapper.put(id.toString(), dispatcher);
        }
    }

    /// Fired to allow mods to register cauldron interactions to existing dispatchers.
    /// You can register to specific dispatchers or to all dispatchers at once.
    /// 
    /// The event is fired on the mod bus
    public static final class Interaction extends RegisterCauldronInteractionEvent {
        @ApiStatus.Internal
        public Interaction(ExtraCodecs.LateBoundIdMapper<String, CauldronInteraction.Dispatcher> idMapper) {
            super(idMapper);
        }

        /// Registers a cauldron interaction for the specified item within the dispatcher associated with the given identifier.
        ///
        /// @param dispatcherId The unique identifier of the dispatcher where the interaction should be registered.
        /// @param item         The item that will trigger the cauldron interaction.
        /// @param interaction  The cauldron interaction to be registered for the specified item.
        /// @throws IllegalArgumentException If no dispatcher is found for the given identifier.
        public void register(Identifier dispatcherId, Item item, CauldronInteraction interaction) {
            CauldronInteraction.Dispatcher dispatcher = idMapper.getValue(dispatcherId.toShortString());
            if (dispatcher == null) {
                throw new IllegalArgumentException("No dispatcher registered with id: " + dispatcherId);
            }
            dispatcher.put(item, interaction);
        }

        /// Registers a cauldron interaction for the specified item tag within the dispatcher associated with the given identifier.
        ///
        /// @param dispatcherId The identifier of the dispatcher to register the interaction with.
        /// @param itemTag      The tag key of the item that will trigger the cauldron interaction.
        /// @param interaction  The cauldron interaction to be registered for the specified item tag.
        /// @throws IllegalArgumentException If no dispatcher is found with the given identifier,
        public void register(Identifier dispatcherId, TagKey<Item> itemTag, CauldronInteraction interaction) {
            CauldronInteraction.Dispatcher dispatcher = idMapper.getValue(dispatcherId.toShortString());
            if (dispatcher == null) {
                throw new IllegalArgumentException("No dispatcher registered with id: " + dispatcherId);
            }
            dispatcher.put(itemTag, interaction);
        }

        /// Registers a cauldron interaction for the specified item across all existing dispatchers.
        ///
        /// @param item        The item that will trigger the cauldron interaction.
        /// @param interaction The cauldron interaction to be registered for the specified item.
        public void registerToAll(Item item, CauldronInteraction interaction) {
            for (CauldronInteraction.Dispatcher dispatcher : idMapper.values()) {
                dispatcher.put(item, interaction);
            }
        }

        /// Registers a cauldron interaction for the specified item tag across all existing dispatchers.
        ///
        /// @param itemTag     The tag key of the items that will trigger the cauldron interaction.
        /// @param interaction The cauldron interaction to be registered for items associated with the specified tag.
        public void registerToAll(TagKey<Item> itemTag, CauldronInteraction interaction) {
            for (CauldronInteraction.Dispatcher dispatcher : idMapper.values()) {
                dispatcher.put(itemTag, interaction);
            }
        }
    }
}
