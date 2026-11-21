/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.inventory;

import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.payload.AdvancedOpenScreenPayload;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.gametest.GameTestPlayer;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "inventory.menu")
public class MenuTypeTests {
    private static DeferredHolder<MenuType<?>, MenuType<TextMenu>> TEXT_MENU;

    /** A menu carrying a text, which the client receives through the extra data of the open screen payload. */
    private static class TextMenu extends AbstractContainerMenu {
        private final String text;

        private TextMenu(int windowId, String text) {
            super(TEXT_MENU.get(), windowId);
            this.text = text;
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slotIndex) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if a menu type created through IMenuTypeExtension sends the extra data written when the menu is opened, and receives it in its factory")
    static void menuTypeExtraData(final DynamicTest test, final RegistrationHelper reg) {
        TEXT_MENU = reg.registrar(Registries.MENU).register("text_menu", () -> IMenuTypeExtension.create((windowId, inventory, data) -> new TextMenu(windowId, data.readUtf())));

        test.onGameTest(helper -> {
            GameTestPlayer player = helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL);
            player.clearOutboundPackets();
            String text = "Hello World!";
            Component title = Component.literal("Text menu");
            player.openMenu(new SimpleMenuProvider((windowId, inventory, p) -> new TextMenu(windowId, text), title), data -> data.writeUtf(text));

            helper.assertTrue(player.containerMenu instanceof TextMenu menu && menu.text.equals(text), "Expected the text menu to be open on the server; menu is " + player.containerMenu);
            AdvancedOpenScreenPayload payload = helper.catchException(() -> player.requireOutboundPayload(AdvancedOpenScreenPayload.class));
            helper.assertValueEqual(payload.windowId(), player.containerMenu.containerId, "Unexpected window id in the payload");
            helper.assertTrue(payload.menuType() == TEXT_MENU.get(), "Expected the registered menu type in the payload; type is " + payload.menuType());
            helper.assertValueEqual(payload.name(), title, "Unexpected title in the payload");

            // The client recreates the menu from the type and the extra data of the payload
            RegistryFriendlyByteBuf data = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(payload.additionalData()), helper.getLevel().registryAccess());
            TextMenu clientMenu = TEXT_MENU.get().create(payload.windowId(), player.getInventory(), data);
            helper.assertValueEqual(clientMenu.text, text, "Expected the extra data to reach the menu factory; text is " + clientMenu.text);

            player.closeContainer();
            helper.succeed();
        });
    }
}
