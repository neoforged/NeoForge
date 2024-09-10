/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.entity.player;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.IArmPoseTransformer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.deferred.DeferredItem;
import net.neoforged.neoforge.registries.deferred.DeferredItems;

/**
 * Tests if item usage animation system works as intended. `item_use_animation_test:thing` is edible item with custom usage animation made with this system.
 * In game, use `/give @s item_use_animation_test:thing 1` to obtain test item
 * When you try to eat it, your arm in 3d person should start swinging really fast.
 * And item in your hand will go down little.
 */
@Mod(ItemUseAnimationTest.MOD_ID)
public class ItemUseAnimationTest {
    public static final String MOD_ID = "item_use_animation_test";

    private static final DeferredItems ITEMS = DeferredItems.createItems(MOD_ID);

    private static final DeferredItem<Item> THING = ITEMS.register("thing", () -> new ThingItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).alwaysEdible().build())));

    public ItemUseAnimationTest(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(this::addCreative);

        if (FMLEnvironment.dist.isClient()) {
            modBus.addListener(ClientEvents::onRegisterClientExtensions);
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT)
            event.accept(THING);
    }

    @SuppressWarnings("unused") // Referenced by enumextender.json
    public static final class EnumParams {
        public static final EnumProxy<HumanoidModel.ArmPose> ARM_POSE_ENUM_PARAMS = new EnumProxy<>(
                HumanoidModel.ArmPose.class, false, (IArmPoseTransformer) (model, entity, arm) -> {
                    if (arm == HumanoidArm.RIGHT) {
                        model.rightArm.xRot = (float) (Math.random() * Math.PI * 2);
                    } else {
                        model.leftArm.xRot = (float) (Math.random() * Math.PI * 2);
                    }
                });
    }

    private static class ThingItem extends Item {
        public ThingItem(Item.Properties props) {
            super(props);
        }

        @Override
        public UseAnim getUseAnimation(ItemStack stack) {
            return UseAnim.CUSTOM;
        }
    }

    private static final class ClientEvents {
        private static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
            event.registerItem(new IClientItemExtensions() {
                private static final HumanoidModel.ArmPose SWING_POSE = EnumParams.ARM_POSE_ENUM_PARAMS.getValue();

                @Override
                public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                    if (!itemStack.isEmpty()) {
                        if (entityLiving.getUsedItemHand() == hand && entityLiving.getUseItemRemainingTicks() > 0) {
                            return SWING_POSE;
                        }
                    }
                    return HumanoidModel.ArmPose.EMPTY;
                }

                @Override
                public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
                    applyItemArmTransform(poseStack, arm);
                    if (player.getUseItem() != itemInHand) {
                        return true;
                    }
                    if (player.isUsingItem()) {
                        poseStack.translate(0.0, -0.05, 0.0);
                    }
                    return true;
                }

                private void applyItemArmTransform(PoseStack poseStack, HumanoidArm arm) {
                    int i = arm == HumanoidArm.RIGHT ? 1 : -1;
                    poseStack.translate(i * 0.56F, -0.52F, -0.72F);
                }
            }, THING.get());
        }
    }
}
