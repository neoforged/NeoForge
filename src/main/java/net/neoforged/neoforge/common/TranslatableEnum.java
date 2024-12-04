/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import net.minecraft.network.chat.Component;

/**
 * An enum value that can be be translated.
 */
public interface TranslatableEnum {
    /**
     * {@return the translated name of this value}
     * Defaults to a {@linkplain Component#literal(String) literal component} with the {@link Enum#name() enum name};
     */
    default Component getTranslatedName() {
        return Component.literal(((Enum<?>) this).name());
    }
}
