package net.neoforged.neoforge.registries;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Special {@link DeferredHolder} for {@link DataComponentType DataComponentTypes}.
 *
 * @param <D> The data type of the component, which must implement {@link Object#equals(Object) equals} and {@link Object#hashCode() hashcode}.
 */
public class DeferredDataComponentType<D> extends DeferredHolder<DataComponentType<?>, DataComponentType<D>> {
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
}
