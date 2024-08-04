/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.TranslatableEnum;

@Mod("configui")
public class ConfigUITest {
    public ConfigUITest(final ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, Client.SPEC);
        container.registerConfig(ModConfig.Type.COMMON, Common.SPEC);
        container.registerConfig(ModConfig.Type.SERVER, Server.SPEC);
        container.registerConfig(ModConfig.Type.STARTUP, Startup.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, MDKConfig.SPEC, "mdk");

        // Test: Accessing this COMMON config value during startup should work:
        final var a = Common.value.get().size();
        // Test: Accessing a STARTUP config value should also work:
        final var b = Startup.value.get();
    }

    @Mod(value = "configui", dist = Dist.CLIENT)
    public static class ConfigUIClient {
        public ConfigUIClient(final ModContainer container) {
            final Random r = new Random();
            container.registerExtensionPoint(IConfigScreenFactory.class,
                    (a, b) -> new ConfigurationScreen(a, b, (context, key, element) -> r.nextFloat() > 0.75f ? null : element));
        }
    }

    public static class Client {
        public static final ModConfigSpec SPEC;

        static {
            final var builder = new ModConfigSpec.Builder();

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
            final var builder = new ModConfigSpec.Builder();

            builder.worldRestart().define("worldRestart", false);

            SPEC = builder.build();
        }
    }

    public static class Common {
        public static final ModConfigSpec SPEC;
        public static Supplier<List<? extends Integer>> value;

        static {
            final var builder = new ModConfigSpec.Builder();

            builder.translation("configuitest.common.section1")
                    .push("section1");

            builder.translation("configuitest.common.section2")
                    .push("section2");

            value = wrap(builder.translation("configuitest.common.numbers").defineListAllowEmpty("numbers", List.of(1, 2), () -> 0, e -> true));

            builder.pop(2);

            builder.worldRestart().define("worldRestart", false);
            builder.gameRestart().define("gameRestart", false);

            SPEC = builder.build();
        }

        static <T> Supplier<T> wrap(final ModConfigSpec.ConfigValue<T> cfg) {
            return new Wrap<>(cfg);
        }

        public record Wrap<T>(ModConfigSpec.ConfigValue<T> cfg) implements Supplier<T> {
            @Override
            public T get() {
                return SPEC.isLoaded() ? cfg.get() : cfg.getDefault();
            }
        }
    }

    public static class Startup {
        public static final ModConfigSpec SPEC;
        public static EnumValue<StartupSpeed> value;

        static {
            final var builder = new ModConfigSpec.Builder();

            value = builder.translation("configuitest.startup.speed").comment("Do you want SPEEED????").defineEnum("startupspeed", StartupSpeed.SLOW);

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

    @SuppressWarnings("deprecation")
    public static class MDKConfig {
        private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

        static {
            BUILDER.comment("Whether to log the dirt block on common setup").define("logDirtBlock", true);

            BUILDER.comment("Where all the wild booleans live").translation("key.random").push("subsection1");
            BUILDER.comment("Whether to log the dirt block on common setup").define("val1", true);
            BUILDER.comment("Whether to log the dirt block on common setup").define("val2", true);
            BUILDER.comment("Whether to log the dirt block on common setup").define("val3", true);

            BUILDER.comment("Where all the wild ints live").translation("key.randomint").push("subsection2");
            BUILDER.comment("A weird number").defineInRange("eridNumber", 43, 43, 53);
            BUILDER.comment("A wild number").defineInRange("num", 99, 55, 555555);
            BUILDER.comment("A wild number").defineInRange("numD", 99.001, 55.55, 555555.0);
            BUILDER.comment("A wild number").defineInRange("numL", -99L, -555L, Long.MAX_VALUE);
            BUILDER.pop();

            BUILDER.defineEnum("dir", Direction.NORTH);
            BUILDER.defineEnum("dir2", Direction.SOUTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.NORTH);

            BUILDER.pop();
            BUILDER.comment("Whether to log the dirt block on common setup").define("outer2", true);

            BUILDER.comment("A magic number").defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

            BUILDER.comment("A mundane number").defineInRange("mundaneNumber", 42, 1, 50);

            BUILDER.comment("What you want the introduction message to be for the magic number").define("magicNumberIntroduction", "The magic number is... ");

            // a list of strings that are treated as resource locations for items
            BUILDER.comment("A list of items to log on common setup.").defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "minecraft:",
                    obj -> obj instanceof final String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.tryParse(itemName)));

            BUILDER.comment("A list of int for no reason.").defineListAllowEmpty("intlist", List.of(1, 2, 3), () -> 0,
                    v -> v != null && (Integer) v >= -1 && (Integer) v < 100);

            BUILDER.comment("A list of something for no reason.").defineListAllowEmpty("alist", List.of(), v -> v != null);

            BUILDER.comment("A list of something for no reason.").defineListAllowEmpty("blist.c",
                    List.of("zero", Integer.valueOf(0), Double.valueOf(0), Long.valueOf(0)), v -> v != null);

            BUILDER.comment("intentionally untranslated entry").define("missing", false);
            BUILDER.define("missing_no_tooltip", false);
            BUILDER.translation("missing_empty_tooltip").define("missing_empty_tooltip", false);

            BUILDER.comment("Integer overflow range").defineInRange("overflow", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            BUILDER.define("x_offset", 0);
        }

        static final ModConfigSpec SPEC = BUILDER.build();
    }
}
