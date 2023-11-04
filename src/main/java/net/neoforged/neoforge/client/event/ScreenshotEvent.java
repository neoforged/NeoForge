/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.io.IOException;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a screenshot is taken, but before it is written to disk.
 *
 * <p>This event is {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 * If this event is cancelled, then the screenshot is not written to disk, and the message in the event will be posted
 * to the player's chat.</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 *
 * @see Screenshot
 */
public class ScreenshotEvent extends Event implements ICancellableEvent {
    public static final Component DEFAULT_CANCEL_REASON = Component.literal("Screenshot canceled");

    private final NativeImage image;
    private File screenshotFile;

    @Nullable
    private Component resultMessage = null;

    @ApiStatus.Internal
    public ScreenshotEvent(NativeImage image, File screenshotFile) {
        this.image = image;
        this.screenshotFile = screenshotFile;
        try {
            this.screenshotFile = screenshotFile.getCanonicalFile(); // FORGE: Fix errors on Windows with paths that include \.\
        } catch (IOException ignored) {}
    }

    /**
     * {@return the in-memory image of the screenshot}
     */
    public NativeImage getImage() {
        return image;
    }

    /**
     * @return the file where the screenshot will be saved to
     */
    public File getScreenshotFile() {
        return screenshotFile;
    }

    /**
     * Sets the new file where the screenshot will be saved to.
     *
     * @param screenshotFile the new filepath
     */
    public void setScreenshotFile(File screenshotFile) {
        this.screenshotFile = screenshotFile;
    }

    /**
     * {@return the custom cancellation message, or {@code null} if no custom message is set}
     */
    @Nullable
    public Component getResultMessage() {
        return resultMessage;
    }

    /**
     * Sets the new custom cancellation message used to inform the player.
     * It may be {@code null}, in which case the {@linkplain #DEFAULT_CANCEL_REASON default cancel reason} will be used.
     *
     * @param resultMessage the new result message
     */
    public void setResultMessage(@Nullable Component resultMessage) {
        this.resultMessage = resultMessage;
    }

    /**
     * Returns the cancellation message to be used in informing the player.
     *
     * <p>If there is no custom message given ({@link #getResultMessage()} returns {@code null}), then
     * the message will be the {@linkplain #DEFAULT_CANCEL_REASON default cancel reason message}.</p>
     *
     * @return the cancel message for the player
     */
    public Component getCancelMessage() {
        return getResultMessage() != null ? getResultMessage() : DEFAULT_CANCEL_REASON;
    }
}
