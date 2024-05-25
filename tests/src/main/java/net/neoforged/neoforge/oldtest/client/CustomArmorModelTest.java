/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
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
    private static final DeferredItem<Item> RED_LEGGINGS = ITEMS.register("red_leggings", () -> new TintedArmorItem(ArmorMaterials.DIAMOND, ArmorItem.Type.LEGGINGS, new Properties().stacksTo(1)));
    // demonstrates the properties are copied from the vanilla model
    private static final DeferredItem<Item> ENDERMAN_CHESTPLATE = ITEMS.register("enderman_chestplate", () -> new EndermanArmorItem(ArmorMaterials.GOLD, ArmorItem.Type.CHESTPLATE, new Properties().stacksTo(1)));
    private static final DeferredItem<Item> ENDERMAN_BOOTS = ITEMS.register("enderman_boots", () -> new EndermanArmorItem(ArmorMaterials.GOLD, ArmorItem.Type.BOOTS, new Properties().stacksTo(1)));

    public CustomArmorModelTest(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(RED_LEGGINGS);
            event.accept(ENDERMAN_CHESTPLATE);
            event.accept(ENDERMAN_BOOTS);
        }
    }

    private static class TintedArmorItem extends ArmorItem {
        public TintedArmorItem(Holder<ArmorMaterial> material, ArmorItem.Type slot, Properties props) {
            super(material, slot, props);
        }

        @Override
        public void initializeClient(Consumer<IClientItemExtensions> consumer) {
            consumer.accept(new IClientItemExtensions() {
                @Override
                public Model getGenericArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> _default) {
                    TintedArmorModel.INSTANCE.base = _default;
                    return TintedArmorModel.INSTANCE;
                }
            });
        }
    }

    private static class EndermanArmorItem extends ArmorItem {
        private static final ResourceLocation ARMOR_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/enderman/enderman.png");

        public EndermanArmorItem(Holder<ArmorMaterial> material, ArmorItem.Type slot, Properties props) {
            super(material, slot, props);
        }

        @Nullable
        @Override
        public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
            return ARMOR_TEXTURE;
        }

        @Override
        public void initializeClient(Consumer<IClientItemExtensions> consumer) {
            consumer.accept(new IClientItemExtensions() {
                @Override
                public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> _default) {
                    return TintedArmorModel.ENDERMAN.get();
                }
            });
        }
    }

    private static class TintedArmorModel extends Model {
        private static final TintedArmorModel INSTANCE = new TintedArmorModel(RenderType::entityCutoutNoCull);
        private static final Lazy<HumanoidModel<LivingEntity>> ENDERMAN = Lazy.of(() -> new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ENDERMAN)));

        private HumanoidModel<?> base;

        private TintedArmorModel(Function<ResourceLocation, RenderType> renderTypeFunction) {
            super(renderTypeFunction);
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int color) {
            if (base != null) {
                color = FastColor.ARGB32.color(FastColor.ARGB32.alpha(color), FastColor.ARGB32.red(color), 0, 0);
                base.renderToBuffer(poseStack, consumer, light, overlay, color);
            }
        }
    }
}
