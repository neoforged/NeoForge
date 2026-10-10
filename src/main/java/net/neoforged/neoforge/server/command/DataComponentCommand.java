/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

class DataComponentCommand {
    private static final SimpleCommandExceptionType ERROR_NO_ITEM = new SimpleCommandExceptionType(
            CommandUtils.makeTranslatableWithFallback("commands.neoforge.data_components.list.error.held_stack_empty"));

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("data_components")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("list")
                        .executes(DataComponentCommand::listComponents));
    }

    private static int listComponents(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            throw ERROR_NO_ITEM.create();
        }

        ctx.getSource().sendSuccess(() -> {
            // Use Item#getName() instead if ItemStack#getDisplayName() to display the actual item name without influence
            // of a written book's title or the ITEM_NAME or CUSTOM_NAME data components
            MutableComponent text = CommandUtils.makeTranslatableWithFallback("commands.neoforge.data_components.list.title", stack.getItem().getName(stack));
            DataComponentMap prototype = stack.getPrototype();
            DataComponentPatch patch = stack.getComponentsPatch();
            prototype.forEach(component -> {
                if (!patch.isPatched(component.type())) { // Component is default
                    Component tooltip = CommandUtils.makeTranslatableWithFallback(
                            "commands.neoforge.data_components.list.tooltip.default",
                            getTypeId(component.type()));
                    text.append(print(component.type(), component.value(), ChatFormatting.WHITE, tooltip));
                }
                Object data = patch.getPatch(component.type());
                if (data == null) { // Component is deleted
                    Component tooltip = CommandUtils.makeTranslatableWithFallback(
                            "commands.neoforge.data_components.list.tooltip.deleted",
                            getTypeId(component.type()),
                            component.value().toString());
                    text.append(print(component.type(), component.value(), ChatFormatting.RED, tooltip));
                } else { // Component is modified
                    Component tooltip = CommandUtils.makeTranslatableWithFallback(
                            "commands.neoforge.data_components.list.tooltip.modified",
                            getTypeId(component.type()),
                            component.value().toString(),
                            data.toString());
                    text.append(print(component.type(), data, ChatFormatting.YELLOW, tooltip));
                }
            });
            patch.keySet().forEach(type -> {
                Object value = patch.getPatch(type);
                if (!prototype.has(type) && value != null) { // New component added
                    Component tooltip = CommandUtils.makeTranslatableWithFallback(
                            "commands.neoforge.data_components.list.tooltip.added",
                            getTypeId(type),
                            value.toString());
                    text.append(print(type, value, ChatFormatting.GREEN, tooltip));
                }
            });
            return text;
        }, false);

        return Command.SINGLE_SUCCESS;
    }

    private static String getTypeId(DataComponentType<?> type) {
        return Objects.requireNonNull(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type)).toString();
    }

    private static Component print(DataComponentType<?> type, Object data, ChatFormatting color, Component tooltip) {
        MutableComponent entry = CommandUtils.makeTranslatableWithFallback("commands.neoforge.data_components.list.entry.key_value", getTypeId(type), data.toString());
        return CommandUtils.makeTranslatableWithFallback("commands.neoforge.data_components.list.entry", entry.withStyle(color))
                .withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(tooltip)));
    }
}
