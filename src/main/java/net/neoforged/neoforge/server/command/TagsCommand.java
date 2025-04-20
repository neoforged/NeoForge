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
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
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
    // The limit of how long the clipboard text can be; no more elements are added to the text if they would push it over this limit
    // This is roughly below 32767, the default limit for UTF-8 strings in FriendlyByteBuf. When adjusting this, make sure to leave
    // ample room for the explanatory text (see #createMessage() below).
    private static final long CLIPBOARD_TEXT_LIMIT = 32600;
    private static final long PAGE_SIZE = 8;
    private static final ResourceKey<Registry<Registry<?>>> ROOT_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("root"));

    private static final DynamicCommandExceptionType UNKNOWN_REGISTRY = new DynamicCommandExceptionType(key -> CommandUtils.makeTranslatableWithFallback("commands.neoforge.tags.error.unknown_registry", key.toString()));
    private static final Dynamic2CommandExceptionType UNKNOWN_TAG = new Dynamic2CommandExceptionType((tag, registry) -> CommandUtils.makeTranslatableWithFallback("commands.neoforge.tags.error.unknown_tag", tag.toString(), registry.toString()));
    private static final Dynamic2CommandExceptionType UNKNOWN_ELEMENT = new Dynamic2CommandExceptionType((tag, registry) -> CommandUtils.makeTranslatableWithFallback("commands.neoforge.tags.error.unknown_element", tag.toString(), registry.toString()));

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        /*
         * /neoforge tags <registry> list [page]
         * /neoforge tags <registry> get <tag> [page]
         * /neoforge tags <registry> query <element> [page]
         */
        return Commands.literal("tags")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("registry", ResourceKeyArgument.key(ROOT_REGISTRY_KEY))
                        .suggests(CommandUtils::suggestRegistries)
                        .then(Commands.literal("list")
                                .executes(ctx -> listTags(ctx, 1))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes(ctx -> listTags(ctx, IntegerArgumentType.getInteger(ctx, "page")))))
                        .then(Commands.literal("get")
                                .then(Commands.argument("tag", ResourceLocationArgument.id())
                                        .suggests(CommandUtils.suggestFromRegistry(r -> r.getTags().map(HolderSet.Named::key).map(TagKey::location)::iterator, "registry", ROOT_REGISTRY_KEY))
                                        .executes(ctx -> listTagElements(ctx, 1))
                                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                                .executes(ctx -> listTagElements(ctx, IntegerArgumentType.getInteger(ctx, "page"))))))
                        .then(Commands.literal("query")
                                .then(Commands.argument("element", ResourceLocationArgument.id())
                                        .suggests(CommandUtils.suggestFromRegistry(Registry::keySet, "registry", ROOT_REGISTRY_KEY))
                                        .executes(ctx -> queryElementTags(ctx, 1))
                                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                                .executes(ctx -> queryElementTags(ctx, IntegerArgumentType.getInteger(ctx, "page")))))));
    }

    private static int listTags(final CommandContext<CommandSourceStack> ctx, final int page) throws CommandSyntaxException {
        final ResourceKey<? extends Registry<?>> registryKey = CommandUtils.getResourceKey(ctx, "registry", ROOT_REGISTRY_KEY)
                .orElseThrow(); // Expect to always retrieve a resource key for the root registry (registry key)
        final Registry<?> registry = ctx.getSource().getServer().registryAccess().lookup(registryKey)
                .orElseThrow(() -> UNKNOWN_REGISTRY.create(registryKey.location()));

        final long tagCount = registry.getTags().count();

        ctx.getSource().sendSuccess(() -> createMessage(
                CommandUtils.makeTranslatableWithFallback("commands.neoforge.tags.registry_key", Component.literal(registryKey.location().toString()).withStyle(ChatFormatting.GOLD)),
                "commands.neoforge.tags.tag_count",
                "commands.neoforge.tags.copy_tag_names",
                tagCount,
                page,
                ChatFormatting.DARK_GREEN,
                () -> registry.getTags()
                        .map(s -> s.unwrap().map(k -> k.location().toString(), Object::toString))),
                false);

        return (int) tagCount;
    }

    private static int listTagElements(final CommandContext<CommandSourceStack> ctx, final int page) throws CommandSyntaxException {
        final ResourceKey<? extends Registry<?>> registryKey = CommandUtils.getResourceKey(ctx, "registry", ROOT_REGISTRY_KEY)
                .orElseThrow(); // Expect to always retrieve a resource key for the root registry (registry key)
        final Registry<?> registry = ctx.getSource().getServer().registryAccess().lookup(registryKey)
                .orElseThrow(() -> UNKNOWN_REGISTRY.create(registryKey.location()));

        final ResourceLocation tagLocation = ResourceLocationArgument.getId(ctx, "tag");
        final TagKey<?> tagKey = TagKey.create(cast(registryKey), tagLocation);

        @SuppressWarnings({ "unchecked", "rawtypes" })
        Optional<HolderSet.Named<?>> optional = registry.get(TagsCommand.<TagKey>cast(tagKey));
        final HolderSet.Named<?> tag = optional.orElseThrow(() -> UNKNOWN_TAG.create(tagKey.location(), registryKey.location()));

        ctx.getSource().sendSuccess(() -> createMessage(
                CommandUtils.makeTranslatableWithFallback("commands.neoforge.tags.tag_key",
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

    private static int queryElementTags(final CommandContext<CommandSourceStack> ctx, final int page) throws CommandSyntaxException {
        final ResourceKey<? extends Registry<?>> registryKey = CommandUtils.getResourceKey(ctx, "registry", ROOT_REGISTRY_KEY)
                .orElseThrow(); // Expect to always retrieve a resource key for the root registry (registry key)
        final Registry<?> registry = ctx.getSource().getServer().registryAccess().lookup(registryKey)
                .orElseThrow(() -> UNKNOWN_REGISTRY.create(registryKey.location()));

        final ResourceLocation elementLocation = ResourceLocationArgument.getId(ctx, "element");
        final ResourceKey<?> elementKey = ResourceKey.create(cast(registryKey), elementLocation);

        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Optional<Holder<?>> elementHolderOpt = registry.get(TagsCommand.<ResourceKey>cast(elementKey));
        final Holder<?> elementHolder = elementHolderOpt.orElseThrow(() -> UNKNOWN_ELEMENT.create(elementLocation, registryKey.location()));

        final long containingTagsCount = elementHolder.tags().count();

        ctx.getSource().sendSuccess(() -> createMessage(
                CommandUtils.makeTranslatableWithFallback("commands.neoforge.tags.element",
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
        final long totalPages = (count - 1) / PAGE_SIZE + 1;
        final long actualPage = (long) Mth.clamp(currentPage, 1, totalPages);

        MutableComponent containsComponent = CommandUtils.makeTranslatableWithFallback(containsText, count);
        if (count > 0) // Highlight the count text, make it clickable, and append page counters
        {
            final String clipboardText;
            final StringBuilder clipboardTextBuilder = new StringBuilder();
            boolean reachedLimit = false;
            int countedLines = 0;

            Iterator<String> iterator = names.get().sorted().iterator();
            while (iterator.hasNext()) {
                final String line = iterator.next();
                if (clipboardTextBuilder.length() + line.length() > CLIPBOARD_TEXT_LIMIT) {
                    // The to-be-added line puts us over the limit, so stop adding lines
                    reachedLimit = true;
                    break;
                }
                clipboardTextBuilder.append(line).append('\n');
                countedLines++;
            }
            // Remove the trailing newline if present
            if (!clipboardTextBuilder.isEmpty()) {
                clipboardTextBuilder.deleteCharAt(clipboardTextBuilder.length() - 1);
            }

            if (reachedLimit) {
                // Almost went over the limit; add additional info to clipboard text
                clipboardText = "(Too many entries to fit in clipboard, showing only first " + countedLines + " entries...)" + '\n'
                        + clipboardTextBuilder + '\n'
                        + "(..." + (count - countedLines) + " more entries not shown)";
            } else {
                clipboardText = clipboardTextBuilder.toString();
            }

            containsComponent = ComponentUtils.wrapInSquareBrackets(containsComponent.withStyle(s -> s
                    .withColor(ChatFormatting.GREEN)
                    .withClickEvent(new ClickEvent.CopyToClipboard(clipboardText))
                    .withHoverEvent(new HoverEvent.ShowText(CommandUtils.makeTranslatableWithFallback(copyHoverText)))));
            containsComponent = CommandUtils.makeTranslatableWithFallback("commands.neoforge.tags.page_info",
                    containsComponent, actualPage, totalPages);
        }

        final MutableComponent tagElements = Component.literal("").append(containsComponent);
        names.get()
                .sorted()
                .skip(PAGE_SIZE * (actualPage - 1))
                .limit(PAGE_SIZE)
                .map(Component::literal)
                .map(t -> t.withStyle(elementColor))
                .map(t -> CommandUtils.makeTranslatableWithFallback("\n - ").append(t))
                .forEach(tagElements::append);

        return header.append("\n").append(tagElements);
    }

    @SuppressWarnings("unchecked")
    private static <O> O cast(final Object input) {
        return (O) input;
    }
}
