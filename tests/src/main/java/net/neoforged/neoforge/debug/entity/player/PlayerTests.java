package net.neoforged.neoforge.debug.entity.player;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.debug.entity.EntityTests;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

import java.util.UUID;

@ForEachTest(groups = PlayerTests.GROUP)
public class PlayerTests {
    public static final String GROUP = EntityTests.GROUP + ".player";

    @GameTest
    @EmptyTemplate(value = "7x7x7", floor = true)
    @TestHolder(description = "Tests if a knockback sword works")
    static void playerAttackKnockback(final DynamicTest test, final RegistrationHelper reg) {
        final var sword = reg.items().register("knockback_sword", () -> new KnockbackSwordItem(Tiers.IRON, 3, -2.4F, 2, new Item.Properties()))
                .withLang("Knockback Sword");

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> {
                    final Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, 3, 2, 3);
                    final Player player = helper.makeMockPlayer();
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(sword.get()));
                    player.attack(pig);
                })
                .thenExecuteAfter(3, () -> helper.assertEntityPresent(EntityType.PIG, new BlockPos(3, 2, 4), 0.5))
                .thenExecuteAfter(2, () -> helper.killAllEntitiesOfClass(Pig.class))
                .thenSucceed());
    }

    private static class KnockbackSwordItem extends SwordItem {
        private final float attackKnockback;
        private final Multimap<Attribute, AttributeModifier> defaultModifiers = ArrayListMultimap.create(); // initialize as empty
        protected static final UUID BASE_ATTACK_KNOCKBACK_UUID = UUID.fromString("01efce91-ab3a-4163-b464-5c7bd1ae5496");

        KnockbackSwordItem(Tier itemTier, int attackDamageIn, float attackSpeedIn, float attackKnockbackIn, Properties properties) {
            super(itemTier, attackDamageIn, attackSpeedIn, properties);
            this.attackKnockback = attackKnockbackIn;
        }

        @Override
        public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlotType) {
            if (equipmentSlotType == EquipmentSlot.MAINHAND) {
                if (this.defaultModifiers.isEmpty()) {
                    Multimap<Attribute, AttributeModifier> oldAttributeModifiers = super.getDefaultAttributeModifiers(equipmentSlotType);
                    this.defaultModifiers.putAll(oldAttributeModifiers);
                    this.defaultModifiers.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(BASE_ATTACK_KNOCKBACK_UUID, "Weapon modifier", (double) this.attackKnockback, AttributeModifier.Operation.ADDITION));
                }
                return this.defaultModifiers;
            } else return super.getDefaultAttributeModifiers(equipmentSlotType);
        }
    }
}