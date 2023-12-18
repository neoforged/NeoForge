/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.testframework.conf.Feature;
import net.neoforged.testframework.impl.MutableTestFramework;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public record ChangeEnabledPacket(MutableTestFramework framework, String testId, boolean enabled) implements CustomPacketPayload {
   
    public static final ResourceLocation ID = new ResourceLocation("neoforge", "tf_change_enabled");
    
    public void handle(PlayPayloadContext context) {
        switch (context.flow().getReceptionSide()) {
            case CLIENT -> {
                final Consumer<String> enablerer = enabled ? id -> framework.tests().enable(id) : id -> framework.tests().disable(id);
                enablerer.accept(testId);
            }
            case SERVER -> {
                final Player player = context.sender().orElseThrow();
                if (framework.configuration().isEnabled(Feature.CLIENT_MODIFICATIONS) && Objects.requireNonNull(player.getServer()).getPlayerList().isOp(player.getGameProfile())) {
                    framework.tests().byId(testId).ifPresent(test -> framework.setEnabled(test, enabled, player));
                }
            }
        }
    }

    public static ChangeEnabledPacket decode(MutableTestFramework framework, FriendlyByteBuf buf) {
        return new ChangeEnabledPacket(framework, buf.readUtf(), buf.readBoolean());
    }
    
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(testId);
        buf.writeBoolean(enabled);
    }
    
    @Override
    public @NotNull ResourceLocation id() {
        return ID;
    }
}
