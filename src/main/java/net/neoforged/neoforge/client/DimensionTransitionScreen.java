package net.neoforged.neoforge.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.network.chat.Component;

public interface DimensionTransitionScreen {
    /**
     * Render an effect on screen when transitioning to/from a dimension via the {@link ReceivingLevelScreen}. If this method returns true, the vanilla rendering will be
     * canceled. Otherwise, the screen will render the menu panorama.
     *
     * @param graphics     the gui graphics
     * @param partialTicks progress between ticks
     * @param screenWidth  the width of the screen
     * @param screenHeight the height of the screen
     * @return true to cancel vanilla rendering
     */
    boolean renderScreenEffect(GuiGraphics graphics, float partialTicks, int screenWidth, int screenHeight);

    /**
     * Called when vanilla renders the "Downloading Terrain" text on the {@link ReceivingLevelScreen}. <br>
     * Use this to render custom text on the screen if you don't want to use vanilla's.
     *
     * @param font         the game font
     * @param graphics     the gui graphics
     * @param screenWidth  the width of the screen
     * @param screenHeight the height of the screen
     */
    default void renderTransitionText(Font font, GuiGraphics graphics, int screenWidth, int screenHeight) {
        graphics.drawCenteredString(font, Component.translatable("multiplayer.downloadingTerrain"), screenWidth / 2, screenHeight / 2 - 50, 16777215);
    }
}
