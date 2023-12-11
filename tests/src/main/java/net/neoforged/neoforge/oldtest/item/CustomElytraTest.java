/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

@Mod(CustomElytraTest.MOD_ID)
public class CustomElytraTest {
    public static final String MOD_ID = "custom_elytra_test";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredItem<Item> TEST_ELYTRA = ITEMS.register("test_elytra", () -> new CustomElytra(new Properties().durability(100)));

    public CustomElytraTest(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(this::onClientSetup);
        modBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
            event.accept(TEST_ELYTRA);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        registerElytraLayer();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @OnlyIn(Dist.CLIENT)
    private void registerElytraLayer() {
        Minecraft mc = Minecraft.getInstance();
        mc.getEntityRenderDispatcher().getSkinMap().values()
                .forEach(player -> ((LivingEntityRenderer) player).addLayer(new CustomElytraLayer((LivingEntityRenderer) player, mc.getEntityModels())));
    }

    public static class CustomElytra extends Item {

        public CustomElytra(Properties properties) {
            super(properties);
            DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
        }

        @Nullable
        @Override
        public EquipmentSlot getEquipmentSlot(ItemStack stack) {
            return EquipmentSlot.CHEST; //Or you could just extend ItemArmor
        }

        @Override
        public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
            return true;
        }

        @Override
        public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
            if (!entity.level().isClientSide) {
                //Adding 1 to flightTicks prevents damage on the very first tick.
                int nextFlightTick = flightTicks + 1;
                if (nextFlightTick % 10 == 0) {
                    if (nextFlightTick % 20 == 0) {
                        stack.hurtAndBreak(1, entity, e -> e.broadcastBreakEvent(EquipmentSlot.CHEST));
                    }
                    entity.gameEvent(GameEvent.ELYTRA_GLIDE);
                }
            }
            return true;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class CustomElytraLayer extends ElytraLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        private static final ResourceLocation TEXTURE_ELYTRA = new ResourceLocation(MOD_ID, "textures/entity/custom_elytra.png");

        public CustomElytraLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer, EntityModelSet modelSet) {
            super(renderer, modelSet);
        }

        @Override
        public boolean shouldRender(ItemStack stack, AbstractClientPlayer entity) {
            return stack.getItem() == TEST_ELYTRA.get();
        }

        @Override
        public ResourceLocation getElytraTexture(ItemStack stack, AbstractClientPlayer entity) {
            return TEXTURE_ELYTRA;
        }
    }
}
