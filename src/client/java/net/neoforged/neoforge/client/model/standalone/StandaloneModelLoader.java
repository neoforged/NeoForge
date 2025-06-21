/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.standalone;

import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.util.thread.ParallelMapTransform;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class StandaloneModelLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    private StandaloneModelLoader() {}

    public static CompletableFuture<LoadedModels> load(Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<StandaloneModelKey<?>, UnbakedStandaloneModel<?>> models = new IdentityHashMap<>();
            ModLoader.postEvent(new ModelEvent.RegisterStandalone(models));
            return new LoadedModels(models);
        }, executor);
    }

    public static CompletableFuture<BakedModels> bake(LoadedModels standaloneModels, ModelBaker baker, Executor executor) {
        return ParallelMapTransform.schedule(standaloneModels.models, (key, model) -> {
            try {
                return model.bake(baker);
            } catch (Exception e) {
                LOGGER.warn("Unable to bake standalone model: '{}': {}", key.getName(), e);
                return null;
            }
        }, executor).thenApply(BakedModels::new);
    }

    public record LoadedModels(Map<StandaloneModelKey<?>, UnbakedStandaloneModel<?>> models) {
        public static final LoadedModels EMPTY = new LoadedModels(Map.of());
    }

    public record BakedModels(Map<StandaloneModelKey<?>, ?> models) {
        public static final BakedModels EMPTY = new BakedModels(Map.of());

        @Nullable
        @SuppressWarnings("unchecked")
        public <T> T get(StandaloneModelKey<T> key) {
            return (T) models.get(key);
        }

        public BakedModels unmodifiable() {
            return new BakedModels(Collections.unmodifiableMap(models));
        }
    }
}
