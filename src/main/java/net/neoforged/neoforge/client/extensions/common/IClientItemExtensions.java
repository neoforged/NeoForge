/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions.common;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.IArmPoseTransformer;
import org.jetbrains.annotations.Nullable;

/**
 * {@linkplain LogicalSide#CLIENT Client-only} extensions to {@link Item}.
 *
 * @see RegisterClientExtensionsEvent
 */
public interface IClientItemExtensions {
    IClientItemExtensions DEFAULT = new IClientItemExtensions() {};

    static IClientItemExtensions of(ItemStack stack) {
        return of(stack.getItem());
    }

    static IClientItemExtensions of(Item item) {
        return ClientExtensionsManager.ITEM_EXTENSIONS.getOrDefault(item, DEFAULT);
    }

    /**
     * Returns the font used to render data related to this item as specified in the {@code context}.
     * Return {@code null} to use the default font.
     *
     * @param stack   The item stack
     * @param context The context in which the font will be used
     * @return A {@link Font} or null to use the default
     */
    @Nullable
    default Font getFont(ItemStack stack, FontContext context) {
        return null;
    }

    /**
     * This method returns an ArmPose that can be defined using the {@link net.minecraft.client.model.HumanoidModel.ArmPose#create(String, boolean, IArmPoseTransformer)} method.
     * This allows for creating custom item use animations.
     *
     * @param entityLiving The entity holding the item
     * @param hand         The hand the ArmPose will be applied to
     * @param itemStack    The stack being held
     * @return A custom ArmPose that can be used to define movement of the arm
     */
    @Nullable
    default HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
        return null;
    }

    /**
     * Called right before when client applies transformations to item in hand and render it.
     *
     * @param poseStack    The pose stack
     * @param player       The player holding the item, it's always main client player
     * @param arm          The arm holding the item
     * @param itemInHand   The held item
     * @param partialTick  Partial tick time, useful for interpolation
     * @param equipProcess Equip process time, Ranging from 0.0 to 1.0. 0.0 when it's done equipping
     * @param swingProcess Swing process time, Ranging from 0.0 to 1.0. 0.0 when it's done swinging
     * @return true if it should skip applying other transforms and go straight to rendering
     */
    default boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
        return false;
    }

    /**
     * Queries the humanoid armor model for this item when it's equipped.
     *
     * @param itemStack The item stack
     * @param layerType The slot the item is in
     * @param original  The original armor model. Will have attributes set.
     * @return A HumanoidModel to be rendered. Relevant properties are to be copied over by the caller.
     * @see #getGenericArmorModel(ItemStack, EquipmentClientInfo.LayerType, Model)
     */
    default Model getHumanoidArmorModel(ItemStack itemStack, EquipmentClientInfo.LayerType layerType, Model original) {
        return original;
    }

    /**
     * Queries the armor model for this item when it's equipped. Useful in place of
     * {@link #getHumanoidArmorModel(ItemStack, EquipmentClientInfo.LayerType, Model)} for wrapping the original
     * model or returning anything non-standard.
     * <p>
     * If you override this method you are responsible for copying any properties you care about from the original model.
     *
     * @param itemStack The item stack
     * @param layerType The slot the item is in
     * @param original  The original armor model. Will have attributes set.
     * @return A Model to be rendered. Relevant properties must be copied over manually.
     * @see #getHumanoidArmorModel(ItemStack, EquipmentClientInfo.LayerType, Model)
     */
    default Model getGenericArmorModel(ItemStack itemStack, EquipmentClientInfo.LayerType layerType, Model original) {
        Model replacement = getHumanoidArmorModel(itemStack, layerType, original);
        if (replacement != original) {
            // FIXME: equipment rendering deals with a plain Model now
            //ClientHooks.copyModelProperties(original, replacement);
            return replacement;
        }
        return original;
    }

    /**
     * Called when an armor piece is about to be rendered, allowing parts of the model to be animated or changed.
     *
     * @param itemStack       The item stack being worn
     * @param livingEntity    The entity wearing the armor
     * @param equipmentSlot   The slot the armor stack is being worn in
     * @param model           The armor model being rendered
     * @param limbSwing       The swing position of the entity's walk animation
     * @param limbSwingAmount The swing speed of the entity's walk animation
     * @param partialTick     The partial tick time
     * @param ageInTicks      The total age of the entity, with partialTick already applied
     * @param netHeadYaw      The yaw (Y rotation) of the entity's head
     * @param headPitch       The pitch (X rotation) of the entity's head
     */
    // TODO 1.21.2: add back patch that calls this method from HumanoidArmorLayer
    default void setupModelAnimations(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, Model model, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {}

    /**
     * Called when the client starts rendering the HUD, and is wearing this item in the helmet slot.
     * <p>
     * This is where pumpkins would render their overlay.
     *
     * @param stack       The item stack
     * @param player      The player entity
     * @param width       The viewport width
     * @param height      Viewport height
     * @param partialTick Partial tick time, useful for interpolation
     */
    default void renderHelmetOverlay(ItemStack stack, Player player, int width, int height, float partialTick) {}

    /**
     * {@return Whether the item should bob when rendered in the world as an entity}
     *
     * @param stack The stack being rendered
     */
    default boolean shouldBobAsEntity(ItemStack stack) {
        return true;
    }

    /**
     * {@return Whether the item should be spread out when rendered in the world as an entity}
     *
     * @param stack The stack being rendered
     */
    default boolean shouldSpreadAsEntity(ItemStack stack) {
        return true;
    }

    /**
     * Called when armor layers are rendered by {@link net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer}.
     * <p>
     * Allows custom dye colors to be specified per-layer; default vanilla behavior allows for only a single dye color
     * (specified by the {@link net.minecraft.core.component.DataComponents#DYED_COLOR} data component) for all layers.
     * <p>
     * Returning 0 here will cause rendering of this layer to be skipped entirely; this is recommended if the layer
     * doesn't need to be rendered for a particular armor slot.
     *
     * @param stack         the armor item stack being rendered
     * @param layer         the armor layer being rendered
     * @param layerIdx      an index into the list of layers for the {@code ArmorMaterial} used by this item
     * @param fallbackColor the return value of {@link #getDefaultDyeColor(ItemStack)}, passed as a parameter for
     *                      performance
     * @return a custom color for the layer, in ARGB format, or 0 to skip rendering
     */
    default int getArmorLayerTintColor(ItemStack stack, EquipmentClientInfo.Layer layer, int layerIdx, int fallbackColor) {
        return EquipmentLayerRenderer.getColorForLayer(layer, fallbackColor);
    }

    /**
     * Called once per render pass of equipped armor items, regardless of the number of layers; the return value of this
     * method is passed to {@link #getArmorLayerTintColor(ItemStack, EquipmentClientInfo.Layer, int, int)} as
     * the {@code fallbackColor} parameter.
     * <p>
     * You can override this method for your custom armor item to provide an alternative default color for the item when
     * no explicit color is specified.
     *
     * @param stack the armor item stack
     * @return a default color for the layer, in ARGB format
     */
    default int getDefaultDyeColor(ItemStack stack) {
        return stack.is(ItemTags.DYEABLE) ? ARGB.opaque(DyedItemColor.getOrDefault(stack, 0)) : 0;
    }

    /**
     * Called by RenderBiped and RenderPlayer to determine the armor texture that
     * should be used for the currently equipped item. This will be called on
     * stacks with the {@link DataComponents#EQUIPPABLE} component.
     *
     * Returning null from this function will use the default value.
     *
     * @param stack    ItemStack for the equipped armor
     * @param type     The layer type of the armor
     * @param layer    The armor layer
     * @param _default The default texture determined by the equipment renderer
     * @return Path of texture to bind, or null to use default
     */
    @Nullable
    default ResourceLocation getArmorTexture(ItemStack stack, EquipmentClientInfo.LayerType type, EquipmentClientInfo.Layer layer, ResourceLocation _default) {
        return null;
    }

    enum FontContext {
        /**
         * Used to display the amount of items in the {@link ItemStack}.
         */
        ITEM_COUNT,
        /**
         * Used to display text in the {@link net.minecraft.world.inventory.tooltip.TooltipComponent}.
         */
        TOOLTIP,
        /**
         * Used to display the item name above the hotbar when the player selects it.
         */
        SELECTED_ITEM_NAME
    }
}
