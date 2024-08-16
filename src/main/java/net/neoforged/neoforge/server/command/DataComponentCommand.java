/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Objects;
import java.util.Optional;
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
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("data_components")
                .then(Commands.literal("list")
                        .requires(cs -> cs.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(DataComponentCommand::listComponents));
    }

    @SuppressWarnings("OptionalAssignedToNull")
    private static int listComponents(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().source instanceof ServerPlayer player)) {
            ctx.getSource().sendFailure(Component.translatable("commands.neoforge.data_components.list.error.not_a_player"));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("commands.neoforge.data_components.list.error.held_stack_empty"));
            return 0;
        }

        ctx.getSource().sendSuccess(() -> {
            MutableComponent text = Component.translatable("commands.neoforge.data_components.list.title", stack.getItem().getName(stack));
            DataComponentMap prototype = stack.getPrototype();
            DataComponentPatch patch = stack.getComponentsPatch();
            prototype.forEach(component -> {
                Optional<?> optData = patch.get(component.type());
                if (optData == null) { // Component is default
                    Component tooltip = Component.translatable(
                            "commands.neoforge.data_components.list.tooltip.default",
                            getTypeId(component.type()));
                    text.append(print(component.type(), component.value(), ChatFormatting.WHITE, tooltip));
                } else if (optData.isEmpty()) { // Component is deleted
                    Component tooltip = Component.translatable(
                            "commands.neoforge.data_components.list.tooltip.deleted",
                            getTypeId(component.type()),
                            component.value().toString());
                    text.append(print(component.type(), component.value(), ChatFormatting.RED, tooltip));
                } else { // Component is modified
                    Component tooltip = Component.translatable(
                            "commands.neoforge.data_components.list.tooltip.modified",
                            getTypeId(component.type()),
                            component.value().toString(),
                            optData.get().toString());
                    text.append(print(component.type(), optData.get(), ChatFormatting.YELLOW, tooltip));
                }
            });
            patch.entrySet().forEach(entry -> {
                if (!prototype.has(entry.getKey()) && entry.getValue().isPresent()) { // New component added
                    Component tooltip = Component.translatable(
                            "commands.neoforge.data_components.list.tooltip.added",
                            getTypeId(entry.getKey()),
                            entry.getValue().get().toString());
                    text.append(print(entry.getKey(), entry.getValue().get(), ChatFormatting.GREEN, tooltip));
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
        MutableComponent entry = Component.translatable("commands.neoforge.data_components.list.entry.key_value", getTypeId(type), data.toString());
        return Component.translatable("commands.neoforge.data_components.list.entry", entry.withStyle(color))
                .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip)));
    }
}