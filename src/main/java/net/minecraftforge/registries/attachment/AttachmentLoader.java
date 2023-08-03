package net.minecraftforge.registries.attachment;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

public final class AttachmentLoader implements PreparableReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PATH = "attachments";
    private final RegistryAccess registryAccess;
    private final ICondition.IContext context;
    private Map<ResourceKey<? extends Registry<?>>, LoadResult<?>> results;

    public AttachmentLoader(RegistryAccess registryAccess, ICondition.IContext context) {
        this.registryAccess = registryAccess;
        this.context = context;
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier pPreparationBarrier, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor) {
        return this.load(pResourceManager, pBackgroundExecutor, pPreparationsProfiler)
                .thenCompose(pPreparationBarrier::wait)
                .thenAcceptAsync(values -> this.results = values, pGameExecutor);
    }

    public void apply() {
        results.forEach((key, result) -> this.apply((Registry) registryAccess.registryOrThrow(key), result));
    }

    private <T> void apply(Registry<T> registry, LoadResult<T> result) {
        final Map<AttachmentTypeKey<?>, Map<Object, ?>> attachments = new HashMap<>();
        registry.getAttachmentHolder().getAttachmentTypes().forEach((key, type) ->
                attachments.put(key, buildAttachmentMap(registry, (AttachmentType) type, result.results.get(key))));
        registry.getAttachmentHolder().bindAttachments(attachments);
    }

    private <A, T> Map<Object, A> buildAttachmentMap(Registry<T> registry, AttachmentType<A, T> type, @Nullable AttachmentLoad<A, T> load) {
        final Map<Object, A> result = new IdentityHashMap<>();
        if (load != null) {
            load.attachments.forEach((object, attachment) -> {
                if (object instanceof TagKey<?>) {
                    final TagKey<T> tag = (TagKey<T>) object;
                    registry.getTag(tag).ifPresent(t -> t.stream()
                            .forEach(target -> {
                                final Holder.Reference<T> reference = (Holder.Reference<T>) target;
                                if (attachment == null) {
                                    result.remove(reference.key());
                                } else {
                                    result.merge(reference.key(), attachment, type.valueMerger()::merge);
                                }
                            }));
                    if (attachment == null) {
                        result.remove(tag);
                    } else {
                        result.put(tag, attachment);
                    }
                } else if (object instanceof ResourceKey<?>) {
                    final ResourceKey<T> key = (ResourceKey<T>) object;
                    if (attachment == null) {
                        result.remove(key);
                    } else {
                        result.merge(key, attachment, type.valueMerger()::merge);
                    }
                } else {
                    final HolderSet<T> holders = ((Supplier<HolderSet<T>>) object).get();
                    holders.stream().forEach(target -> {
                        if (attachment == null) {
                            result.remove(target.unwrapKey().orElseThrow());
                        } else {
                            result.merge(target.unwrapKey().orElseThrow(), attachment, type.valueMerger()::merge);
                        }
                    });
                    if (holders instanceof HolderSet.Named<T> named) {
                        if (attachment == null) {
                            result.remove(named.key());
                        } else {
                            result.put(named.key(), attachment);
                        }
                    }
                }
            });
        }
        registry.holders()
                .filter(ref -> result.get(ref.key()) == null)
                .forEach(value -> {
                    final A attachment = type.defaultProvider().apply(value.value());
                    if (attachment != null) {
                        result.put(value.key(), attachment);
                    }
                });
        return result;
    }

    private CompletableFuture<Map<ResourceKey<? extends Registry<?>>, LoadResult<?>>> load(ResourceManager manager, Executor executor, ProfilerFiller profiler) {
        return CompletableFuture.supplyAsync(() -> load(manager, profiler, registryAccess, context), executor);
    }

    private static Map<ResourceKey<? extends Registry<?>>, LoadResult<?>> load(ResourceManager manager, ProfilerFiller profiler, RegistryAccess access, ICondition.IContext context) {
        final RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, access);

        final Map<ResourceKey<? extends Registry<?>>, LoadResult<?>> values = new HashMap<>();
        access.registries().forEach(registryEntry -> {
            final var registryKey = registryEntry.key();
            final var registry = registryEntry.value();
            profiler.push("registry_attachments/" + registryKey.location() + "/locating");
            final var fileToId = FileToIdConverter.json(getAttachmentLocation(registryKey.location()));
            for (Map.Entry<ResourceLocation, List<Resource>> entry : fileToId.listMatchingResourceStacks(manager).entrySet()) {
                ResourceLocation key = entry.getKey();
                final ResourceLocation attachmentId = fileToId.fileToId(key);
                final var attachmentTypeKey = AttachmentTypeKey.get(attachmentId);
                final var attachment = registry.getAttachmentHolder().getAttachmentTypes().get(attachmentTypeKey);
                if (attachment == null) {
                    // TODO - error
                    continue;
                }
                profiler.popPush("registry_attachments/" + registryKey.location() + "/" + attachmentId + "/loading");
                values.computeIfAbsent(registryKey, k -> new LoadResult<>(new HashMap<>())).results.put(attachmentTypeKey, readAttachments(
                        ops, attachment, (ResourceKey) registryKey, entry.getValue(), context
                ));
            }
            profiler.pop();
        });

        return values;
    }

    public static String getAttachmentLocation(ResourceLocation registryId) {
        return PATH + "/" + (registryId.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) ? "" : registryId.getNamespace() + "/") + registryId.getPath();
    }

    private static <A, T> AttachmentLoad<A, T> readAttachments(RegistryOps<JsonElement> ops, AttachmentType<A, T> attachmentType, ResourceKey<Registry<T>> registryKey, List<Resource> resources, ICondition.IContext context) {
        final Function<String, Object> keyComputer = key -> key.startsWith("#") ? TagKey.create(registryKey, new ResourceLocation(key.substring(1))) : ResourceKey.create(registryKey, new ResourceLocation(key));
        LinkedHashMap<Object, A> attachments = new LinkedHashMap<>();
        for (final Resource resource : resources) {
            try {
                try (Reader reader = resource.openAsReader()) {
                    JsonElement jsonelement = JsonParser.parseReader(reader);
                    if (!(jsonelement instanceof JsonObject object)) continue;

                    final boolean replace = GsonHelper.getAsBoolean(object, "replace", false);
                    if (replace) {
                        attachments.clear();
                    }

                    if (object.has("remove")) {
                        final JsonElement removeObject = object.get("remove");
                        final Iterable<String> toRemove = removeObject.isJsonArray() ? StreamSupport.stream(removeObject.getAsJsonArray().spliterator(), false)
                                .map(JsonElement::getAsString)::iterator : List.of(removeObject.getAsString());
                        toRemove.forEach(key -> attachments.put(keyComputer.apply(key), null));
                    }

                    Map<String, JsonElement> normal = Map.of();
                    final List<JsonObject> holderSets = new ArrayList<>();
                    if (object.has("values")) {
                        final JsonElement valuesJson = object.get("values");
                        if (valuesJson.isJsonArray()) {
                            valuesJson.getAsJsonArray().forEach(el -> holderSets.add(el.getAsJsonObject()));
                        } else {
                            normal = valuesJson.getAsJsonObject().asMap();
                        }
                    }

                    normal.forEach((key, value) -> {
                        if (!shouldConsiderAttachment(value, context)) return;
                        final A attach = attachmentType.attachmentCodec().decode(ops, value).getOrThrow(false, e -> {}).getFirst();
                        final Object attachKey = keyComputer.apply(key);
                        attachments.merge(attachKey, attach, attachmentType.valueMerger()::merge);
                    });
                    if (!holderSets.isEmpty()) {
                        final Codec<HolderSet<T>> holderSetCodec = RegistryCodecs.homogeneousList(registryKey);
                        holderSets.forEach(holderSet -> {
                            if (!shouldConsiderAttachment(holderSet, context)) return;
                            // wrap holdersets in a supplier so we can lazily read them
                            final Supplier<HolderSet<T>> set = () -> holderSetCodec.decode(ops, holderSet.get("objects")).getOrThrow(false, e -> {}).getFirst();
                            final A attach = attachmentType.attachmentCodec().decode(ops, holderSet.get("value")).getOrThrow(false, e -> {}).getFirst();
                            attachments.put(set, attach);
                        });
                    }
                }
            } catch (Exception exception) {
                LOGGER.error("Could not read attachments of type {} for registry {}", attachmentType.key(), registryKey, exception);
            }
        }
        return new AttachmentLoad<>(attachments);
    }

    private static boolean shouldConsiderAttachment(JsonElement json, ICondition.IContext context) {
        if (!(json instanceof JsonObject obj) || !obj.has("forge:conditions"))
            return true;

        return CraftingHelper.processConditions(obj, "forge:conditions", context);
    }

    private record LoadResult<T>(Map<AttachmentTypeKey<?>, AttachmentLoad<?, T>> results) {
    }

    private record AttachmentLoad<A, T>(
            LinkedHashMap<Object, A> attachments
    ) {

    }
}
