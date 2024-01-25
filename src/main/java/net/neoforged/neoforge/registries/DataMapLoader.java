/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapFile;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapValueMerger;
import org.slf4j.Logger;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DataMapLoader implements PreparableReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String PATH = "data_maps";
    private Map<ResourceKey<? extends Registry<?>>, LoadResult<?>> results;
    private final ICondition.IContext conditionContext;
    private final RegistryAccess registryAccess;

    public DataMapLoader(ICondition.IContext conditionContext, RegistryAccess registryAccess) {
        this.conditionContext = conditionContext;
        this.registryAccess = registryAccess;
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        return this.load(resourceManager, backgroundExecutor, preparationsProfiler)
                .thenCompose(preparationBarrier::wait)
                .thenAcceptAsync(values -> this.results = values, gameExecutor);
    }

    public void apply() {
        results.forEach((key, result) -> this.apply((BaseMappedRegistry) registryAccess.registryOrThrow(key), result));

        // Clear the intermediary maps and objects
        results = null;
        System.gc();
    }

    private <T> void apply(BaseMappedRegistry<T> registry, LoadResult<T> result) {
        registry.dataMaps.clear();
        result.results().forEach((key, entries) -> registry.dataMaps.put(
                key, this.buildDataMap(registry, key, (List) entries)));
    }

    private <T, R> Map<ResourceKey<R>, T> buildDataMap(Registry<R> registry, DataMapType<T, R> attachment, List<DataMapFile<T, R>> entries) {
        record WithSource<T, R>(T attachment, Either<TagKey<R>, ResourceKey<R>> source) {}
        final Map<ResourceKey<R>, WithSource<T, R>> result = new IdentityHashMap<>();
        final BiConsumer<Either<TagKey<R>, ResourceKey<R>>, Consumer<Holder<R>>> valueResolver = (key, cons) -> key.ifLeft(tag -> registry.getTagOrEmpty(tag).forEach(cons)).ifRight(k -> cons.accept(registry.getHolderOrThrow(k)));
        final DataMapValueMerger<T, R> merger = attachment instanceof AdvancedDataMapType<T, R, ?> adv ? adv.merger() : DataMapValueMerger.defaultMerger();
        entries.forEach(entry -> {
            if (entry.replace()) {
                result.clear();
            }

            entry.values().forEach((tKey, value) -> {
                if (value.isEmpty()) return;

                valueResolver.accept(tKey, holder -> {
                    final var newValue = value.get().carrier();
                    final var key = holder.unwrapKey().orElseThrow();
                    final var oldValue = result.get(key);
                    if (oldValue == null || newValue.replace()) {
                        result.put(key, new WithSource<>(newValue.value(), tKey));
                    } else {
                        result.put(key, new WithSource<>(merger.merge(registry, oldValue.source(), oldValue.attachment(), tKey, newValue.value()), tKey));
                    }
                });
            });

            entry.removals().forEach(trRemoval -> valueResolver.accept(trRemoval.key(), holder -> {
                if (trRemoval.remover().isPresent()) {
                    final var key = holder.unwrapKey().orElseThrow();
                    final var oldValue = result.get(key);
                    if (oldValue != null) {
                        final var newValue = trRemoval.remover().get().remove(oldValue.attachment(), registry, oldValue.source(), holder.value());
                        if (newValue.isEmpty()) {
                            result.remove(key);
                        } else {
                            result.put(key, new WithSource<>(newValue.get(), oldValue.source()));
                        }
                    }
                } else {
                    result.remove(holder.unwrapKey().orElseThrow());
                }
            }));
        });
        final Map<ResourceKey<R>, T> newMap = new IdentityHashMap<>();
        result.forEach((key, val) -> newMap.put(key, val.attachment()));

        return newMap;
    }

    private CompletableFuture<Map<ResourceKey<? extends Registry<?>>, LoadResult<?>>> load(ResourceManager manager, Executor executor, ProfilerFiller profiler) {
        return CompletableFuture.supplyAsync(() -> load(manager, profiler, registryAccess, conditionContext), executor);
    }

    private static Map<ResourceKey<? extends Registry<?>>, LoadResult<?>> load(ResourceManager manager, ProfilerFiller profiler, RegistryAccess access, ICondition.IContext context) {
        final RegistryOps<JsonElement> ops = ConditionalOps.create(RegistryOps.create(JsonOps.INSTANCE, access), context);

        final Map<ResourceKey<? extends Registry<?>>, LoadResult<?>> values = new HashMap<>();
        access.registries().forEach(registryEntry -> {
            final var registryKey = registryEntry.key();
            profiler.push("registry_data_maps/" + registryKey.location() + "/locating");
            final var fileToId = FileToIdConverter.json(PATH + "/" + getFolderLocation(registryKey.location()));
            for (Map.Entry<ResourceLocation, List<Resource>> entry : fileToId.listMatchingResourceStacks(manager).entrySet()) {
                ResourceLocation key = entry.getKey();
                final ResourceLocation attachmentId = fileToId.fileToId(key);
                final var attachment = RegistryManager.getDataMap((ResourceKey) registryKey, attachmentId);
                if (attachment == null) {
                    LOGGER.warn("Found data map file for non-existent data map type '{}' on registry '{}'.", attachmentId, registryKey.location());
                    continue;
                }
                profiler.popPush("registry_data_maps/" + registryKey.location() + "/" + attachmentId + "/loading");
                values.computeIfAbsent(registryKey, k -> new LoadResult<>(new HashMap<>())).results.put(attachment, readData(
                        ops, attachment, (ResourceKey) registryKey, entry.getValue()));
            }
            profiler.pop();
        });

        return values;
    }

    public static String getFolderLocation(ResourceLocation registryId) {
        return (registryId.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) ? "" : registryId.getNamespace() + "/") + registryId.getPath();
    }

    private static <A, T> List<DataMapFile<A, T>> readData(RegistryOps<JsonElement> ops, DataMapType<A, T> attachmentType, ResourceKey<Registry<T>> registryKey, List<Resource> resources) {
        final var codec = DataMapFile.codec(registryKey, attachmentType);
        final List<DataMapFile<A, T>> entries = new LinkedList<>();
        for (final Resource resource : resources) {
            try (Reader reader = resource.openAsReader()) {
                JsonElement jsonelement = JsonParser.parseReader(reader);
                entries.add(codec.decode(ops, jsonelement)
                        .getOrThrow(false, LOGGER::error).getFirst());
            } catch (Exception exception) {
                LOGGER.error("Could not read data map of type {} for registry {}", attachmentType.id(), registryKey, exception);
            }
        }
        return entries;
    }

    private record LoadResult<T>(Map<DataMapType<?, T>, List<DataMapFile<?, T>>> results) {}
}
