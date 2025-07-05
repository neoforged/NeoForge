/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.resources;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.debug.capabilities.handlers.resources.ResourceHandlerTestSetup;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.resources.InfiniteResourceHandler;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

// Mostly to show that alternate resources are possible with the same backing interface allowing for cross mod interactions to be easier with buffered or accumulated resources
@ForEachTest(groups = ResourceHandlerTestSetup.GROUP_ID, idPrefix = "resource.handler.alternate.")
public class AlternateResourceTests {
    public static final BlockCapability<IResourceHandler<TestElementResource>, Void> BLOCK_CAPABILITY = BlockCapability.createVoid(ResourceLocation.fromNamespaceAndPath("resource_handler_tests", "elements_of_lol"), IResourceHandler.asClass());

    @OnInit
    static void init(final TestFramework framework) {
        var bus = framework.modEventBus();

        bus.<RegisterCapabilitiesEvent>addListener(e -> e.registerBlockEntity(
                BLOCK_CAPABILITY,
                ResourceHandlerTestSetup.Content.RESOURCE_BLOCK_ENTITY.value(),
                (blockEntity, context) -> new InfiniteResourceHandler<>(TestElementResource.FIRE)));
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Alternate resource test to ensure that it is simple enough at the very base level to create a new resource type, and use it the cap system.")
    private static void alternate(ExtendedGameTestHelper helper) {
        var pos = ResourceHandlerTestSetup.setupLevelEnvironment(helper);
        var cap = helper.requireCapability(BLOCK_CAPABILITY, pos, null);

        //Validates that we are infact able to get infinite fire, doesn't do much besides that.
        helper.assertValueEqual(cap.getResource(0), TestElementResource.FIRE, "element");

        helper.succeed();
    }
}
