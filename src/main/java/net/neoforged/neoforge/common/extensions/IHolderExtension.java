package net.neoforged.neoforge.common.extensions;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.datamaps.IWithData;

/**
 * Extension for {@link Holder}
 */
public interface IHolderExtension<T> extends IWithData<T> {

    /**
     * {@return the holder that this holder wraps}
     *
     * Used by {@link Registry#safeCastToReference} to resolve the underlying {@link Holder.Reference} for delegating holders.
     */
    default Holder<T> getDelegate() {
        return (Holder<T>) this;
    }

    /**
     * Attempts to resolve the underlying {@link HolderLookup.RegistryLookup} from a {@link Holder}.
     * <p>
     * This will only succeed if the underlying holder is a {@link Holder.Reference}.
     */
    @Nullable
    default HolderLookup.RegistryLookup<T> unwrapLookup() {
        return null;
    }

}
