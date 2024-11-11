/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;

public interface EntityRenderStateModifier<E extends Entity, S extends EntityRenderState> {
    void accept(E entity, S renderState);
}
