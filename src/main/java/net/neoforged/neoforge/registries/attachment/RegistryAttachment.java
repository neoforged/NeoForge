package net.neoforged.neoforge.registries.attachment;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * A registry attachment is a data-driven object that can be attached to a registry object. <p>
 * Attachments are registered to the {@link RegisterRegistryAttachmentsEvent}. <p>
 * They are loaded from JSON files located at:
 * <pre>
 * <code>:attachmentNamespace/attachments/:registryNamespace/:registryPath/:attachmentPath.json</code>
 * </pre>
 * The {@code registryNamespace} is omitted if it is {@value ResourceLocation#DEFAULT_NAMESPACE}. <br>
 * The structure of the json file is as follows:
 * <pre><code>
 *     {
 *         "replace": false // If true, all previous attachments will be cleared.
 *         // The values object is a map of registry entry ID / tag to attachment values.
 *         "values": {
 *             "someobject:someid": {},
 *             "#somepath:sometag": {}
 *         },
 *         // Optional object. Entries specified here will be removed after the attachments of the current json files are attached
 *         // The remove object can also be a list (i.e. ["minecraft:carrot", "#minecraft:logs"]). When it is a list the attachments of the objects with the specified IDs/tags will simply be removed, without invoking the remover.
 *         "remove": {
 *              "someobject:someid2": {} // Remover object
 *         }
 *     }
 * </pre></code>
 * Attachments support conditions both JSON-level and attachment-level through the {@value ConditionalOps#CONDITIONAL_VALUE_KEY} object.
 * <p>
 * Registry attachments may be synced by specifying a {@link #networkCodec()}. If the attachment is {@link #mandatorySync() mandatory},
 * then vanilla clients (or any client that doesn't support this attachment) will not be able to connect.
 *
 * <p>
 * If the attachment is used as a replacement for a vanilla map, a {@link #defaultValue() default value function} may be provided,
 * which will be invoked for entries that have no explicit attachment. You should refrain from using the default value
 * in order to use attachments in a code-driven way.
 *
 * <p>
 * Attachments also provide a {@link #remover() remover} which will be used to support targeted removals that
 * support decomposition, instead of the removal of the entire attachment. That way, one is able to remove just a value with
 * a specific map from an attachment, instead of the entire map.
 *
 * <p>
 * Attachments have the ability of handling conflicts between datapacks that attach an object to the same registry object.
 * Using {@link #merger() mergers}, {@linkplain RegistryAttachmentValueMerger#listMerger() collection-based} attachments can, as such, merge values provided by multiple packs
 * in the same collection. <br>
 * The {@link RegistryAttachmentValueMerger#defaultMerger() default merge} will however have the overriding behaviour of "last come wins", similar to recipes.
 *
 * <p>
 * Both datapack registries and normal, built-in registries support attachments.
 *
 * @param id
 * @param codec
 * @param networkCodec
 * @param mandatorySync
 * @param defaultValue
 * @param remover
 * @param merger
 * @param <T>
 * @param <R>
 * @param <VR>
 */
public record RegistryAttachment<T, R, VR extends RegistryAttachmentValueRemover<T, R>>(
        ResourceLocation id,
        Codec<T> codec, @Nullable Codec<T> networkCodec,
        boolean mandatorySync,
        Function<R, T> defaultValue,
        Codec<VR> remover,
        RegistryAttachmentValueMerger<T, R> merger
) {

    public static <T, R> Builder<T, R, RegistryAttachmentValueRemover.Default<T, R>> builder(ResourceLocation id, ResourceKey<Registry<R>> registry, Codec<T> codec) {
        return new Builder<T, R, RegistryAttachmentValueRemover<T, R>>(id, codec).remover(RegistryAttachmentValueRemover.Default.codec());
    }


    public static class Builder<T, R, VR extends RegistryAttachmentValueRemover<T, R>> {
        private final ResourceLocation id;
        private final Codec<T> codec;

        private @Nullable Codec<T> networkCodec;
        private boolean mandatorySync;
        private Function<R, T> defaultValue;
        private Codec<VR> remover;
        private RegistryAttachmentValueMerger<T, R> merger = RegistryAttachmentValueMerger.defaultMerger();

        private Builder(ResourceLocation id, Codec<T> codec) {
            this.id = id;
            this.codec = codec;
        }

        public <VR1 extends RegistryAttachmentValueRemover<T, R>> Builder<T, R, VR1> remover(Codec<VR1> remover) {
            this.remover = (Codec) remover;
            return (Builder<T, R, VR1>) this;
        }

        public Builder<T, R, VR> synced(Codec<T> networkCodec, boolean mandatory) {
            this.mandatorySync = mandatory;
            this.networkCodec = networkCodec;
            return this;
        }

        public Builder<T, R, VR> defaultValue(Function<R, T> defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder<T, R, VR> merger(RegistryAttachmentValueMerger<T, R> merger) {
            this.merger = merger;
            return this;
        }

        public RegistryAttachment<T, R, VR> build() {
            Preconditions.checkArgument(networkCodec != null || !mandatorySync, "Mandatory sync cannot be enabled when the attachment isn't synchronized");
            return new RegistryAttachment<>(id, codec, networkCodec, mandatorySync, defaultValue, remover, merger);
        }
    }
}
