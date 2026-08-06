package org.bukkit.craftbukkit.registry;

import com.google.common.base.Preconditions;
import java.util.Locale;
import net.minecraft.core.Holder;
import org.bukkit.NamespacedKey;
import org.bukkit.util.OldEnum;
import org.jetbrains.annotations.NotNull;

@Deprecated
public abstract class CraftOldEnumRegistryItem<T extends OldEnum<T>, M> extends CraftRegistryItem<M> implements OldEnum<T> {

    private final int ordinal;
    private final String name;

    protected CraftOldEnumRegistryItem(NamespacedKey key, Holder<M> handle, int ordinal) {
        super(key, handle);
        this.ordinal = ordinal;

        if (isRegistered()) {
            // For backwards compatibility, minecraft values will stile return the uppercase name without the namespace,
            // in case plugins use for example the name as key in a config file to receive registry item specific values.
            // Custom registry items will return the key with namespace. For a plugin this should look than like a new registry item
            // (which can always be added in new minecraft versions and the plugin should therefore handle it accordingly).
            if (NamespacedKey.MINECRAFT.equals(key.getNamespace())) {
                this.name = key.getKey().toUpperCase(Locale.ROOT);
            } else {
                this.name = key.toString();
            }
        } else {
            this.name = null;
        }
    }

    @Override
    public int compareTo(@NotNull T other) {
        checkState();
        return ordinal - other.ordinal();
    }

    @NotNull
    @Override
    public String name() {
        checkState();
        return this.name;
    }

    @Override
    public int ordinal() {
        checkState();
        return this.ordinal;
    }

    @Override
    public String toString() {
        // For backwards compatibility
        if (isRegistered()) {
            return name();
        }

        return super.toString();
    }

    private void checkState() {
        Preconditions.checkState(isRegistered(), "Cannot call method for this registry item, because it is not registered. Use #isRegistered() before calling this method.");
    }
}
