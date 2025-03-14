package net.neoforged.neoforge.network.payload;

import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.connection.ConnectionType;

import java.util.List;

/**
 * We use this to transfer the actual recipe content from server to client.
 */
public final class RecipeContentPayload implements CustomPacketPayload {
    public static final Type<RecipeContentPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "recipe_content"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeContentPayload> STREAM_CODEC = StreamCodec.ofMember(
            RecipeContentPayload::write,
            RecipeContentPayload::read
    );
    private static final StreamCodec<RegistryFriendlyByteBuf, List<RecipeHolder<?>>> RECIPES_STREAM_CODEC = RecipeHolder.STREAM_CODEC.apply(ByteBufCodecs.list());

    private volatile RegistryFriendlyByteBuf cachedPayload;

    private static RecipeContentPayload read(RegistryFriendlyByteBuf buffer) {
        var recipes = RECIPES_STREAM_CODEC.decode(buffer);
        return new RecipeContentPayload(recipes);
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        if (cachedPayload == null) {
            synchronized (this) {
                var cachedBuffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), buffer.registryAccess(), ConnectionType.NEOFORGE);
                RECIPES_STREAM_CODEC.encode(buffer, recipes);
                cachedPayload = cachedBuffer;
            }
        }
        buffer.writeBytes(cachedPayload, 0, cachedPayload.readableBytes());
    }

    private final List<RecipeHolder<?>> recipes;

    public RecipeContentPayload(
            List<RecipeHolder<?>> recipes
    ) {
        this.recipes = recipes;
    }

    @Override
    public Type<RecipeContentPayload> type() {
        return TYPE;
    }

    public List<RecipeHolder<?>> recipes() {
        return recipes;
    }
}
