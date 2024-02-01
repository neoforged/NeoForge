/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.callback.AddCallback;
import net.neoforged.neoforge.registries.callback.BakeCallback;
import net.neoforged.neoforge.registries.callback.ClearCallback;
import net.neoforged.neoforge.registries.callback.RegistryCallback;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.Nullable;

/**
 * An extension for {@link Registry}, adding some additional functionality to vanilla registries, such as
 * callbacks and ID limits.
 * 
 * @param <T> the type of registry entries
 */
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
     * Depending on the interfaces implemented by the object,
     * the callback will be called when the registry is
     * {@linkplain AddCallback added to},
     * {@linkplain BakeCallback baked},
     * and/or {@linkplain ClearCallback cleared}.
     *
     * @param callback the callback to add
     */
    void addCallback(RegistryCallback<T> callback);

    /**
     * Adds a lambda-implemented callback to this registry.
     * <p>
     * The callback will be called when the registry is added to, baked, or cleared.
     * The {@code Class} parameter is used to determine the lambda type.
     *
     * @param type     the type of the callback to add
     * @param callback the callback to add
     */
    default <C extends RegistryCallback<T>> void addCallback(Class<C> type, C callback) {
        addCallback(callback);
    }

    /**
     * Adds an alias that maps from the name specified by <code>from</code> to the name specified by <code>to</code>.
     * <p>
     * Any registry lookups that target the first name will resolve as the second name, if the first name is not present.
     *
     * @param from the source registry name to alias from
     * @param to   the target registry name to alias to
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
     * Gets the integer id linked to the given key. If the key is not present in the registry, the default entry's
     * integer id is returned if the registry is defaulted or {@code -1} if the registry is not defaulted
     *
     * @param key the resource key to lookup
     * @return the integer id linked to the given key
     */
    int getId(ResourceKey<T> key);

    /**
     * Gets the integer id linked to the given name. If the name is not present in the registry, the default entry's
     * integer id is returned if the registry is defaulted or {@code -1} if the registry is not defaulted
     *
     * @param name the resource name to lookup
     * @return the integer id linked to the given name
     */
    int getId(ResourceLocation name);

    /**
     * {@return {@code true} if this registry contains the {@code value}}
     *
     * @param value the object whose existence to check for
     */
    boolean containsValue(T value);

    /**
     * {@return the data map value attached with the object with the key, or {@code null} if there's no attached value}
     *
     * @param type the type of the data map
     * @param key  the object to get the value for
     * @param <A>  the data type
     */
    @Nullable
    <A> A getData(DataMapType<T, A> type, ResourceKey<T> key);

    /**
     * {@return the data map of the given {@code type}}
     *
     * @param <A> the data type
     */
    <A> Map<ResourceKey<T>, A> getDataMap(DataMapType<T, A> type);
}
