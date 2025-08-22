package net.neoforged.neoforge.debug.transfer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = "transfer.vanillahandlers")
public class VanillaHandlersTests {
    private static final ItemResource RESOURCE = ItemResource.of(Items.APPLE);

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Test cooldown handling of the hopper wrapper")
    public static void testHopperCooldown(ExtendedGameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.HOPPER);

        // Note: Cooldown is initialized to -1, which also counts as not having cooldown.
        var hopperEntity = helper.getBlockEntity(pos, HopperBlockEntity.class);
        var hopper = VanillaContainerWrapper.of(hopperEntity);

        // Insertion into empty hopper -> cooldown
        try (var transaction = Transaction.open(null)) {
            hopper.insert(RESOURCE, 10, transaction);
            transaction.commit();
        }
        helper.assertValueEqual(HopperBlockEntity.MOVE_ITEM_SPEED, getHopperCooldown(hopperEntity), "hopper cooldown");
        hopperEntity.setCooldown(0);

        // Second insertion into hopper -> no cooldown
        try (var transaction = Transaction.open(null)) {
            hopper.insert(RESOURCE, 10, transaction);
            transaction.commit();
        }
        helper.assertValueEqual(0, getHopperCooldown(hopperEntity), "hopper cooldown");

        hopperEntity.clearContent();
        hopperEntity.setItem(4, RESOURCE.toStack());

        // Insertion into non-empty (with an item at a different index) hopper -> no cooldown
        try (var transaction = Transaction.open(null)) {
            hopper.insert(RESOURCE, 10, transaction);
            transaction.commit();
        }
        helper.assertValueEqual(0, getHopperCooldown(hopperEntity), "hopper cooldown");

        // Extraction -> no cooldown
        try (var transaction = Transaction.open(null)) {
            hopper.extract(RESOURCE, 15, transaction);
            transaction.commit();
        }
        helper.assertContainerEmpty(pos);
        helper.assertValueEqual(0, getHopperCooldown(hopperEntity), "hopper cooldown");

        // Simulated insertion into empty hopper -> no cooldown
        try (var transaction = Transaction.open(null)) {
            hopper.insert(RESOURCE, 10, transaction);
        }
        helper.assertValueEqual(0, getHopperCooldown(hopperEntity), "hopper cooldown");

        // Insertion into empty hopper + extract in the same transaction -> cooldown
        try (var transaction = Transaction.open(null)) {
            hopper.insert(RESOURCE, 10, transaction);
            helper.assertContainerContains(pos, RESOURCE.getItem());
            hopper.extract(RESOURCE, 10, transaction);
            transaction.commit();
        }
        helper.assertValueEqual(HopperBlockEntity.MOVE_ITEM_SPEED, getHopperCooldown(hopperEntity), "hopper cooldown");

        helper.succeed();
    }

    private static int getHopperCooldown(HopperBlockEntity hopper) {
        return ObfuscationReflectionHelper.getPrivateValue(HopperBlockEntity.class, hopper, "cooldownTime");
    }
}
