package net.neoforged.neoforge.transfer.resources;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.tags.TagKey;

import java.util.function.Predicate;

/**
 * A helper and optional interface that groups resources together if they are expected to be registered.
 *
 * @param <T> The backing type that the resource is targeting. Such as {@link net.minecraft.world.item.Item Item} for {@link ItemResource} or {@link net.minecraft.world.level.material.Fluid Fluid} for {@link FluidResource}
 */
public interface IRegisteredResource<T> extends IResource, DataComponentHolder {

    /**
     * @return The backing value of the resource.
     */
    T getInstanceValue();

    /**
     * @return The holder of the backing value.
     */
    Holder<T> getHolder();

    DataComponentMap getComponents();

    DataComponentPatch getComponentsPatch();

    boolean isComponentsPatchEmpty();

    boolean is(T item);

    boolean is(TagKey<T> tag);

    boolean is(Predicate<Holder<T>> predicate);

    default boolean is(Holder<T> holder) {
        return is(holder.value());
    }

    default boolean is(HolderSet<T> holderSet) {
        return holderSet.contains(getHolder());
    }

    /**
     * @return the full value and data components in string form
     */
    default String toExpandedString(){
        if (isComponentsPatchEmpty()) {
            return toString();
        } else {
            return "%s %s".formatted(getInstanceValue(), getComponentsPatch().toString());
        }
    }
}
