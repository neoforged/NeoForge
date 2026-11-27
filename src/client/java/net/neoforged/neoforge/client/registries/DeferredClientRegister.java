/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.registries;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.core.registries.BuiltInClientRegistries;
import net.minecraft.client.core.registries.ClientRegistries;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/// Specialized [DeferredRegister] for [client-only registries][BuiltInClientRegistries] whose objects are connected
/// to a [common registry][BuiltInRegistries], requiring the names of the two related objects to match.
///
/// @see MenuScreen
/// @see BlockEntityRenderers
/// @see EntityRenderers
/// @see ParticleProviders
public class DeferredClientRegister<A, B> extends DeferredRegister<B> {
    public DeferredClientRegister(ResourceKey<? extends Registry<B>> registryKey, String namespace) {
        super(registryKey, namespace);
    }

    public final <I extends B> DeferredHolder<B, I> register(Holder<A> holder, Supplier<? extends I> sup) {
        return super.register(resolveName(holder), sup);
    }

    public final <I extends B> DeferredHolder<B, I> register(Holder<A> holder, Function<Identifier, ? extends I> func) {
        return super.register(resolveName(holder), func);
    }

    private String resolveName(Holder<A> holder) {
        Identifier identifier = holder.key().identifier();
        if (!identifier.getNamespace().equals(getNamespace())) {
            throw new IllegalArgumentException("Common object's namespace does not match register namespace " + getNamespace());
        }
        return identifier.getPath();
    }

    /// Dedicated implementation for [MenuScreens.ScreenConstructor]s, ensuring type-safety between the [MenuType] and screen constructor.
    public static final class MenuScreen extends DeferredClientRegister<MenuType<?>, MenuScreens.ScreenConstructor<?, ?>> {
        public MenuScreen(String namespace) {
            super(ClientRegistries.MENU_SCREEN, namespace);
        }

        public <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> DeferredHolder<MenuScreens.ScreenConstructor<?, ?>, MenuScreens.ScreenConstructor<M, S>> registerScreen(
                DeferredHolder<MenuType<?>, MenuType<M>> holder, MenuScreens.ScreenConstructor<M, S> constructor) {
            return super.register(holder, () -> constructor);
        }
    }

    /// Dedicated implementation for [BlockEntityRendererProvider]s, ensuring type-safety between the [BlockEntityType] and renderer.
    public static final class BlockEntityRenderers extends DeferredClientRegister<BlockEntityType<?>, BlockEntityRendererProvider<?, ?>> {
        public BlockEntityRenderers(String namespace) {
            super(ClientRegistries.BLOCK_ENTITY_RENDERER, namespace);
        }

        public <B extends BlockEntity, S extends BlockEntityRenderState> DeferredHolder<BlockEntityRendererProvider<?, ?>, BlockEntityRendererProvider<B, S>> registerRenderer(
                DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<? extends B>> holder, BlockEntityRendererProvider<B, S> provider) {
            return super.register(holder, () -> provider);
        }
    }

    /// Dedicated implementation for [EntityRendererProvider]s, ensuring type-safety between the [EntityType] and renderer.
    public static final class EntityRenderers extends DeferredClientRegister<EntityType<?>, EntityRendererProvider<?>> {
        public EntityRenderers(String namespace) {
            super(ClientRegistries.ENTITY_RENDERER, namespace);
        }

        public <E extends Entity> DeferredHolder<EntityRendererProvider<?>, EntityRendererProvider<E>> registerRenderer(
                DeferredHolder<EntityType<?>, ? extends EntityType<? extends E>> holder, EntityRendererProvider<E> provider) {
            return super.register(holder, () -> provider);
        }
    }

    /// Dedicated implementation for [ParticleProvider]s, ensuring type-safety between the [ParticleType] and particle provider
    /// and allowing registration of [ParticleResources.SpriteParticleRegistration]s.
    public static final class ParticleProviders extends DeferredClientRegister<ParticleType<?>, ParticleProvider<?>> {
        public ParticleProviders(String namespace) {
            super(ClientRegistries.PARTICLE_PROVIDER, namespace);
        }

        public <O extends ParticleOptions, I extends ParticleProvider<O>> DeferredHolder<ParticleProvider<?>, ParticleProvider<O>> registerProvider(
                DeferredHolder<ParticleType<?>, ParticleType<O>> holder, Supplier<? extends I> sup) {
            return super.register(holder, sup);
        }

        public <O extends ParticleOptions> DeferredHolder<ParticleProvider<?>, ParticleProvider<O>> registerSpriteRegistration(
                DeferredHolder<ParticleType<?>, ParticleType<O>> holder,
                Supplier<? extends ParticleResources.SpriteParticleRegistration<O>> sup) {
            return super.register(holder, () -> ParticleResources.instantiateSpriteParticleRegistration(holder.value(), sup.get()));
        }
    }
}
