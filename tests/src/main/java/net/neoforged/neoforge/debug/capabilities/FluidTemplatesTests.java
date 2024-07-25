/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.transfer.HandlerUtil;
import net.neoforged.neoforge.transfer.ResourceStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.context.templates.PlayerContext;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.fluids.templates.SingleFluidStorageItem;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "capabilities.fluidtemplates")
public class FluidTemplatesTests {
    private static final RegistrationHelper REG_HELPER = RegistrationHelper.create("neotests_capabilities_fluidtemplates");
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceStack<FluidResource>>> SIMPLE_FLUID_CONTENT = REG_HELPER
            .registrar(Registries.DATA_COMPONENT_TYPE)
            .register("simple_fluid_content", () -> DataComponentType.<ResourceStack<FluidResource>>builder()
                    .persistent(ResourceStack.codec(FluidResource.CODEC))
                    .networkSynchronized(ResourceStack.streamCodec(FluidResource.OPTIONAL_STREAM_CODEC)).build());

    @OnInit
    static void register(final TestFramework framework) {
        REG_HELPER.register(framework.modEventBus(), framework.container());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that FluidHandlerItemStack works")
    public static void testFluidHandlerItemStack(ExtendedGameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, Items.APPLE.getDefaultInstance());
        IItemContext context = PlayerContext.ofHand(player, InteractionHand.MAIN_HAND);
        int capacity = 2 * FluidType.BUCKET_VOLUME;
        var fluidHandler = new SingleFluidStorageItem(context, SIMPLE_FLUID_CONTENT.get(), capacity);

        if (fluidHandler.size() != 1) {
            helper.fail("Expected a single tank");
        }
        if (fluidHandler.getCapacity(0) != capacity) {
            helper.fail("Expected tank capacity of " + capacity);
        }
        if (fluidHandler.getAmount(0) != 0) {
            helper.fail("Expected empty tank");
        }

        if (fluidHandler.insert(0, Fluids.WATER.defaultResource, FluidType.BUCKET_VOLUME, TransferAction.EXECUTE) != FluidType.BUCKET_VOLUME) {
            helper.fail("Expected to be able to fill a bucket of water");
        }
        if (!player.getMainHandItem().has(SIMPLE_FLUID_CONTENT)) {
            helper.fail("Expected fluid stack component");
        }
        if (HandlerUtil.resourceAndCountMatches(fluidHandler, 0, Fluids.WATER.defaultResource, FluidType.BUCKET_VOLUME)) {
            helper.fail("Expected a bucket of water");
        }

        var drained = fluidHandler.extract(0, Fluids.WATER.defaultResource, FluidType.BUCKET_VOLUME, TransferAction.EXECUTE);
        if (drained != FluidType.BUCKET_VOLUME) {
            helper.fail("Expected to drain a bucket of water");
        }
        if (!HandlerUtil.isIndexEmpty(fluidHandler, 0)) {
            helper.fail("Expected empty tank");
        }
        if (player.getMainHandItem().has(SIMPLE_FLUID_CONTENT)) {
            helper.fail("Expected no fluid stack component");
        }

        helper.succeed();
    }
}
