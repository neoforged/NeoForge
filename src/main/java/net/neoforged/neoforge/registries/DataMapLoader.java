package net.neoforged.neoforge.registries;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.registries.attachment.DataMapType;
import net.neoforged.neoforge.registries.attachment.DataMapValueRemover;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DataMapLoader implements PreparableReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PATH = "data_maps";
    private Map<ResourceKey<? extends Registry<?>>, LoadResult<?>> results;
    private final ICondition.IContext conditionContext;
    private final RegistryAccess registryAccess;

    public DataMapLoader(ICondition.IContext conditionContext, RegistryAccess registryAccess) {
        this.conditionContext = conditionContext;
        this.registryAccess = registryAccess;
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier pPreparationBarrier, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor) {
        return this.load(pResourceManager, pBackgroundExecutor, pPreparationsProfiler)
                .thenCompose(pPreparationBarrier::wait)
                .thenAcceptAsync(values -> this.results = values, pGameExecutor);
    }

    public void apply() {
        results.forEach((key, result) -> this.apply((BaseMappedRegistry) registryAccess.registryOrThrow(key), result));

        // Clear the intermediary maps and objects
        System.gc();
    }

    private <T> void apply(BaseMappedRegistry<T> registry, LoadResult<T> result) {
        registry.dataMaps.clear();
        result.results().forEach((key, entries) -> registry.dataMaps.put(
                key, this.buildDataMap(registry, key, (List) entries)
        ));
    }

    private <T, R> Map<ResourceKey<R>, T> buildDataMap(Registry<R> registry, DataMapType<T, R, ?> attachment, List<LoadEntry<T, R>> entries) {
        record WithSource<T, R>(T attachment, Either<TagKey<R>, ResourceKey<R>> source) {}
        final Map<ResourceKey<R>, WithSource<T, R>> result = new IdentityHashMap<>();
        final BiConsumer<Either<TagKey<R>, ResourceKey<R>>, Consumer<Holder<R>>> valueResolver = (key, cons) ->
                key.ifLeft(tag -> registry.getTagOrEmpty(tag).forEach(cons)).ifRight(k -> cons.accept(registry.getHolderOrThrow(k)));
        entries.forEach(entry -> {
            if (entry.replace) {
                result.clear();
            }

            entry.values().forEach((tKey, value) -> valueResolver.accept(tKey, holder -> {
                if (value.isEmpty()) return;
                final var newValue = value.get();
                final var key = holder.unwrapKey().get();
                final var oldValue = result.get(key);
                if (oldValue == null || newValue.replace()) {
                    result.put(key, new WithSource<>(newValue.attachment(), tKey));
                } else {
                    result.put(key, new WithSource<>(attachment.merger().merge(registry, oldValue.source(), oldValue.attachment(), tKey, newValue.attachment()), tKey));
                }
            }));

            entry.removals().forEach(trRemoval -> valueResolver.accept(trRemoval.key(), holder -> {
                if (trRemoval.remover().isPresent()) {
                    final var key = holder.unwrapKey().get();
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

        registry.holders().forEach(ref -> {
            final var key = ref.key();
            if (newMap.get(key) == null) {
                final var def = attachment.defaultValue().apply(ref.value());
                if (def != null) {
                    newMap.put(key, def);
                }
            }
        });

        return newMap;
    }

    private CompletableFuture<Map<ResourceKey<? extends Registry<?>>, LoadResult<?>>> load(ResourceManager manager, Executor executor, ProfilerFiller profiler) {
        return CompletableFuture.supplyAsync(() -> load(manager, profiler, registryAccess, conditionContext), executor);
    }

    private static Map<ResourceKey<? extends Registry<?>>, LoadResult<?>> load(ResourceManager manager, ProfilerFiller profiler, RegistryAccess access, ICondition.IContext context) {
        final RegistryOps<JsonElement> ops = ConditionalOps.create(RegistryOps.create(JsonOps.INSTANCE, access), context);

        final Map<ResourceKey<? extends Registry<?>>, LoadResult<?>> values = new HashMap<>();
        access.registries().forEach(registryEntry ->
        {
            final var registryKey = registryEntry.key();
            profiler.push("registry_attachments/" + registryKey.location() + "/locating");
            final var fileToId = FileToIdConverter.json(getFolderLocation(registryKey.location()));
            for (Map.Entry<ResourceLocation, List<Resource>> entry : fileToId.listMatchingResourceStacks(manager).entrySet()) {
                ResourceLocation key = entry.getKey();
                final ResourceLocation attachmentId = fileToId.fileToId(key);
                final var attachment = RegistryManager.getDataMap((ResourceKey) registryKey, attachmentId);
                if (attachment == null) {
                    LOGGER.warn("Found attachment file for inexistent attachment type '{}' on registry '{}'.", attachmentId, registryKey.location());
                    continue;
                }
                profiler.popPush("registry_attachments/" + registryKey.location() + "/" + attachmentId + "/loading");
                values.computeIfAbsent(registryKey, k -> new LoadResult<>(new HashMap<>())).results.put(attachment, readAttachments(
                        ops, attachment, (ResourceKey) registryKey, entry.getValue(), context
                ));
            }
            profiler.pop();
        });

        return values;
    }

    public static String getFolderLocation(ResourceLocation registryId) {
        return PATH + "/" + (registryId.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) ? "" : registryId.getNamespace() + "/") + registryId.getPath();
    }

    private static <A, T> List<LoadEntry<A, T>> readAttachments(RegistryOps<JsonElement> ops, DataMapType<A, T, ?> attachmentType, ResourceKey<Registry<T>> registryKey, List<Resource> resources, ICondition.IContext context) {
        final var codec = LoadEntry.codec(registryKey, attachmentType);
        final List<LoadEntry<A, T>> entries = new LinkedList<>();
        for (final Resource resource : resources) {
            try {
                try (Reader reader = resource.openAsReader()) {
                    JsonElement jsonelement = JsonParser.parseReader(reader);
                    entries.add(codec.decode(ops, jsonelement)
                            .getOrThrow(false, LOGGER::error).getFirst());
                }
            } catch (Exception exception) {
                LOGGER.error("Could not read data map of type {} for registry {}", attachmentType.id(), registryKey, exception);
            }
        }
        return entries;
    }

    private record LoadResult<T>(Map<DataMapType<?, T, ?>, List<LoadEntry<?, T>>> results) {
    }

    private record Removal<T, R>(Either<TagKey<R>, ResourceKey<R>> key,
                                 Optional<DataMapValueRemover<T, R>> remover) {
        public static <T, R, VR extends DataMapValueRemover<T, R>> Codec<Removal<T, R>> codec(Codec<Either<TagKey<R>, ResourceKey<R>>> tagOrValue, DataMapType<T, R, VR> attachment) {
            return RecordCodecBuilder.create(in -> in.group(
                    tagOrValue.fieldOf("key").forGetter(Removal::key),
                    attachment.remover().<DataMapValueRemover<T, R>>xmap(vr -> vr, v -> (VR) v).optionalFieldOf("remover").forGetter(Removal::remover)
            ).apply(in, Removal::new));
        }
    }

    private record AttachmentValue<T>(T attachment, boolean replace) {
        private AttachmentValue(T attachment) {
            this(attachment, false);
        }
    }

    private record LoadEntry<T, R>(
            boolean replace,
            Map<Either<TagKey<R>, ResourceKey<R>>, Optional<AttachmentValue<T>>> values,
            List<Removal<T, R>> removals
    ) {
        public static <T, R, VR extends DataMapValueRemover<T, R>> Codec<LoadEntry<T, R>> codec(ResourceKey<Registry<R>> registryKey, DataMapType<T, R, VR> attachment) {
            final Codec<Either<TagKey<R>, ResourceKey<R>>> tagOrValue = ExtraCodecs.TAG_OR_ELEMENT_ID.xmap(
                    l -> l.tag() ? Either.left(TagKey.create(registryKey, l.id())) : Either.right(ResourceKey.create(registryKey, l.id())),
                    e -> e.map(t -> new ExtraCodecs.TagOrElementLocation(t.location(), true), r -> new ExtraCodecs.TagOrElementLocation(r.location(), false))
            );
            final var removalCodec = Removal.codec(tagOrValue, attachment);
            return RecordCodecBuilder.create(in -> in.group(
                    ExtraCodecs.strictOptionalField(Codec.BOOL, "replace", false).forGetter(LoadEntry::replace),
                    ExtraCodecs.strictUnboundedMap(tagOrValue, ConditionalOps.createConditionalCodec(ExtraCodecs.withAlternative(
                            RecordCodecBuilder.create(i -> i.group(
                                    attachment.codec().fieldOf("value").forGetter(AttachmentValue::attachment),
                                    ExtraCodecs.strictOptionalField(Codec.BOOL, "replace", false).forGetter(AttachmentValue::replace)
                            ).apply(i, AttachmentValue::new)), attachment.codec().xmap(AttachmentValue::new, AttachmentValue::attachment)
                    ))).fieldOf("values").forGetter(LoadEntry::values),
                    ExtraCodecs.strictOptionalField(NeoForgeExtraCodecs.withAlternative(
                            NeoForgeExtraCodecs.withAlternative(removalCodec.listOf(), NeoForgeExtraCodecs.decodeOnly(tagOrValue.listOf()
                                    .map(l -> l.stream().map(k -> new Removal<T, R>(k, Optional.empty())).toList()))),
                            NeoForgeExtraCodecs.decodeOnly(ExtraCodecs.strictUnboundedMap(tagOrValue, attachment.remover())
                                    .map(map -> map.entrySet().stream()
                                            .map(entry -> new Removal<>(entry.getKey(), Optional.of(entry.getValue()))).toList()))
                    ), "remove", List.of()).forGetter(LoadEntry::removals)
            ).apply(in, LoadEntry::new));
        }
    }
}
