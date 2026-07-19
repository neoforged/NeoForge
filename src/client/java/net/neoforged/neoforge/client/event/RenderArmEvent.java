/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/// Fired before the player's arm is rendered in first person. This is a more targeted version of [RenderHandEvent],
/// and can be used to replace the rendering of the player's arm, such as for rendering armor on the arm or outright
/// replacing the arm with armor.
///
/// This event is [cancellable][ICancellableEvent]. If this event is cancelled, then the arm will not be rendered.
///
/// This event is fired on the [main NeoForge event bus][NeoForge#EVENT_BUS], only on the [logical client][LogicalSide#CLIENT].
///
/// @param <AvatarlikeEntity> This generic parameter **cannot** be used to target specific subclasses of [Avatar], nor narrow the type in any other way (i.e. it must be specified as a wild card).
public class RenderArmEvent<AvatarlikeEntity extends Avatar & ClientAvatarEntity> extends Event implements ICancellableEvent {
    private final PoseStack poseStack;
    private final SubmitNodeCollector submitNodeCollector;
    private final int lightCoords;
    private final Identifier skinTexture;
    private final boolean hasSleeve;
    private final AvatarlikeEntity avatar;
    private final HumanoidArm arm;
    private final ModelPart armPart;

    @ApiStatus.Internal
    public RenderArmEvent(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, Identifier skinTexture, boolean hasSleeve,
            AvatarlikeEntity avatar, HumanoidArm arm, ModelPart armPart) {
        this.poseStack = poseStack;
        this.submitNodeCollector = submitNodeCollector;
        this.lightCoords = lightCoords;
        this.skinTexture = skinTexture;
        this.hasSleeve = hasSleeve;
        this.avatar = avatar;
        this.arm = arm;
        this.armPart = armPart;
    }

    /**
     * {@return the arm being rendered}
     */
    public HumanoidArm getArm() {
        return arm;
    }

    /**
     * {@return the pose stack used for rendering}
     */
    public PoseStack getPoseStack() {
        return poseStack;
    }

    /**
     * {@return the submit node collector}
     */
    public SubmitNodeCollector getSubmitNodeCollector() {
        return submitNodeCollector;
    }

    /**
     * {@return the amount of packed (sky and block) light for rendering}
     *
     * @see net.minecraft.util.LightCoordsUtil
     */
    public int getLightCoords() {
        return lightCoords;
    }

    /// {@return the avatar's skin texture}
    public Identifier getSkinTexture() {
        return skinTexture;
    }

    /// {@return whether the avatar's sleeves are showing}
    public boolean hasSleeve() {
        return hasSleeve;
    }

    /// {@return the model part for the arm being rendered}
    public ModelPart getArmPart() {
        return armPart;
    }

    /// {@return the avatar that is having their arm rendered} In general, this will be the same as [net.minecraft.client.Minecraft#player].
    public AvatarlikeEntity getAvatar() {
        return avatar;
    }
}
