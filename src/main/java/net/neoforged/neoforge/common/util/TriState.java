/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

/**
 * Represents a boolean value that can be {@code true}, {@code false} or refer to a default value.
 */
public enum TriState {
    /**
     * Represents the boolean value {@code true}.
     */
    TRUE,
    /**
     * Represents a "default" value, often used as a fallback.
     */
    DEFAULT,
    /**
     * Represents the boolean value {@code false}.
     */
    FALSE
}
