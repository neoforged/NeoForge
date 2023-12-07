/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl.packet;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.simple.SimpleMessage;
import net.neoforged.testframework.conf.Feature;
import net.neoforged.testframework.impl.MutableTestFramework;

public record ChangeEnabledPacket(MutableTestFramework framework, String testId, boolean enabled) implements SimpleMessage {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(testId);
        buf.writeBoolean(enabled);
    }

    @Override
    public void handleMainThread(NetworkEvent.Context context) {
        switch (context.getDirection().getReceptionSide()) {
            case CLIENT -> {
                final Consumer<String> enablerer = enabled ? id -> framework.tests().enable(id) : id -> framework.tests().disable(id);
                enablerer.accept(testId);
            }
            case SERVER -> {
                final ServerPlayer player = Objects.requireNonNull(context.getSender());
                if (framework.configuration().isEnabled(Feature.CLIENT_MODIFICATIONS) && Objects.requireNonNull(player.getServer()).getPlayerList().isOp(player.getGameProfile())) {
                    framework.tests().byId(testId).ifPresent(test -> framework.setEnabled(test, enabled, player));
                }
            }
        }
    }

    public static ChangeEnabledPacket decode(MutableTestFramework framework, FriendlyByteBuf buf) {
        return new ChangeEnabledPacket(framework, buf.readUtf(), buf.readBoolean());
    }
}
