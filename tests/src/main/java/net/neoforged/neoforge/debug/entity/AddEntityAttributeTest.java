/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

@Mod("add_entity_attribute_test")
public class AddEntityAttributeTest {
    public static final boolean ENABLE = true;
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, "add_entity_attribute_test");
    public static final RegistryObject<Attribute> TEST_ATTR = ATTRIBUTES.register("test_attr", () -> new RangedAttribute("forge.test_attr", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public AddEntityAttributeTest() {
        if (ENABLE) {
            ATTRIBUTES.register(FMLJavaModLoadingContext.get().getModEventBus());
            FMLJavaModLoadingContext.get().getModEventBus().register(this);
        }
    }

    @SubscribeEvent
    public void entityAttributeSetup(EntityAttributeModificationEvent event) {
        event.getTypes().forEach(entityType -> {
            if (!event.has(entityType, TEST_ATTR.get())) {
                event.add(entityType, TEST_ATTR.get());
            }
        });
    }
}
