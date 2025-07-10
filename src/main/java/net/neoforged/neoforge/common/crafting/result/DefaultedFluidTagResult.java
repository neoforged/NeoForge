package net.neoforged.neoforge.common.crafting.result;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.NeoForgeEventHandler;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Optional;

/**
 * Represents a fluid-based recipe result that represents the fluid as a tag-fallback combination.
 * {@link DefaultedFluidTagResult#resolve()} resolves that combination into a concrete {@link FluidStack}.
 */
public class DefaultedFluidTagResult implements Result<FluidStack> {
    public static final MapCodec<DefaultedFluidTagResult> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            TagKey.codec(Registries.FLUID).fieldOf("tag").forGetter(it -> it.tagKey),
            BuiltInRegistries.FLUID.holderByNameCodec().fieldOf("fallback").forGetter(it -> it.fallback),
            ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(it -> it.amount),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(it -> it.components)
    ).apply(inst, DefaultedFluidTagResult::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DefaultedFluidTagResult> STREAM_CODEC = StreamCodec.composite(
            TagKey.streamCodec(Registries.FLUID), it -> it.tagKey,
            FluidStack.FLUID_STREAM_CODEC, it -> it.fallback,
            ByteBufCodecs.VAR_INT, it -> it.amount,
            DataComponentPatch.STREAM_CODEC, it -> it.components,
            DefaultedFluidTagResult::new);

    private final TagKey<Fluid> tagKey;
    private final Holder<Fluid> fallback;
    private final int amount;
    private final DataComponentPatch components;

    /**
     * @param tagKey     The {@link TagKey} to use for looking up the result.
     * @param fallback   The fallback to use if the tag-based lookup did not yield a conclusive result.
     * @param amount     The amount to use. Corresponds to {@link FluidStack#getAmount()}.
     * @param components The data components to use. Corresponds to {@link FluidStack#getComponents()}.
     */
    public DefaultedFluidTagResult(TagKey<Fluid> tagKey, Holder<Fluid> fallback, int amount, DataComponentPatch components) {
        this.tagKey = tagKey;
        this.fallback = fallback;
        this.amount = amount;
        this.components = components;
    }

    /**
     * @param tagKey   The {@link TagKey} to use for looking up the result.
     * @param fallback The fallback to use if the tag-based lookup did not yield a conclusive result.
     * @param amount   The amount to use. Corresponds to {@link FluidStack#getAmount()}.
     */
    public DefaultedFluidTagResult(TagKey<Fluid> tagKey, Holder<Fluid> fallback, int amount) {
        this(tagKey, fallback, amount, DataComponentPatch.EMPTY);
    }

    /**
     * @param tagKey   The {@link TagKey} to use for looking up the result.
     * @param fallback The fallback to use if the tag-based lookup did not yield a conclusive result.
     */
    public DefaultedFluidTagResult(TagKey<Fluid> tagKey, Holder<Fluid> fallback) {
        this(tagKey, fallback, 1, DataComponentPatch.EMPTY);
    }

    @Override
    public FluidStack resolve() {
        Optional<Fluid> optional = NeoForgeEventHandler.getTagDefaultsManager().resolve(Registries.FLUID, tagKey);
        return new FluidStack(optional.<Holder<Fluid>>map(Fluid::builtInRegistryHolder).orElse(fallback), amount, components);
    }

    @Override
    public ResultType<? extends Result<FluidStack>> type() {
        return NeoForgeMod.DEFAULTED_FLUID_TAG_RESULT_TYPE.get();
    }

    @Override
    public SlotDisplay display() {
        return new Display(this);
    }

    public record Display(DefaultedFluidTagResult result) implements FluidResultSlotDisplay {
        public static final MapCodec<Display> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                DefaultedFluidTagResult.MAP_CODEC.fieldOf("result").forGetter(Display::result)
        ).apply(inst, Display::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Display> STREAM_CODEC = StreamCodec.composite(
                DefaultedFluidTagResult.STREAM_CODEC, Display::result,
                Display::new);

        @Override
        public Type<? extends SlotDisplay> type() {
            return NeoForgeMod.DEFAULTED_FLUID_TAG_RESULT_SLOT_DISPLAY.get();
        }
    }
}
