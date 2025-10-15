/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.entity.animation.json;

import com.google.common.collect.MapMaker;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.Util;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * A loader for entity animations written in JSON. You can also get parsed animations from this class.
 */
public final class AnimationLoader extends ContextAwareReloadListener implements PreparableReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final AnimationLoader INSTANCE = new AnimationLoader();
    public static final StateKey<PendingAnimations> STATE_KEY = new StateKey<>();
    private static final FileToIdConverter LISTER = FileToIdConverter.json("neoforge/animations/entity");

    private final Map<ResourceLocation, AnimationHolder> animations = new MapMaker().weakValues().concurrencyLevel(1).makeMap();
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<AnimationHolder> strongHolderReferences = new ArrayList<>();

    private AnimationLoader() {}

    /**
     * Gets a loaded {@link AnimationDefinition} with the specified {@code key}.
     */
    @Nullable
    public AnimationDefinition getAnimation(ResourceLocation key) {
        final var holder = animations.get(key);
        return holder != null ? holder.getOrNull() : null;
    }

    /**
     * Returns an {@link AnimationHolder} for an animation. If the specified animation has not been loaded, the holder
     * will be unbound, but may be bound in the future.
     */
    public AnimationHolder getAnimationHolder(ResourceLocation key) {
        return animations.computeIfAbsent(key, AnimationHolder::new);
    }

    @Override
    public void prepareSharedState(SharedState sharedState) {
        sharedState.set(STATE_KEY, new PendingAnimations());
    }

    @Override
    public CompletableFuture<Void> reload(SharedState sharedState, Executor prepareExecutor, PreparationBarrier barrier, Executor applyExecutor) {
        ResourceManager resourceManager = sharedState.resourceManager();
        PendingAnimations pending = sharedState.get(STATE_KEY);
        return CompletableFuture.supplyAsync(() -> this.prepare(resourceManager), prepareExecutor)
                .whenComplete((map, error) -> {
                    if (map != null) {
                        pending.future.complete(map);
                    } else {
                        pending.future.completeExceptionally(error);
                    }
                })
                .thenCompose(barrier::wait)
                .thenAcceptAsync(this::apply, applyExecutor);
    }

    private Map<ResourceLocation, AnimationDefinition> prepare(ResourceManager resourceManager) {
        Map<ResourceLocation, AnimationDefinition> map = new HashMap<>();
        SimpleJsonResourceReloadListener.scanDirectory(resourceManager, LISTER, makeConditionalOps(JsonOps.INSTANCE), AnimationParser.CODEC, map);
        return map;
    }

    private void apply(Map<ResourceLocation, AnimationDefinition> animationJsons) {
        animations.values().forEach(AnimationHolder::unbind);
        strongHolderReferences.clear();
        int loaded = 0;
        for (final var entry : animationJsons.entrySet()) {
            final var holder = getAnimationHolder(entry.getKey());
            holder.bind(entry.getValue());
            strongHolderReferences.add(holder);
            loaded++;
        }
        LOGGER.info("Loaded {} entity animations", loaded);
    }

    public static final class PendingAnimations {
        public static final PendingAnimations EMPTY = Util.make(new PendingAnimations(), pending -> pending.future.complete(Map.of()));

        private final CompletableFuture<Map<ResourceLocation, AnimationDefinition>> future;

        private PendingAnimations() {
            this.future = new CompletableFuture<>();
        }

        @Nullable
        public AnimationDefinition get(ResourceLocation id) {
            return this.future.join().get(id);
        }
    }
}
