/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.server.command.arguments.RegistryArgument;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The {@code /neoforge dump registry} command for printing out the contents of a registry to a file in the game directory's dumps/registry folder.
 * </ul>
 */
class DumpCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String REGISTRY_PARAM = "registry";
    private static final String ALPHABETICAL_SORT_PARAM = "alphabetical_sort";
    private static final String PRINT_NUMERIC_ID_PARAM = "print_numeric_ids";

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        /*
         * /neoforge dump registry <registry> <alphabetical_sort> <print_numeric_ids>
         */
        return Commands.literal("dump")
                .requires(cs -> cs.hasPermission(Commands.LEVEL_OWNERS))
                .then(Commands.literal("registry")
                        .then(Commands.argument(REGISTRY_PARAM, RegistryArgument.registryArgument())
                                .executes(context -> dumpRegistry(context, RegistryArgument.getRegistry(context, REGISTRY_PARAM), false, false))
                                .then(Commands.argument(ALPHABETICAL_SORT_PARAM, BoolArgumentType.bool())
                                        .executes(context -> dumpRegistry(context, RegistryArgument.getRegistry(context, REGISTRY_PARAM), BoolArgumentType.getBool(context, ALPHABETICAL_SORT_PARAM), false))
                                        .then(Commands.argument(PRINT_NUMERIC_ID_PARAM, BoolArgumentType.bool())
                                                .executes(context -> dumpRegistry(context, RegistryArgument.getRegistry(context, REGISTRY_PARAM), BoolArgumentType.getBool(context, ALPHABETICAL_SORT_PARAM), BoolArgumentType.getBool(context, PRINT_NUMERIC_ID_PARAM)))))));
    }

    private static int dumpRegistry(final CommandContext<CommandSourceStack> ctx, Registry<?> registry, boolean alphabeticalSort, boolean printNumericIds) {
        final ResourceKey<? extends Registry<?>> registryKey = registry.key();

        String fileLocationForErrorReporting = "";
        try {
            Path registryDumpDirectory = FMLLoader.getGamePath().resolve("dumps").resolve("registry");
            Path registryNamespaceDirectory = registryDumpDirectory.resolve(registryKey.location().getNamespace().replaceAll("[/:.]", "_"));
            Files.createDirectories(registryNamespaceDirectory);

            String fileName = registryKey.location().getPath().replaceAll("[/:.]", "_") + ".txt";
            Path registryDumpFile = Paths.get(registryNamespaceDirectory.toString(), fileName);
            fileLocationForErrorReporting = registryDumpFile.toString();

            try (var outputStream = Files.newOutputStream(registryDumpFile)) {
                List<ResourceLocation> sortedRegistryKeys = getSortedRegistryKeys(alphabeticalSort, printNumericIds, registry);

                for (ResourceLocation registryKeys : sortedRegistryKeys) {
                    String results = registryKeys.toString();
                    if (printNumericIds) {
                        results = registry.getId(registryKeys) + " - " + results;
                    }
                    outputStream.write((results + "\r\n").getBytes());
                }
            }

            ctx.getSource().sendSuccess(() -> Component.translatable(
                    "commands.neoforge.registry_dump.success",
                    Component.literal(registryKey.location().toString()).withStyle(ChatFormatting.YELLOW),
                    Component.literal("..." + FMLLoader.getGamePath().relativize(registryDumpFile))
                            .withStyle(ChatFormatting.UNDERLINE)
                            .withStyle(ChatFormatting.GOLD)
                            .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, registryDumpFile.toString())))),
                    false);

            return 1;
        } catch (Exception e) {

            ctx.getSource().sendFailure(
                    Component.translatable(
                            "commands.neoforge.registry_dump.failure",
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
