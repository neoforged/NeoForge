/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl.packet;

import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.conf.Feature;
import net.neoforged.testframework.impl.MutableTestFramework;
import org.jetbrains.annotations.NotNull;

public record ChangeStatusPayload(MutableTestFramework framework, String testId, Test.Status status) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation("neoforge", "tf_change_status");

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(testId);
        buf.writeEnum(status.result());
        buf.writeUtf(status.message());
    }

    @Override
    public @NotNull ResourceLocation id() {
        return ID;
    }

    public void handle(PlayPayloadContext context) {
        switch (context.flow().getReceptionSide()) {
            case CLIENT -> framework.tests().setStatus(testId, status);
            case SERVER -> {
                final Player player = context.sender().orElseThrow();
                if (framework.configuration().isEnabled(Feature.CLIENT_MODIFICATIONS) && Objects.requireNonNull(player.getServer()).getPlayerList().isOp(player.getGameProfile())) {
                    framework.tests().byId(testId).ifPresent(test -> framework.changeStatus(test, status, player));
                }
            }
        }
    }

    public static ChangeStatusPayload decode(MutableTestFramework framework, FriendlyByteBuf buf) {
        return new ChangeStatusPayload(framework, buf.readUtf(), new Test.Status(buf.readEnum(Test.Result.class), buf.readUtf()));
    }
}
