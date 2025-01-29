/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.handlers;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.handlers.templates.fluids.FluidStorageHandler;
import net.neoforged.neoforge.transfer.handlers.templates.items.ItemStorageHandler;
import net.neoforged.neoforge.transfer.handlers.templates.storage.ResourceStorageAttachment;
import net.neoforged.neoforge.transfer.handlers.templates.storage.ResourceStorageComponent;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.annotation.TestGroup;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.registration.DeferredBlocks;
import net.neoforged.testframework.registration.RegistrationHelper;

public record ResourceHandlerTestSetup() {
    @TestGroup(name = "Resource Handler Group", enabledByDefault = true)
    public static final String GROUP_ID = "handlers.resource";

    public static final int TANK_CAPACITY = 2 * FluidType.BUCKET_VOLUME;
    public static final int TANK_COUNT = 3;

    private interface Registry {
        RegistrationHelper HELPER = RegistrationHelper.create("resource_handler_tests");
        DeferredBlocks BLOCKS = HELPER.blocks();
        DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = HELPER.registrar(Registries.BLOCK_ENTITY_TYPE);
        DeferredRegister<AttachmentType<?>> ATTACHMENTS = HELPER.attachments();
        DeferredRegister<DataComponentType<?>> COMPONENTS = HELPER.registrar(Registries.DATA_COMPONENT_TYPE);
    }

    public interface Content {
        Content INSTANCE = new Content() {};
        DeferredBlock<Block> RESOURCE_BLOCK = Registry.BLOCKS.registerBlock(
                "resource_block",
                ResourceBlockExample::new);
        DeferredHolder<BlockEntityType<?>, BlockEntityType<ResourceBlockExample.Entity>> RESOURCE_BLOCK_ENTITY = Registry.BLOCK_ENTITIES.register(
                "resource_container",
                () -> new BlockEntityType<>(ResourceBlockExample.Entity::new, RESOURCE_BLOCK.get()));
        Supplier<AttachmentType<TestResourceContainerAttachment>> RESOURCE_ATTACHMENT = Registry.ATTACHMENTS.register("container", TestResourceContainerAttachment.BUILDER::build);
        Supplier<AttachmentType<ResourceStorageAttachment<FluidResource>>> FLUID_ATTACHMENT = Registry.ATTACHMENTS.register("fluid_container", AttachmentType.builder(() -> ResourceStorageAttachment.of(1, FluidResource.NONE))::build);

        DeferredHolder<DataComponentType<?>, DataComponentType<ResourceStack<FluidResource>>> SIMPLE_FLUID_CONTENT = Registry.COMPONENTS.register(
                "simple_fluid_content", () -> DataComponentType.<ResourceStack<FluidResource>>builder()
                        .persistent(ResourceStack.codec(FluidResource.OPTIONAL_CODEC))
                        .networkSynchronized(ResourceStack.streamCodec(FluidResource.STREAM_CODEC))
                        .build());

        DeferredHolder<DataComponentType<?>, DataComponentType<ResourceStorageComponent<FluidResource>>> FLUID_STORAGE_COMPONENT = Registry.COMPONENTS.register(
                "fluid_storage", () -> DataComponentType.<ResourceStorageComponent<FluidResource>>builder()
                        .persistent(ResourceStorageComponent.codec(FluidResource.OPTIONAL_CODEC))
                        .networkSynchronized(ResourceStorageComponent.streamCodec(ResourceStack.streamCodec(FluidResource.STREAM_CODEC)))
                        .build());

        DeferredHolder<DataComponentType<?>, DataComponentType<ResourceStorageComponent<ItemResource>>> ITEM_STORAGE_COMPONENT = Registry.COMPONENTS.register(
                "item_storage", () -> DataComponentType.<ResourceStorageComponent<ItemResource>>builder()
                        .persistent(ResourceStorageComponent.codec(ItemResource.OPTIONAL_CODEC))
                        .networkSynchronized(ResourceStorageComponent.streamCodec(ResourceStack.streamCodec(ItemResource.STREAM_CODEC)))
                        .build());
    }

    @OnInit
    static void init(final TestFramework framework) {
        var bus = framework.modEventBus();
        Registry.BLOCKS.register(bus);
        Registry.BLOCK_ENTITIES.register(bus);
        Registry.ATTACHMENTS.register(bus);
        Registry.COMPONENTS.register(bus);
        var dummy = Content.INSTANCE;

        bus.<RegisterCapabilitiesEvent>addListener(e -> e.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK, Content.RESOURCE_BLOCK_ENTITY.value(), (blockEntity, context) -> {
                    var data = blockEntity.getData(Content.RESOURCE_ATTACHMENT);
                    return switch (context) {
                        case UP -> data.both;
                        case NORTH -> data.input;
                        case SOUTH -> data.output;
                        case null, default -> null;
                    };
                }));

        bus.<RegisterCapabilitiesEvent>addListener(e -> e.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK, Content.RESOURCE_BLOCK_ENTITY.value(), (blockEntity, context) -> blockEntity.getData(Content.RESOURCE_ATTACHMENT).fluidHandler));

        bus.<RegisterCapabilitiesEvent>addListener(e -> e.registerItem(
                Capabilities.FluidHandler.ITEM, (object, context) -> new FluidStorageHandler.Component(context, ResourceHandlerTestSetup.Content.FLUID_STORAGE_COMPONENT.get(), TANK_COUNT, TANK_CAPACITY),
                Items.APPLE));

        bus.<RegisterCapabilitiesEvent>addListener(e -> e.registerItem(
                Capabilities.ItemHandler.ITEM, (object, context) -> new ItemStorageHandler.Component(context, ResourceHandlerTestSetup.Content.ITEM_STORAGE_COMPONENT.get(), 100),
                Items.APPLE));
    }

    public static BlockPos setupLevelEnvironment(ExtendedGameTestHelper helper) {
        var blockPos = helper.relativePos(BlockPos.ZERO);
        helper.setBlock(blockPos, Content.RESOURCE_BLOCK.value());
        return blockPos;
    }

    public static BlockPos setupLevelEnvironmentSecond(ExtendedGameTestHelper helper) {
        var blockPos = helper.relativePos(new BlockPos(1, 0, 0));
        helper.setBlock(blockPos, Content.RESOURCE_BLOCK.value());
        return blockPos;
    }

    public static class ResourceBlockExample extends Block implements EntityBlock {
        public ResourceBlockExample(Properties properties) {
            super(properties);
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new Entity(pos, state);
        }

        public static class Entity extends BlockEntity {
            public Entity(BlockPos pos, BlockState state) {
                super(Content.RESOURCE_BLOCK_ENTITY.get(), pos, state);
            }
        }
    }
}
