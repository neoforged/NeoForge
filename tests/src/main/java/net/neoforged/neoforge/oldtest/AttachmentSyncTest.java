/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

@Mod(AttachmentSyncTest.MOD_ID)
public class AttachmentSyncTest {
    public static final String MOD_ID = "attachment_sync_test";
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);
    private static final Supplier<AttachmentType<Integer>> ATTACHMENT_TYPE = ATTACHMENT_TYPES.register("test",
            () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    // TODO: use streamcodec version at some point
                    .sync(new AttachmentSyncHandler<Integer>() {
                        @Override
                        public void write(RegistryFriendlyByteBuf buf, Integer attachment, boolean initialSync) {
                            buf.writeInt(attachment);
                        }

                        @Override
                        public Integer read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable Integer previousValue) {
                            return buf.readInt();
                        }
                    })
                    .build());

    public AttachmentSyncTest(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);

        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> {
            registerCommands(event.getDispatcher(), "attachment_sync_test");
        });
    }

    @EventBusSubscriber(Dist.CLIENT)
    static class ClientOnly {
        @SubscribeEvent
        private static void registerClientCommands(RegisterClientCommandsEvent event) {
            registerCommands(event.getDispatcher(), "attachment_sync_test_client");
        }
    }

    private static final SimpleCommandExceptionType ERROR_NOT_A_BLOCK_ENTITY = new SimpleCommandExceptionType(Component.literal("Not a block entity"));

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, String commandName) {
        dispatcher.register(Commands.literal(commandName)
                .requires(source -> source.hasPermission(4))
                .then(Commands.literal("blockentity")
                        .then(
                                addGetSet(
                                        Commands.argument("pos", BlockPosArgument.blockPos()),
                                        context -> {
                                            var pos = BlockPosArgument.getBlockPos(context, "pos");
                                            var blockEntity = context.getSource().getUnsidedLevel().getBlockEntity(pos);
                                            if (blockEntity == null) {
                                                throw ERROR_NOT_A_BLOCK_ENTITY.create();
                                            }
                                            return blockEntity;
                                        })))
                .then(Commands.literal("chunk")
                        .then(
                                addGetSet(
                                        Commands.argument("pos", BlockPosArgument.blockPos()),
                                        context -> {
                                            var pos = BlockPosArgument.getBlockPos(context, "pos");
                                            return context.getSource().getUnsidedLevel().getChunkAt(pos);
                                        })))
                .then(Commands.literal("entity")
                        .then(
                                addGetSet(
                                        Commands.argument("entity", EntityArgument.entity()),
                                        context -> EntityArgument.getEntity(context, "entity"))))
                .then(
                        addGetSet(
                                Commands.literal("level"),
                                context -> context.getSource().getUnsidedLevel())));
    }

    private interface HolderFinder {
        IAttachmentHolder find(CommandContext<CommandSourceStack> source) throws CommandSyntaxException;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addGetSet(ArgumentBuilder<CommandSourceStack, ?> builder, HolderFinder holderFinder) {
        return builder
                .then(Commands.literal("get")
                        .executes(context -> {
                            var holder = holderFinder.find(context);
                            var data = holder.getExistingData(ATTACHMENT_TYPE).orElse(null);
                            context.getSource().sendSuccess(() -> Component.literal("Value of data: " + data), false);
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("set")
                        .then(Commands.argument("value", IntegerArgumentType.integer())
                                .executes(context -> {
                                    var holder = holderFinder.find(context);
                                    var data = IntegerArgumentType.getInteger(context, "value");
                                    var previousData = holder.setData(ATTACHMENT_TYPE, data);
                                    context.getSource().sendSuccess(() -> Component.literal("Previous value of data: " + previousData + ". New value: " + data), false);
                                    return Command.SINGLE_SUCCESS;
                                })));
    }
}
