/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod("add_entity_attribute_test")
public class AddEntityAttributeTest {
    public static final boolean ENABLE = true;
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, "add_entity_attribute_test");
    public static final DeferredHolder<Attribute, Attribute> TEST_ATTR = ATTRIBUTES.register("test_attr", () -> new RangedAttribute("neoforge.test_attr", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public AddEntityAttributeTest(IEventBus modEventBus) {
        if (ENABLE) {
            ATTRIBUTES.register(modEventBus);
            modEventBus.register(this);
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
