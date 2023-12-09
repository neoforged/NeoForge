/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * An {@code ItemCapability} gives flexible access to objects of type {@code T} from item stacks.
 *
 * <h3>Querying an item capability</h3>
 * <p>To get an object of type {@code T}, use {@link ItemStack#getCapability(ItemCapability)}.
 * For example, to query an item handler from an item stack:
 * 
 * <pre>{@code
 * ItemStack stack = ...;
 *
 * IItemHandler maybeHandler = stack.getCapability(Capabilities.ItemHandler.ITEM);
 * if (maybeHandler != null) {
 *     // Use maybeHandler
 * }
 * }</pre>
 *
 * <h3>Providing an item capability</h3>
 * <p>To provide objects of type {@code T}, register providers to {@link RegisterCapabilitiesEvent}. For example:
 * 
 * <pre>{@code
 * modBus.addListener((RegisterCapabilitiesEvent event) -> {
 *     event.registerItem(
 *         Capabilities.ItemHandler.ITEM, // capability to register for
 *         (itemStack, context) -> <return the IItemHandler for the itemStack>,
 *         MY_ITEM);
 * });
 * }</pre>
 *
 * @param <T> type of queried objects
 * @param <C> type of the additional context
 */
public final class ItemCapability<T, C> extends BaseCapability<T, C> {
    /**
     * Creates a new item capability, or gets it if it already exists.
     *
     * @param name         name of the capability
     * @param typeClass    type of the queried API
     * @param contextClass type of the additional context
     */
    public static <T, C> ItemCapability<T, C> create(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        return (ItemCapability<T, C>) registry.create(name, typeClass, contextClass);
    }

    /**
     * Creates a new item capability with {@code Void} context, or gets it if it already exists.
     * This should be used for capabilities that do not require any additional context.
     *
     * @see #create(ResourceLocation, Class, Class)
     */
    public static <T> ItemCapability<T, Void> createVoid(ResourceLocation name, Class<T> typeClass) {
        return create(name, typeClass, void.class);
    }

    /**
     * {@return a new immutable copy of all the currently known item capabilities}
     */
    public static synchronized List<ItemCapability<?, ?>> getAll() {
        return registry.getAll();
    }

    // INTERNAL

    // Requires explicitly-typed constructor due to ECJ inference failure.
    private static final CapabilityRegistry<ItemCapability<?, ?>> registry = new CapabilityRegistry<ItemCapability<?, ?>>(ItemCapability::new);

    private ItemCapability(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        super(name, typeClass, contextClass);
    }

    final Map<Item, List<ICapabilityProvider<ItemStack, C, T>>> providers = new IdentityHashMap<>();

    @ApiStatus.Internal
    @Nullable
    public T getCapability(ItemStack stack, C context) {
        if (stack.isEmpty()) {
            // This check exists in case modders still register capability providers for Items.AIR,
            // for example when registering a provider for all items.
            // We thus disallow empty stacks to provide capabilities for now,
            // but this can be reconsidered in the future if solid reasoning is given.
            return null;
        }

        for (var provider : providers.getOrDefault(stack.getItem(), List.of())) {
            var ret = provider.getCapability(stack, context);
            if (ret != null)
                return ret;
        }
        return null;
    }
}
