/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world;

import net.minecraft.resources.Identifier;
import net.minecraft.world.attribute.AttributeType;
import net.minecraft.world.attribute.EnvironmentAttribute;

public final class NeoForgeAttributeTypes {
    /**
     * Allows {@link Identifier} to be used as the type of {@link EnvironmentAttribute}.
     */
    public static final AttributeType<Identifier> IDENTIFIER = AttributeType.ofNotInterpolated(Identifier.CODEC);

    private NeoForgeAttributeTypes() {}
}
