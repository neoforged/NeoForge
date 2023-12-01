/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.item;

import java.util.Map;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(value = ForgeSpawnEggItemTest.MODID)
public class ForgeSpawnEggItemTest {
    static final String MODID = "forge_spawnegg_test";
    static final boolean ENABLED = true;

    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MODID);
    private static final DeferredHolder<EntityType<?>, EntityType<Pig>> ENTITY = ENTITIES.register("test_entity", () -> EntityType.Builder.of(Pig::new, MobCategory.CREATURE).sized(1, 1).build("test_entity"));

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final DeferredItem<DeferredSpawnEggItem> EGG = ITEMS.register("test_spawn_egg", () -> new DeferredSpawnEggItem(ENTITY, 0x0000FF, 0xFF0000, new Item.Properties()));

    public ForgeSpawnEggItemTest(ModContainer modContainer) {
        if (ENABLED) {
            var eventBus = modContainer.getEventBus();
            ITEMS.register(eventBus);
            ENTITIES.register(eventBus);
            eventBus.register(this);
            eventBus.addListener(this::addCreative);
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
            event.accept(EGG);
    }

    @SubscribeEvent
    public void onRegisterAttributes(final EntityAttributeCreationEvent event) {
        AttributeSupplier.Builder attributes = Pig.createAttributes();
        //Remove step height attribute to validate that things are handled properly when an entity doesn't have it
        Map<Attribute, AttributeInstance> builder = ObfuscationReflectionHelper.getPrivateValue(AttributeSupplier.Builder.class, attributes, "builder");
        if (builder != null) {
            builder.remove(NeoForgeMod.STEP_HEIGHT.value());
        }
        event.put(ENTITY.get(), attributes.build());
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    private static class ClientEvents {
        @SubscribeEvent
        public static void onRegisterRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            if (!ENABLED) {
                return;
            }

            event.registerEntityRenderer(ENTITY.get(), PigRenderer::new);
        }
    }
}
