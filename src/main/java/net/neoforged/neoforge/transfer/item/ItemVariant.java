package net.neoforged.neoforge.transfer.item;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/**
 * Immutable combination of an {@link Item} and data components.
 * Similar to an {@link ItemStack}, but immutable and without amount information.
 */
public final class ItemVariant implements DataComponentHolder {
    /**
     * Codec for an item variant.
     * Same format as {@link ItemStack#SINGLE_ITEM_CODEC}.
     * Does <b>not</b> accept blank variants.
     */
    public static final Codec<ItemVariant> CODEC = ItemStack.SINGLE_ITEM_CODEC
            .xmap(ItemVariant::of, ItemVariant::toStack);
    /**
     * Codec for an item variant. Same format as {@link #CODEC}, and also accepts blank variants.
     */
    public static final Codec<ItemVariant> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
            .xmap(o -> o.orElse(ItemVariant.EMPTY), r -> r.isBlank() ? Optional.of(ItemVariant.EMPTY) : Optional.of(r));
    // TODO
//    /**
//     * Codec for an item resource and an amount. Does <b>not</b> accept empty stacks.
//     */
//    public static final Codec<ResourceAmount<ItemResource>> WITH_AMOUNT_CODEC = ItemStack.CODEC
//            .xmap(ItemStack::immutable, ItemStack::of);
//    /**
//     * Codec for an item resource and an amount. Accepts empty stacks.
//     */
//    public static final Codec<ResourceAmount<ItemResource>> OPTIONAL_WITH_AMOUNT_CODEC = ItemStack.OPTIONAL_CODEC
//            .xmap(ItemStack::immutable, ItemStack::of);
    /**
     * Stream codec for an item variant. Accepts blank variants.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemVariant> OPTIONAL_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ITEM),
            ItemVariant::getItemHolder,
            DataComponentPatch.STREAM_CODEC,
            ItemVariant::getComponentsPatch,
            ItemVariant::of);

    public static final ItemVariant EMPTY = new ItemVariant(ItemStack.EMPTY);

    public static ItemVariant of(ItemStack itemStack) {
        return itemStack.isEmpty() ? EMPTY : new ItemVariant(itemStack.copyWithCount(1));
    }

    public static ItemVariant of(ItemLike itemLike) {
        var item = itemLike.asItem();
        return item == Items.AIR ? EMPTY : new ItemVariant(new ItemStack(item));
    }

    public static ItemVariant of(Holder<Item> item, DataComponentPatch patch) {
        return item.value() == Items.AIR ? EMPTY : new ItemVariant(new ItemStack(item, 1, patch));
    }

    /**
     * We wrap an item stack which must never be exposed and/or modified.
     */
    final ItemStack innerStack;

    private ItemVariant(ItemStack innerStack) {
        this.innerStack = innerStack;
    }

    public boolean isBlank() {
        return innerStack.isEmpty();
    }

    public Item getItem() {
        return innerStack.getItem();
    }

    public Holder<Item> getItemHolder() {
        return innerStack.getItemHolder();
    }

    @Override
    public DataComponentMap getComponents() {
        return innerStack.getComponents();
    }

    public DataComponentPatch getComponentsPatch() {
        return innerStack.getComponentsPatch();
    }

    public boolean isComponentsPatchEmpty() {
        return innerStack.isComponentsPatchEmpty();
    }

    public boolean matches(ItemStack stack) {
        return ItemStack.isSameItemSameComponents(stack, innerStack);
    }

    public boolean is(TagKey<Item> tagKey) {
        return this.getItem().builtInRegistryHolder().is(tagKey);
    }

    public boolean is(Item item) {
        return this.getItem() == item;
    }

    public boolean is(Predicate<Holder<Item>> predicate) {
        return predicate.test(this.getItem().builtInRegistryHolder());
    }

    public boolean is(Holder<Item> holder) {
        return is(holder.value());
    }

    public boolean is(HolderSet<Item> holderSet) {
        return holderSet.contains(this.getItemHolder());
    }

    public int getMaxStackSize() {
        return innerStack.getMaxStackSize();
    }

    public ItemStack toStack() {
        return toStack(1);
    }

    public ItemStack toStack(int count) {
        return this.innerStack.copyWithCount(count);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof ItemVariant v && ItemStack.isSameItemSameComponents(v.innerStack, innerStack);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashItemAndComponents(innerStack);
    }
}
