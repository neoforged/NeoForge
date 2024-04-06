/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.logging.LogUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLLoader;
import org.slf4j.Logger;

/**
 * The {@code /neoforge registryDump} command for printing out the contents of a registry to a file.
 * </ul>
 */
class RegistryDumpCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceKey<Registry<Registry<?>>> ROOT_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation("root"));
    private static final String ALPHABETICAL_SORT_PARAM = "alphabeticalSort";
    private static final String PRINT_NUMERIC_ID_PARAM = "printNumericIds";

    private static final DynamicCommandExceptionType UNKNOWN_REGISTRY = new DynamicCommandExceptionType(key -> Component.translatable("commands.neoforge.tags.error.unknown_registry", key));

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        /*
         * /neoforge registryDump <registry> <alphabeticalSort> <printNumericIds>
         */
        return Commands.literal("registryDump")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("registry", ResourceKeyArgument.key(ROOT_REGISTRY_KEY))
                        .suggests(RegistryDumpCommand::suggestRegistries)
                        .executes(context -> dumpRegistry(context, false, false))
                        .then(Commands.argument(ALPHABETICAL_SORT_PARAM, BoolArgumentType.bool())
                                .executes(context -> dumpRegistry(context, BoolArgumentType.getBool(context, ALPHABETICAL_SORT_PARAM), false))
                                .then(Commands.argument(PRINT_NUMERIC_ID_PARAM, BoolArgumentType.bool())
                                        .executes(context -> dumpRegistry(context, BoolArgumentType.getBool(context, ALPHABETICAL_SORT_PARAM), BoolArgumentType.getBool(context, PRINT_NUMERIC_ID_PARAM))))));
    }

    private static int dumpRegistry(final CommandContext<CommandSourceStack> ctx, boolean alphabeticalSort, boolean printNumericIds) throws CommandSyntaxException {
        final ResourceKey<? extends Registry<?>> registryKey = getResourceKey(ctx, "registry", ROOT_REGISTRY_KEY)
                .orElseThrow(); // Expect to be always retrieve a resource key for the root registry (registry key)

        final Registry<?> registry = ctx.getSource().getServer().registryAccess().registry(registryKey)
                .orElseThrow(() -> UNKNOWN_REGISTRY.create(registryKey.location()));

        String fileLocationForErrorReporting = "";
        try {
            Path registryDumpDirectory = FMLLoader.getGamePath().resolve("registry_dumps");
            Files.createDirectories(registryDumpDirectory);

            String fileName = registryKey.location().toString().replaceAll("[/:.]", "_") + ".txt";
            Path registryDumpFile = Paths.get(registryDumpDirectory.toString(), fileName);
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
                    Component.literal(registryDumpFile.getFileName().toString())
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
            sortedRegistryNames.sort((r1, r2) -> {
                if (r1.getNamespace().equals(r2.getNamespace())) {
                    return r1.getPath().compareTo(r2.getPath());
                }

                return r1.getNamespace().compareTo(r2.getNamespace());
            });
        } else if (printNumericIds) {
            sortedRegistryNames = sortedRegistryNames.stream().sorted(Comparator.comparingInt(registry::getId)).toList();
        }

        return sortedRegistryNames;
    }

    private static CompletableFuture<Suggestions> suggestRegistries(final CommandContext<CommandSourceStack> ctx,
            final SuggestionsBuilder builder) {
        ctx.getSource().registryAccess().registries()
                .map(RegistryAccess.RegistryEntry::key)
                .map(ResourceKey::location)
                .map(ResourceLocation::toString)
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> Optional<ResourceKey<T>> getResourceKey(final CommandContext<CommandSourceStack> ctx,
            final String name,
            final ResourceKey<Registry<T>> registryKey) {
        // Don't inline to avoid an unchecked cast warning due to raw types
        final ResourceKey<?> key = ctx.getArgument(name, ResourceKey.class);
        return key.cast(registryKey);
    }
}
