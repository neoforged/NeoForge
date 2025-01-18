package net.neoforged.neoforge.internal;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.loading.FMLLoader;

/**
 * Allows common code to call client-only methods, through {@code NeoForgeClientProxy}.
 *
 * <p>Try not to add methods to this class, there are generally better ways to
 * handle this kind of thing, possibly through different API design.
 */
public class NeoForgeProxy {
    public static final NeoForgeProxy INSTANCE = instantiate();

    private static NeoForgeProxy instantiate() {
        return switch (FMLLoader.getDist()) {
            case CLIENT -> {
                try {
                    yield (NeoForgeProxy) Class.forName("net.neoforged.neoforge.client.internal.NeoForgeClientProxy").getConstructor().newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Failed to instantiate client proxy", e);
                }
            }
            case DEDICATED_SERVER -> new NeoForgeProxy();
        };
    }

    public boolean isBlockInSolidLayer(BlockState state) {
        return false;
    }

    public void sendToServer(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        throw new UnsupportedOperationException("Cannot send serverbound payloads on the server");
    }
}
