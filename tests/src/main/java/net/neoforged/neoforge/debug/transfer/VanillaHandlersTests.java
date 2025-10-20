/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.transfer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ComposterWrapper;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.LivingEntityEquipmentWrapper;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.RootCommitJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;
import org.apache.commons.lang3.ArrayUtils;

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
        try (var transaction = Transaction.openRoot()) {
            hopper.insert(RESOURCE, 10, transaction);
            transaction.commit();
        }
        helper.assertValueEqual(HopperBlockEntity.MOVE_ITEM_SPEED, getHopperCooldown(hopperEntity), "hopper cooldown");
        hopperEntity.setCooldown(0);

        // Second insertion into hopper -> no cooldown because the hopper is not empty
        try (var transaction = Transaction.openRoot()) {
            hopper.insert(RESOURCE, 10, transaction);
            transaction.commit();
        }
        helper.assertValueEqual(0, getHopperCooldown(hopperEntity), "hopper cooldown");

        hopperEntity.clearContent();
        hopperEntity.setItem(4, RESOURCE.toStack());

        // Insertion into non-empty (with an item at a different index) hopper -> no cooldown
        try (var transaction = Transaction.openRoot()) {
            hopper.insert(RESOURCE, 10, transaction);
            transaction.commit();
        }
        helper.assertValueEqual(0, getHopperCooldown(hopperEntity), "hopper cooldown");

        // Extraction -> no cooldown
        try (var transaction = Transaction.openRoot()) {
            hopper.extract(RESOURCE, 15, transaction);
            transaction.commit();
        }
        helper.assertContainerEmpty(pos);
        helper.assertValueEqual(0, getHopperCooldown(hopperEntity), "hopper cooldown");

        // Simulated insertion into empty hopper -> no cooldown
        try (var transaction = Transaction.openRoot()) {
            hopper.insert(RESOURCE, 10, transaction);
        }
        helper.assertValueEqual(0, getHopperCooldown(hopperEntity), "hopper cooldown");

        // Insertion into empty hopper + extract in the same transaction -> cooldown
        try (var transaction = Transaction.openRoot()) {
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

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Ensures that furnace cook time is only reset when extraction is actually committed.")
    public static void testFurnaceCookTime(ExtendedGameTestHelper helper) {
        BlockPos pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, Blocks.FURNACE.defaultBlockState());
        FurnaceBlockEntity furnace = helper.getBlockEntity(pos, FurnaceBlockEntity.class);

        ItemResource rawIron = ItemResource.of(Items.RAW_IRON);
        furnace.setItem(0, rawIron.toStack(64));
        furnace.setItem(1, new ItemStack(Items.COAL, 64));
        var furnaceWrapper = VanillaContainerWrapper.of(furnace);

        IntSupplier cookingTimerAccessor = () -> ObfuscationReflectionHelper.getPrivateValue(AbstractFurnaceBlockEntity.class, furnace, "cookingTimer");

        helper.runAtTickTime(5, () -> {
            helper.assertTrue(cookingTimerAccessor.getAsInt() > 0, "Furnace should have started cooking.");

            try (Transaction transaction = Transaction.openRoot()) {
                if (furnaceWrapper.extract(rawIron, 64, transaction) != 64) {
                    throw helper.assertionException("Failed to extract 64 raw iron.");
                }
            }

            helper.assertTrue(cookingTimerAccessor.getAsInt() > 0, "Furnace should still cook after simulation.");

            try (Transaction transaction = Transaction.openRoot()) {
                if (furnaceWrapper.extract(rawIron, 64, transaction) != 64) {
                    throw helper.assertionException("Failed to extract 64 raw iron.");
                }
                // Even if we re-insert, the furnace got emptied at some point so it should reset its cook timer
                if (furnaceWrapper.insert(rawIron, 64, transaction) != 64) {
                    throw helper.assertionException("Failed to extract 64 raw iron.");
                }

                transaction.commit();
            }

            helper.assertTrue(cookingTimerAccessor.getAsInt() == 0, "Furnace should have reset cook time after being emptied.");

            helper.succeed();
        });
    }

    /**
     * Tests that the passed block doesn't update adjacent comparators until the very end of a committed transaction.
     *
     * @param block    A block with a Container block entity.
     * @param resource The resource to try to insert (needs to be supported by the Container).
     */
    private static <T extends BlockEntity & Container> void testComparatorOnContainer(ExtendedGameTestHelper helper, Block block, ItemResource resource, Class<T> containerClass) {
        var level = helper.getLevel();

        BlockPos pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, block.defaultBlockState());
        T container = helper.getBlockEntity(pos, containerClass);
        var resourceHandler = VanillaContainerWrapper.of(container);

        BlockPos comparatorPos = new BlockPos(1, 1, 0);
        BlockPos absoluteComparatorPos = helper.absolutePos(comparatorPos);
        Direction comparatorFacing = helper.getTestRotation().rotate(Direction.WEST);
        // support block under the comparator
        helper.setBlock(comparatorPos.relative(Direction.DOWN), Blocks.GREEN_WOOL.defaultBlockState());
        // comparator
        helper.setBlock(comparatorPos, Blocks.COMPARATOR.defaultBlockState().setValue(ComparatorBlock.FACING, comparatorFacing));

        try (Transaction transaction = Transaction.openRoot()) {
            if (level.getBlockTicks().hasScheduledTick(absoluteComparatorPos, Blocks.COMPARATOR)) {
                throw helper.assertionException("Comparator should not have a tick scheduled.");
            }

            resourceHandler.insert(resource, 1000000, transaction);

            // uncommitted insert should not schedule an update
            if (level.getBlockTicks().hasScheduledTick(absoluteComparatorPos, Blocks.COMPARATOR)) {
                throw helper.assertionException("Comparator should not have a tick scheduled.");
            }

            transaction.commit();
        }

        // committed insert should schedule an update
        if (!level.getBlockTicks().hasScheduledTick(absoluteComparatorPos, Blocks.COMPARATOR)) {
            throw helper.assertionException("Comparator should have a tick scheduled.");
        }

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that containers such as chests don't update adjacent comparators until the very end of a committed transaction.")
    public static void testChestComparator(ExtendedGameTestHelper helper) {
        testComparatorOnContainer(helper, Blocks.CHEST, ItemResource.of(Items.DIAMOND), ChestBlockEntity.class);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Same as testChestComparator but for chiseled bookshelves, because their implementation is very... strange.")
    public static void testChiseledBookshelfComparator(ExtendedGameTestHelper helper) {
        testComparatorOnContainer(helper, Blocks.CHISELED_BOOKSHELF, ItemResource.of(Items.BOOK), ChiseledBookShelfBlockEntity.class);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Test for chiseled bookshelves, because their implementation is very... strange.")
    public static void testChiseledBookshelf(ExtendedGameTestHelper helper) {
        ItemResource book = ItemResource.of(Items.BOOK);

        BlockPos pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        ChiseledBookShelfBlockEntity bookshelf = helper.getBlockEntity(pos, ChiseledBookShelfBlockEntity.class);
        var resourceHandler = VanillaContainerWrapper.of(bookshelf);

        // First, check that we can correctly undo insert operations, because vanilla's setItem doesn't permit it without our patches.
        try (Transaction transaction = Transaction.openRoot()) {
            if (resourceHandler.insert(book, 2, transaction) != 2) throw helper.assertionException("Should have inserted 2 books");

            if (bookshelf.getItem(0).getCount() != 1) throw helper.assertionException("Bookshelf stack 0 should have size 1");
            if (!book.matches(bookshelf.getItem(0))) throw helper.assertionException("Bookshelf stack 0 should be a book");
            if (bookshelf.getItem(1).getCount() != 1) throw helper.assertionException("Bookshelf stack 1 should have size 1");
            if (!book.matches(bookshelf.getItem(1))) throw helper.assertionException("Bookshelf stack 1 should be a book");
        }

        if (!bookshelf.getItem(0).isEmpty()) throw helper.assertionException("Bookshelf stack 0 should be empty again after aborting transaction");
        if (!bookshelf.getItem(1).isEmpty()) throw helper.assertionException("Bookshelf stack 1 should be empty again after aborting transaction");

        // Second, check that we correctly update the last modified slot.
        try (Transaction tx = Transaction.openRoot()) {
            if (resourceHandler.insert(1, book, 1, tx) != 1) throw helper.assertionException("Should have inserted 1 book");
            if (bookshelf.getLastInteractedSlot() != 1) throw helper.assertionException("Last modified slot should be 1");

            if (resourceHandler.insert(2, book, 1, tx) != 1) throw helper.assertionException("Should have inserted 1 book");
            if (bookshelf.getLastInteractedSlot() != 2) throw helper.assertionException("Last modified slot should be 2");

            if (resourceHandler.extract(1, book, 1, tx) != 1) throw helper.assertionException("Should have extracted 1 book");
            if (bookshelf.getLastInteractedSlot() != 1) throw helper.assertionException("Last modified slot should be 1");

            // Now, create an aborted nested transaction.
            try (Transaction nested = Transaction.open(tx)) {
                if (resourceHandler.insert(book, 100, nested) != 5) throw helper.assertionException("Should have inserted 5 books");
                // Now, last modified slot should be 5.
                if (bookshelf.getLastInteractedSlot() != 5) throw helper.assertionException("Last modified slot should be 5");
            }

            // And it's back to 1 in theory.
            if (bookshelf.getLastInteractedSlot() != 1) throw helper.assertionException("Last modified slot should be 1");
            tx.commit();
        }

        if (bookshelf.getLastInteractedSlot() != 1) throw helper.assertionException("Last modified slot should be 1 after committing transaction");

        // Let's also check the state properties. Only slot 2 should be occupied.
        BlockState state = bookshelf.getBlockState();

        for (var property : ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES) {
            helper.assertValueEqual(
                    property == BlockStateProperties.SLOT_2_OCCUPIED,
                    state.getValue(property),
                    "Value of property " + property.getName());
        }

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Test the onRootCommit for chiseled bookshelves in case the bookshelf gets deleted by another onRootCommit before.")
    public static void chiseledBookshelfRemovedBeforeOnRootCommit(ExtendedGameTestHelper helper) {
        ItemResource book = ItemResource.of(Items.BOOK);

        BlockPos pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        var resourceHandler = helper.requireCapability(Capabilities.Item.BLOCK, pos, null);

        var deleteTheBookshelf = new RootCommitJournal(() -> helper.destroyBlock(pos));

        try (var tx = Transaction.openRoot()) {
            // This will trigger the destruction of the bookshelf before its onRootCommit get to run.
            deleteTheBookshelf.updateSnapshots(tx);
            int inserted = resourceHandler.insert(book, 1, tx);
            helper.assertValueEqual(1, inserted, "books inserted");
            tx.commit();
        }

        helper.assertBlockNotPresent(Blocks.CHISELED_BOOKSHELF, pos);
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Test the onRootCommit for cauldrons in case the cauldron gets deleted by another onRootCommit before.")
    public static void cauldronRemovedBeforeOnRootCommit(ExtendedGameTestHelper helper) {
        FluidResource water = FluidResource.of(Fluids.WATER);

        BlockPos pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, Blocks.CAULDRON.defaultBlockState());
        var resourceHandler = helper.requireCapability(Capabilities.Fluid.BLOCK, pos, null);

        var deleteTheCauldron = new RootCommitJournal(() -> helper.destroyBlock(pos));

        try (var tx = Transaction.openRoot()) {
            // This will trigger the destruction of the cauldron before its onRootCommit get to run.
            deleteTheCauldron.updateSnapshots(tx);
            int inserted = resourceHandler.insert(water, FluidType.BUCKET_VOLUME, tx);
            helper.assertValueEqual(FluidType.BUCKET_VOLUME, inserted, "water inserted");
            tx.commit();
        }

        helper.assertBlockNotPresent(Blocks.CAULDRON, pos);
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Test the onRootCommit for composters in case the composter gets deleted by another onRootCommit before.")
    public static void composterRemovedBeforeOnRootCommit(ExtendedGameTestHelper helper) {
        ItemResource leaves = ItemResource.of(Items.OAK_LEAVES);

        BlockPos pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, Blocks.COMPOSTER.defaultBlockState());
        var resourceHandler = helper.requireCapability(Capabilities.Item.BLOCK, pos, Direction.UP);

        var deleteTheComposter = new RootCommitJournal(() -> helper.destroyBlock(pos));

        try (var tx = Transaction.openRoot()) {
            // This will trigger the destruction of the composter before its onRootCommit get to run.
            deleteTheComposter.updateSnapshots(tx);
            // Since this is the first insert, it will always trigger a level increase regardless of the probability.
            int inserted = resourceHandler.insert(leaves, 1, tx);
            helper.assertValueEqual(1, inserted, "leaves inserted");
            tx.commit();
        }

        helper.assertBlockNotPresent(Blocks.COMPOSTER, pos);
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate("7x7x7") // Need enough room for the dropped items
    @TestHolder(description = "Test the onRootCommit for a player's DroppedItems in case an event triggered by the dropping triggers more drops.")
    public static void playerInventoryDropWhileDropping(DynamicTest test) {
        // When dropping a carrot, drop a golden carrot in front of all fake players
        test.eventListeners().forge().addListener((ItemTossEvent event) -> {
            if (event.getEntity().getItem().is(Items.CARROT)) {
                try (var tx = Transaction.openRoot()) {
                    PlayerInventoryWrapper.of(event.getPlayer()).drop(ItemResource.of(Items.GOLDEN_CARROT), 1, false, false, tx);
                    tx.commit();
                }
            }
        });

        test.onGameTest(helper -> {
            var player = helper.makeMockPlayer();
            player.setPos(helper.getBounds().getCenter());

            var inventory = PlayerInventoryWrapper.of(player);
            try (var tx = Transaction.openRoot()) {
                inventory.drop(ItemResource.of(Items.CARROT), 1, false, false, tx);
                inventory.drop(ItemResource.of(Items.CARROT), 1, false, false, tx);
                tx.commit();
            }

            // 2 carrots and 2 golden carrots
            helper.assertEntitiesPresent(EntityType.ITEM, 4);
            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that shulker boxes cannot be inserted into other shulker boxes.")
    public static void testShulkerNoInsert(ExtendedGameTestHelper helper) {
        BlockPos pos = new BlockPos(0, 2, 0);
        helper.setBlock(pos, Blocks.SHULKER_BOX);
        ShulkerBoxBlockEntity shulker = helper.getBlockEntity(pos, ShulkerBoxBlockEntity.class);

        for (var side : ArrayUtils.add(Direction.values(), null)) {
            var resourceHandler = new WorldlyContainerWrapper(shulker, side);

            try (var tx = Transaction.openRoot()) {
                if (resourceHandler.insert(ItemResource.of(Items.SHULKER_BOX), 1, tx) > 0) {
                    helper.fail("Expected shulker box to be rejected from side: " + side, pos);
                }
            }
        }

        helper.succeed();
    }

    /**
     * {@link Container#canPlaceItem(int, ItemStack)} is supposed to be independent of the stack size.
     * However, to limit some stackable inputs to a size of 1, brewing stands and furnaces don't follow this rule in all cases.
     * This test ensures that the Transfer API works around this issue for furnaces.
     */
    @GameTest
    @EmptyTemplate
    @TestHolder
    public static void testBadFurnaceIsValid(ExtendedGameTestHelper helper) {
        BlockPos pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, Blocks.FURNACE.defaultBlockState());
        FurnaceBlockEntity furnace = helper.getBlockEntity(pos, FurnaceBlockEntity.class);
        var furnaceWrapper = VanillaContainerWrapper.of(furnace);

        try (Transaction tx = Transaction.openRoot()) {
            if (furnaceWrapper.insert(1, ItemResource.of(Items.BUCKET), 2, tx) != 1) {
                throw helper.assertionException("Exactly 1 bucket should have been inserted");
            }
        }

        helper.succeed();
    }

    /**
     * Same as {@link #testBadFurnaceIsValid}, but for brewing stands.
     */
    @GameTest
    @EmptyTemplate
    @TestHolder
    public static void testBadBrewingStandIsValid(ExtendedGameTestHelper helper) {
        BlockPos pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, Blocks.BREWING_STAND.defaultBlockState());
        BrewingStandBlockEntity brewingStand = helper.getBlockEntity(pos, BrewingStandBlockEntity.class);
        var brewingStandWrapper = VanillaContainerWrapper.of(brewingStand);

        var glassBottle = ItemResource.of(Items.GLASS_BOTTLE);

        try (Transaction tx = Transaction.openRoot()) {
            for (int bottleSlot = 0; bottleSlot < 3; ++bottleSlot) {
                if (brewingStandWrapper.insert(bottleSlot, glassBottle, 2, tx) != 1) {
                    throw helper.assertionException("Exactly 1 glass bottle should have been inserted");
                }
            }

            if (brewingStandWrapper.insert(3, ItemResource.of(Items.REDSTONE), 2, tx) != 2) {
                throw helper.assertionException("Brewing ingredient insertion should not be limited");
            }
        }

        try (Transaction tx = Transaction.openRoot()) {
            // Insertion of glass bottles should put exactly 1 bottle in each bottle slot
            if (brewingStandWrapper.insert(glassBottle, 10, tx) != 3) {
                throw helper.assertionException("Exactly 3 glass bottles should have been inserted");
            }

            for (int bottleSlot = 0; bottleSlot < 3; ++bottleSlot) {
                if (!glassBottle.equals(brewingStandWrapper.getResource(bottleSlot)) || brewingStandWrapper.getAmountAsInt(bottleSlot) != 1) {
                    throw helper.assertionException("Exactly 1 glass bottle should be stored at the bottle slot " + bottleSlot);
                }
            }
        }

        helper.succeed();
    }

    /**
     * Regression test for <a href="https://github.com/FabricMC/fabric/issues/2810">double chest wrapper only updating modified halves</a>.
     */
    // TODO: I would like to bring this test over, but we need snbt test structure support first
    // @GameTest(template = "fabric-transfer-api-v1-testmod:double_chest_comparators")
    // @TestHolder
    public static void testDoubleChestComparator(ExtendedGameTestHelper helper) {
        BlockPos chestPos = new BlockPos(2, 1, 2);
        // TODO: use ResourceHandler capability
        ResourceHandler<ItemResource> handler = EmptyResourceHandler.instance(); // helper.requireCapability(Capabilities.ItemHandler.BLOCK, chestPos, Direction.UP);

        // Insert one item
        try (Transaction tx = Transaction.openRoot()) {
            helper.assertTrue(handler.insert(ItemResource.of(Items.DIAMOND), 1, tx) == 1, "Diamond should have been inserted");
            tx.commit();
        }

        // Check that the container and the handler match
        Container container = HopperBlockEntity.getContainerAt(helper.getLevel(), helper.absolutePos(chestPos));
        helper.assertTrue(container != null, "Container must not be null");

        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack stack = container.getItem(i);
            ItemResource resource = ItemResource.of(stack);
            helper.assertTrue(resource.matches(stack), "Item resource in slot " + i + " must match stack");
            int expectedCount = stack.getCount();
            int actualCount = handler.getAmountAsInt(i);
            helper.assertValueEqual(expectedCount, actualCount, "slot " + i + " item count");
        }

        // Check that an update is queued for every single comparator
        AtomicInteger comparatorCount = new AtomicInteger();

        helper.forEveryBlockInStructure(relativePos -> {
            if (helper.getBlockState(relativePos).getBlock() != Blocks.COMPARATOR) {
                return;
            }

            comparatorCount.incrementAndGet();

            if (!helper.getLevel().getBlockTicks().hasScheduledTick(helper.absolutePos(relativePos), Blocks.COMPARATOR)) {
                throw helper.assertionException("Comparator at " + relativePos + " should have an update scheduled");
            }
        });

        helper.assertTrue(comparatorCount.intValue() == 6, "Expected exactly 6 comparators");

        helper.succeed();
    }

    /**
     * Regression test for <a href="https://github.com/FabricMC/fabric/issues/3017">composters not always incrementing their level on the first insert</a>.
     */
    @GameTest
    @EmptyTemplate
    @TestHolder
    public static void testComposterFirstInsert(ExtendedGameTestHelper helper) {
        BlockPos pos = new BlockPos(0, 1, 0);

        ItemResource carrot = ItemResource.of(Items.CARROT);

        for (int i = 0; i < 200; ++i) { // Run many times as this can be random.
            helper.setBlock(pos, Blocks.COMPOSTER.defaultBlockState());
            var wrapper = ComposterWrapper.get(helper.getLevel(), helper.absolutePos(pos), Direction.UP);

            try (Transaction tx = Transaction.openRoot()) {
                if (wrapper.insert(carrot, 1, tx) != 1) {
                    helper.fail("Carrot should have been inserted", pos);
                }

                tx.commit();
            }

            helper.assertBlockState(pos, state -> state.getValue(ComposterBlock.LEVEL) == 1, s -> Component.literal("Composter should have level 1: " + s));
        }

        helper.succeed();
    }

    /**
     * Regression test for <a href="https://github.com/FabricMC/fabric/issues/3485">jukeboxes having their state changed mid-transaction</a>.
     */
    @GameTest
    @EmptyTemplate
    @TestHolder
    public static void testJukeboxState(ExtendedGameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, Blocks.JUKEBOX.defaultBlockState());
        var resourceHandler = VanillaContainerWrapper.of(helper.getBlockEntity(pos, JukeboxBlockEntity.class));

        try (Transaction tx = Transaction.openRoot()) {
            resourceHandler.insert(ItemResource.of(Items.MUSIC_DISC_11), 1, tx);
            helper.assertBlockState(pos, state -> !state.getValue(JukeboxBlock.HAS_RECORD), b -> Component.literal("Jukebox should not have its state changed mid-transaction"));
            tx.commit();
        }

        helper.assertBlockState(pos, state -> state.getValue(JukeboxBlock.HAS_RECORD), b -> Component.literal("Jukebox should have its state changed"));
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate("5x5x5")
    @TestHolder(description = "Horse armor wrapper defers notifications and doesn't allow insertion of non-armor items")
    public static void testHorseArmorWrapper(DynamicTest test) {
        AtomicInteger equipEvents = new AtomicInteger();
        AtomicInteger unequipEvents = new AtomicInteger();
        test.whenEnabled(listeners -> listeners.forge().addListener((VanillaGameEvent event) -> {
            if (event.getVanillaEvent() == GameEvent.EQUIP) {
                equipEvents.incrementAndGet();
            }
            if (event.getVanillaEvent() == GameEvent.UNEQUIP) {
                unequipEvents.incrementAndGet();
            }
        }));

        test.onGameTest(helper -> {
            var horse = helper.spawnWithNoFreeWill(EntityType.HORSE, new BlockPos(2, 2, 2));
            var wrapper = LivingEntityEquipmentWrapper.of(horse, EquipmentSlot.Type.ANIMAL_ARMOR);

            equipEvents.setPlain(0);
            unequipEvents.setPlain(0);

            // Wait 1 tick such that the entity will start emitting (un)equip events
            helper.runAtTickTime(1, () -> {
                try (var tx = Transaction.openRoot()) {
                    // Check that non-armor items can't be inserted
                    if (wrapper.insert(ItemResource.of(Items.DIAMOND_PICKAXE), 1, tx) != 0) {
                        helper.fail("Should have rejected diamond pickaxe as horse armor");
                    }

                    if (wrapper.insert(ItemResource.of(Items.DIAMOND_HORSE_ARMOR), 1, tx) != 1) {
                        helper.fail("Should have inserted 1 diamond horse armor");
                    }
                    // No events yet - in case the insertion is rolled back
                    helper.assertValueEqual(0, equipEvents.getPlain(), "equip event count");
                    helper.assertValueEqual(0, unequipEvents.getPlain(), "unequip event count");

                    // Once we commit, the equip event should fire
                    tx.commit();
                    helper.assertValueEqual(1, equipEvents.getPlain(), "equip event count");
                    helper.assertValueEqual(0, unequipEvents.getPlain(), "unequip event count");
                }

                try (var tx = Transaction.openRoot()) {
                    if (wrapper.extract(ItemResource.of(Items.DIAMOND_HORSE_ARMOR), 1, tx) != 1) {
                        helper.fail("Should have extracted 1 diamond horse armor");
                    }
                    // No additional events yet - in case the extraction is rolled back
                    helper.assertValueEqual(1, equipEvents.getPlain(), "equip event count");
                    helper.assertValueEqual(0, unequipEvents.getPlain(), "unequip event count");

                    // Once we commit, the unequip event should fire
                    tx.commit();
                    helper.assertValueEqual(1, equipEvents.getPlain(), "equip event count");
                    helper.assertValueEqual(1, unequipEvents.getPlain(), "unequip event count");
                }

                helper.succeed();
            });
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Player armor wrapper only allows equippable items, and checks for curse of binding.")
    public static void testPlayerArmorWrapper(ExtendedGameTestHelper helper) {
        var creativePlayer = helper.makeMockPlayer();
        var chestWrapper = PlayerInventoryWrapper.of(creativePlayer).getArmorSlot(EquipmentSlot.CHEST);

        var diamondChestplate = ItemResource.of(Items.DIAMOND_CHESTPLATE);
        var curseOfBinding = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        curseOfBinding.set(helper.getHolder(Enchantments.BINDING_CURSE), 1);
        var cursedDiamondChestplate = diamondChestplate.with(DataComponents.ENCHANTMENTS, curseOfBinding.toImmutable());

        try (var tx = Transaction.openRoot()) {
            if (chestWrapper.insert(ItemResource.of(Items.DIAMOND_PICKAXE), 1, tx) != 0) {
                helper.fail("Should have rejected diamond pickaxe as player armor");
            }
            if (chestWrapper.insert(diamondChestplate, 1, tx) != 1) {
                helper.fail("Should have inserted 1 diamond chestplate");
            }
            if (chestWrapper.extract(diamondChestplate, 1, tx) != 1) {
                helper.fail("Should have extracted 1 diamond chestplate");
            }
            if (chestWrapper.insert(cursedDiamondChestplate, 1, tx) != 1) {
                helper.fail("Should have inserted 1 cursed diamond chestplate");
            }
            // Extraction of the cursed chestplate is allowed in creative mode
            if (chestWrapper.extract(cursedDiamondChestplate, 1, tx) != 1) {
                helper.fail("Should have extracted 1 cursed diamond chestplate");
            }
        }

        var survivalPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
        var survivalChestWrapper = PlayerInventoryWrapper.of(survivalPlayer).getArmorSlot(EquipmentSlot.CHEST);

        try (var tx = Transaction.openRoot()) {
            if (survivalChestWrapper.insert(cursedDiamondChestplate, 1, tx) != 1) {
                helper.fail("Should have inserted 1 cursed diamond chestplate");
            }
            // Extraction of the cursed chestplate is disallowed in survival mode
            if (survivalChestWrapper.extract(cursedDiamondChestplate, 1, tx) != 0) {
                helper.fail("Should have not been able to extract 1 cursed diamond chestplate");
            }
        }

        helper.succeed();
    }
}
