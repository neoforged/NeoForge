/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.renderstate;

import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Avatar;

/// Specialized [EntityRenderState] modifier for renderers extending [AvatarRenderer]
public abstract class AvatarRenderStateModifier {
    public abstract <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState renderState);
}
