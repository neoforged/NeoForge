package net.neoforged.neoforge.debug.handlers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.handlers.templates.EmptyHandler;
import net.neoforged.neoforge.transfer.handlers.templates.InfiniteResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.VoidResourceHandler;
import net.neoforged.neoforge.transfer.items.ItemResource;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.registration.DeferredBlocks;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "handlers.resource", idPrefix = "testResourceHandler.")
public class ResourceHandlerTests {
    private static final RegistrationHelper HELPER = RegistrationHelper.create("resource_handler_tests");
    private static final DeferredBlocks BLOCKS = HELPER.blocks();
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = HELPER.registrar(Registries.BLOCK_ENTITY_TYPE);

    private static final DeferredBlock<Block> RESOURCE_BLOCK = BLOCKS.registerBlock(
            "resource_block",
            ResourceBlockExample::new,
            BlockBehaviour.Properties.of()
    );

    private static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResourceBlockExample.Entity>> RESOURCE_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "energy",
            () -> new BlockEntityType<>(ResourceBlockExample.Entity::new, RESOURCE_BLOCK.get())
    );

    @OnInit
    static void init(final TestFramework framework) {
        var bus = framework.modEventBus();
        BLOCKS.register(bus);
        BLOCK_ENTITIES.register(bus);

        bus.<RegisterCapabilitiesEvent>addListener(e -> e.registerBlockEntity(
                Capabilities.EnergyHandler.BLOCK, RESOURCE_BLOCK_ENTITY.value(), (blockEntity, context) -> {
                    if (context == null) return null;
                    return null;
                }
        ));
    }

    private static BlockPos setupLevelEnvironment(ExtendedGameTestHelper helper) {
        var blockPos = new BlockPos(1, 1, 1);
        helper.setBlock(blockPos, RESOURCE_BLOCK.value());
        return blockPos;
    }

    /**
     * Uses {@link EmptyHandler}, {@link VoidResourceHandler},
     */
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests baseline IResourceHandler instances.")
    public static void baseline(ExtendedGameTestHelper helper) {
        setupLevelEnvironment(helper);

        //EMPTY no operation handlers
        testEmptyHandler(helper, EmptyHandler.ITEM);
        testEmptyHandler(helper, EmptyHandler.FLUID);


        //VoidResourceHandlers destroys resources but doesn't allow extraction
        testVoidResource(helper, VoidResourceHandler.ITEM, ItemResource.NONE);
        testVoidResource(helper, VoidResourceHandler.FLUID, FluidResource.NONE);

        //InfiniteResourceHandlers creates infinite of a specified resource, but doesn't allow insertion
        testEndlessResource(helper, FluidResource.of(Fluids.WATER));
        testEndlessResource(helper, ItemResource.of(Blocks.COBBLESTONE));

        helper.succeed();
    }


    private static <T extends  IResource> void testVoidResource(ExtendedGameTestHelper helper, VoidResourceHandler<T> handler, T emptyResource){
        helper.assertValueEqual(handler.size(), 1, "Size should be");
        helper.assertFalse(handler.allowsExtraction(), "Extraction should be not allowed");
        helper.assertFalse(handler.allowsExtraction(0), "Extraction should be not allowed");
        helper.assertFalse(handler.allowsExtraction(1337), "Extraction should be not allowed");

        helper.assertTrue(handler.allowsInsertion(), "Insertion should be allowed");
        helper.assertTrue(handler.allowsInsertion(0), "Insertion should be allowed");
        helper.assertTrue(handler.allowsInsertion(1337), "Insertion should be allowed");

        helper.assertTrue(handler.isValid(emptyResource), "Every resource should match");

        helper.assertValueEqual(handler.getCapacity(0, emptyResource), ResourceHandlerUtil.PRETTY_MAX_INT, "Capacity should match");
        helper.assertValueEqual(handler.getCapacity(0), ResourceHandlerUtil.PRETTY_MAX_INT, "Capacity should match");
        helper.assertValueEqual(handler.getCapacity(1), ResourceHandlerUtil.PRETTY_MAX_INT, "Capacity should match");

        helper.assertValueEqual(handler.getResource(0), emptyResource, "Resource should match");
        helper.assertValueEqual(handler.getResource(1), emptyResource, "Resource should match");

        helper.assertValueEqual(handler.insert(0, emptyResource, ResourceHandlerUtil.PRETTY_MAX_INT, TransferAction.EXECUTE), ResourceHandlerUtil.PRETTY_MAX_INT, "Insertion should match");
        helper.assertValueEqual(handler.insert(emptyResource, ResourceHandlerUtil.PRETTY_MAX_INT, TransferAction.EXECUTE), ResourceHandlerUtil.PRETTY_MAX_INT, "Insertion should match");

        helper.assertValueEqual(handler.extract(0, emptyResource, 1, TransferAction.EXECUTE), 0, "Extraction should match");
        helper.assertValueEqual(handler.extract(emptyResource, 1, TransferAction.EXECUTE), 0, "Extraction should match");
    }

    private static <T extends  IResource> void testEndlessResource(ExtendedGameTestHelper helper,T resource){
        InfiniteResourceHandler<T> handler = new InfiniteResourceHandler<>(resource);
        helper.assertValueEqual(handler.size(), 1, "Size should be");
        helper.assertTrue(handler.allowsExtraction(), "Extraction should be allowed");
        helper.assertTrue(handler.allowsExtraction(0), "Extraction should be allowed");
        helper.assertTrue(handler.allowsExtraction(1337), "Extraction should be allowed");

        helper.assertFalse(handler.allowsInsertion(), "Insertion should not be allowed");
        helper.assertFalse(handler.allowsInsertion(0), "Insertion should not be allowed");
        helper.assertFalse(handler.allowsInsertion(1337), "Insertion should not be allowed");

        helper.assertTrue(handler.isValid(resource), "Resource should match");

        helper.assertValueEqual(handler.getCapacity(0, resource), ResourceHandlerUtil.PRETTY_MAX_INT, "Capacity should match");
        helper.assertValueEqual(handler.getCapacity(0), ResourceHandlerUtil.PRETTY_MAX_INT, "Capacity should match");
        helper.assertValueEqual(handler.getCapacity(1), ResourceHandlerUtil.PRETTY_MAX_INT, "Capacity should match");

        helper.assertValueEqual(handler.getResource(0), resource, "Resource should match");
        helper.assertValueEqual(handler.getResource(1), resource, "Resource should match");

        helper.assertValueEqual(handler.insert(0, resource, 1, TransferAction.EXECUTE), 0, "Insertion should match");
        helper.assertValueEqual(handler.insert(resource, 1, TransferAction.EXECUTE), 0, "Insertion should match");

        helper.assertValueEqual(handler.extract(0, resource, 1, TransferAction.EXECUTE), 1, "Extraction should match");
        helper.assertValueEqual(handler.extract(resource, 1, TransferAction.EXECUTE), 1, "Extraction should match");
    }

    private static <T extends IResource> void testEmptyHandler(ExtendedGameTestHelper helper, EmptyHandler<T> handler) {
        T emptyResource = handler.emptyResource();

        helper.assertValueEqual(handler.size(), 0, "Empty should no-op");
        helper.assertFalse(handler.allowsExtraction(), "Empty should no-op");
        helper.assertFalse(handler.allowsExtraction(0), "Empty should no-op");
        helper.assertFalse(handler.allowsInsertion(), "Empty should no-op");
        helper.assertFalse(handler.allowsInsertion(0), "Empty should no-op");
        helper.assertFalse(handler.isValid(emptyResource), "Empty should no-op");
        helper.assertFalse(handler.isValid(0, emptyResource), "Empty should no-op, but should return empty");
        helper.assertValueEqual(handler.getCapacity(0, emptyResource), 0, "Empty should no-op");
        helper.assertValueEqual(handler.getCapacity(0), 0, "Empty should no-op");
        helper.assertValueEqual(handler.getAmount(0), 0, "Empty should no-op");
        helper.assertValueEqual(handler.getResource(0), emptyResource, "Empty should no-op, but should return empty");
        helper.assertValueEqual(handler.insert(0, emptyResource, 1, TransferAction.SIMULATE), 0, "Empty should no-op");
        helper.assertValueEqual(handler.insert(0, emptyResource, 1, TransferAction.EXECUTE), 0, "Empty should no-op");
        helper.assertValueEqual(handler.insert(emptyResource, 1, TransferAction.SIMULATE), 0, "Empty should no-op");
        helper.assertValueEqual(handler.insert(emptyResource, 1, TransferAction.EXECUTE), 0, "Empty should no-op");
        helper.assertValueEqual(handler.extract(0, emptyResource, 1, TransferAction.SIMULATE), 0, "Empty should no-op");
        helper.assertValueEqual(handler.extract(0, emptyResource, 1, TransferAction.EXECUTE), 0, "Empty should no-op");
        helper.assertValueEqual(handler.extract(emptyResource, 1, TransferAction.SIMULATE), 0, "Empty should no-op");
        helper.assertValueEqual(handler.extract(emptyResource, 1, TransferAction.EXECUTE), 0, "Empty should no-op");
    }


    private static class ResourceBlockExample extends Block implements EntityBlock {
        public ResourceBlockExample(Properties properties) {
            super(properties);
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new Entity(pos, state);
        }

        private static class Entity extends BlockEntity {
            //Do we want to pull in the builder I made for energy buffers for simple resource handlers?
            //                private final IEnergyHandler simpleEnergyBuffer = EnergyBuffer.Builder.create(1, MAX_CAPACITY) // We create the handler with 1 buffer. We specify instead of making an override with just capacity to ensure user intent when creating.
            //                        .energy(MAX_CAPACITY)
            //                        .maxExtractRate(MAX_CAPACITY)
            //                        .maxInsertRate(MAX_INSERTION)
            //                        .build();
            //                private final IEnergyHandler complexEnergyBuffer = EnergyBuffer.Builder.create(3, MAX_CAPACITY) // Creates 3 unique sub-buffers all with the aforementioned max capacity
            //                        .maxInsertRate(MAX_INSERTION) // This implementation only allows one max insertion value shared across all sub-buffers
            //                        .maxExtractRate(0) // Disallows extraction
            //                        .energy(1, 100) // Sets the buffer at index 1 initial energy value to 100. Buffers 0 & 2 are still 0
            //                        .build();

            public Entity(BlockPos pos, BlockState state) {
                super(RESOURCE_BLOCK_ENTITY.get(), pos, state);
            }
        }
    }
}
