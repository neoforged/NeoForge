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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.NeoForgeEventHandler;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.Optional;

/**
 * Represents an item-based recipe result that represents the item as a tag-fallback combination.
 * {@link DefaultedItemTagResult#resolve()} resolves that combination into a concrete {@link ItemStack}.
 */
public class DefaultedItemTagResult implements Result<ItemStack> {
    public static final MapCodec<DefaultedItemTagResult> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(it -> it.tagKey),
            BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("fallback").forGetter(it -> it.fallback),
            ExtraCodecs.intRange(1, 99).fieldOf("count").orElse(1).forGetter(it -> it.count),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(it -> it.components)
    ).apply(inst, DefaultedItemTagResult::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DefaultedItemTagResult> STREAM_CODEC = StreamCodec.composite(
            TagKey.streamCodec(Registries.ITEM), it -> it.tagKey,
            Item.STREAM_CODEC, it -> it.fallback,
            ByteBufCodecs.VAR_INT, it -> it.count,
            DataComponentPatch.STREAM_CODEC, it -> it.components,
            DefaultedItemTagResult::new);

    private final TagKey<Item> tagKey;
    private final Holder<Item> fallback;
    private final int count;
    private final DataComponentPatch components;

    /**
     * @param tagKey     The {@link TagKey} to use for looking up the result.
     * @param fallback   The fallback to use if the tag-based lookup did not yield a conclusive result.
     * @param count      The count to use. Corresponds to {@link ItemStack#getCount()}.
     * @param components The data components to use. Corresponds to {@link ItemStack#getComponents()}.
     */
    public DefaultedItemTagResult(TagKey<Item> tagKey, Holder<Item> fallback, int count, DataComponentPatch components) {
        this.tagKey = tagKey;
        this.fallback = fallback;
        this.count = count;
        this.components = components;
    }

    @Override
    public ItemStack resolve() {
        Optional<Item> optional = NeoForgeEventHandler.getTagDefaultsManager().resolve(Registries.ITEM, tagKey);
        return new ItemStack(optional.<Holder<Item>>map(Item::builtInRegistryHolder).orElse(fallback), count, components);
    }

    @Override
    public ResultType<?> type() {
        return NeoForgeMod.DEFAULTED_ITEM_TAG_RESULT_TYPE.get();
    }

    @Override
    public SlotDisplay display() {
        return new Display(this);
    }

    public record Display(DefaultedItemTagResult result) implements ItemResultSlotDisplay {
        public static final MapCodec<Display> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                DefaultedItemTagResult.MAP_CODEC.fieldOf("result").forGetter(Display::result)
        ).apply(inst, Display::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Display> STREAM_CODEC = StreamCodec.composite(
                DefaultedItemTagResult.STREAM_CODEC, Display::result,
                Display::new);

        @Override
        public Type<? extends SlotDisplay> type() {
            return NeoForgeMod.DEFAULTED_FLUID_TAG_RESULT_SLOT_DISPLAY.get();
        }
    }
}
