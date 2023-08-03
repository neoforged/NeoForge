package net.minecraftforge.common.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.attachment.AttachmentLoader;
import net.minecraftforge.registries.attachment.AttachmentType;
import net.minecraftforge.registries.attachment.AttachmentTypeKey;
import org.apache.commons.lang3.tuple.Triple;

import java.util.concurrent.CompletableFuture;

public abstract class AttachmentProvider<A, T> implements DataProvider {
    private final PackOutput output;
    private final ResourceKey<Registry<T>> registryKey;
    private final AttachmentTypeKey<A> attachmentType;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;
    private final boolean replace;

    public AttachmentProvider(PackOutput output, ResourceKey<Registry<T>> registryKey, AttachmentTypeKey<A> attachmentType, CompletableFuture<HolderLookup.Provider> lookupProvider, boolean replace) {
        this.output = output;
        this.registryKey = registryKey;
        this.attachmentType = attachmentType;
        this.lookupProvider = lookupProvider;
        this.replace = replace;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        return lookupProvider.thenApply(registries -> Pair.of(RegistryOps.create(JsonOps.INSTANCE, registries), registries.lookupOrThrow(registryKey)))
                .thenApply(pair -> {
                    final AttachmentType<A, T> attachment = (AttachmentType<A, T>) pair.getSecond().getAttachmentTypes().get(attachmentType);
                    if (attachment == null) {
                        throw new IllegalArgumentException("Registry " + registryKey + " does not support attachments of type " + attachmentType);
                    }
                    return Triple.of(pair.getFirst(), pair.getSecond(), attachment);
                })
                .thenCompose(tri -> {
                    final JsonArray elements = new JsonArray();
                    final Codec<HolderSet<T>> registryCodec = RegistryCodecs.homogeneousList(registryKey);
                    buildAttachments(tri.getMiddle(), new AttachmentBuilder<>() {
                        @Override
                        public void attach(Holder<T> value, A attachment, ICondition... conditions) {
                            attach(HolderSet.direct(value), attachment, conditions);
                        }

                        @Override
                        public void attach(TagKey<T> tag, A attachment, ICondition... conditions) {
                            attach(tri.getMiddle().getOrThrow(tag), attachment, conditions);
                        }

                        @Override
                        public void attach(HolderSet<T> holder, A attachment, ICondition... conditions) {
                            elements.add(buildJson(tri.getLeft(), registryCodec, tri.getRight().attachmentCodec(), holder, attachment, conditions));
                        }
                    });
                    final JsonObject finalObject = new JsonObject();
                    finalObject.addProperty("replace", replace);
                    finalObject.add("values", elements);

                    return DataProvider.saveStable(pOutput, finalObject, output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(attachmentType.getId().getNamespace() + "/" + AttachmentLoader.getAttachmentLocation(registryKey.location()) + "/" + attachmentType.getId().getPath() + ".json"));
                });
    }

    private JsonObject buildJson(RegistryOps<JsonElement> ops, Codec<HolderSet<T>> registryCodec, Codec<A> codec, HolderSet<T> value, A attachment, ICondition[] conditions) {
        final JsonObject object = new JsonObject();
        object.add("objects", registryCodec.encodeStart(ops, value).getOrThrow(false, LOGGER::error));
        object.add("value", codec.encodeStart(ops, attachment).getOrThrow(false, LOGGER::error));
        if (conditions.length > 0) {
            object.add("forge:conditions", CraftingHelper.serialize(conditions));
        }
        return object;
    }

    protected abstract void buildAttachments(HolderLookup.RegistryLookup<T> registry, AttachmentBuilder<A, T> builder);

    @Override
    public String getName() {
        return "Registry attachments for registry ";
    }

    public interface AttachmentBuilder<A, T> {
        void attach(Holder<T> value, A attachment, ICondition... conditions);
        void attach(TagKey<T> tag, A attachment, ICondition... conditions);
        void attach(HolderSet<T> holder, A attachment, ICondition... conditions);
    }
}
