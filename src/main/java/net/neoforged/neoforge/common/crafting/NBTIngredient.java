/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import org.jetbrains.annotations.Nullable;

/**
 * Ingredient that matches the given items, performing either a {@link NBTIngredient#isStrict() strict} or a partial NBT test.
 * <p>
 * Strict NBT ingredients will only match items that have <b>exactly</b> the provided tag, while partial ones will
 * match if the item's tags contain all of the elements of the provided one, while allowing for additional elements to exist.
 */
public class NBTIngredient extends Ingredient {
    public static final Codec<NBTIngredient> CODEC = RecordCodecBuilder.create(
            builder -> builder
                    .group(
                            NeoForgeExtraCodecs.singularOrPluralCodec(BuiltInRegistries.ITEM.byNameCodec(), "item").forGetter(NBTIngredient::getContainedItems),
                            ExtraCodecs.strictOptionalField(CraftingHelper.TAG_CODEC, "tag").forGetter(NBTIngredient::getOptionalTag),
                            ExtraCodecs.strictOptionalField(CraftingHelper.TAG_CODEC, AttachmentHolder.ATTACHMENTS_NBT_KEY).forGetter(NBTIngredient::getAttachmentNbt),
                            Codec.BOOL.optionalFieldOf("strict", false).forGetter(NBTIngredient::isStrict))
                    .apply(builder, NBTIngredient::new));

    public static final Codec<NBTIngredient> CODEC_NONEMPTY = RecordCodecBuilder.create(
            builder -> builder
                    .group(
                            NeoForgeExtraCodecs.singularOrPluralCodecNotEmpty(BuiltInRegistries.ITEM.byNameCodec(), "item").forGetter(NBTIngredient::getContainedItems),
                            ExtraCodecs.strictOptionalField(CraftingHelper.TAG_CODEC, "tag").forGetter(NBTIngredient::getOptionalTag),
                            ExtraCodecs.strictOptionalField(CraftingHelper.TAG_CODEC, AttachmentHolder.ATTACHMENTS_NBT_KEY).forGetter(NBTIngredient::getAttachmentNbt),
                            Codec.BOOL.optionalFieldOf("strict", false).forGetter(NBTIngredient::isStrict))
                    .apply(builder, NBTIngredient::new));

    private final boolean strict;

    @Deprecated(forRemoval = true, since = "1.20.4")
    protected NBTIngredient(Set<Item> items, CompoundTag tag, boolean strict) {
        this(items, tag, null, strict);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private NBTIngredient(Set<Item> items, Optional<CompoundTag> tag, Optional<CompoundTag> attachmentTag, boolean strict) {
        this(items, tag.orElse(null), attachmentTag.orElse(null), strict);
    }

    protected NBTIngredient(Set<Item> items, @Nullable CompoundTag tag, @Nullable CompoundTag attachmentTag, boolean strict) {
        super(items.stream().map(item -> {
            //Copy the attachment tag in case a modder mutates the tag they get passed when deserializing an attachment
            ItemStack stack = new ItemStack(item, 1, attachmentTag == null ? null : attachmentTag.copy());
            // copy NBT to prevent the stack from modifying the original, as attachments or vanilla item durability will modify the tag
            if (tag != null) {
                stack.setTag(tag.copy());
            }
            return new Ingredient.ItemValue(stack, strict ? ItemStack::matches : NBTIngredient::compareStacksWithNBT);
        }), NeoForgeMod.NBT_INGREDIENT_TYPE);
        Preconditions.checkArgument(!items.isEmpty(), "At least one item needs to be provided for a nbt matching ingredient");
        Preconditions.checkArgument(tag != null || attachmentTag != null, "Either nbt or attachment nbt needs to be provided for a nbt matching ingredient");
        this.strict = strict;
    }

    @Override
    protected boolean areStacksEqual(ItemStack left, ItemStack right) {
        return strict ? ItemStack.matches(left, right) : compareStacksWithNBT(left, right);
    }

    @Override
    public boolean synchronizeWithContents() {
        return false;
    }

    private static boolean compareStacksWithNBT(ItemStack left, ItemStack right) {
        return left.is(right.getItem()) && NbtUtils.compareNbt(left.getTag(), right.getTag(), true) && NbtUtils.compareNbt(left.serializeAttachments(), right.serializeAttachments(), true);
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given NBT
     */
    public static NBTIngredient of(boolean strict, CompoundTag nbt, ItemLike... items) {
        Objects.requireNonNull(nbt, "NBT Ingredient requires NBT");
        return new NBTIngredient(Arrays.stream(items).map(ItemLike::asItem).collect(Collectors.toSet()), nbt, null, strict);
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given NBT
     */
    public static NBTIngredient of(boolean strict, CompoundTag nbt, CompoundTag attachmentNbt, ItemLike... items) {
        Objects.requireNonNull(nbt, "NBT Ingredient requires NBT");
        Objects.requireNonNull(attachmentNbt, "NBT Ingredient requires attachment NBT");
        return new NBTIngredient(Arrays.stream(items).map(ItemLike::asItem).collect(Collectors.toSet()), nbt, attachmentNbt, strict);
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given NBT
     */
    public static NBTIngredient ofAttachment(boolean strict, CompoundTag attachmentNbt, ItemLike... items) {
        Objects.requireNonNull(attachmentNbt, "NBT Ingredient requires attachment NBT");
        return new NBTIngredient(Arrays.stream(items).map(ItemLike::asItem).collect(Collectors.toSet()), null, attachmentNbt, strict);
    }

    /**
     * Creates a new ingredient matching the given item, containing the given NBT
     */
    public static NBTIngredient of(boolean strict, ItemStack stack) {
        CompoundTag attachmentNbt = stack.serializeAttachments();
        //Only force create a tag if there is no serializable attachments
        CompoundTag nbt = attachmentNbt == null ? stack.getOrCreateTag() : stack.getTag();
        return new NBTIngredient(Set.of(stack.getItem()), nbt, attachmentNbt, strict);
    }

    public Set<Item> getContainedItems() {
        return Arrays.stream(getItems()).map(ItemStack::getItem).collect(Collectors.toSet());
    }

    public CompoundTag getTag() {
        return getOptionalTag().orElseGet(CompoundTag::new);
    }

    //TODO - 1.20.5: Replace getTag with this method and rename this to getTag
    public Optional<CompoundTag> getOptionalTag() {
        return getFirstItem().map(ItemStack::getTag);
    }

    public Optional<CompoundTag> getAttachmentNbt() {
        return getFirstItem().map(AttachmentHolder::serializeAttachments);
    }

    private Optional<ItemStack> getFirstItem() {
        final ItemStack[] items = getItems();
        return items.length == 0 ? Optional.empty() : Optional.of(items[0]);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    public boolean isStrict() {
        return strict;
    }
}
