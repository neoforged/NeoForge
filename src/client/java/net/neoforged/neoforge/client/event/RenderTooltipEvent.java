/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Fired during tooltip rendering.
 * See the various subclasses for listening to specific events.
 *
 * @see RenderTooltipEvent.GatherComponents
 * @see RenderTooltipEvent.Pre
 * @see RenderTooltipEvent.Texture
 */
public abstract class RenderTooltipEvent extends Event {
    protected final ItemStack itemStack;
    protected final GuiGraphics graphics;
    protected int x;
    protected int y;
    protected Font font;
    protected final List<ClientTooltipComponent> components;

    @ApiStatus.Internal
    protected RenderTooltipEvent(ItemStack itemStack, GuiGraphics graphics, int x, int y, Font font, List<ClientTooltipComponent> components) {
        this.itemStack = itemStack;
        this.graphics = graphics;
        this.components = Collections.unmodifiableList(components);
        this.x = x;
        this.y = y;
        this.font = font;
    }

    /**
     * {@return the item stack which the tooltip is being rendered for, or an {@linkplain ItemStack#isEmpty() empty
     * item stack} if there is no associated item stack}
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * {@return the graphics helper for the gui}
     */
    public GuiGraphics getGraphics() {
        return this.graphics;
    }

    /**
     * {@return the unmodifiable list of tooltip components}
     *
     * <p>Use {@link ItemTooltipEvent} or {@link GatherComponents} to modify tooltip contents or components.</p>
     */
    public List<ClientTooltipComponent> getComponents() {
        return components;
    }

    /**
     * {@return the X position of the tooltip box} By default, this is the mouse X position.
     */
    public int getX() {
        return x;
    }

    /**
     * {@return the Y position of the tooltip box} By default, this is the mouse Y position.
     */
    public int getY() {
        return y;
    }

    /**
     * {@return The font used to render the text}
     */
    public Font getFont() {
        return font;
    }

    /**
     * Fired when a tooltip gathers the {@link TooltipComponent}s to be rendered, before any text wrapping or processing.
     * The list of components and the maximum width of the tooltip can be modified through this event.
     *
     * <p>This event is {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
     * If this event is cancelled, then the list of components will be empty, causing the tooltip to not be rendered and
     * the corresponding {@link RenderTooltipEvent.Pre} and {@link RenderTooltipEvent.Texture} to not be fired.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class GatherComponents extends Event implements ICancellableEvent {
        private final ItemStack itemStack;
        private final int screenWidth;
        private final int screenHeight;
        private final List<Either<FormattedText, TooltipComponent>> tooltipElements;
        private int maxWidth;

        @ApiStatus.Internal
        public GatherComponents(ItemStack itemStack, int screenWidth, int screenHeight, List<Either<FormattedText, TooltipComponent>> tooltipElements, int maxWidth) {
            this.itemStack = itemStack;
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.tooltipElements = tooltipElements instanceof ArrayList<Either<FormattedText, TooltipComponent>> ? tooltipElements : new ArrayList<>(tooltipElements);
            this.maxWidth = maxWidth;
        }

        /**
         * {@return the item stack which the tooltip is being rendered for, or an {@linkplain ItemStack#isEmpty() empty
         * item stack} if there is no associated item stack}
         */
        public ItemStack getItemStack() {
            return itemStack;
        }

        /**
         * {@return the width of the screen}.
         * The lines of text within the tooltip are wrapped to be within the screen width, and the tooltip box itself
         * is moved to be within the screen width.
         */
        public int getScreenWidth() {
            return screenWidth;
        }

        /**
         * {@return the height of the screen}
         * The tooltip box is moved to be within the screen height.
         */
        public int getScreenHeight() {
            return screenHeight;
        }

        /**
         * {@return the modifiable list of elements to be rendered on the tooltip} These elements can be either
         * formatted text or custom tooltip components.
         */
        public List<Either<FormattedText, TooltipComponent>> getTooltipElements() {
            return tooltipElements;
        }

        /**
         * {@return the maximum width of the tooltip when being rendered}
         *
         * <p>A value of {@code -1} means an unlimited maximum width. However, an unlimited maximum width will still
         * be wrapped to be within the screen bounds.</p>
         */
        public int getMaxWidth() {
            return maxWidth;
        }

        /**
         * Sets the maximum width of the tooltip. Use {@code -1} for unlimited maximum width.
         *
         * @param maxWidth the new maximum width
         */
        public void setMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
        }
    }

    /**
     * Fired <b>before</b> the tooltip is rendered.
     * This can be used to modify the positioning and font of the tooltip.
     *
     * <p>This event is {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
     * If this event is cancelled, then the tooltip will not be rendered and the corresponding
     * {@link RenderTooltipEvent.Texture} will not be fired.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class Pre extends RenderTooltipEvent implements ICancellableEvent {
        private final int screenWidth;
        private final int screenHeight;
        private final ClientTooltipPositioner positioner;

        @ApiStatus.Internal
        public Pre(ItemStack stack, GuiGraphics graphics, int x, int y, int screenWidth, int screenHeight, Font font, List<ClientTooltipComponent> components, ClientTooltipPositioner positioner) {
            super(stack, graphics, x, y, font, components);
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.positioner = positioner;
        }

        /**
         * {@return the width of the screen}.
         * The lines of text within the tooltip are wrapped to be within the screen width, and the tooltip box itself
         * is moved to be within the screen width.
         */
        public int getScreenWidth() {
            return screenWidth;
        }

        /**
         * {@return the height of the screen}
         * The tooltip box is moved to be within the screen height.
         */
        public int getScreenHeight() {
            return screenHeight;
        }

        public ClientTooltipPositioner getTooltipPositioner() {
            return positioner;
        }

        /**
         * Sets the font to be used to render text.
         *
         * @param fr the new font
         */
        public void setFont(Font fr) {
            this.font = fr;
        }

        /**
         * Sets the X origin of the tooltip.
         *
         * @param x the new X origin
         */
        public void setX(int x) {
            this.x = x;
        }

        /**
         * Sets the Y origin of the tooltip.
         *
         * @param y the new Y origin
         */
        public void setY(int y) {
            this.y = y;
        }
    }

    /**
     * Fired when the textures for the tooltip background are determined.
     * This can be used to modify the background and frame texture.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class Texture extends RenderTooltipEvent {
        @Nullable
        private final ResourceLocation originalTexture;
        @Nullable
        private ResourceLocation texture;

        @ApiStatus.Internal
        public Texture(ItemStack stack, GuiGraphics graphics, int x, int y, Font font, List<ClientTooltipComponent> components, @Nullable ResourceLocation texture) {
            super(stack, graphics, x, y, font, components);
            this.originalTexture = texture;
            this.texture = texture;
        }

        /**
         * {@return the original texture location given to the tooltip render method (may originate from {@link DataComponents#TOOLTIP_STYLE})}
         */
        @Nullable
        public ResourceLocation getOriginalTexture() {
            return originalTexture;
        }

        /**
         * {@return the texture location that will be used to render the tooltip}
         */
        @Nullable
        public ResourceLocation getTexture() {
            return texture;
        }

        /**
         * Set the texture to use for the tooltip background and frame or {@code null} to use the default textures.
         * <p>
         * The given {@link ResourceLocation} will be prefixed with {@code tooltip/} and suffixed with {@code _background}
         * and {@code _frame} to determine the background and frame texture respectively
         */
        public void setTexture(@Nullable ResourceLocation texture) {
            this.texture = texture;
        }
    }
}
