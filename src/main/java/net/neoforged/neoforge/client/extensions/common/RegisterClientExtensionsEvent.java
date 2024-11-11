/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions.common;

import java.util.Arrays;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Allows registering client extensions for various game objects.
 *
 * <p>
 * This event is not {@linkplain ICancellableEvent cancellable}.
 *
 * <p>
 * This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.
 */
public final class RegisterClientExtensionsEvent extends Event implements IModBusEvent {
    RegisterClientExtensionsEvent() {}

    /**
     * Register the given {@link IClientBlockExtensions} for the given {@link Block}s
     */
    public void registerBlock(IClientBlockExtensions extensions, Block... blocks) {
        ClientExtensionsManager.register(extensions, ClientExtensionsManager.BLOCK_EXTENSIONS, blocks);
    }

    /**
     * Register the given {@link IClientBlockExtensions} for the given {@link Block}s
     */
    @SafeVarargs
    public final void registerBlock(IClientBlockExtensions extensions, Holder<Block>... blocks) {
        registerBlock(extensions, Arrays.stream(blocks).map(Holder::value).toArray(Block[]::new));
    }

    /**
     * {@return whether a {@link IClientBlockExtensions} has been registered for the given {@link Block}}
     */
    public boolean isBlockRegistered(Block block) {
        return ClientExtensionsManager.BLOCK_EXTENSIONS.containsKey(block);
    }

    /**
     * Register the given {@link IClientItemExtensions} for the given {@link Item}s
     */
    public void registerItem(IClientItemExtensions extensions, Item... items) {
        ClientExtensionsManager.register(extensions, ClientExtensionsManager.ITEM_EXTENSIONS, items);
    }

    /**
     * Register the given {@link IClientItemExtensions} for the given {@link Item}s
     */
    @SafeVarargs
    public final void registerItem(IClientItemExtensions extensions, Holder<Item>... items) {
        registerItem(extensions, Arrays.stream(items).map(Holder::value).toArray(Item[]::new));
    }

    /**
     * {@return whether a {@link IClientItemExtensions} has been registered for the given {@link Item}}
     */
    public boolean isItemRegistered(Item item) {
        return ClientExtensionsManager.ITEM_EXTENSIONS.containsKey(item);
    }

    /**
     * Register the given {@link IClientMobEffectExtensions} for the given {@link MobEffect}s
     */
    public void registerMobEffect(IClientMobEffectExtensions extensions, MobEffect... mobEffects) {
        ClientExtensionsManager.register(extensions, ClientExtensionsManager.MOB_EFFECT_EXTENSIONS, mobEffects);
    }

    /**
     * Register the given {@link IClientMobEffectExtensions} for the given {@link MobEffect}s
     */
    @SafeVarargs
    public final void registerMobEffect(IClientMobEffectExtensions extensions, Holder<MobEffect>... mobEffects) {
        registerMobEffect(extensions, Arrays.stream(mobEffects).map(Holder::value).toArray(MobEffect[]::new));
    }

    /**
     * {@return whether a {@link IClientMobEffectExtensions} has been registered for the given {@link MobEffect}}
     */
    public boolean isMobEffectRegistered(MobEffect mobEffect) {
        return ClientExtensionsManager.MOB_EFFECT_EXTENSIONS.containsKey(mobEffect);
    }

    /**
     * Register the given {@link IClientFluidTypeExtensions} for the given {@link FluidType}s
     */
    public void registerFluidType(IClientFluidTypeExtensions extensions, FluidType... fluidTypes) {
        ClientExtensionsManager.register(extensions, ClientExtensionsManager.FLUID_TYPE_EXTENSIONS, fluidTypes);
    }

    /**
     * Register the given {@link IClientFluidTypeExtensions} for the given {@link FluidType}s
     */
    @SafeVarargs
    public final void registerFluidType(IClientFluidTypeExtensions extensions, Holder<FluidType>... fluidTypes) {
        registerFluidType(extensions, Arrays.stream(fluidTypes).map(Holder::value).toArray(FluidType[]::new));
    }

    /**
     * {@return whether a {@link IClientFluidTypeExtensions} has been registered for the given {@link FluidType}}
     */
    public boolean isFluidTypeRegistered(FluidType fluidType) {
        return ClientExtensionsManager.FLUID_TYPE_EXTENSIONS.containsKey(fluidType);
    }
}
