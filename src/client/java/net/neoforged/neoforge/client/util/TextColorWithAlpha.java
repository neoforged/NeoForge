/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.util;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;

/// A combined [TextColor] with alpha component.
///
/// @param color the text color component
/// @param alpha the alpha component, within the range of 0 to 255 (`0xFF`)
public record TextColorWithAlpha(TextColor color, int alpha) {
    /// Constructs a [TextColorWithAlpha] with the given color and alpha component.
    ///
    /// @param color the text color component
    /// @param alpha the alpha component, will be clamped within the acceptable range of values
    public TextColorWithAlpha {
        Objects.requireNonNull(color);
        alpha = Math.clamp(alpha, 0, 0xFF);
    }

    /// Constructs a [TextColorWithAlpha] with the given color and a full alpha value (fully opaque).
    ///
    /// @param color the text color component
    public TextColorWithAlpha(TextColor color) {
        this(color, 0xFF);
    }

    /// Creates a [TextColorWithAlpha] from the given ARGB color value.
    ///
    /// @param argb an ARGB color value
    /// @return an instance of this class with that color value
    public static TextColorWithAlpha fromArgb(int argb) {
        return new TextColorWithAlpha(TextColor.fromRgb(argb), ARGB.alpha(argb));
    }

    /// Creates a [TextColorWithAlpha] from the given RGB color value with full alpha value.
    ///
    /// @param rgb an RGB color value
    /// @return an instance of this class with that color value and full alpha value
    public static TextColorWithAlpha fromRgb(int rgb) {
        return new TextColorWithAlpha(TextColor.fromRgb(rgb));
    }

    /// Parses a color as a [TextColorWithAlpha], either as a hex string (`#AARRGGBB`) or as a predefined color name.
    /// Predefined colors are the named colors represented as static fields in [TextColor].
    ///
    /// @param color the color string, either a hex string or a predefined color name
    /// @return a data result, with a [TextColorWithAlpha] if successfully parsed
    public static DataResult<TextColorWithAlpha> parseColor(String color) {
        if (color.startsWith("#")) {
            try {
                int value = Integer.parseInt(color.substring(1), 16);
                return DataResult.success(TextColorWithAlpha.fromArgb(value), Lifecycle.stable());
            } catch (NumberFormatException e) {
                return DataResult.error(() -> "Invalid color value: " + color);
            }
        }
        // Handle predefined colors
        return TextColor.parseColor(color).map(TextColorWithAlpha::new);
    }

    /// {@return the value of this text color with alpha, in ARGB format}
    public int getValue() {
        return this.alpha << 24 | this.color.getValue();
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "#%06X%06X", this.alpha, this.color.getValue());
    }
}
