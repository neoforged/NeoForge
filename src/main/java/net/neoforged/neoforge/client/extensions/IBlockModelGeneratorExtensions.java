/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public interface IBlockModelGeneratorExtensions {
    default void createButton(Block button, Block fullBlock) {
        var textures = TextureMapping.cube(fullBlock);
        var depressedModel = ModelTemplates.BUTTON.create(button, textures, self().modelOutput);
        var pressedModel = ModelTemplates.BUTTON_PRESSED.create(button, textures, self().modelOutput);
        var inventoryModel = ModelTemplates.BUTTON_INVENTORY.create(button, textures, self().modelOutput);
        self().blockStateOutput.accept(BlockModelGenerators.createButton(button, depressedModel, pressedModel));
        self().registerSimpleItemModel(button, inventoryModel);
    }

    default void createWall(Block wall, Block fullBlock) {
        var textures = TextureMapping.cube(fullBlock);
        var postModel = ModelTemplates.WALL_POST.create(wall, textures, self().modelOutput);
        var lowSideModel = ModelTemplates.WALL_LOW_SIDE.create(wall, textures, self().modelOutput);
        var tallSideModel = ModelTemplates.WALL_TALL_SIDE.create(wall, textures, self().modelOutput);
        var inventoryModel = ModelTemplates.WALL_INVENTORY.create(wall, textures, self().modelOutput);
        self().blockStateOutput.accept(BlockModelGenerators.createWall(wall, postModel, lowSideModel, tallSideModel));
        self().registerSimpleItemModel(wall, inventoryModel);
    }

    default void createCustomFence(Block fence) {
        var textures = TextureMapping.customParticle(fence);
        var postModel = ModelTemplates.CUSTOM_FENCE_POST.create(fence, textures, self().modelOutput);
        var sideNorthModel = ModelTemplates.CUSTOM_FENCE_SIDE_NORTH.create(fence, textures, self().modelOutput);
        var sideEastModel = ModelTemplates.CUSTOM_FENCE_SIDE_EAST.create(fence, textures, self().modelOutput);
        var sideSouthModel = ModelTemplates.CUSTOM_FENCE_SIDE_SOUTH.create(fence, textures, self().modelOutput);
        var sideWestModel = ModelTemplates.CUSTOM_FENCE_SIDE_WEST.create(fence, textures, self().modelOutput);
        var inventoryModel = ModelTemplates.CUSTOM_FENCE_INVENTORY.create(fence, textures, self().modelOutput);
        self().blockStateOutput.accept(BlockModelGenerators.createCustomFence(fence, postModel, sideNorthModel, sideEastModel, sideSouthModel, sideWestModel));
        self().registerSimpleItemModel(fence, inventoryModel);
    }

    default void createFence(Block fence, Block fullBlock) {
        var textures = TextureMapping.cube(fullBlock);
        var postModel = ModelTemplates.FENCE_POST.create(fence, textures, self().modelOutput);
        var sideModel = ModelTemplates.FENCE_SIDE.create(fence, textures, self().modelOutput);
        var inventoryModel = ModelTemplates.FENCE_INVENTORY.create(fence, textures, self().modelOutput);
        self().blockStateOutput.accept(BlockModelGenerators.createFence(fence, postModel, sideModel));
        self().registerSimpleItemModel(fence, inventoryModel);
    }

    default void createCustomFenceGate(Block fenceGate) {
        var textures = TextureMapping.customParticle(fenceGate);
        var gateOpenModel = ModelTemplates.CUSTOM_FENCE_GATE_OPEN.create(fenceGate, textures, self().modelOutput);
        var gateClosedModel = ModelTemplates.CUSTOM_FENCE_GATE_CLOSED.create(fenceGate, textures, self().modelOutput);
        var wallOpenModel = ModelTemplates.CUSTOM_FENCE_GATE_WALL_OPEN.create(fenceGate, textures, self().modelOutput);
        var wallClosedModel = ModelTemplates.CUSTOM_FENCE_GATE_WALL_CLOSED.create(fenceGate, textures, self().modelOutput);
        self().blockStateOutput.accept(BlockModelGenerators.createFenceGate(fenceGate, gateOpenModel, gateClosedModel, wallOpenModel, wallClosedModel, false));
    }

    default void createFenceGate(Block fenceGate, Block fullBlock) {
        var textures = TextureMapping.cube(fullBlock);
        var gateOpenModel = ModelTemplates.FENCE_GATE_OPEN.create(fenceGate, textures, self().modelOutput);
        var gateClosedModel = ModelTemplates.FENCE_GATE_CLOSED.create(fenceGate, textures, self().modelOutput);
        var wallOpenModel = ModelTemplates.FENCE_GATE_WALL_OPEN.create(fenceGate, textures, self().modelOutput);
        var wallClosedModel = ModelTemplates.FENCE_GATE_WALL_CLOSED.create(fenceGate, textures, self().modelOutput);
        self().blockStateOutput.accept(BlockModelGenerators.createFenceGate(fenceGate, gateOpenModel, gateClosedModel, wallOpenModel, wallClosedModel, true));
    }

    default void createPressurePlate(Block pressurePlate, Block fullBlock) {
        var textures = TextureMapping.cube(fullBlock);
        var upModel = ModelTemplates.PRESSURE_PLATE_UP.create(pressurePlate, textures, self().modelOutput);
        var downModel = ModelTemplates.PRESSURE_PLATE_DOWN.create(pressurePlate, textures, self().modelOutput);
        self().blockStateOutput.accept(BlockModelGenerators.createPressurePlate(pressurePlate, upModel, downModel));
    }

    default void createSign(Block sign, Block wallSign, Block fullBlock) {
        var textures = TextureMapping.cube(fullBlock);
        var particle = ModelTemplates.PARTICLE_ONLY.create(sign, textures, self().modelOutput);
        self().blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(sign, particle));
        self().blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(wallSign, particle));
        self().registerSimpleFlatItemModel(sign.asItem());
    }

    default void createSlab(Block slab, Block fullBlock) {
        var textures = TextureMapping.cube(fullBlock);
        var slabBottom = ModelTemplates.SLAB_BOTTOM.create(slab, textures, self().modelOutput);
        var slabTop = ModelTemplates.SLAB_TOP.create(slab, textures, self().modelOutput);
        self().blockStateOutput.accept(BlockModelGenerators.createSlab(slab, slabBottom, slabTop, ModelLocationUtils.getModelLocation(fullBlock)));
        self().registerSimpleItemModel(slab, slabBottom);
    }

    default void createStairs(Block stairs, Block fullBlock) {
        var textures = TextureMapping.cube(fullBlock);
        var stairsInner = ModelTemplates.STAIRS_INNER.create(stairs, textures, self().modelOutput);
        var stairsStraight = ModelTemplates.STAIRS_STRAIGHT.create(stairs, textures, self().modelOutput);
        var stairsOuter = ModelTemplates.STAIRS_OUTER.create(stairs, textures, self().modelOutput);
        self().blockStateOutput.accept(BlockModelGenerators.createStairs(stairs, stairsInner, stairsStraight, stairsOuter));
        self().registerSimpleItemModel(stairs, stairsStraight);
    }

    default void createScaffolding(Block scaffolding) {
        var stableModel = ModelLocationUtils.getModelLocation(scaffolding, "_stable");
        var unstableModel = ModelLocationUtils.getModelLocation(scaffolding, "_unstable");
        self().registerSimpleItemModel(scaffolding, stableModel);
        self().blockStateOutput.accept(MultiVariantGenerator.multiVariant(scaffolding).with(BlockModelGenerators.createBooleanModelDispatch(BlockStateProperties.BOTTOM, unstableModel, stableModel)));
    }

    private BlockModelGenerators self() {
        return (BlockModelGenerators) this;
    }
}
