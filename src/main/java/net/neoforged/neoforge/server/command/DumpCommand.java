/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLLoader;
import org.slf4j.Logger;

/**
 * The {@code /neoforge dump registry} command for printing out the contents of a registry to a file in the game directory's dumps/registry folder.
 */
class DumpCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceKey<Registry<Registry<?>>> ROOT_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation("root"));
    private static final String ALPHABETICAL_SORT_PARAM = "alphabetical_sort";
    private static final String PRINT_NUMERIC_ID_PARAM = "print_numeric_ids";

    private static final DynamicCommandExceptionType UNKNOWN_REGISTRY = new DynamicCommandExceptionType(key -> Component.translatable("commands.neoforge.dump.error.unknown_registry", key));

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        /*
         * /neoforge dump registry <registry> <alphabetical_sort> <print_numeric_ids>
         */
        return Commands.literal("dump")
                .requires(cs -> cs.hasPermission(Commands.LEVEL_OWNERS))
                .then(Commands.literal("registry")
                        .then(Commands.argument("registry", ResourceKeyArgument.key(ROOT_REGISTRY_KEY))
                                .suggests(CommandUtils::suggestRegistries)
                                .executes(context -> dumpRegistry(context, false, false))
                                .then(Commands.argument(ALPHABETICAL_SORT_PARAM, BoolArgumentType.bool())
                                        .executes(context -> dumpRegistry(context, BoolArgumentType.getBool(context, ALPHABETICAL_SORT_PARAM), false))
                                        .then(Commands.argument(PRINT_NUMERIC_ID_PARAM, BoolArgumentType.bool())
                                                .executes(context -> dumpRegistry(context, BoolArgumentType.getBool(context, ALPHABETICAL_SORT_PARAM), BoolArgumentType.getBool(context, PRINT_NUMERIC_ID_PARAM)))))));
    }

    private static int dumpRegistry(final CommandContext<CommandSourceStack> ctx, boolean alphabeticalSort, boolean printNumericIds) throws CommandSyntaxException {
        final ResourceKey<? extends Registry<?>> registryKey = CommandUtils.getResourceKey(ctx, "registry", ROOT_REGISTRY_KEY)
                .orElseThrow(); // Expect to always retrieve a resource key for the root registry (registry key)

        final Registry<?> registry = ctx.getSource().getServer().registryAccess().registry(registryKey)
                .orElseThrow(() -> UNKNOWN_REGISTRY.create(registryKey.location()));

        String fileLocationForErrorReporting = "";
        try {
            Path registryDumpDirectory = FMLLoader.getGamePath().resolve("dumps").resolve("registry");
            Path registryNamespaceDirectory = registryDumpDirectory.resolve(registryKey.location().getNamespace().replaceAll("[/:.]", "_"));
            Files.createDirectories(registryNamespaceDirectory);

            String fileName = registryKey.location().getPath().replaceAll("[/:.]", "_") + ".txt";
            Path registryDumpFile = registryNamespaceDirectory.resolve(fileName);
            fileLocationForErrorReporting = registryDumpFile.toString();

            try (var outputStream = Files.newOutputStream(registryDumpFile)) {
                List<ResourceLocation> sortedRegistryKeys = getSortedRegistryKeys(alphabeticalSort, printNumericIds, registry);

                for (ResourceLocation registryKeys : sortedRegistryKeys) {
                    String results = registryKeys.toString();
                    if (printNumericIds) {
                        results = registry.getId(registryKeys) + " - " + results;
                    }
                    outputStream.write((results + "\n").getBytes());
                }
            }

            MutableComponent filePathComponent = Component.literal("..." + FMLLoader.getGamePath().relativize(registryDumpFile))
                    .withStyle(ChatFormatting.UNDERLINE)
                    .withStyle(ChatFormatting.GOLD);

            // Click action not allow on dedicated servers as client cannot click link to a server's file path.
            if (!FMLLoader.getDist().isDedicatedServer()) {
                filePathComponent.withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, registryDumpFile.toString())));
            }

            ctx.getSource().sendSuccess(() -> Component.translatable(
                    "commands.neoforge.dump.success",
                    Component.literal(registryKey.location().toString()).withStyle(ChatFormatting.YELLOW),
                    filePathComponent),
                    false);

            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {

            ctx.getSource().sendFailure(
                    Component.translatable(
                            "commands.neoforge.dump.failure",
                            Component.literal(registryKey.location().toString()).withStyle(ChatFormatting.YELLOW),
                            Component.literal(fileLocationForErrorReporting).withStyle(ChatFormatting.GOLD)));

            LOGGER.error("Failed to create new file with " + registryKey + " registry's contents at " + fileLocationForErrorReporting, e);

            return 0;
        }
    }

    private static List<ResourceLocation> getSortedRegistryKeys(boolean alphabeticalSort, boolean printNumericIds, Registry<?> registry) {
        List<ResourceLocation> sortedRegistryNames = new ArrayList<>(registry.keySet());

        if (alphabeticalSort) {
            sortedRegistryNames.sort(ResourceLocation::compareNamespaced);
        } else if (printNumericIds) {
            sortedRegistryNames = sortedRegistryNames.stream().sorted(Comparator.comparingInt(registry::getId)).toList();
        }

        return sortedRegistryNames;
    }
}
