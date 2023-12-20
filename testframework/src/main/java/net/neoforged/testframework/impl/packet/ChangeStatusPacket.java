/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl.packet;

import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.simple.SimpleMessage;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.conf.Feature;
import net.neoforged.testframework.impl.MutableTestFramework;

public record ChangeStatusPacket(MutableTestFramework framework, String testId, Test.Status status) implements SimpleMessage {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(testId);
        buf.writeEnum(status.result());
        buf.writeUtf(status.message());
    }

    @Override
    public void handleMainThread(NetworkEvent.Context context) {
        switch (context.getDirection().getReceptionSide()) {
            case CLIENT -> framework.tests().setStatus(testId, status);
            case SERVER -> {
                final ServerPlayer player = Objects.requireNonNull(context.getSender());
                if (framework.configuration().isEnabled(Feature.CLIENT_MODIFICATIONS) && Objects.requireNonNull(player.getServer()).getPlayerList().isOp(player.getGameProfile())) {
                    framework.tests().byId(testId).ifPresent(test -> framework.changeStatus(test, status, player));
                }
            }
        }
    }

    public static ChangeStatusPacket decode(MutableTestFramework framework, FriendlyByteBuf buf) {
        return new ChangeStatusPacket(framework, buf.readUtf(), new Test.Status(buf.readEnum(Test.Result.class), buf.readUtf()));
    }
}
