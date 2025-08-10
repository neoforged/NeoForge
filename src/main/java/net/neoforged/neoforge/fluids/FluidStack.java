/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * {@link ItemStack} equivalent for fluids.
 * The main difference is that a fluid stack is always required to have an amount, while an item stack defaults to 1.
 * Another difference is that the component prototype of a fluid stack is currently always empty, while an item stack gets its component prototype from the item.
 *
 * <p>Most methods in this class are adapted from {@link ItemStack}.
 */
public final class FluidStack implements MutableDataComponentHolder {
    public static final Codec<Holder<Fluid>> FLUID_NON_EMPTY_CODEC = BuiltInRegistries.FLUID.holderByNameCodec().validate(holder -> {
        return holder.is(Fluids.EMPTY.builtInRegistryHolder()) ? DataResult.error(() -> {
            return "Fluid must not be minecraft:empty";
        }) : DataResult.success(holder);
    });
    /**
     * A standard map codec for fluid stacks that does not accept empty stacks.
     */
    public static final MapCodec<FluidStack> MAP_CODEC = MapCodec.recursive(
            "FluidStack",
            c -> RecordCodecBuilder.mapCodec(
                    instance -> instance.group(
                            FLUID_NON_EMPTY_CODEC.fieldOf("id").forGetter(FluidStack::getFluidHolder),
                            ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(FluidStack::getAmount), // note: no .orElse(1) compared to ItemStack
                            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                                    .forGetter(stack -> stack.components.asPatch()))
                            .apply(instance, FluidStack::new)));
    /**
     * A standard codec for fluid stacks that does not accept empty stacks.
     */
    public static final Codec<FluidStack> CODEC = Codec.lazyInitialized(MAP_CODEC::codec);

    /**
     * A standard codec for fluid stacks that always deserializes with a fixed amount,
     * and does not accept empty stacks.
     *
     * <p>Fluid equivalent of {@link ItemStack#SINGLE_ITEM_CODEC}.
     */
    public static Codec<FluidStack> fixedAmountCodec(int amount) {
        return Codec.lazyInitialized(
                () -> RecordCodecBuilder.create(
                        instance -> instance.group(
                                FLUID_NON_EMPTY_CODEC.fieldOf("id").forGetter(FluidStack::getFluidHolder),
                                DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                                        .forGetter(stack -> stack.components.asPatch()))
                                .apply(instance, (holder, patch) -> new FluidStack(holder, amount, patch))));
    }

