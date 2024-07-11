/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.TranslatableEnum;

@Mod("configui")
public class ConfigUITest {
    public ConfigUITest(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, Client.SPEC);
        container.registerConfig(ModConfig.Type.COMMON, Common.SPEC);
        container.registerConfig(ModConfig.Type.SERVER, Server.SPEC);
        container.registerConfig(ModConfig.Type.STARTUP, Startup.SPEC);
    }

    @Mod(value = "configui", dist = Dist.CLIENT)
    public static class ConfigUIClient {
        public ConfigUIClient(ModContainer container) {
            container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }

    public static class Client {
        public static final ModConfigSpec SPEC;

        static {
            var builder = new ModConfigSpec.Builder();

            builder.comment("Play a sound when you jump").translation("configuitest.client.playsound").define("playsound", true);

            builder.translation("configuitest.client.visuals")
                    .comment("Visual stuff")
                    .push("visuals");

            builder.comment("The amount of squirrels to show")
                    .translation("configuitest.client.squirrels")
                    .defineInRange("squirrels", 1, 1, Integer.MAX_VALUE);

            builder.pop();

            SPEC = builder.build();
        }
    }

    public static class Server {
        public static final ModConfigSpec SPEC;

        static {
            var builder = new ModConfigSpec.Builder();

            SPEC = builder.build();
        }
    }

    public static class Common {
        public static final ModConfigSpec SPEC;

        static {
            var builder = new ModConfigSpec.Builder();

            builder.translation("configuitest.common.section1")
                    .push("section1");

            builder.translation("configuitest.common.section2")
                    .push("section2");

            builder.translation("configuitest.common.numbers").defineListAllowEmpty("numbers", List.of(1, 2), () -> 0, e -> true);

            builder.pop(2);

            SPEC = builder.build();
        }
    }

    public static class Startup {
        public static final ModConfigSpec SPEC;

        static {
            var builder = new ModConfigSpec.Builder();

            builder.translation("configuitest.startup.speed")
                    .comment("Do you want SPEEED????")
                    .defineEnum("startupspeed", StartupSpeed.SLOW);

            SPEC = builder.build();
        }

        public enum StartupSpeed implements TranslatableEnum {
            FAST,
            SLOW {
                @Override
                public Component getTranslatedName() {
                    return Component.literal("Slow that it hurts");
                }
            },
            FASTEST
        }
    }
}
