/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.chat;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.exceptions.UnregisteredPermissionException;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = "chat.permission")
public class PermissionTests {
    private static final PermissionNode<Boolean> BOOLEAN_NODE = new PermissionNode<>("neotests", "test.boolean", PermissionTypes.BOOLEAN, (player, uuid, context) -> true);
    private static final PermissionNode<String> STRING_NODE = new PermissionNode<>("neotests", "test.string", PermissionTypes.STRING, (player, uuid, context) -> "Hello World");
    private static final PermissionNode<Integer> INTEGER_NODE = new PermissionNode<>("neotests", "test.integer", PermissionTypes.INTEGER, (player, uuid, context) -> 3);
    private static final PermissionNode<Component> COMPONENT_NODE = new PermissionNode<>("neotests", "test.component", PermissionTypes.COMPONENT, (player, uuid, context) -> Component.literal("This is a component"));
    private static final PermissionNode<Boolean> UNREGISTERED_NODE = new PermissionNode<>("neotests", "test.unregistered", PermissionTypes.BOOLEAN, (player, uuid, context) -> false);

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if permission nodes gathered when the server starts can be queried through the PermissionAPI, and that unregistered nodes are rejected", enabledByDefault = true)
    static void permissionApi(final DynamicTest test) {
        // The nodes are gathered when the server starts, so this test is enabled by default to have its listener in place
        test.eventListeners().forge().addListener((final PermissionGatherEvent.Nodes event) -> event.addNodes(BOOLEAN_NODE, STRING_NODE, INTEGER_NODE, COMPONENT_NODE));

        test.onGameTest(helper -> {
            ServerPlayer player = helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL);
            helper.assertTrue(PermissionAPI.getRegisteredNodes().contains(BOOLEAN_NODE), "Expected the gathered nodes to be registered");
            helper.assertValueEqual(PermissionAPI.getPermission(player, BOOLEAN_NODE), true, "Unexpected boolean permission");
            helper.assertValueEqual(PermissionAPI.getPermission(player, STRING_NODE), "Hello World", "Unexpected string permission");
            helper.assertValueEqual(PermissionAPI.getPermission(player, INTEGER_NODE), 3, "Unexpected integer permission");
            helper.assertValueEqual(PermissionAPI.getPermission(player, COMPONENT_NODE).getString(), "This is a component", "Unexpected component permission");

            boolean rejected = false;
            try {
                PermissionAPI.getPermission(player, UNREGISTERED_NODE);
            } catch (UnregisteredPermissionException e) {
                rejected = true;
            }
            helper.assertTrue(rejected, "Expected querying a node that was not gathered to be rejected");
            helper.succeed();
        });
    }
}
