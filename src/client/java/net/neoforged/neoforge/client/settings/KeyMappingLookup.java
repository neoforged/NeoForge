/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.settings;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;

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
     * modifiers and conflict context. When releasing, returns all key mappings that
     * may have been marked as down by the released key, so stale down states are
     * always cleared even if the current modifiers or conflict context no longer
     * match.
     *
     * @param keyCode   the key being pressed or released
     * @param releasing if the key is being released
     * @return the list of key mappings
     */
    public List<KeyMapping> getAll(InputConstants.Key keyCode, boolean releasing) {
        KeyModifier keyCodeModifier = KeyModifier.getKeyModifier(keyCode);

        if (releasing) {
            // Releases must clear every mapping that may have been marked down by
            // this key, regardless of the current modifiers or conflict context.
            var matchingBindings = new LinkedHashSet<KeyMapping>();

            for (var modifierBindings : map.values()) {
                Collection<KeyMapping> bindingsForKey = modifierBindings.get(keyCode);
                if (bindingsForKey != null) {
                    matchingBindings.addAll(bindingsForKey);
                }
            }

            if (keyCodeModifier != KeyModifier.NONE) {
                for (var entry : map.get(keyCodeModifier).entrySet()) {
                    if (entry.getKey() != InputConstants.UNKNOWN) {
                        matchingBindings.addAll(entry.getValue());
                    }
                }
            }

            return new ArrayList<>(matchingBindings);
        }

        List<KeyMapping> matchingBindings = new ArrayList<>();
        List<KeyModifier> activeModifiers = this.getActiveModifiers();

        for (var modifier : activeModifiers) {
            if (modifier != keyCodeModifier) {
                matchingBindings.addAll(findKeybinds(keyCode, modifier));
                continue;
            }

            // If the key code is itself a modifier, check bindings for other active modifier keys.
            for (var otherModifier : activeModifiers) {
                if (otherModifier == modifier) {
                    continue;
                }

                for (var otherModifierCode : otherModifier.codes()) {
                    if (this.isKeyDown(otherModifierCode)) {
                        matchingBindings.addAll(findKeybinds(otherModifierCode, modifier));
                    }
                }
            }
        }

        if (!matchingBindings.isEmpty()) {
            return matchingBindings;
        }
        return findKeybinds(keyCode, KeyModifier.NONE);
    }

    @VisibleForTesting
    protected List<KeyModifier> getActiveModifiers() {
        return KeyModifier.getActiveModifiers();
    }

    @VisibleForTesting
    protected boolean isKeyDown(InputConstants.Key key) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), key.getValue());
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
