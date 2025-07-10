package net.neoforged.neoforge.common.crafting.result;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
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

    public static <T> Codec<Result<T>> makeCodec(Codec<T> baseCodec, Codec<ResultType<? extends Result<T>>> registryCodec, Function<T, ? extends Result<T>> toResult) {
        return Codec.xor(baseCodec, registryCodec.<Result<T>>dispatch("neoforge:result_type", Result::type, ResultType::codec)).xmap(
                either -> Either.unwrap(either.mapLeft(toResult)),
                result -> result.isVanilla() ? Either.left(result.resolve()) : Either.right(result)
        );
    }

    private ResultHooks() {
    }
}
