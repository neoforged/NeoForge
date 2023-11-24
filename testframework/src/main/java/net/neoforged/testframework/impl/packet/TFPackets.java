package net.neoforged.testframework.impl.packet;

import net.neoforged.testframework.impl.TestFrameworkInternal;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.simple.SimpleChannel;
import net.neoforged.neoforge.network.simple.SimpleMessage;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.BiFunction;

@ApiStatus.Internal
public record TFPackets(SimpleChannel channel, TestFrameworkInternal framework) {
    @SubscribeEvent
    public void onCommonSetup(final FMLCommonSetupEvent event) {
        class Registrar {
            private final SimpleChannel channel;
            int id = 0;

            Registrar(SimpleChannel channel) {
                this.channel = channel;
            }

            <P extends SimpleMessage> void register(Class<P> pkt, BiFunction<TestFrameworkInternal, FriendlyByteBuf, P> decoder) {
                channel.simpleMessageBuilder(pkt, id++)
                        .decoder(buf -> decoder.apply(framework, buf))
                        .add();
            }
        }

        final Registrar registrar = new Registrar(channel);
        registrar.register(ChangeStatusPacket.class, ChangeStatusPacket::decode);
        registrar.register(ChangeEnabledPacket.class, ChangeEnabledPacket::decode);
    }
}
