package net.neoforged.neoforge.common.crafting.result;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Function;

/**
 * Helper class for various result-related hooks.
 */
public final class ResultHooks {
    public static final Codec<Result<ItemStack>> ITEM_STACK_RESULT_CODEC = makeCodec(ItemStack.STRICT_CODEC, NeoForgeRegistries.ITEM_RESULT_TYPES.byNameCodec(), ResultWrapper::item);
    public static final Codec<Result<FluidStack>> FLUID_STACK_RESULT_CODEC = makeCodec(FluidStack.CODEC, NeoForgeRegistries.FLUID_RESULT_TYPES.byNameCodec(), ResultWrapper::fluid);
    private static final int CUSTOM_RESULT_MARKER = -1000;
    public static final StreamCodec<RegistryFriendlyByteBuf, Result<ItemStack>> ITEM_STACK_RESULT_STREAM_CODEC = makeStreamCodec(
            ItemStack.STREAM_CODEC,
            ByteBufCodecs.registry(NeoForgeRegistries.Keys.ITEM_RESULT_TYPES).dispatch(Result::type, ResultType::streamCodec),
            ResultWrapper::item
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, Result<FluidStack>> FLUID_STACK_RESULT_STREAM_CODEC = makeStreamCodec(
            FluidStack.STREAM_CODEC,
            ByteBufCodecs.registry(NeoForgeRegistries.Keys.FLUID_RESULT_TYPES).dispatch(Result::type, ResultType::streamCodec),
            ResultWrapper::fluid
    );

    /**
     * Creates a new codec for {@link Result}s.
     *
     * @param vanillaCodec  The codec to use for vanilla values.
     * @param registryCodec The {@link Result} registry codec to use for non-vanilla values.
     * @param toResult      A {@code T} -> {@code Result<T>} converter.
     * @return A codec for {@link Result}s.
     * @param <T> The {@link Result}'s generic type.
     * @see net.neoforged.neoforge.common.crafting.IngredientCodecs#codec(Codec)
     */
    public static <T> Codec<Result<T>> makeCodec(Codec<T> vanillaCodec, Codec<ResultType<? extends Result<T>>> registryCodec, Function<T, ? extends Result<T>> toResult) {
        return Codec.xor(vanillaCodec, registryCodec.<Result<T>>dispatch("neoforge:result_type", Result::type, ResultType::codec)).xmap(
                either -> Either.unwrap(either.mapLeft(toResult)),
                result -> result.isVanilla() ? Either.left(result.resolve()) : Either.right(result)
        );
    }

    /**
     * Creates a new stream codec for {@link Result}s.
     *
     * @param vanillaCodec The stream codec to use for vanilla values.
     * @param resultCodec  The {@link Result} stream codec to use for non-vanilla values.
     * @param toResult     A {@code T} -> {@code Result<T>} converter.
     * @return A stream codec for {@link Result}s.
     * @param <T> The {@link Result}'s generic type.
     * @see net.neoforged.neoforge.common.crafting.IngredientCodecs#streamCodec(StreamCodec)
     */
    public static <T> StreamCodec<RegistryFriendlyByteBuf, Result<T>> makeStreamCodec(
            StreamCodec<RegistryFriendlyByteBuf, T> vanillaCodec,
            StreamCodec<RegistryFriendlyByteBuf, Result<T>> resultCodec,
            Function<T, ? extends Result<T>> toResult
    ) {
        return new StreamCodec<>() {
            @Override
            public Result<T> decode(RegistryFriendlyByteBuf buf) {
                var readerIndex = buf.readerIndex();
                var length = buf.readVarInt();
                if (length == CUSTOM_RESULT_MARKER) {
                    return resultCodec.decode(buf);
                } else {
                    buf.readerIndex(readerIndex);
                    return toResult.apply(vanillaCodec.decode(buf));
                }
            };

            @Override
            public void encode(RegistryFriendlyByteBuf buf, Result<T> result) {
                if (!result.isVanilla() && buf.getConnectionType().isNeoForge()) {
                    buf.writeVarInt(CUSTOM_RESULT_MARKER);
                    resultCodec.encode(buf, result);
                } else {
                    vanillaCodec.encode(buf, result.resolve());
                }
            }
        };
    }

    private ResultHooks() {
    }
}
