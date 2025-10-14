/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows users to register custom {@link KeyMapping key mappings} and {@link KeyMapping.Category key mapping categories}.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}.
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterKeyMappingsEvent extends Event implements IModBusEvent {
    private final Options options;
    private final Map<ResourceLocation, KeyMapping.Category> moddedCategories = new HashMap<>();

    @ApiStatus.Internal
    public RegisterKeyMappingsEvent(Options options) {
        this.options = options;
    }

    /**
     * Register a new key mapping.
     */
    public void register(KeyMapping key) {
        options.keyMappings = ArrayUtils.add(options.keyMappings, key);
    }

    /**
     * Register a new key mapping category.
     */
    public void registerCategory(KeyMapping.Category category) {
        if (this.moddedCategories.putIfAbsent(category.id(), category) != null) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "KeyMapping.Category '%s' is already registered.", category.id()));
        }
    }

    @ApiStatus.Internal
    public void sortAndStoreCategories(List<KeyMapping.Category> categories) {
        List<KeyMapping.Category> custom = new ArrayList<>(this.moddedCategories.values());
        custom.sort((c1, c2) -> c1.id().compareNamespaced(c2.id()));
        categories.addAll(custom);
    }
}