    /**
     * A standard codec for fluid stacks that accepts empty stacks, serializing them as {@code {}}.
     */
    public static final Codec<FluidStack> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
            .xmap(optional -> optional.orElse(FluidStack.EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
    /**
     * A stream codec for fluid stacks that accepts empty stacks.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> OPTIONAL_STREAM_CODEC = new StreamCodec<>() {
        private static final StreamCodec<RegistryFriendlyByteBuf, Holder<Fluid>> FLUID_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.FLUID);

        @Override
        public FluidStack decode(RegistryFriendlyByteBuf buf) {
            int amount = buf.readVarInt();
            if (amount <= 0) {
                return FluidStack.EMPTY;
            } else {
                Holder<Fluid> holder = FLUID_STREAM_CODEC.decode(buf);
                DataComponentPatch patch = DataComponentPatch.STREAM_CODEC.decode(buf);
                return new FluidStack(holder, amount, patch);
            }
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, FluidStack stack) {
            if (stack.isEmpty()) {
                buf.writeVarInt(0);
            } else {
                buf.writeVarInt(stack.getAmount());
                FLUID_STREAM_CODEC.encode(buf, stack.getFluidHolder());
                DataComponentPatch.STREAM_CODEC.encode(buf, stack.components.asPatch());
            }
        }
    };
    /**
     * A stream codec for fluid stacks that does not accept empty stacks.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public FluidStack decode(RegistryFriendlyByteBuf buf) {
            FluidStack stack = FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
            if (stack.isEmpty()) {
                throw new DecoderException("Empty FluidStack not allowed");
            } else {
                return stack;
            }
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, FluidStack stack) {
            if (stack.isEmpty()) {
                throw new EncoderException("Empty FluidStack not allowed");
            } else {
                FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
            }
        }
    };
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final FluidStack EMPTY = new FluidStack(null);
    private int amount;
    private final Fluid fluid;
    private final PatchedDataComponentMap components;

    @Override
    public PatchedDataComponentMap getComponents() {
        return components;
    }

    public DataComponentPatch getComponentsPatch() {
        return !this.isEmpty() ? this.components.asPatch() : DataComponentPatch.EMPTY;
    }

    public DataComponentMap immutableComponents() {
        return !this.isEmpty() ? this.components.toImmutableMap() : DataComponentMap.EMPTY;
    }

    public boolean isComponentsPatchEmpty() {
        return !this.isEmpty() ? this.components.isPatchEmpty() : true;
    }

    public FluidStack(Holder<Fluid> fluid, int amount, DataComponentPatch patch) {
        this(fluid.value(), amount, patch);
    }

    public FluidStack(Fluid fluid, int amount, DataComponentPatch patch) {
        this(fluid, amount, PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch));
    }

    public FluidStack(Holder<Fluid> fluid, int amount) {
        this(fluid.value(), amount);
    }

    public FluidStack(Fluid fluid, int amount) {
        this(fluid, amount, new PatchedDataComponentMap(DataComponentMap.EMPTY));
    }

    private FluidStack(Fluid fluid, int amount, PatchedDataComponentMap components) {
        this.fluid = fluid;
        this.amount = amount;
        this.components = components;
    }

    private FluidStack(@Nullable Void unused) {
        this.fluid = null;
        this.components = new PatchedDataComponentMap(DataComponentMap.EMPTY);
    }

    /**
     * Checks if this fluid stack is empty.
     */
    public boolean isEmpty() {
        return this == EMPTY || this.fluid == Fluids.EMPTY || this.amount <= 0;
    }

    /**
     * Splits off a stack of the given amount of this stack and reduces this stack by the amount.
     */
    public FluidStack split(int amount) {
        int i = Math.min(amount, this.amount);
        FluidStack fluidStack = this.copyWithAmount(i);
        this.shrink(i);
        return fluidStack;
    }

    /**
     * Creates a copy of this stack with {@code 0} amount.
     */
    public FluidStack copyAndClear() {
        if (this.isEmpty()) {
            return EMPTY;
        } else {
            FluidStack fluidStack = this.copy();
            this.setAmount(0);
            return fluidStack;
        }
    }

    /**
     * Returns the fluid in this stack, or {@link Fluids#EMPTY} if this stack is empty.
     */
    public Fluid getFluid() {
        return this.isEmpty() ? Fluids.EMPTY : this.fluid;
    }

    public Holder<Fluid> getFluidHolder() {
        return this.getFluid().builtInRegistryHolder();
    }

    public boolean is(TagKey<Fluid> tag) {
        return this.getFluid().builtInRegistryHolder().is(tag);
    }

    public boolean is(Fluid fluid) {
        return this.getFluid() == fluid;
    }

    public boolean is(Predicate<Holder<Fluid>> holderPredicate) {
        return holderPredicate.test(this.getFluidHolder());
    }

    public boolean is(Holder<Fluid> holder) {
        return is(holder.value());
    }

    public boolean is(HolderSet<Fluid> holderSet) {
        return holderSet.contains(this.getFluidHolder());
    }

    public Stream<TagKey<Fluid>> getTags() {
        return this.getFluid().builtInRegistryHolder().tags();
    }

    /**
     * Creates a copy of this fluid stack.
     */
    public FluidStack copy() {
        if (this.isEmpty()) {
            return EMPTY;
        } else {
            return new FluidStack(this.fluid, this.amount, this.components.copy());
        }
    }

    /**
     * Creates a copy of this fluid stack with the given amount.
     */
    public FluidStack copyWithAmount(int amount) {
        if (this.isEmpty()) {
            return EMPTY;
        } else {
            FluidStack fluidStack = this.copy();
            fluidStack.setAmount(amount);
            return fluidStack;
        }
    }

    /**
     * Checks if the two fluid stacks are equal. This checks the fluid, amount, and components.
     *
     * @return {@code true} if the two fluid stacks have equal fluid, amount, and components
     */
    public static boolean matches(FluidStack first, FluidStack second) {
        if (first == second) {
            return true;
        } else {
            return first.getAmount() != second.getAmount() ? false : isSameFluidSameComponents(first, second);
        }
    }

    /**
     * Checks if the two fluid stacks have the same fluid. Ignores amount and components.
     *
     * @return {@code true} if the two fluid stacks have the same fluid
     */
    public static boolean isSameFluid(FluidStack first, FluidStack second) {
        return first.is(second.getFluid());
    }

    /**
     * Checks if the two fluid stacks have the same fluid and components. Ignores amount.
     *
     * @return {@code true} if the two fluid stacks have the same fluid and components
     */
    public static boolean isSameFluidSameComponents(FluidStack first, FluidStack second) {
        if (!first.is(second.getFluid())) {
            return false;
        } else {
            return first.isEmpty() && second.isEmpty() ? true : Objects.equals(first.components, second.components);
        }
    }

    public static MapCodec<FluidStack> lenientOptionalFieldOf(String fieldName) {
        return CODEC.lenientOptionalFieldOf(fieldName)
                .xmap(optional -> optional.orElse(EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
    }

    /**
     * Hashes the fluid and components of this stack, ignoring the amount.
     */
    public static int hashFluidAndComponents(@Nullable FluidStack stack) {
        if (stack != null) {
            int i = 31 + stack.getFluid().hashCode();
            return 31 * i + stack.getComponents().hashCode();
        } else {
            return 0;
        }
    }

    /**
     * Returns the {@link FluidType#getDescriptionId(FluidStack) description id} of this stack.
     */
    public String getDescriptionId() {
        return this.getFluidType().getDescriptionId(this);
    }

    @Override
    public String toString() {
        return this.getAmount() + " " + this.getFluid();
    }

    /**
     * Sets a data component.
     */
    @Nullable
    @Override
    public <T> T set(DataComponentType<T> type, @Nullable T component) {
        return this.components.set(type, component);
    }

    /**
     * Removes a data component.
     */
    @Nullable
    @Override
    public <T> T remove(DataComponentType<? extends T> type) {
        return this.components.remove(type);
    }

    /**
     * Applies a set of component changes to this stack.
     */
    @Override
    public void applyComponents(DataComponentPatch patch) {
        this.components.applyPatch(patch);
    }

    /**
     * Applies a set of component changes to this stack.
     */
    @Override
    public void applyComponents(DataComponentMap components) {
        this.components.setAll(components);
    }

    /**
     * Returns the hover name of this stack.
     */
    public Component getHoverName() {
        return getFluidType().getDescription(this);
    }

    /**
     * Returns the amount of this stack.
     */
    public int getAmount() {
        return this.isEmpty() ? 0 : this.amount;
    }

    /**
     * Sets the amount of this stack.
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Limits the amount of this stack is at most the given amount.
     */
    public void limitSize(int amount) {
        if (!this.isEmpty() && this.getAmount() > amount) {
            this.setAmount(amount);
        }
    }

    /**
     * Adds the given amount to this stack.
     */
    public void grow(int addedAmount) {
        this.setAmount(this.getAmount() + addedAmount);
    }

    /**
     * Removes the given amount from this stack.
     */
    public void shrink(int removedAmount) {
        this.grow(-removedAmount);
    }

    // Extra methods that are not directly adapted from ItemStack go below

    /**
     * Returns the fluid type of this stack.
     */
    public FluidType getFluidType() {
        return getFluid().getFluidType();
    }

    /**
     * Check if the fluid type of this stack is equal to the given fluid type.
     */
    public boolean is(FluidType fluidType) {
        return getFluidType() == fluidType;
    }
}
