/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import org.jetbrains.annotations.ApiStatus;

/**
 * Signifies that an enum is not exhaustive, and additional values may be added while NeoForge is stable.
 * <br>
 * Care should be taken to ensure no reliance on the specific number or order of values in a tagged enum.
 * <p>
 * Do not rely on the number of values.
 * <br>
 * Do not rely on the ordinal of a specific value.
 * <br>
 * Do not use in a switch without a default case.
 */
@ApiStatus.Internal
@Target(ElementType.TYPE)
public @interface NonExhaustiveEnum {
    /**
     * This is done as an annotation member to preserve the comment without sources attached.
     */
    String reason();
}
