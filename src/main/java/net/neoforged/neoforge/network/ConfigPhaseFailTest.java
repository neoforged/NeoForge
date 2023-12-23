package net.neoforged.neoforge.network;

import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import net.neoforged.neoforge.network.event.OnGameConfigurationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;

@Mod.EventBusSubscriber(modid = ConfigPhaseFailTest.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ConfigPhaseFailTest {
    static final String MOD_ID = NeoForgeVersion.MOD_ID;

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlerEvent event) {
        event.registrar(MOD_ID).configuration(
                ClientBreakConfigPayload.ID,
                ClientBreakConfigPayload::new,
                handler -> handler.client(ClientBreakConfigPayload::handle));
    }

    @SubscribeEvent
    public static void onCollectConfigTasks(OnGameConfigurationEvent event) {
        event.register(new BreakConfigTask());
    }

    private record ClientBreakConfigPayload() implements CustomPacketPayload {
        public static final ResourceLocation ID = new ResourceLocation(MOD_ID, "break_config");

        public ClientBreakConfigPayload(FriendlyByteBuf buf) {
            this();
        }

        @Override
        public void write(FriendlyByteBuf buffer) {}

        @Override
        public ResourceLocation id() {
            return ID;
        }

        public void handle(ConfigurationPayloadContext ctx) {
            ctx.packetHandler().disconnect(Component.literal("Thonk"));
        }
    }

    private static final class BreakConfigTask implements ICustomConfigurationTask {
        private static final Type TYPE = new Type(new ResourceLocation(MOD_ID, "break_config"));

        @Override
        public void run(Consumer<CustomPacketPayload> sender) {
            sender.accept(new ClientBreakConfigPayload());
        }

        @Override
        public Type type() {
            return TYPE;
        }
    }

    private ConfigPhaseFailTest() {}
}
