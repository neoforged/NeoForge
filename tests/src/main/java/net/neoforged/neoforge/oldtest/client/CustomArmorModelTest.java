/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

@Mod(CustomArmorModelTest.MOD_ID)
public class CustomArmorModelTest {
    static final String MOD_ID = "custom_armor_model_test";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    // demonstrates custom non-humanoid model
    private static final DeferredItem<Item> RED_LEGGINGS = ITEMS.registerItem(
            "red_leggings",
            props -> new ArmorItem(
                    ArmorMaterials.DIAMOND,
                    ArmorType.LEGGINGS,
                    props.stacksTo(1).component(DataComponents.DYED_COLOR, new DyedItemColor(0xFF0000, false))));
    // demonstrates the properties are copied from the vanilla model
    private static final DeferredItem<Item> ENDERMAN_CHESTPLATE = ITEMS.registerItem("enderman_chestplate", props -> new ArmorItem(ArmorMaterials.GOLD, ArmorType.CHESTPLATE, props.stacksTo(1)));
    private static final DeferredItem<Item> ENDERMAN_BOOTS = ITEMS.registerItem("enderman_boots", props -> new ArmorItem(ArmorMaterials.GOLD, ArmorType.BOOTS, props.stacksTo(1)));

    public CustomArmorModelTest(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(this::addCreative);

        if (FMLEnvironment.dist.isClient()) {
            modBus.addListener(ClientEvents::onRegisterClientExtensions);
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(RED_LEGGINGS);
            event.accept(ENDERMAN_CHESTPLATE);
            event.accept(ENDERMAN_BOOTS);
        }
    }

    private static final class ClientEvents {
        private static final ResourceLocation ARMOR_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/enderman/enderman.png");
        private static final Lazy<HumanoidModel<HumanoidRenderState>> ENDERMAN = Lazy.of(() -> new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ENDERMAN)));

        private static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
            event.registerItem(new IClientItemExtensions() {
                @Override
                public HumanoidModel<?> getHumanoidArmorModel(ItemStack itemStack, EquipmentClientInfo.LayerType armorSlot, Model _default) {
                    return ENDERMAN.get();
                }

                @Nullable
                @Override
                public ResourceLocation getArmorTexture(ItemStack stack, EquipmentClientInfo.LayerType type, EquipmentClientInfo.Layer layer, ResourceLocation _default) {
                    return ARMOR_TEXTURE;
                }
            }, ENDERMAN_BOOTS.get(), ENDERMAN_CHESTPLATE.get());
        }
    }
}
