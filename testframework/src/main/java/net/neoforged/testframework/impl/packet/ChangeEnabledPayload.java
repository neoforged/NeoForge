/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl.packet;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.testframework.conf.Feature;
import net.neoforged.testframework.impl.MutableTestFramework;

public record ChangeEnabledPayload(MutableTestFramework framework, String testId, boolean enabled) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation("neoforge", "tf_change_enabled");

    public void handle(PlayPayloadContext context) {
        switch (context.flow().getReceptionSide()) {
            case CLIENT -> {
                final Consumer<String> enablerer = enabled ? id -> framework.tests().enable(id) : id -> framework.tests().disable(id);
                enablerer.accept(testId);
            }
            case SERVER -> {
                final Player player = context.player().orElseThrow();
                if (framework.configuration().isEnabled(Feature.CLIENT_MODIFICATIONS) && Objects.requireNonNull(player.getServer()).getPlayerList().isOp(player.getGameProfile())) {
                    framework.tests().byId(testId).ifPresent(test -> framework.setEnabled(test, enabled, player));
                }
            }
        }
    }

    public static ChangeEnabledPayload decode(MutableTestFramework framework, FriendlyByteBuf buf) {
        return new ChangeEnabledPayload(framework, buf.readUtf(), buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(testId);
        buf.writeBoolean(enabled);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
