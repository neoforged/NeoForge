/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

/// Extension interface for [KeyMapping].
public interface IKeyMappingExtension {
    private KeyMapping self() {
        return (KeyMapping) this;
    }

    InputConstants.Key getKey();

    /// {@return true if the key conflict context and modifier are active and the keyCode matches this binding, false otherwise}
    default boolean isActiveAndMatches(InputConstants.Key keyCode) {
        return KeyMappingModifierHelper.isActiveAndMatches(keyCode, getKey(), getKeyConflictContext(), getKeyModifier());
    }

    public default void setToDefault() {
        setKeyModifierAndCode(getDefaultKeyModifier(), self().getDefaultKey());
    }

    void setKeyConflictContext(IKeyConflictContext keyConflictContext);

    IKeyConflictContext getKeyConflictContext();

    KeyModifier getDefaultKeyModifier();

    KeyModifier getKeyModifier();

    void setKeyModifierAndCode(KeyModifier keyModifier, InputConstants.Key keyCode);

    default boolean isConflictContextAndModifierActive() {
        IKeyConflictContext keyConflictContext = getKeyConflictContext();
        return keyConflictContext.isActive() && KeyMappingModifierHelper.isModifierActive(keyConflictContext, getKeyModifier(), getKey());
    }

    /// Returns true when one of the bindings' key codes conflicts with the other's modifier.
    default boolean hasKeyModifierConflict(KeyMapping other) {
        return KeyMappingModifierHelper.hasKeyModifierConflict(self(), other);
    }

    /// {@return the display name of this key mapping}
    /// Defaults to a [translatable component][Component#translatable(String)] of the
    /// [name][KeyMapping#getName()].
    default Component getDisplayName() {
        return Component.translatable(self().getName());
    }
}
