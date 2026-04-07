/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.modlist;

import java.net.URI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

// TODO: delete once testing is finished
@ApiStatus.Internal
class TestingResources {
    static ModDisplayInfo winterFox() {
        return new ModDisplayInfo() {
            @Override
            public String id() {
                return "winterfox";
            }

            @Override
            public Component displayName() {
                //noinspection UnnecessaryUnicodeEscape
                return Component.literal("\u2744 Winter Fox \u2744");
            }

            @Override
            public String version() {
                return "2024.12";
            }

            @Override
            public Component authors() {
                return Component.empty();
            }

            @Override
            public Component credits() {
                return Component.empty();
            }

            @Override
            public Component description() {
                return Component.translatable("%s \n %s",
                        Component.literal("test ".repeat(20)),
                        Component.literal("Click this link!").withStyle(style -> style
                                .applyFormat(ChatFormatting.UNDERLINE)
                                .withClickEvent(new ClickEvent.OpenUrl(URI.create("https://neoforged.net")))
                                .withHoverEvent(new HoverEvent.ShowText(Component.literal("boop!")))));
            }

            @Override
            public Component license() {
                return Component.literal("BSD-0").withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.literal("PLS WORK."))));
            }

            @Nullable
            @Override
            public ImageResource logo() {
                return null;
            }

            @Override
            public ImageResource icon() {
                return ImageResource.packRoot("mod/neoforge", "snowy_boi.png");
            }

            @Nullable
            @Override
            public URI displayUrl() {
                return null;
            }

            @Nullable
            @Override
            public URI issuesUrl() {
                return null;
            }
        };
    }

    static ModDisplayInfo neoPride() {
        return new ModDisplayInfo() {
            @Override
            public String id() {
                return "neo_pride";
            }

            @Override
            public Component displayName() {
                final MutableComponent base = Component.empty();
                TextColor[] colors = {
                        TextColor.fromRgb(0xaa212b), // red
                        TextColor.fromRgb(0xfb8918), // orange
                        TextColor.fromRgb(0xffe359), // yellow
                        TextColor.fromRgb(0x32d850), // green
                        TextColor.fromRgb(0x3894ff), // blue
                        TextColor.fromRgb(0x6e5cb8)  // violet
                };
                int index = 0;
                for (char c : "NeoForged Pride".toCharArray()) {
                    if (c != ' ') {
                        TextColor color = colors[index++ % colors.length];
                        base.append(Component.literal(String.valueOf(c)).withStyle(s -> s.withColor(color)));
                    } else {
                        base.append(Component.literal(" "));
                    }
                }
                return base;
            }

            @Override
            public String version() {
                return "2024.06";
            }

            @Override
            public Component authors() {
                return Component.empty();
            }

            @Override
            public Component credits() {
                return Component.empty();
            }

            @Override
            public Component description() {
                return Component.empty();
            }

            @Override
            public Component license() {
                return Component.literal("No rights reserved!");
            }

            @Nullable
            @Override
            public ImageResource logo() {
                return null;
            }

            @Override
            public ImageResource icon() {
                return ImageResource.packRoot("mod/neoforge", "neoforged_pride.png");
            }

            @Nullable
            @Override
            public URI displayUrl() {
                return null;
            }

            @Nullable
            @Override
            public URI issuesUrl() {
                return null;
            }
        };
    }

    static ModDisplayInfo exercise(String modId) {
        return new ModDisplayInfo() {
            @Override
            public String id() {
                return modId;
            }

            @Override
            public Component displayName() {
                return Component.literal(modId + " " + "HA".repeat(10));
            }

            @Override
            public String version() {
                return "01.02.03.04.05.06.07.08.09";
            }

            @Override
            public Component authors() {
                return Component.empty();
            }

            @Override
            public Component credits() {
                return Component.empty();
            }

            @Override
            public Component description() {
                return Component.empty();
            }

            @Override
            public Component license() {
                return Component.literal("Some rights reserved!");
            }

            @Nullable
            @Override
            public ImageResource logo() {
                return null;
            }

            @Nullable
            @Override
            public ImageResource icon() {
                return null;
            }

            @Nullable
            @Override
            public URI displayUrl() {
                return null;
            }

            @Nullable
            @Override
            public URI issuesUrl() {
                return null;
            }
        };
    }
}
