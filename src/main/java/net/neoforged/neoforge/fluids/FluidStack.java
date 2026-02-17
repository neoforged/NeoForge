/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.event.EventHooks;
import org.jspecify.annotations.Nullable;

/**
 * {@link ItemStack} equivalent for fluids.
 * The main difference is that a fluid stack is always required to have an amount, while an item stack defaults to 1.
 * Another difference is that the component prototype of a fluid stack is currently always empty, while an item stack gets its component prototype from the item.
 *
 * <p>Most methods in this class are adapted from {@link ItemStack}.
 */
public final class FluidStack implements MutableDataComponentHolder, FluidInstance {
    /**
     * A standard map codec for fluid stacks that does not accept empty stacks.
     */
    public static final MapCodec<FluidStack> MAP_CODEC = MapCodec.recursive(
            "FluidStack",
            c -> RecordCodecBuilder.mapCodec(
                    instance -> instance.group(
                            FLUID_HOLDER_CODEC_WITH_BOUND_COMPONENTS.fieldOf(FIELD_ID).forGetter(FluidStack::typeHolder),
                            ExtraCodecs.POSITIVE_INT.fieldOf(FIELD_AMOUNT).forGetter(FluidStack::getAmount), // note: no .orElse(1) compared to ItemStack
                            DataComponentPatch.CODEC.optionalFieldOf(FIELD_COMPONENTS, DataComponentPatch.EMPTY)
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
                                FLUID_HOLDER_CODEC.fieldOf(FIELD_ID).forGetter(FluidStack::typeHolder),
                                DataComponentPatch.CODEC.optionalFieldOf(FIELD_COMPONENTS, DataComponentPatch.EMPTY)
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
        @Override
        public FluidStack decode(RegistryFriendlyByteBuf buf) {
            int amount = buf.readVarInt();
            if (amount <= 0) {
                return FluidStack.EMPTY;
            } else {
                Holder<Fluid> holder = FLUID_HOLDER_STREAM_CODEC.decode(buf);
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
                FLUID_HOLDER_STREAM_CODEC.encode(buf, stack.typeHolder());
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
    public static final FluidStack EMPTY = new FluidStack(null);
    private int amount;
    private final @Nullable Holder<Fluid> fluid;
    private final PatchedDataComponentMap components;

    @Override
    public DataComponentMap getComponents() {
        return isEmpty() ? DataComponentMap.EMPTY : components;
    }

    public DataComponentMap getPrototype() {
        return isEmpty() ? DataComponentMap.EMPTY : typeHolder().components();
    }

    public DataComponentPatch getComponentsPatch() {
        return !this.isEmpty() ? this.components.asPatch() : DataComponentPatch.EMPTY;
    }

    public DataComponentMap immutableComponents() {
        return !this.isEmpty() ? this.components.toImmutableMap() : DataComponentMap.EMPTY;
    }

    public boolean hasNonDefault(DataComponentType<?> type) {
        return !isEmpty() && components.hasNonDefault(type);
    }

    public boolean isComponentsPatchEmpty() {
        return !this.isEmpty() ? this.components.isPatchEmpty() : true;
    }

    public FluidStack(Fluid fluid, int amount, DataComponentPatch patch) {
        this(fluid.builtInRegistryHolder(), amount, patch);
    }

    public FluidStack(Fluid fluid, int amount) {
        this(fluid, amount, DataComponentPatch.EMPTY);
    }

    public FluidStack(Holder<Fluid> fluid, int amount) {
        this(fluid, amount, DataComponentPatch.EMPTY);
    }

    public FluidStack(Holder<Fluid> fluid, int amount, DataComponentPatch patch) {
        this(fluid, amount, PatchedDataComponentMap.fromPatch(fluid.components(), patch));
    }

    private FluidStack(Holder<Fluid> fluid, int amount, PatchedDataComponentMap components) {
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
        return this == EMPTY || fluid.value().isSame(Fluids.EMPTY) || this.amount <= 0;
    }

    /**
     * Splits off a stack of the given amount of this stack and reduces this stack by the amount.
     */
    public FluidStack split(int amount) {
        int i = Math.min(amount, getAmount());
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
        return typeHolder().value();
    }

    @Override
    public Holder<Fluid> typeHolder() {
        return isEmpty() ? Fluids.EMPTY.builtInRegistryHolder() : fluid;
    }

    public boolean is(Predicate<Holder<Fluid>> holderPredicate) {
        return holderPredicate.test(this.typeHolder());
    }

    /**
     * Creates a copy of this fluid stack.
     */
    public FluidStack copy() {
        if (this.isEmpty()) {
            return EMPTY;
        } else {
            return new FluidStack(typeHolder(), amount(), this.components.copy());
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

    public FluidStack transmuteCopy(Fluid newFluid) {
        return transmuteCopy(newFluid, amount());
    }

    public FluidStack transmuteCopy(Fluid newFluid, int newAmount) {
        return isEmpty() ? EMPTY : transmuteCopyIgnoreEmpty(newFluid, newAmount);
    }

    private FluidStack transmuteCopyIgnoreEmpty(Fluid newFluid, int newAmount) {
        return new FluidStack(newFluid, newAmount, components.asPatch());
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

    /// Compares a fluidstack with a [FluidStackTemplate] as per [#matches(FluidStack, FluidStack)].
    public static boolean matches(FluidStack a, @Nullable FluidStackTemplate b) {
        if (b == null) {
            return a.isEmpty();
        }

        return a.amount() == b.amount() && isSameFluidSameComponents(a, b);
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

    /// {@return true if a and b refer to the same fluid, or if a is empty and b is null}
    public static boolean isSameFluid(FluidStack a, @Nullable FluidStackTemplate b) {
        return b == null ? a.isEmpty() : a.is(b.fluid());
    }

    /// Compares the fluid and components of this stack against a [FluidStackTemplate].
    ///
    /// @return True if either this stack is empty and the template is null, or they reference the same fluid and have equivalent component patches.
    public static boolean isSameFluidSameComponents(FluidStack a, @Nullable FluidStackTemplate b) {
        if (a.isEmpty() || b == null) {
            return a.isEmpty() == (b == null);
        } else {
            return a.is(b.fluid()) && a.components.patchEquals(b.components());
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
     * Builds the tooltip lines for this fluid stack, intended for use by mods
     * rendering fluids in GUIs.
     *
     * <p>This mirrors the behavior of
     * {@link ItemStack#getTooltipLines(Item.TooltipContext, Player, TooltipFlag)}
     * as closely as possible for fluids.</p>
     *
     * <p>The tooltip consists of:
     * <ul>
     * <li>The styled hover name</li>
     * <li>Additional lines provided by the fluid itself</li>
     * <li>Lines added by {@link EventHooks#onFluidTooltip}</li>
     * <li>The registry name when advanced tooltips are enabled</li>
     * </ul>
     * </p>
     *
     * <p>If tooltips are hidden via {@link TooltipDisplay} and the tooltip is not
     * being rendered in creative mode, an empty list is returned.</p>
     *
     * @param context the tooltip context
     * @param player  the player viewing the tooltip, or {@code null}
     * @param flag    controls tooltip verbosity and advanced information
     * @return a list of tooltip components, possibly empty
     */
    public List<Component> getTooltipLines(Item.TooltipContext context, @Nullable Player player, TooltipFlag flag) {
        TooltipDisplay tooltipDisplay = this.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
        if (!flag.isCreative() && tooltipDisplay.hideTooltip()) {
            return List.of();
        } else {
            Fluid fluid = getFluid();
            List<Component> list = Lists.newArrayList();
            list.add(this.getHoverName());
            fluid.appendHoverText(this, context, tooltipDisplay, list::add, flag);
            EventHooks.onFluidTooltip(this, player, list, flag, context);
            if (flag.isAdvanced()) {
                list.add(Component.literal(BuiltInRegistries.FLUID.getKey(fluid).toString()).withStyle(ChatFormatting.DARK_GRAY));
                int componentCount = this.components.size();
                if (componentCount > 0) {
                    list.add(Component.translatable("item.components", componentCount).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
            return list;
        }
    }

    /**
     * Sets a data component.
     */
    @Nullable
    @Override
    public <T> T set(DataComponentType<T> type, @Nullable T component) {
        return this.components.set(type, component);
    }

    public <T> @Nullable T set(TypedDataComponent<T> value) {
        return components.set(value);
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

    @Override
    public int amount() {
        return this.isEmpty() ? 0 : this.amount;
    }

    /**
     * Returns the amount of this stack.
     */
    public int getAmount() {
        return amount();
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
}
