/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Registers 255 mob effects that log every tick on the client.
 * Used to test the patches for saving MobEffects to NBT.
 *
 * To verify correct function:
 * - Check that the Potion item and suspicious stew item provided via the creative tab function correctly
 * - Check that the effect given by the above items can persist on an entity as well as on ItemStacks in inventories
 * - Right click a Mushroom Cow using the above given Potion Item. Verify that it obtained the effect by right-clicking
 * it again using an empty hand before and after reloading the world.
 */
@Mod(ManyMobEffectsTest.MODID)
public class ManyMobEffectsTest {
    static final String MODID = "many_mob_effects_test";

    private static final boolean ENABLED = true;

    private static final Logger LOGGER = LogManager.getLogger();

    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, MODID);
    private static final DeferredHolder<MobEffect, MobEffect> LAST_EFFECT;

    static {
        DeferredHolder<MobEffect, MobEffect> effect = null;
        for (int i = 0; i < 255; i++) {
            final var index = i;
            effect = MOB_EFFECTS.register("effect_" + i, () -> new MobEffect(MobEffectCategory.NEUTRAL, 0xFF0000) {
                @Override
                public boolean applyEffectTick(LivingEntity entity, int amplifier) {
                    if (entity.level().isClientSide) {
                        LOGGER.info("Effect Tick for {} on the client", index);
                    }
                    return super.applyEffectTick(entity, amplifier);
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return true;
                }
            });
        }
        LAST_EFFECT = effect;
    }

    public ManyMobEffectsTest(IEventBus modBus) {
        if (!ENABLED) return;
        MOB_EFFECTS.register(modBus);
        NeoForge.EVENT_BUS.addListener(ManyMobEffectsTest::mobInteract);
        modBus.addListener((final RegisterEvent event) -> event.register(Registries.CREATIVE_MODE_TAB, helper -> helper.register(ResourceLocation.fromNamespaceAndPath(MODID, "many_mob_effects_test"), CreativeModeTab.builder().withSearchBar()
                .icon(() -> new ItemStack(Items.POTION))
                .displayItems((params, output) -> {
                    var stack = new ItemStack(Items.POTION);
                    stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), List.of(new MobEffectInstance(LAST_EFFECT, 1000))));
                    output.accept(stack);

                    stack = new ItemStack(Items.SUSPICIOUS_STEW);
                    stack.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, new SuspiciousStewEffects(List.of(new SuspiciousStewEffects.Entry(LAST_EFFECT, 1000))));
                    output.accept(stack);
                })
                .build())));
    }

    private static void mobInteract(PlayerInteractEvent.EntityInteract event) {
        if (!event.getTarget().level().isClientSide() && event.getTarget() instanceof MushroomCow cow) {
            var heldItem = event.getEntity().getItemInHand(event.getHand());
            if (heldItem.is(Items.POTION)) {
                var effects = heldItem.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects().iterator();
                if (effects.hasNext()) {
                    MobEffectInstance effect = effects.next();
                    ObfuscationReflectionHelper.setPrivateValue(MushroomCow.class, cow, effect.getEffect(), "effect");
                    ObfuscationReflectionHelper.setPrivateValue(MushroomCow.class, cow, effect.getDuration(), "effectDuration");
                }
            } else if (heldItem.isEmpty()) {
                var effect = ((MobEffect) ObfuscationReflectionHelper.getPrivateValue(MushroomCow.class, cow, "effect"));
                if (effect != null) {
                    event.getEntity().sendSystemMessage(Component.literal(String.valueOf(BuiltInRegistries.MOB_EFFECT.getKey(effect))));
                }
            }
        }
    }
}
