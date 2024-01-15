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
 * A registry data map contains data-driven object that can be attached to a registry object. <p>
 * Data maps are registered to the {@link RegisterDataMapTypesEvent}. <p>
 * They are loaded from JSON files located at:
 * <pre>
 * <code>:mapNamespace/data_maps/:registryNamespace/:registryPath/:mapPath.json</code>
 * </pre>
 * The {@code registryNamespace} is omitted if it is {@value ResourceLocation#DEFAULT_NAMESPACE}. <br>
 * The structure of the json file is as follows:
 * <pre><code>
 *     {
 *         "replace": false // If true, all previous data will be cleared.
 *         // The values object is a map of registry entry ID / tag to attachment values.
 *         "values": {
 *             "someobject:someid": {},
 *             "#somepath:sometag": {}
 *         },
 *         // Optional object. Entries specified here will be removed after the data of the current json file is attached
 *         // The remove object can also be a list (i.e. ["minecraft:carrot", "#minecraft:logs"]). When it is a list the data of the objects with the specified IDs/tags will simply be removed, without invoking the remover.
 *         "remove": {
 *              "someobject:someid2": {} // Remover object
 *         }
 *     }
 * </pre></code>
 * Data maps support conditions both JSON-level and attachment-level through the {@value ConditionalOps#CONDITIONAL_VALUE_KEY} object.
 * <p>
 * Data maps may be synced by specifying a {@link #networkCodec()}. If the map is {@link #mandatorySync() mandatory},
 * then vanilla clients (or any client that doesn't support this map) will not be able to connect.
 *
 * <p>
 * If the data map is used as a replacement for a vanilla map, a {@link #defaultValue() default value function} may be provided,
 * which will be invoked for entries that have no explicit value attached. You should refrain from using the default value
 * in order to use data maps in a code-driven way.
 *
 * <p>
 * Data maps also provide a {@link #remover() remover} which will be used to support targeted removals that
 * support decomposition, instead of the removal of the entire value. That way, for instance, one is able to remove just a value with
 * a specific key from a {@link java.util.Map map-based} data map, instead of the entire map.
 *
 * <p>
 * Data maps have the ability of handling conflicts between datapacks that attach an object to the same registry object.
 * Using {@link #merger() mergers}, {@linkplain DataMapValueMerger#listMerger() collection-based} data maps can, as such, merge values provided by multiple packs
 * in the same collection. <br>
 * The {@link DataMapValueMerger#defaultMerger() default merge} will however have the overriding behaviour of "last come wins", similar to recipes.
 *
 * <p>
 * Both datapack registries and normal, built-in registries support data maps.
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
public record DataMapType<T, R, VR extends DataMapValueRemover<T, R>>(
        ResourceLocation id,
        Codec<T> codec, @Nullable Codec<T> networkCodec,
        boolean mandatorySync,
        Function<R, T> defaultValue,
        Codec<VR> remover,
        DataMapValueMerger<T, R> merger
) {

    public static <T, R> Builder<T, R, DataMapValueRemover.Default<T, R>> builder(ResourceLocation id, ResourceKey<Registry<R>> registry, Codec<T> codec) {
        return new Builder<T, R, DataMapValueRemover<T, R>>(id, codec).remover(DataMapValueRemover.Default.codec());
    }


    public static class Builder<T, R, VR extends DataMapValueRemover<T, R>> {
        private final ResourceLocation id;
        private final Codec<T> codec;

        private @Nullable Codec<T> networkCodec;
        private boolean mandatorySync;
        private Function<R, T> defaultValue;
        private Codec<VR> remover;
        private DataMapValueMerger<T, R> merger = DataMapValueMerger.defaultMerger();

        private Builder(ResourceLocation id, Codec<T> codec) {
            this.id = id;
            this.codec = codec;
        }

        public <VR1 extends DataMapValueRemover<T, R>> Builder<T, R, VR1> remover(Codec<VR1> remover) {
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

        public Builder<T, R, VR> merger(DataMapValueMerger<T, R> merger) {
            this.merger = merger;
            return this;
        }

        public DataMapType<T, R, VR> build() {
            Preconditions.checkArgument(networkCodec != null || !mandatorySync, "Mandatory sync cannot be enabled when the attachment isn't synchronized");
            return new DataMapType<>(id, codec, networkCodec, mandatorySync, defaultValue, remover, merger);
        }
    }
}
