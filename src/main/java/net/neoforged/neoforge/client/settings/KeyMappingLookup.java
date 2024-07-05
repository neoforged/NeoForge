/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.settings;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.KeyMapping;

public class KeyMappingLookup {
    private static final EnumMap<KeyModifier, Map<InputConstants.Key, Collection<KeyMapping>>> map = new EnumMap<>(KeyModifier.class);
    static {
        for (KeyModifier modifier : KeyModifier.values()) {
            map.put(modifier, new HashMap<>());
        }
    }

    /**
     * Returns all active keys associated with the given key code and the active
     * modifiers and conflict context.
     *
     * @param keyCode the key being pressed
     * @return the list of key mappings
     */
    public List<KeyMapping> getAll(InputConstants.Key keyCode) {
        List<KeyMapping> matchingBindings = new ArrayList<>();
        KeyModifier activeModifier = KeyModifier.getActiveModifier();
        // Apply active modifier only if the pressed key is not the modifier itself
        // Otherwise, look for key bindings without modifiers
        if (activeModifier == KeyModifier.NONE || activeModifier.matches(keyCode) || !matchingBindings.addAll(findKeybinds(keyCode, activeModifier))) {
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
        Collection<KeyMapping> bindingsForKey = bindingsMap.computeIfAbsent(keyCode, k -> new ArrayList<>());
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
