/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.level.ExplosionKnockbackEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTestPlayer;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = { EntityTests.GROUP + ".event", "event" })
public class EntityEventTests {
    @GameTest
    @EmptyTemplate(value = "15x5x15", floor = true)
    @TestHolder(description = "Tests if the entity teleport event is fired")
    static void entityTeleportEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final EntityTeleportEvent.ChorusFruit event) -> {
            if (event.getEntity() instanceof Pig) {
                event.setCanceled(true);
            } else if (event.getEntity() instanceof Cow cow) {
                event.setTargetZ(event.getPrevZ() + 3 * (cow.getRandom().nextInt(2) + 1));
                event.setTargetX(event.getPrevX() + 3.5 * cow.getRandom().nextInt(2));
            }
            test.pass();
        });

        test.onGameTest(helper -> helper.startSequence()
                .thenSequence(seq -> seq
                        .thenMap(() -> helper.spawnWithNoFreeWill(EntityType.PIG, 7, 2, 7))
                        .thenExecute(pig -> new ItemStack(Items.CHORUS_FRUIT).finishUsingItem(helper.getLevel(), pig))
                        .thenExecute(() -> helper.assertEntityPresent(EntityType.PIG, 7, 2, 7)))

                .thenIdle(10)

                .thenSequence(seq -> seq
                        .thenMap(() -> helper.spawnWithNoFreeWill(EntityType.COW, 7, 2, 7))
                        .thenExecute(cow -> new ItemStack(Items.CHORUS_FRUIT).finishUsingItem(helper.getLevel(), cow))
                        .thenExecute(() -> helper.assertEntityNotPresent(EntityType.COW, 7, 2, 7)))

                .thenIdle(10)
                .thenExecute(helper::killAllEntities)
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the EntityAttributeModificationEvent is fired")
    static void entityAttributeModificationEvent(final DynamicTest test, final RegistrationHelper reg) {
        final var testAttr = reg.registrar(Registries.ATTRIBUTE).register("test_attribute", () -> new RangedAttribute(reg.modId() + ".test_attr", 1.5D, 0.0D, 1024.0D).setSyncable(true));
        test.framework().modEventBus().addListener((final EntityAttributeModificationEvent event) -> {
            event.add(EntityType.DONKEY, testAttr);
        });

        test.onGameTest(helper -> helper.startSequence(() -> helper.spawnWithNoFreeWill(EntityType.DONKEY, 1, 2, 1))
                .thenExecute(donkey -> helper.assertEntityProperty(
                        donkey, d -> d.getAttribute(testAttr).getValue(), "test attribute", 1.5D))
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if EntityInvulnerabilityCheckEvent prevents damage when modified.")
    static void entityInvulnerabilityCheckEvent(final DynamicTest test, final RegistrationHelper reg) {
        final Component NAME = Component.literal("invulnerable_entity");
        test.eventListeners().forge().addListener((final EntityInvulnerabilityCheckEvent event) -> {
            if (event.getEntity() instanceof GameTestPlayer entity && entity.hasCustomName() && Objects.equals(entity.getCustomName(), NAME))
                event.setInvulnerable(false);
        });

        test.onGameTest(helper -> {
            DamageSource source = new DamageSource(helper.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK));
            helper.startSequence(() -> helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL))
                    .thenExecute(player -> player.setCustomName(NAME))
                    .thenExecute(player -> player.setInvulnerable(true))
                    .thenWaitUntil(player -> helper.assertFalse(player.isInvulnerableTo(source), "Player Invulnerability not bypassed."))
                    .thenSucceed();
        });
    }

    @GameTest
    @EmptyTemplate(value = "15x5x15", floor = true)
    @TestHolder(description = "Tests if the pig only gets vertical knockback from explosion knockback event")
    static void entityVerticalExplosionKnockbackEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final ExplosionKnockbackEvent event) -> {
            if (event.getAffectedEntity() instanceof Pig) {
                event.setKnockbackVelocity(new Vec3(0, event.getKnockbackVelocity().y(), 0));
            }
        });

        test.onGameTest(helper -> helper.startSequence(() -> helper.spawnWithNoFreeWill(EntityType.PIG, 8, 3, 7))
                .thenExecute(pig -> helper.setBlock(8, 2, 7, Blocks.ACACIA_LEAVES))
                .thenExecute(pig -> helper.getLevel().explode(null, helper.getLevel().damageSources().generic(), null, helper.absolutePos(new BlockPos(7, 2, 7)).getCenter(), 2f, false, Level.ExplosionInteraction.TNT))
                .thenExecute(pig -> helper.assertEntityProperty(pig, p -> pig.getDeltaMovement().x() == 0 && pig.getDeltaMovement().y() != 0 && pig.getDeltaMovement().z() == 0, "Check explosion Knockback"))
                .thenIdle(10)
                .thenExecute(helper::killAllEntities)
                .thenSucceed());
    }
}
