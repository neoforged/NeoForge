/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.misc;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.energy.templates.AttachmentEnergyStorage;
import net.neoforged.neoforge.transfer.energy.IEnergyHandler;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

@Mod(GameTestTest.MODID)
public class GameTestTest {
    public static final String MODID = "gametest_test";
    public static final boolean ENABLED = true;
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);
    private static final DeferredBlock<Block> ENERGY_BLOCK = BLOCKS.register("energy_block",
            () -> new EnergyBlock(Properties.of().mapColor(MapColor.STONE)));
    @SuppressWarnings("unused")
    private static final DeferredItem<BlockItem> ENERGY_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(ENERGY_BLOCK);
    private static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyBlockEntity>> ENERGY_BLOCK_ENTITY = BLOCK_ENTITIES.register("energy",
            () -> BlockEntityType.Builder.of(EnergyBlockEntity::new, ENERGY_BLOCK.get()).build(null));

    public GameTestTest(IEventBus modBus) {
        if (ENABLED) {
            modBus.register(this);

            BLOCKS.register(modBus);
            ITEMS.register(modBus);
            BLOCK_ENTITIES.register(modBus);
            modBus.addListener(this::addCreative);
            modBus.addListener(this::registerCaps);
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
            event.accept(ENERGY_BLOCK_ITEM);
    }

    private void registerCaps(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ENERGY_BLOCK_ENTITY.get(), (be, side) -> be.energyStorage);
    }

    @SubscribeEvent
    public void onRegisterGameTests(RegisterGameTestsEvent event) {
        // Registers all methods in this class annotated with @GameTest or @GameTestGenerator to the GameTestRegistry
        event.register(this.getClass());
    }

    /**
     * An example game test
     * <p><ul>
     * <li>Must take <b>one</b> parameter, the {@link GameTestHelper}</li>
     * <li>The return type is ignored, so it should be {@code void}</li>
     * <li>Can be {@code static} or non-static<p>
     * WARNING: If made non-static, then it will create an instance of the class every time it is run.</li>
     * </ul>
     * The default template name converts the containing class's name to all lowercase, and the method name to all lowercase.
     * In this example, the structure name would be "gametesttest.testwood" under the "gametest_test" namespace.
     */
    @GameTest(templateNamespace = MODID)
    public static void testWood(GameTestHelper helper) {
        // The woodPos is in the bottom center of the 3x3x3 structure
        BlockPos woodPos = new BlockPos(1, 1, 1);

        // assertBlockState will convert the relative woodPos into a real world block position and check it with the predicate.
        // Relative positions are made absolute by adding their value to the block position of the structure tile entity,
        // which is always the lowest northwest corner inside the structure.
        // If the predicate fails, the String supplier will be used to construct an exception message, which is thrown
        helper.assertBlockState(woodPos, b -> b.is(Blocks.OAK_LOG), () -> "Block was not an oak log");

        // If we got to this point, the assert succeeded, so the test has succeeded.
        // Game tests require explicitly declaring success, otherwise they fail from timeout.
        helper.succeed();
    }

    /**
     * An example game test generator.
     * <p>
     * A <b>game test generator</b> generates a collection of test functions.
     * It is called immediately when registered to GameTestRegistry.
     * <p><ul>
     * <li>Must return {@code Collection<TestFunction>} (or a subclass)</li>
     * <li>Must take no parameters</li>
     * <li>Can be {@code static} or non-static</li>
     * <p>
     * WARNING: If made non-static, then it will create an instance of the class every time it is run.</li>
     * </ul>
     */
    @GameTestGenerator
    public static List<TestFunction> generateTests() {
        // An example test function, run in the default batch, with the test name "teststone", and the structure name "gametesttest.teststone" under the "gametest_test" namespace.
        // No rotation, 100 ticks until the test times out if it does not fail or succeed, 0 ticks for setup time, and the actual code to run.
        TestFunction testStone = new TestFunction("defaultBatch", "teststone", ResourceLocation.fromNamespaceAndPath(MODID, "gametesttest.teststone").toString(), Rotation.NONE,
                100, 0, true,
                helper -> {
                    BlockPos stonePos = new BlockPos(1, 1, 1);

                    // This should always assert to true, since we set it then directly check it
                    helper.setBlock(stonePos, Blocks.STONE);
                    helper.assertBlockState(stonePos, b -> b.is(Blocks.STONE), () -> "Block was not stone");

                    helper.succeed();
                });
        return List.of(testStone);
    }

    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = MODID, template = "empty3x3x3")
    public static void testHopperPickup(GameTestHelper helper) {
        BlockPos hopperPos = new BlockPos(1, 1, 1);

        // Sets (1,1,1) to a hopper and spawns a golden apple one block above it
        helper.setBlock(hopperPos, Blocks.HOPPER);
        helper.spawnItem(Items.GOLDEN_APPLE, 1, 2, 1);

        // Waits 20 ticks before checking that the hopper contains the golden apple
        helper.assertAtTickTimeContainerContains(20, hopperPos, Items.GOLDEN_APPLE);

        // Succeeds at 21 ticks if the previous check didn't throw an exception
        helper.runAtTickTime(21, helper::succeed);
    }

    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = MODID, template = "empty3x3x3")
    public static void testEnergyStorage(GameTestHelper helper) {
        BlockPos energyPos = new BlockPos(1, 1, 1);

        // Sets (1,1,1) to our custom energy block
        helper.setBlock(energyPos, ENERGY_BLOCK.get());

        // Queries the energy capability
        IEnergyHandler energyStorage = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, helper.absolutePos(energyPos), null);
        if (energyStorage == null) {
            helper.fail("Expected energy storage", energyPos);
        }

        // Adds 2000 FE, but our energy storage can only hold 1000 FE
        energyStorage.insert(2000, false);

        // Fails test if stored energy is not equal to 1000 FE
        int energy = energyStorage.getAmount();
        int target = 1000;
        if (energy != target) {
            helper.fail("Expected energy=" + target + " but it was energy=" + energy, energyPos);
        }

        helper.succeed();
    }

    private static class EnergyBlock extends Block implements EntityBlock {
        public EnergyBlock(Properties properties) {
            super(properties);
        }

        @Nullable
        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new EnergyBlockEntity(pos, state);
        }
    }

    private static class EnergyBlockEntity extends BlockEntity {
        private final AttachmentEnergyStorage energyStorage = new AttachmentEnergyStorage(1000);

        public EnergyBlockEntity(BlockPos pos, BlockState state) {
            super(ENERGY_BLOCK_ENTITY.get(), pos, state);
        }
    }
}
