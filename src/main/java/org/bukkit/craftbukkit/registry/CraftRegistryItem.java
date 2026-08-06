package org.bukkit.craftbukkit.registry;

import com.google.common.base.Preconditions;
import java.util.Objects;
import net.minecraft.core.Holder;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.HolderHandleable;
import org.bukkit.registry.RegistryAware;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CraftRegistryItem<M> implements RegistryAware, HolderHandleable<M> {

    private final NamespacedKey key;
    private final Holder<M> handle;

    protected CraftRegistryItem(NamespacedKey key, Holder<M> handle) {
        this.key = key;
        this.handle = handle;
    }

    @Override
    public Holder<M> getHandleHolder() {
        return this.handle;
    }

    @Override
    public M getHandle() {
        return getHandleHolder().value();
    }

    @NotNull
    @Override
    public NamespacedKey getKeyOrThrow() {
        Preconditions.checkState(isRegistered(), "Cannot get key of this registry item, because it is not registered. Use #isRegistered() before calling this method.");
        return this.key;
    }

    @Nullable
    @Override
    public NamespacedKey getKeyOrNull() {
        return this.key;
    }

    @Override
    public boolean isRegistered() {
        return this.key != null;
    }

    @Override
    public int hashCode() {
        if (isRegistered()) {
            return getKeyOrThrow().hashCode();
        }

        return getHandle().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        CraftRegistryItem<?> other = (CraftRegistryItem<?>) obj;

        if (isRegistered() || other.isRegistered()) {
            return Objects.equals(this.key, other.key);
        }

        return Objects.equals(getHandle(), other.getHandle());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{key=" + this.key + ", handle=" + this.handle + "}";
    }
}
