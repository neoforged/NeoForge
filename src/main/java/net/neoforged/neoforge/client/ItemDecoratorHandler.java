/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ItemDecoratorHandler {
    private final List<IItemDecorator> itemDecorators;
    private final GlStateBackup stateBackup = new GlStateBackup();

    private static Map<Item, ItemDecoratorHandler> DECORATOR_LOOKUP = ImmutableMap.of();

    private static final ItemDecoratorHandler EMPTY = new ItemDecoratorHandler();

    private ItemDecoratorHandler() {
        this.itemDecorators = ImmutableList.of();
    }

    private ItemDecoratorHandler(List<IItemDecorator> itemDecorators) {
        this.itemDecorators = ImmutableList.copyOf(itemDecorators);
    }

    public static void init() {
        var decorators = new HashMap<Item, List<IItemDecorator>>();
        var event = new RegisterItemDecorationsEvent(decorators);
        ModLoader.get().postEventWrapContainerInModOrder(event);
        var builder = new ImmutableMap.Builder<Item, ItemDecoratorHandler>();
        decorators.forEach((item, itemDecorators) -> builder.put(item, new ItemDecoratorHandler(itemDecorators)));
        DECORATOR_LOOKUP = builder.build();
    }

    public static ItemDecoratorHandler of(ItemStack stack) {
        return DECORATOR_LOOKUP.getOrDefault(stack.getItem(), EMPTY);
    }

    public void render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        RenderSystem.backupGlState(stateBackup);

        resetRenderState();
        for (IItemDecorator itemDecorator : itemDecorators) {
            if (itemDecorator.render(guiGraphics, font, stack, xOffset, yOffset))
                resetRenderState();
        }

        RenderSystem.restoreGlState(stateBackup);
    }

    private void resetRenderState() {
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }
}
