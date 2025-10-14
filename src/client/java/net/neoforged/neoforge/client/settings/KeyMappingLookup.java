/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.settings;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class KeyMappingLookup {
    private final EnumMap<KeyModifier, Map<InputConstants.Key, Collection<KeyMapping>>> map = Util.make(new EnumMap<>(KeyModifier.class), map -> {
        for (KeyModifier modifier : KeyModifier.values()) {
            map.put(modifier, Maps.newConcurrentMap());
        }
    });

    /**
     * Returns all active keys associated with the given key code and the active
     * modifiers and conflict context.
     *
     * @param keyCode the key being pressed
     * @return the list of key mappings
     */
    public List<KeyMapping> getAll(InputConstants.Key keyCode) {
        return this.getAll(keyCode, false);
    }

    /**
     * Returns all active keys associated with the given key code and the active
     * modifiers and conflict context.
     *
     * @param keyCode   the key being pressed
     * @param releasing if the key is being released
     * @return the list of key mappings
     */
    public List<KeyMapping> getAll(InputConstants.Key keyCode, boolean releasing) {
        List<KeyMapping> matchingBindings = new ArrayList<>();
        // Get a list of all active modifiers
        List<KeyModifier> activeModifiers = KeyModifier.getActiveModifiers();
        // Get modifier for key code
        KeyModifier keyCodeModifier = KeyModifier.getKeyModifier(keyCode);

        for (var modifier : activeModifiers) {
            // If modifier matches, add other modifiers
            if (modifier.matches(keyCode)) {
                // Check if binding matches with another modifier
                for (var otherModifier : activeModifiers) {

                    // Skip if modifier matches current key code
                    if (otherModifier == keyCodeModifier) {
                        continue;
                    }

                    // Loop through all modifier codes
                    for (var otherModifierCode : otherModifier.codes()) {
                        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), otherModifierCode.getValue())) {
                            matchingBindings.addAll(findKeybinds(otherModifierCode, modifier));
                        }
                    }
                }
            } else {
                // Attempt to add all bindings where the keycode and the modifier are different
                matchingBindings.addAll(findKeybinds(keyCode, modifier));
            }
        }

        // Release all bindings which use this key code as a modifier
        if (releasing && keyCodeModifier != KeyModifier.NONE) {
            matchingBindings.addAll(map.get(keyCodeModifier).entrySet().stream()
                    // Only match keys that are mapped
                    .filter(entry -> entry.getKey() != InputConstants.UNKNOWN)
                    .flatMap(entry -> entry.getValue().stream())
                    // Make sure the key is active in the current context
                    .filter(mapping -> mapping.getKeyConflictContext().isActive())
                    .toList());
        }

        // If there were no matches or a key is being released, check the key without any modifiers
        if (releasing || matchingBindings.isEmpty()) {
            matchingBindings.addAll(findKeybinds(keyCode, KeyModifier.NONE));
        }

        return matchingBindings;
    }

    private List<KeyMapping> findKeybinds(InputConstants.Key keyCode, KeyModifier modifier) {
        Collection<KeyMapping> modifierBindings = map.get(modifier).get(keyCode);
        if (modifierBindings != null) {
            return modifierBindings.stream()
                    .filter(binding -> binding.isActiveAndMatches(keyCode))
                    .toList();
        }
        return List.of();
    }

    public void put(InputConstants.Key keyCode, KeyMapping keyBinding) {
        KeyModifier keyModifier = keyBinding.getKeyModifier();
        Map<InputConstants.Key, Collection<KeyMapping>> bindingsMap = map.get(keyModifier);
        Collection<KeyMapping> bindingsForKey = bindingsMap.computeIfAbsent(keyCode, k -> Lists.newCopyOnWriteArrayList());
        bindingsForKey.add(keyBinding);
    }

    public void remove(KeyMapping keyBinding) {
        KeyModifier keyModifier = keyBinding.getKeyModifier();
        InputConstants.Key keyCode = keyBinding.getKey();
        Map<InputConstants.Key, Collection<KeyMapping>> bindingsMap = map.get(keyModifier);
        Collection<KeyMapping> bindingsForKey = bindingsMap.get(keyCode);
        if (bindingsForKey != null) {
            bindingsForKey.remove(keyBinding);
            if (bindingsForKey.isEmpty()) {
                bindingsMap.remove(keyCode);
            }
        }
    }

    public void clear() {
        for (Map<InputConstants.Key, Collection<KeyMapping>> bindings : map.values()) {
            bindings.clear();
        }
    }
}
