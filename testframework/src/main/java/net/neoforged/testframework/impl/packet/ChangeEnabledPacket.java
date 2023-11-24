package net.neoforged.testframework.impl.packet;

import net.neoforged.testframework.conf.Feature;
import net.neoforged.testframework.impl.TestFrameworkInternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.simple.SimpleMessage;

import java.util.Objects;
import java.util.function.Consumer;

public record ChangeEnabledPacket(TestFrameworkInternal framework, String testId, boolean enabled) implements SimpleMessage {
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

    public static ChangeEnabledPacket decode(TestFrameworkInternal framework, FriendlyByteBuf buf) {
        return new ChangeEnabledPacket(framework, buf.readUtf(), buf.readBoolean());
    }
}
