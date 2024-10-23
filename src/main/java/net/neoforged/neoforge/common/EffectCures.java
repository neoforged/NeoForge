/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import java.util.Set;

public class EffectCures {
    /**
     * Cure used when a milk bucket is consumed. Cures any effect by default.
     */
    public static final EffectCure MILK = EffectCure.get("milk");
    /**
     * Cure used when a honey bottle is consumed. Only cures poison by default.
     */
    public static final EffectCure HONEY = EffectCure.get("honey");
    /**
     * Cure used when a totem of undying protects the player from death. Cures any effect by default.
     */
    public static final EffectCure PROTECTED_BY_TOTEM = EffectCure.get("protected_by_totem");

    public static final Set<EffectCure> DEFAULT_CURES = Set.of(MILK, PROTECTED_BY_TOTEM);
}
