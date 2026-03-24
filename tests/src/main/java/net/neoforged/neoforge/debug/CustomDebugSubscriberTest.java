/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.CommonColors;
import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.AddDebugSubscriptionFlagsEvent;
import net.neoforged.neoforge.client.event.RegisterDebugRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "custom_debug_subscribers", side = Dist.CLIENT)
public interface CustomDebugSubscriberTest {
    @TestHolder(description = "Renders debug info for a new block entity using debug renderers and subscribers", enabledByDefault = true)
    static void testDebugSubscriptions(DynamicTest test, RegistrationHelper reg) {
        var id = "debug_subscription";
        var registryName = Identifier.fromNamespaceAndPath(reg.modId(), id);
        var blockEntityType = DeferredHolder.create(Registries.BLOCK_ENTITY_TYPE, registryName);

        // register our custom debug subscription
        var debugSubscription = reg.registrar(Registries.DEBUG_SUBSCRIPTION).register(id, () -> new DebugSubscription<>(ByteBufCodecs.VAR_INT, 200));

        final class DebugBlockEntity extends BlockEntity {
            private int counter = 0;

            private DebugBlockEntity(BlockPos pos, BlockState blockState) {
                super(blockEntityType.value(), pos, blockState);
            }

            public void incrementCounter() {
                counter++;
                setChanged();
            }

            @Override
            protected void saveAdditional(ValueOutput output) {
                super.saveAdditional(output);
                output.putInt("counter", counter);
            }

            @Override
            protected void loadAdditional(ValueInput input) {
                super.loadAdditional(input);
                counter = input.getIntOr("counter", 0);
            }

            @Override
            public void registerDebugValues(ServerLevel level, Registration registration) {
                // mark this entity for displaying debug info for our subscription
                registration.register(debugSubscription.value(), () -> counter);
            }
        }

        // generic block for our block entity
        final class DebugBlock extends BaseEntityBlock {
            private DebugBlock(Properties properties) {
                super(properties);
            }

            @Override
            protected MapCodec<? extends BaseEntityBlock> codec() {
                return null;
            }

            @Override
            protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
                if (!(level.getBlockEntity(pos) instanceof DebugBlockEntity blockEntity)) {
                    return InteractionResult.FAIL;
                }

                blockEntity.incrementCounter();
                return InteractionResult.SUCCESS;
            }

            @Override
            public BlockEntity newBlockEntity(BlockPos pos, BlockState blockState) {
                return new DebugBlockEntity(pos, blockState);
            }
        }

        var block = reg.blocks().registerBlock(id, DebugBlock::new);
        reg.items().registerSimpleBlockItem(block);
        reg.registrar(Registries.BLOCK_ENTITY_TYPE).register(id, () -> new BlockEntityType<>(DebugBlockEntity::new, block.value()));

        // debug renderer used to render counters above our block entities
        final class DebugCountRenderer implements DebugRenderer.SimpleDebugRenderer {
            private DebugCountRenderer(Minecraft client) {}

            @Override
            public void emitGizmos(double camX, double camY, double camZ, DebugValueAccess valueAccess, Frustum frustum, float partialTicks) {
                // list is populated via 'DebugBlockEntity.registerDebugValues'
                // while we are using block entities this, you can also register subscribers for entities and chunks too
                valueAccess.forEachBlock(debugSubscription.value(), (pos, count) -> {
                    Gizmos.billboardTextOverBlock(count + "", pos, 0, CommonColors.WHITE, .5F);
                });
            }
        }

        // mark our subscriber as only being active in dev
        NeoForge.EVENT_BUS.addListener(AddDebugSubscriptionFlagsEvent.class, event -> event.addActiveInDev(debugSubscription.value()));

        // register our debug renderer
        reg.eventListeners().accept((RegisterDebugRenderersEvent event) -> event.register(DebugCountRenderer::new));

        test.pass();
    }
}
