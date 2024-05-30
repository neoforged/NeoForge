/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.codec.NonNullListCodecs;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.function.Function;

public class ItemStackHandler implements IItemHandler, IItemHandlerModifiable, INBTSerializable<CompoundTag> {
    protected NonNullList<ItemStack> stacks;

    public static final Codec<ItemStackHandler> CODEC = RecordCodecBuilder.create(i -> i.group(
        NonNullListCodecs.withIndices(ItemStack.OPTIONAL_CODEC, ItemStack.EMPTY, "slot", ItemStack::isEmpty)
            .fieldOf("stacks").forGetter(handler -> handler.stacks)
    ).apply(i, ItemStackHandler::new));

    /**
     * Use if you require 1-to-1 support for the output of {@link #serializeNBT(HolderLookup.Provider)}.
     * Otherwise, prefer {#link CODEC} as a newer alternative to saving data to disk.
     * @param provider
     * @return
     */
    @Deprecated
    public static Codec<ItemStackHandler> nbtCompatibleCodec(HolderLookup.Provider provider) {
        return new LegacyNbtItemStackHandlerCodec(provider);
    }

    public ItemStackHandler() {
        this(1);
    }

    public ItemStackHandler(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public ItemStackHandler(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    public void setSize(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        this.stacks.set(slot, stack);
        onContentsChanged(slot);
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return this.stacks.get(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        if (!isItemValid(slot, stack))
            return stack;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        int limit = getStackLimit(slot, stack);

        if (!existing.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                this.stacks.set(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0)
            return ItemStack.EMPTY;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        if (existing.isEmpty())
            return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                this.stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
                return existing;
            } else {
                return existing.copy();
            }
        } else {
            if (!simulate) {
                this.stacks.set(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
                onContentsChanged(slot);
            }

            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    protected int getStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                nbtTagList.add(stacks.get(i).save(provider, itemTag));
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        setSize(nbt.contains("Size", Tag.TAG_INT) ? nbt.getInt("Size") : stacks.size());
        ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if (slot >= 0 && slot < stacks.size()) {
                ItemStack.parse(provider, itemTags).ifPresent(stack -> stacks.set(slot, stack));
            }
        }
        onLoad();
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= stacks.size())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
    }

    protected void onLoad() {}

    protected void onContentsChanged(int slot) {}

    /**
     * This codec provides support for the current NBT implementation provided by {@link ItemStackHandler#serializeNBT(HolderLookup.Provider)}.
     *
     * @param provider
     * @deprecated Prefer using the newer codec if you do not need support for the older NBT, and instead simply wish
     * to transfer data over a network.
     */
    @Deprecated(since = "1.20.6")
    public record LegacyNbtItemStackHandlerCodec(HolderLookup.Provider provider) implements Codec<ItemStackHandler> {

        @Override
        public <T> DataResult<Pair<ItemStackHandler, T>> decode(DynamicOps<T> ops, T input) {
            final var map = ops.getMap(input).getOrThrow();

            int maxSize = Codec.INT.fieldOf("Size")
                .decode(ops, map)
                .getOrThrow();

            ItemStackHandler handler = new ItemStackHandler(maxSize);
            CompoundTag.CODEC.listOf()
                .fieldOf("Items")
                .decode(ops, map)
                .ifSuccess(itemTags -> {
                    itemTags.forEach(itemTag -> {
                        int slot = itemTag.getInt("Slot");
                        var stack = ItemStack.parseOptional(provider, itemTag);
                        handler.setStackInSlot(slot, stack);
                    });
                });

            return DataResult.success(Pair.of(handler, input));
        }

        @Override
        public <T> DataResult<T> encode(ItemStackHandler input, DynamicOps<T> ops, T prefix) {
            var list = ops.listBuilder();
            for (int i = 0; i < input.stacks.size(); i++) {
                if (!input.stacks.get(i).isEmpty()) {
                    CompoundTag itemTag = new CompoundTag();
                    itemTag.putInt("Slot", i);
                    var encodedStack = input.stacks.get(i).save(provider, itemTag);
                    if(encodedStack instanceof CompoundTag ct)
                        list.add(CompoundTag.CODEC.encode(ct, ops, ops.empty()));
                }
            }

            return ops.mapBuilder()
                .add("Items", list.build(ops.empty()))
                .add("Size", ops.createInt(input.stacks.size()))
                .build(prefix);
        }
    }
}
