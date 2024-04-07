/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.server.command.arguments.RegistryArgument;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The {@code /neoforge tags} command for listing a registry's tags, getting the elements of tags, and querying the tags of a
 * registry object.
 *
 * <p>Each command is paginated, showing {@value PAGE_SIZE} entries at a time. When there are more than 0 entries,
 * the text indicating the amount of entries is highlighted and can be clicked to copy the list of all entries (across
 * all pages) to the clipboard. (This is reflected by the use of green text in brackets, mimicking the clickable
 * coordinates in the {@code /locate} command's message)</p>
 *
 * <p>The command has three subcommands:</p>
 * <ul>
 * <li>{@code /neoforge tags &lt;registry> list [page]} - Lists all available tags in the given registry.</li>
 * <li>{@code /neoforge tags &lt;registry> get &lt;tag> [page]} - Gets all elements of the given tag in the given registry.</li>
 * <li>{@code /neoforge tags &lt;registry> query &lt;element> [page]} - Queries for all tags in the given registry which
 * contain the given registry object.</li>
 * </ul>
 */
class TagsCommand {
    private static final long PAGE_SIZE = 8;
    private static final ResourceKey<Registry<Registry<?>>> ROOT_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation("root"));

    private static final Dynamic2CommandExceptionType UNKNOWN_TAG = new Dynamic2CommandExceptionType((tag, registry) -> Component.translatable("commands.neoforge.tags.error.unknown_tag", tag, registry));
    private static final Dynamic2CommandExceptionType UNKNOWN_ELEMENT = new Dynamic2CommandExceptionType((tag, registry) -> Component.translatable("commands.neoforge.tags.error.unknown_element", tag, registry));

    private static final String REGISTRY_PARAM = "registry";

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        /*
         * /neoforge tags <registry> list [page]
         * /neoforge tags <registry> get <tag> [page]
         * /neoforge tags <registry> query <element> [page]
         */
        return Commands.literal("tags")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument(REGISTRY_PARAM, RegistryArgument.registryArgument())
                        .then(Commands.literal("list")
                                .executes(ctx -> listTags(ctx, RegistryArgument.getRegistry(ctx, REGISTRY_PARAM), 1))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes(ctx -> listTags(ctx, RegistryArgument.getRegistry(ctx, REGISTRY_PARAM), IntegerArgumentType.getInteger(ctx, "page")))))
                        .then(Commands.literal("get")
                                .then(Commands.argument("tag", ResourceLocationArgument.id())
                                        .executes(ctx -> listTagElements(ctx, RegistryArgument.getRegistry(ctx, REGISTRY_PARAM), 1))
                                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                                .executes(ctx -> listTagElements(ctx, RegistryArgument.getRegistry(ctx, REGISTRY_PARAM), IntegerArgumentType.getInteger(ctx, "page"))))))
                        .then(Commands.literal("query")
                                .then(Commands.argument("element", ResourceLocationArgument.id())
                                        .executes(ctx -> queryElementTags(ctx, RegistryArgument.getRegistry(ctx, REGISTRY_PARAM), 1))
                                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                                .executes(ctx -> queryElementTags(ctx, RegistryArgument.getRegistry(ctx, REGISTRY_PARAM), IntegerArgumentType.getInteger(ctx, "page")))))));
    }

    private static int listTags(final CommandContext<CommandSourceStack> ctx, final Registry<?> registry, final int page) {
        final ResourceKey<? extends Registry<?>> registryKey = registry.key();
        final long tagCount = registry.getTags().count();

        ctx.getSource().sendSuccess(() -> createMessage(
                Component.translatable("commands.neoforge.tags.registry_key", Component.literal(registryKey.location().toString()).withStyle(ChatFormatting.GOLD)),
                "commands.neoforge.tags.tag_count",
                "commands.neoforge.tags.copy_tag_names",
                tagCount,
                page,
                ChatFormatting.DARK_GREEN,
                () -> registry.getTags()
                        .map(Pair::getSecond)
                        .map(s -> s.unwrap().map(k -> k.location().toString(), Object::toString))),
                false);

        return (int) tagCount;
    }

    private static int listTagElements(final CommandContext<CommandSourceStack> ctx, final Registry<?> registry, final int page) throws CommandSyntaxException {
        final ResourceKey<? extends Registry<?>> registryKey = registry.key();

        final ResourceLocation tagLocation = ResourceLocationArgument.getId(ctx, "tag");
        final TagKey<?> tagKey = TagKey.create(cast(registryKey), tagLocation);

        final HolderSet.Named<?> tag = registry.getTag(cast(tagKey))
                .orElseThrow(() -> UNKNOWN_TAG.create(tagKey.location(), registryKey.location()));

        ctx.getSource().sendSuccess(() -> createMessage(
                Component.translatable("commands.neoforge.tags.tag_key",
                        Component.literal(tagKey.registry().location().toString()).withStyle(ChatFormatting.GOLD),
                        Component.literal(tagKey.location().toString()).withStyle(ChatFormatting.DARK_GREEN)),
                "commands.neoforge.tags.element_count",
                "commands.neoforge.tags.copy_element_names",
                tag.size(),
                page,
                ChatFormatting.YELLOW,
                () -> tag.stream().map(s -> s.unwrap().map(k -> k.location().toString(), Object::toString))), false);

        return tag.size();
    }

    private static int queryElementTags(final CommandContext<CommandSourceStack> ctx, final Registry<?> registry, final int page) throws CommandSyntaxException {
        final ResourceKey<? extends Registry<?>> registryKey = registry.key();

        final ResourceLocation elementLocation = ResourceLocationArgument.getId(ctx, "element");
        final ResourceKey<?> elementKey = ResourceKey.create(cast(registryKey), elementLocation);

        final Holder<?> elementHolder = registry.getHolder(cast(elementKey))
                .orElseThrow(() -> UNKNOWN_ELEMENT.create(elementLocation, registryKey.location()));

        final long containingTagsCount = elementHolder.tags().count();

        ctx.getSource().sendSuccess(() -> createMessage(
                Component.translatable("commands.neoforge.tags.element",
                        Component.literal(registryKey.location().toString()).withStyle(ChatFormatting.GOLD),
                        Component.literal(elementLocation.toString()).withStyle(ChatFormatting.YELLOW)),
                "commands.neoforge.tags.containing_tag_count",
                "commands.neoforge.tags.copy_tag_names",
                containingTagsCount,
                page,
                ChatFormatting.DARK_GREEN,
                () -> elementHolder.tags().map(k -> k.location().toString())), false);

        return (int) containingTagsCount;
    }

    private static MutableComponent createMessage(final MutableComponent header,
            final String containsText,
            final String copyHoverText,
            final long count,
            final long currentPage,
            final ChatFormatting elementColor,
            final Supplier<Stream<String>> names) {
        final String allElementNames = names.get().sorted().collect(Collectors.joining("\n"));
        final long totalPages = (count - 1) / PAGE_SIZE + 1;
        final long actualPage = (long) Mth.clamp(currentPage, 1, totalPages);

        MutableComponent containsComponent = Component.translatable(containsText, count);
        if (count > 0) // Highlight the count text, make it clickable, and append page counters
        {
            containsComponent = ComponentUtils.wrapInSquareBrackets(containsComponent.withStyle(s -> s
                    .withColor(ChatFormatting.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, allElementNames))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Component.translatable(copyHoverText)))));
            containsComponent = Component.translatable("commands.neoforge.tags.page_info",
                    containsComponent, actualPage, totalPages);
        }

        final MutableComponent tagElements = Component.literal("").append(containsComponent);
        names.get()
                .sorted()
                .skip(PAGE_SIZE * (actualPage - 1))
                .limit(PAGE_SIZE)
                .map(Component::literal)
                .map(t -> t.withStyle(elementColor))
                .map(t -> Component.translatable("\n - ").append(t))
                .forEach(tagElements::append);

        return header.append("\n").append(tagElements);
    }

    @SuppressWarnings("unchecked")
    private static <O> O cast(final Object input) {
        return (O) input;
    }
}
