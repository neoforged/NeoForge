package net.neoforged.neoforge.registries;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.callback.RegistryCallback;

import javax.annotation.Nullable;
import java.util.Optional;

public interface IRegistryExtension<T> {
    /**
     * {@return whether this registry should be synced to clients}
     */
    boolean doesSync();

    /**
     * {@return the highest id that an entry in this registry is <i>allowed</i> to use}
     * For the size of this registry, see {@link Registry#size()}.
     */
    int getMaxId();

    /**
     * Adds a callback to this registry.
     * <p>
     * The callback will be called when the registry is added to, baked, or cleared.
     *
     * @param callback The callback to add.
     */
    void addCallback(RegistryCallback<T> callback);

    /**
     * Adds an alias that maps from the name specified by <code>from</code> to the name specified by <code>to</code>.
     * <p>
     * Any registry lookups that target the first name will resolve as the second name, iff the first name is not present.
     *
     * @param from The source registry name to alias from.
     * @param to The target registry name to alias to.
     */
    void addAlias(ResourceLocation from, ResourceLocation to);

    /**
     * Resolves a registry name of a potential object in this registry.
     * The original name will be returned if it is contained in this registry.
     * If not, the alias map will be checked for entries.
     * Resolving supports alias chains (A -> B -> C) and will terminate when an alias has an entry
     * or the last alias in the chain is reached.
     *
     * @param name the input registry name of a potential object in this registry
     * @return the resolved registry name
     */
    ResourceLocation resolve(ResourceLocation name);

    /**
     * Resolves a registry key of a potential object in this registry.
     * The original key will be returned if it is contained in this registry.
     * If not, the alias map will be checked for entries.
     * Resolving supports alias chains (A -> B -> C) and will terminate when an alias has an entry
     * or the last alias in the chain is reached.
     *
     * @param key the input registry key of a potential object in this registry
     * @return the resolved registry key
     */
    ResourceKey<T> resolve(ResourceKey<T> key);

    /**
     * Gets the integer id linked to the given key.
     *
     * @param key the resource key to lookup
     * @return the integer id linked to the given key,
     * or {@code -1} if the key is not present in this registry.
     */
    int getId(ResourceKey<T> key);

    /**
     * Gets the integer id linked to the given name.
     *
     * @param name the resource name to lookup
     * @return the integer id linked to the given name,
     * or {@code -1} if the name is not present in this registry.
     */
    int getId(ResourceLocation name);

    boolean containsValue(T value);

//    /**
//     * Gets the optional holder linked to the given value in this registry.
//     *
//     * @param value the value to lookup
//     * @return the optional holder linked to the given value in this registry,
//     * or an empty optional if the value is not present in this registry.
//     */
//    Optional<Holder.Reference<T>> getHolder(T value);
//
//    /**
//     * Gets the holder linked to the given value in this registry.
//     *
//     * @param value the value to lookup
//     * @return the holder linked to the given value in this registry
//     * @throws IllegalStateException if the value is not present in this registry
//     */
//    Holder.Reference<T> getHolderOrThrow(T value);
}
