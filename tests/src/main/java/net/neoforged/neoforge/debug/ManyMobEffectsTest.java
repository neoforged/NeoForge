/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import java.util.List;
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
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryObject;
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

    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);
    private static final RegistryObject<MobEffect> LAST_EFFECT;

    static {
        RegistryObject<MobEffect> effect = null;
        for (int i = 0; i < 255; i++) {
            final var index = i;
            effect = MOB_EFFECTS.register("effect_" + i, () -> new MobEffect(MobEffectCategory.NEUTRAL, 0xFF0000) {
                @Override
                public void applyEffectTick(LivingEntity entity, int amplifier) {
                    if (entity.level().isClientSide) {
                        LOGGER.info("Effect Tick for {} on the client", index);
                    }
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return true;
                }
            });
        }
        LAST_EFFECT = effect;
    }

    public ManyMobEffectsTest() {
        if (!ENABLED) return;
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        MOB_EFFECTS.register(modBus);
        NeoForge.EVENT_BUS.addListener(ManyMobEffectsTest::mobInteract);
        modBus.addListener((final RegisterEvent event) -> event.register(Registries.CREATIVE_MODE_TAB, helper -> helper.register(new ResourceLocation(MODID, "many_mob_effects_test"), CreativeModeTab.builder().withSearchBar()
                .icon(() -> new ItemStack(Items.POTION))
                .displayItems((params, output) -> {
                    var stack = new ItemStack(Items.POTION);
                    PotionUtils.setCustomEffects(stack, List.of(new MobEffectInstance(LAST_EFFECT.get(), 1000)));
                    output.accept(stack);

                    stack = new ItemStack(Items.SUSPICIOUS_STEW);
                    SuspiciousStewItem.saveMobEffects(stack, List.of(new SuspiciousEffectHolder.EffectEntry(LAST_EFFECT.get(), 1000)));
                    output.accept(stack);
                })
                .build())));
    }

    private static void mobInteract(PlayerInteractEvent.EntityInteract event) {
        if (!event.getTarget().level().isClientSide() && event.getTarget() instanceof MushroomCow cow) {
            var heldItem = event.getEntity().getItemInHand(event.getHand());
            if (heldItem.is(Items.POTION)) {
                var effects = PotionUtils.getMobEffects(heldItem);
                if (!effects.isEmpty()) {
                    ObfuscationReflectionHelper.setPrivateValue(MushroomCow.class, cow, effects.get(0).getEffect(), "effect");
                    ObfuscationReflectionHelper.setPrivateValue(MushroomCow.class, cow, effects.get(0).getDuration(), "effectDuration");
                }
            } else if (heldItem.isEmpty()) {
                var effect = ((MobEffect) ObfuscationReflectionHelper.getPrivateValue(MushroomCow.class, cow, "effect"));
                if (effect != null) {
                    event.getEntity().sendSystemMessage(Component.literal(String.valueOf(ForgeRegistries.MOB_EFFECTS.getKey(effect))));
                }
            }
        }
    }

}
