/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.debug;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.damagesource.IDeathMessageProvider;
import net.minecraftforge.common.damagesource.IScalingFunction;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod("custom_damage_type_test")
@Mod.EventBusSubscriber(bus = Bus.MOD, modid = "custom_damage_type_test")
public class CustomDamageTypeTest
{
    public static final boolean ENABLE = true;

    public static final IScalingFunction SCALE_FUNC = (source, target, amount, difficulty) -> {
        return switch (target.level().getDifficulty())
        {
            case PEACEFUL -> amount * 0F;
            case EASY -> amount * 0.75F;
            case NORMAL -> amount;
            case HARD -> amount * 5F;
        };
    };

    public static final IDeathMessageProvider MSG_PROVIDER = (entity, lastEntry, sigFall) -> {
        DamageSource dmgSrc = lastEntry.source();
        return Component.literal(entity.getName().getString() + " was killed via test damage by " + dmgSrc.getDirectEntity().getName().getString());
    };

    public static final DamageEffects EFFECTS;
    public static final DamageScaling SCALING;
    public static final DeathMessageType MSGTYPE;

    public static final ResourceKey<DamageType> TEST_DMG_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("test", "test"));

    static
    {
        if (ENABLE)
        {
            EFFECTS = DamageEffects.create("TEST_EFFECTS", "test:effects", () -> SoundEvents.DONKEY_ANGRY);
            SCALING = DamageScaling.create("TEST_SCALING", "test:scaling", SCALE_FUNC);
            MSGTYPE = DeathMessageType.create("TEST_MSGTYPE", "test:msgtype", MSG_PROVIDER);
        }
        else
        {
            // Don't create these enums if the test is disabled.
            EFFECTS = null;
            SCALING = null;
            MSGTYPE = null;
        }
    }

    @SubscribeEvent
    public static void register(RegisterEvent e)
    {
        if (ENABLE && e.getForgeRegistry() == (Object) ForgeRegistries.ITEMS)
        {
            e.getForgeRegistry().register("test", new Item(new Item.Properties())
            {
                @Override
                public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity)
                {
                    if (entity instanceof LivingEntity living)
                    {
                        living.hurt(player.level().damageSources().source(TEST_DMG_TYPE, player), 2);
                    }
                    return true;
                }
            });
        }
    }
}