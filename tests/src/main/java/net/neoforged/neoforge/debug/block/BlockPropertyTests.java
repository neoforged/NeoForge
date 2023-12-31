/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import net.neoforged.neoforge.eventtest.internal.TestsMod;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.RegisterStructureTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = BlockTests.GROUP + ".properties")
public class BlockPropertyTests {
    private static final String TEMPLATE_3x4_BOX = "neotests_level_sensitive_light:light_box_3x3:";

    @RegisterStructureTemplate(TEMPLATE_3x4_BOX)
    public static final StructureTemplate TEMPLATE3x4 = StructureTemplateBuilder.withSize(3, 4, 3)
            .fill(0, 0, 0, 2, 3, 2, Blocks.STONE)
            .set(1, 1, 1, Blocks.AIR.defaultBlockState())
            .set(1, 2, 1, Blocks.AIR.defaultBlockState())
            .build();

    @GameTest(template = TEMPLATE_3x4_BOX)
    @TestHolder(description = "Adds a toggleable light source to test if level-sensitive light emission works")
    static void levelSensitiveLight(final DynamicTest test, final RegistrationHelper reg) {
        final var lightBlock = reg.blocks().registerBlockWithBEType("light_block", LightBlock::new, LightBlockEntity::new, BlockBehaviour.Properties.of())
                .withLang("Light block").withBlockItem();

        BlockPos lightPos = new BlockPos(1, 2, 1);
        BlockPos testPos = new BlockPos(1, 3, 1);

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.setBlock(lightPos, lightBlock.get()))
                .thenExecute(() -> helper.useBlock(lightPos, helper.makeMockPlayer(), Items.ACACIA_BUTTON.getDefaultInstance()))
                .thenMap(() -> helper.getLevel().getChunkAt(helper.absolutePos(testPos)))
                .thenMap(chunk -> ((ThreadedLevelLightEngine) helper.getLevel().getLightEngine()).waitForPendingTasks(chunk.getPos().x, chunk.getPos().z))
                .thenWaitUntil(future -> helper.assertTrue(future.isDone(), "Light engine did not update to lit"))
                .thenExecute(() -> helper.assertTrue(helper.getLevel().getLightEngine().getRawBrightness(helper.absolutePos(testPos), 15) == 14, "Lit light level was not as expected"))
                .thenExecute(() -> helper.destroyBlock(lightPos))
                .thenMap(() -> helper.getLevel().getChunkAt(helper.absolutePos(new BlockPos(1, 3, 1))))
                .thenMap(chunk -> ((ThreadedLevelLightEngine) helper.getLevel().getLightEngine()).waitForPendingTasks(chunk.getPos().x, chunk.getPos().z))
                .thenWaitUntil(future -> helper.assertTrue(future.isDone(), "Light engine did not update to unlit"))
                .thenExecute(() -> helper.assertTrue(helper.getLevel().getLightEngine().getRawBrightness(helper.absolutePos(testPos), 15) == 0, "Unlit light level was not as expected"))
                .thenSucceed());
    }

    @GameTest(template = TestsMod.TEMPLATE_9x9)
    @TestHolder(description = "Adds a block whose resistance is based on a state property")
    static void explosionResistance(final DynamicTest test, final RegistrationHelper reg) {
        final var resistantBlock = reg.blocks().register("resistant_block", () -> new Block(BlockBehaviour.Properties.of()) {
            {
                this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.AGE_7, Integer.valueOf(0)));
            }

            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                builder.add(BlockStateProperties.AGE_7);
            }

            @Override
            public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
                return state.getValue(BlockStateProperties.AGE_7) >= 5 ? 1000f : 0.25f;
            }
        }).withBlockItem().withLang("Resistant Block").withDefaultWhiteModel();

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.setBlock(new BlockPos(4, 6, 4), resistantBlock.get().defaultBlockState().setValue(BlockStateProperties.AGE_7, 5)))
                .thenExecute(() -> helper.setBlock(new BlockPos(4, 4, 7), resistantBlock.get().defaultBlockState().setValue(BlockStateProperties.AGE_7, 3)))
                .thenExecuteAfter(2, () -> helper.getLevel().explode(null, null, null, helper.absoluteVec(new BlockPos(4, 5, 4).getCenter()), 4, false, Level.ExplosionInteraction.BLOCK))

                .thenIdle(15)

                .thenExecute(() -> helper.assertBlockPresent(resistantBlock.get(), new BlockPos(4, 6, 4)))
                .thenExecute(() -> helper.assertBlockNotPresent(resistantBlock.get(), new BlockPos(4, 4, 7)))

                .thenSucceed());
    }

    private static class LightBlock extends Block implements EntityBlock {
        private final Supplier<BlockEntityType<LightBlockEntity>> beType;

        public LightBlock(Properties properties, Supplier<BlockEntityType<LightBlockEntity>> beType) {
            super(properties);
            this.beType = beType;
        }

        @Override
        @SuppressWarnings("deprecation")
        public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof LightBlockEntity be) {
                be.switchLight();
            }
            return InteractionResult.SUCCESS;
        }

        @Override
        public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
            AuxiliaryLightManager lightManager = level.getAuxLightManager(pos);
            if (lightManager != null) {
                return lightManager.getLightAt(pos);
            }
            return 0;
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new LightBlockEntity(beType.get(), pos, state);
        }
    }

    private static class LightBlockEntity extends BlockEntity {
        private boolean lit = false;

        public LightBlockEntity(BlockEntityType<?> beType, BlockPos pos, BlockState state) {
            super(beType, pos, state);
        }

        public void switchLight() {
            setLit(!lit);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            setChanged();
        }

        private void setLit(boolean lit) {
            if (lit != this.lit) {
                this.lit = lit;
                AuxiliaryLightManager lightManager = level.getAuxLightManager(worldPosition);
                if (lightManager != null) {
                    lightManager.setLightAt(worldPosition, lit ? 15 : 0);
                    level.getLightEngine().checkBlock(worldPosition);
                }
            }
        }

        @Override
        public Packet<ClientGamePacketListener> getUpdatePacket() {
            return ClientboundBlockEntityDataPacket.create(this);
        }

        @Override
        public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
            CompoundTag tag = pkt.getTag();
            if (tag != null) {
                setLit(tag.getBoolean("lit"));
            }
        }

        @Override
        public CompoundTag getUpdateTag() {
            CompoundTag tag = super.getUpdateTag();
            tag.putBoolean("lit", lit);
            return tag;
        }

        @Override
        public void handleUpdateTag(CompoundTag tag) {
            super.handleUpdateTag(tag);
            lit = tag.getBoolean("lit");
        }

        @Override
        public void load(CompoundTag tag) {
            super.load(tag);
            lit = tag.getBoolean("lit");
        }

        @Override
        protected void saveAdditional(CompoundTag tag) {
            super.saveAdditional(tag);
            tag.putBoolean("lit", lit);
        }
    }
}
