/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ColorResolver;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired for registering block and item color handlers at the appropriate time.
 * See the two subclasses for registering block or item color handlers.
 *
 * <p>These events are fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 *
 * @see BlockTintSources
 * @see RegisterColorHandlersEvent.ItemTintSources
 * @see RegisterColorHandlersEvent.ColorResolvers
 */
public abstract class RegisterColorHandlersEvent extends Event implements IModBusEvent {
    @ApiStatus.Internal
    protected RegisterColorHandlersEvent() {}

    /**
     * Fired for registering block color handlers.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
     *
     * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class BlockTintSources extends RegisterColorHandlersEvent {
        private final BlockColors blockColors;

        @ApiStatus.Internal
        public BlockTintSources(BlockColors blockColors) {
            this.blockColors = blockColors;
        }

        /**
         * {@return the block colors registry}
         *
         * @see BlockColors#register(List, net.minecraft.world.level.block.Block...)
         */
        public BlockColors getBlockColors() {
            return blockColors;
        }

        /**
         * Registers a list of {@link BlockTintSource}s for a set of blocks.
         *
         * @param tintSources The color provider
         * @param blocks      The blocks
         */
        public void register(List<BlockTintSource> tintSources, net.minecraft.world.level.block.Block... blocks) {
            blockColors.register(tintSources, blocks);
        }
    }

    /**
     * Allows registration of custom {@link ColorResolver} implementations to be used with
     * {@link net.minecraft.client.renderer.block.BlockAndTintGetter#getBlockTint(BlockPos, ColorResolver)}.
     */
    public static class ColorResolvers extends RegisterColorHandlersEvent {
        private final ImmutableList.Builder<ColorResolver> builder;

        @ApiStatus.Internal
        public ColorResolvers(ImmutableList.Builder<ColorResolver> builder) {
            this.builder = builder;
        }

        public void register(ColorResolver resolver) {
            this.builder.add(resolver);
        }
    }

    /**
     * Fired for registering item color handlers.
     * <p>
     * This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}
     *
     * @see ItemTintSource
     * @see ItemTintSources
     */
    public static class ItemTintSources extends RegisterColorHandlersEvent {
        private final ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends ItemTintSource>> idMapper;

        @ApiStatus.Internal
        public ItemTintSources(ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends ItemTintSource>> idMapper) {
            this.idMapper = idMapper;
        }

        public void register(Identifier location, MapCodec<? extends ItemTintSource> source) {
            this.idMapper.put(location, source);
        }
    }
}
