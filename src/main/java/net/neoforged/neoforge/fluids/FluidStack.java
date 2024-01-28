/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * ItemStack substitute for Fluids.
 *
 * NOTE: Equality is based on the Fluid, not the amount. Use
 * {@link #isFluidStackIdentical(FluidStack)} to determine if FluidID, Amount and NBT Tag are all
 * equal.
 *
 */
public class FluidStack {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);

    public static final Codec<FluidStack> CODEC = RecordCodecBuilder.create(
            instance -> instance
                    .group(
                            BuiltInRegistries.FLUID.byNameCodec().fieldOf("FluidName").forGetter(FluidStack::getFluid),
                            Codec.INT.fieldOf("Amount").forGetter(FluidStack::getAmount),
                            CompoundTag.CODEC.optionalFieldOf("Tag").forGetter(stack -> Optional.ofNullable(stack.getTag())))
                    .apply(instance, (fluid, amount, tag) -> {
                        FluidStack stack = new FluidStack(fluid, amount);
                        tag.ifPresent(stack::setTag);
                        return stack;
                    }));

    private boolean isEmpty;
    private int amount;
    @Nullable
    private CompoundTag tag;
    private final Fluid fluid;

    public FluidStack(Fluid fluid, int amount) {
        if (fluid == null) {
            LOGGER.fatal("Null fluid supplied to fluidstack. Did you try and create a stack for an unregistered fluid?");
            throw new IllegalArgumentException("Cannot create a fluidstack from a null fluid");
        } else if (!BuiltInRegistries.FLUID.containsValue(fluid)) {
            LOGGER.fatal("Failed attempt to create a FluidStack for an unregistered Fluid {} (type {})", fluid, fluid.getClass().getName());
            throw new IllegalArgumentException("Cannot create a fluidstack from an unregistered fluid");
        }
        this.amount = amount;
        this.fluid = fluid;

        updateEmpty();
    }

    public FluidStack(Holder<Fluid> fluid, int amount) {
        this(fluid.value(), amount);
    }

    public FluidStack(Fluid fluid, int amount, @Nullable CompoundTag nbt) {
        this(fluid, amount);

        if (nbt != null) {
            tag = nbt.copy();
        }
    }

    public FluidStack(Holder<Fluid> fluid, int amount, @Nullable CompoundTag nbt) {
        this(fluid.value(), amount, nbt);
    }

    public FluidStack(FluidStack stack, int amount) {
        this(stack.getFluid(), amount, stack.tag);
    }

    /**
     * This provides a safe method for retrieving a FluidStack - if the Fluid is invalid, the stack
     * will return as null.
     */
    public static FluidStack loadFluidStackFromNBT(CompoundTag nbt) {
        if (nbt == null) {
            return EMPTY;
        }
        if (!nbt.contains("FluidName", Tag.TAG_STRING)) {
            return EMPTY;
        }

        ResourceLocation fluidName = new ResourceLocation(nbt.getString("FluidName"));
        Fluid fluid = BuiltInRegistries.FLUID.get(fluidName);
        if (fluid == Fluids.EMPTY) {
            return EMPTY;
        }
        FluidStack stack = new FluidStack(fluid, nbt.getInt("Amount"));

        if (nbt.contains("Tag", Tag.TAG_COMPOUND)) {
            stack.tag = nbt.getCompound("Tag");
        }
        return stack;
    }

    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.putString("FluidName", BuiltInRegistries.FLUID.getKey(getFluid()).toString());
        nbt.putInt("Amount", amount);

        if (tag != null) {
            nbt.put("Tag", tag);
        }
        return nbt;
    }

    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeId(BuiltInRegistries.FLUID, getFluid());
        buf.writeVarInt(getAmount());
        buf.writeNbt(tag);
    }

    public static FluidStack readFromPacket(FriendlyByteBuf buf) {
        Fluid fluid = buf.readById(BuiltInRegistries.FLUID);
        int amount = buf.readVarInt();
        CompoundTag tag = buf.readNbt();
        if (fluid == Fluids.EMPTY) return EMPTY;
        return new FluidStack(fluid, amount, tag);
    }

    public final Fluid getFluid() {
        return isEmpty ? Fluids.EMPTY : getRawFluid();
    }

    public final Fluid getRawFluid() {
        return fluid;
    }

    public final FluidType getFluidType() {
        return getFluid().getFluidType();
    }

    public final Holder<Fluid> getFluidHolder() {
        return getFluid().builtInRegistryHolder();
    }

    public final boolean is(TagKey<Fluid> tag) {
        return getFluidHolder().is(tag);
    }

    public final boolean is(Fluid fluid) {
        return getFluid() == fluid;
    }

    public final boolean is(FluidType fluidType) {
        return getFluidType() == fluidType;
    }

    public final boolean is(Holder<Fluid> holder) {
        return is(holder.value());
    }

    public final boolean is(HolderSet<Fluid> holderSet) {
        return holderSet.contains(getFluidHolder());
    }

    public final Stream<TagKey<Fluid>> getTags() {
        return getFluidHolder().tags();
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    protected void updateEmpty() {
        isEmpty = fluid == Fluids.EMPTY || amount <= 0;
    }

    public int getAmount() {
        return isEmpty ? 0 : amount;
    }

    public void setAmount(int amount) {
        if (fluid == Fluids.EMPTY) throw new IllegalStateException("Can't modify the empty stack.");
        this.amount = amount;
        updateEmpty();
    }

    public void grow(int amount) {
        setAmount(this.amount + amount);
    }

    public void shrink(int amount) {
        setAmount(this.amount - amount);
    }

    public boolean hasTag() {
        return tag != null;
    }

    @Nullable
    public CompoundTag getTag() {
        return tag;
    }

    public void setTag(CompoundTag tag) {
        if (fluid == Fluids.EMPTY) throw new IllegalStateException("Can't modify the empty stack.");
        this.tag = tag;
    }

    public CompoundTag getOrCreateTag() {
        if (tag == null)
            setTag(new CompoundTag());
        return tag;
    }

    public CompoundTag getChildTag(String childName) {
        if (tag == null)
            return null;
        return tag.getCompound(childName);
    }

    public CompoundTag getOrCreateChildTag(String childName) {
        getOrCreateTag();
        CompoundTag child = tag.getCompound(childName);
        if (!tag.contains(childName, Tag.TAG_COMPOUND)) {
            tag.put(childName, child);
        }
        return child;
    }

    public void removeChildTag(String childName) {
        if (tag != null)
            tag.remove(childName);
    }

    public Component getDisplayName() {
        return getFluidType().getDescription(this);
    }

    public String getTranslationKey() {
        return getFluidType().getDescriptionId(this);
    }

    /**
     * @return A copy of this FluidStack
     */
    public FluidStack copy() {
        return new FluidStack(getFluid(), amount, tag);
    }

    /**
     * @return A copy of this FluidStack
     */
    public FluidStack copyWithAmount(int amount) {
        return new FluidStack(getFluid(), amount, tag);
    }

    /**
     * Determines if the FluidIDs and NBT Tags are equal. This does not check amounts.
     *
     * @param other
     *              The FluidStack for comparison
     * @return true if the Fluids (IDs and NBT Tags) are the same
     */
    public boolean isFluidEqual(FluidStack other) {
        return is(other.getFluid()) && isFluidStackTagEqual(other);
    }

    private boolean isFluidStackTagEqual(FluidStack other) {
        return tag == null ? other.tag == null : other.tag != null && tag.equals(other.tag);
    }

    /**
     * Determines if the NBT Tags are equal. Useful if the FluidIDs are known to be equal.
     */
    public static boolean areFluidStackTagsEqual(FluidStack stack1, FluidStack stack2) {
        return stack1.isFluidStackTagEqual(stack2);
    }

    /**
     * Determines if the Fluids are equal and this stack is larger.
     *
     * @return true if this FluidStack contains the other FluidStack (same fluid and >= amount)
     */
    public boolean containsFluid(FluidStack other) {
        return isFluidEqual(other) && amount >= other.amount;
    }

    /**
     * Determines if the FluidIDs, Amounts, and NBT Tags are all equal.
     *
     * @param other
     *              - the FluidStack for comparison
     * @return true if the two FluidStacks are exactly the same
     */
    public boolean isFluidStackIdentical(FluidStack other) {
        return isFluidEqual(other) && amount == other.amount;
    }

    /**
     * Determines if the FluidIDs and NBT Tags are equal compared to a registered container
     * ItemStack. This does not check amounts.
     *
     * @param other
     *              The ItemStack for comparison
     * @return true if the Fluids (IDs and NBT Tags) are the same
     */
    public boolean isFluidEqual(ItemStack other) {
        return FluidUtil.getFluidContained(other).map(this::isFluidEqual).orElse(false);
    }

    @Override
    public final int hashCode() {
        int code = 1;
        code = 31 * code + getFluid().hashCode();
        if (tag != null)
            code = 31 * code + tag.hashCode();
        return code;
    }

    /**
     * Default equality comparison for a FluidStack. Same functionality as isFluidEqual().
     *
     * This is included for use in data structures.
     */
    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof FluidStack)) {
            return false;
        }
        return isFluidEqual((FluidStack) o);
    }
}
