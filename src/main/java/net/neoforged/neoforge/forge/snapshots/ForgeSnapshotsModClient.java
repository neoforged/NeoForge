/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.forge.snapshots;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ForgeSnapshotsModClient {
    public static void renderMainMenuWarning(String neoForgeVersion, GuiGraphics graphics, Font font, int width, int height, int alpha) {
        if (neoForgeVersion.contains("-beta")) {
            // Render a warning at the top of the screen
            Component line = Component.translatable("neoforge.update.beta.1", ChatFormatting.RED, ChatFormatting.RESET).withStyle(ChatFormatting.RED);
            graphics.drawCenteredString(font, line, width / 2, 4 + (0 * (font.lineHeight + 1)), 0xFFFFFF | alpha);
            line = Component.translatable("neoforge.update.beta.2");
            graphics.drawCenteredString(font, line, width / 2, 4 + (1 * (font.lineHeight + 1)), 0xFFFFFF | alpha);
        }
    }
}
