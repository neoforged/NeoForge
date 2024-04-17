package net.neoforged.neoforge.registries;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Special {@link DeferredHolder} for {@link DataComponentType DataComponentTypes} that implements {@link DataComponentType}.
 *
 * @param <D> The data type of the component, which must implement {@link Object#equals(Object) equals} and {@link Object#hashCode() hashcode}.
 */
public class DeferredDataComponentType<D> extends DeferredHolder<DataComponentType<?>, DataComponentType<D>> implements DataComponentType<D> {
    /**
     * Creates a new {@link DeferredHolder} targeting the {@link DataComponentType} with the specified name.
     * 
     * @param <D>  The data type of the component.
     * @param name The name of the target {@link DataComponentType}.
     */
    public static <D> DeferredDataComponentType<D> createType(ResourceLocation name) {
        return createType(ResourceKey.create(Registries.DATA_COMPONENT_TYPE, name));
    }

    /**
     * Creates a new {@link DeferredHolder} targeting the specified {@link DataComponentType}.
     * 
     * @param <D> The data type of the component.
     * @param key The resource key of the target {@link DataComponentType}.
     */
    public static <D> DeferredDataComponentType<D> createType(ResourceKey<DataComponentType<?>> key) {
        return new DeferredDataComponentType<>(key);
    }

    protected DeferredDataComponentType(ResourceKey<DataComponentType<?>> key) {
        super(key);
    }

    // Implement each of these to wrap the held data's methods in case they are implemented differently.
    @Nullable
    @Override
    public Codec<D> codec() {
        return get().codec();
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, D> streamCodec() {
        return get().streamCodec();
    }

    @Override
    public boolean isTransient() {
        return get().isTransient();
    }

    @Override
    public Codec<D> codecOrThrow() {
        return get().codecOrThrow();
    }
}
