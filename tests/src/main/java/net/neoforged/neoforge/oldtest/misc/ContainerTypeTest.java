/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.misc;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.NetworkHooks;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod("containertypetest")
public class ContainerTypeTest {
    public static final DeferredHolder<MenuType<?>, MenuType<TestContainer>> TYPE = DeferredHolder.create(Registries.MENU, new ResourceLocation("containertypetest", "container"));

    public static class TestContainer extends AbstractContainerMenu {
        private final String text;

        protected TestContainer(int windowId, Inventory playerInv, FriendlyByteBuf extraData) {
            this(windowId, new SimpleContainer(9), extraData.readUtf(128));
        }

        public TestContainer(int windowId, SimpleContainer inv, String text) {
            super(TYPE.get(), windowId);
            this.text = text;
            for (int i = 0; i < 9; i++) {
                this.addSlot(new Slot(inv, i, (i % 3) * 18, (i / 3) * 18));
            }
        }

        @Override
        public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player playerIn) {
            return true;
        }
    }

    public class TestGui extends AbstractContainerScreen<TestContainer> {
        public TestGui(TestContainer container, Inventory inv, Component name) {
            super(container, inv, name);
        }

        @Override
        protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
            graphics.drawString(this.font, getMenu().text, mouseX, mouseY, -1);
        }
    }

    public ContainerTypeTest(IEventBus modEventBus) {
        modEventBus.addListener(this::registerContainers);
        modEventBus.addListener(this::setup);
        NeoForge.EVENT_BUS.addListener(this::onRightClick);
    }

    private void registerContainers(final RegisterEvent event) {
        event.register(Registries.MENU, helper -> helper.register("container", IMenuTypeExtension.create(TestContainer::new)));
    }

    private void setup(FMLClientSetupEvent event) {
        MenuScreens.register(TYPE.get(), TestGui::new);
    }

    private void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide && event.getHand() == InteractionHand.MAIN_HAND) {
            if (event.getLevel().getBlockState(event.getPos()).getBlock() == Blocks.SPONGE) {
                String text = "Hello World!";
                NetworkHooks.openScreen((ServerPlayer) event.getEntity(), new MenuProvider() {
                    @Override
                    public AbstractContainerMenu createMenu(int p_createMenu_1_, Inventory p_createMenu_2_, Player p_createMenu_3_) {
                        SimpleContainer inv = new SimpleContainer(9);
                        for (int i = 0; i < inv.getContainerSize(); i++) {
                            inv.setItem(i, new ItemStack(Items.DIAMOND));
                        }
                        return new TestContainer(p_createMenu_1_, inv, text);
                    }

                    @Override
                    public Component getDisplayName() {
                        return Component.literal("Test");
                    }
                }, extraData -> {
                    extraData.writeUtf(text);
                });
            }
        }
    }
}
