package net.neoforged.neoforge.common.tagdefaults;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForgeEventHandler;

import java.util.function.Function;

/**
 * This class represents a recipe result backed by a {@link TagDefaults}.
 * Create an instance via {@link DefaultedTagResult#of(Registry, TagKey, Object)} or {@link DefaultedTagResult#ofHolder(Registry, TagKey, Holder)}.
 *
 * <p>Despite having two unrelated generics, in practice they are either one and the same, or {@code F} is a {@code Holder<T>}.
 * (This does not necessarily hold if you use custom subclasses.)</p>
 *
 * @param <T> The type of the {@link TagKey}.
 * @param <F> The type of the fallback.
 */
public class DefaultedTagResult<T, F> {
    /**
     * A variation of {@link Item#CODEC} that can alternatively be a {@link DefaultedTagResult}. Usage is identical to {@link Item#CODEC}.
     */
    public static final Codec<Holder<Item>> ITEM_HOLDER_CODEC = Codec.lazyInitialized(
            () -> Codec.either(Item.CODEC, makeHolderCodec(BuiltInRegistries.ITEM).codec())
                    .xmap(either -> Either.unwrap(either.mapRight(DefaultedTagResult::resolve)), Either::left));
    /**
     * A variation of {@link ItemStack#MAP_CODEC} that additionally allows the item to be specified as a {@link DefaultedTagResult}.
     */
    public static final MapCodec<ItemStack> RECIPE_RESULT_MAP_CODEC = MapCodec.recursive("ItemStack", codec -> RecordCodecBuilder.mapCodec(inst -> inst.group(
            ITEM_HOLDER_CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder),
            ExtraCodecs.intRange(1, 99).fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch)
    ).apply(inst, ItemStack::new)));
    /**
     * A variation of {@link ItemStack#CODEC} that additionally allows the item to be specified as a {@link DefaultedTagResult}.
     * All vanilla recipe results have been patched to use this codec instead.
     */
    public static final Codec<ItemStack> RECIPE_RESULT_CODEC = Codec.lazyInitialized(RECIPE_RESULT_MAP_CODEC::codec);

    protected final ResourceKey<? extends Registry<T>> registryKey;
    protected final TagKey<T> tagKey;
    protected final F fallback;
    protected final Function<T, F> fallbackConverter;

    /**
     * This constructor is protected to allow subclassing. Callers should use {@link DefaultedTagResult#of(Registry, TagKey, Object)} or {@link DefaultedTagResult#ofHolder(Registry, TagKey, Holder)} instead.
     *
     * <p>Subclass implementations should provide their own versions of {@link DefaultedTagResult#of(Registry, TagKey, Object)} and {@link DefaultedTagResult#ofHolder(Registry, TagKey, Holder)}.</p>
     *
     * @param registryKey       The key of the associated {@link Registry}.
     * @param tagKey            The {@link TagKey} to use.
     * @param fallback          The fallback value to use.
     * @param fallbackConverter A converter function that will convert the entry taken from the {@link TagKey} into the fallback type.
     */
    protected DefaultedTagResult(ResourceKey<? extends Registry<T>> registryKey, TagKey<T> tagKey, F fallback, Function<T, F> fallbackConverter) {
        this.registryKey = registryKey;
        this.tagKey = tagKey;
        this.fallback = fallback;
        this.fallbackConverter = fallbackConverter;
    }

    /**
     * Resolves the {@link DefaultedTagResult} into either an object of type {@code F} (based on the surrounding configuration), or the fallback.
     *
     * @return An object to consider the "actual result".
     */
    public F resolve() {
        return NeoForgeEventHandler.getTagDefaultsManager().resolve(registryKey, tagKey).map(fallbackConverter).orElse(fallback);
    }

    /**
     * Creates a new {@link DefaultedTagResult}.
     *
     * @param registry The associated {@link Registry}.
     * @param tagKey   The {@link TagKey} to use.
     * @param fallback The fallback value to use.
     * @param <T>      The type of the {@link Registry}, {@link TagKey} and fallback value.
     * @return A new {@link DefaultedTagResult}.
     */
    public static <T> DefaultedTagResult<T, T> of(Registry<T> registry, TagKey<T> tagKey, T fallback) {
        return new DefaultedTagResult<>(registry.key(), tagKey, fallback, Function.identity());
    }

    /**
     * Creates a new {@link DefaultedTagResult} that wraps the fallback type using {@link Holder}.
     *
     * @param registry The associated {@link Registry}.
     * @param tagKey   The {@link TagKey} to use.
     * @param fallback The fallback value to use, wrapped in a {@link Holder}.
     * @param <T>      The type of the {@link Registry}, {@link TagKey} and fallback value, with the latter wrapped in a {@link Holder}.
     * @return A new {@link DefaultedTagResult}.
     */
    public static <T> DefaultedTagResult<T, Holder<T>> ofHolder(Registry<T> registry, TagKey<T> tagKey, Holder<T> fallback) {
        return new DefaultedTagResult<>(registry.key(), tagKey, fallback, registry::wrapAsHolder);
    }

    /**
     * Creates a {@link Codec} for {@link DefaultedTagResult}s.
     *
     * @param registry The {@link Registry} for which to create the {@link Codec}.
     * @param <T>      The type of the {@link Registry}, {@link TagKey} and fallback value.
     * @return A {@link Codec} for {@link DefaultedTagResult}s.
     */
    public static <T> MapCodec<DefaultedTagResult<T, T>> makeCodec(Registry<T> registry) {
        return RecordCodecBuilder.mapCodec(inst -> inst.group(
                TagKey.codec(registry.key()).fieldOf("tag").forGetter(it -> it.tagKey),
                registry.byNameCodec().fieldOf("fallback").forGetter(it -> it.fallback)
        ).apply(inst, (tagKey, fallback) -> DefaultedTagResult.of(registry, tagKey, fallback)));
    }

    /**
     * Creates a {@link Codec} for {@link DefaultedTagResult}s, wrapping the fallback type in a {@link Holder}.
     *
     * @param registry The {@link Registry} for which to create the {@link Codec}.
     * @param <T>      The type of the {@link Registry}, {@link TagKey} and fallback value.
     * @return A {@link Codec} for {@link DefaultedTagResult}s with a {@link Holder} fallback type.
     */
    public static <T> MapCodec<DefaultedTagResult<T, Holder<T>>> makeHolderCodec(Registry<T> registry) {
        return RecordCodecBuilder.mapCodec(inst -> inst.group(
                TagKey.codec(registry.key()).fieldOf("tag").forGetter(it -> it.tagKey),
                registry.holderByNameCodec().fieldOf("fallback").forGetter(it -> it.fallback)
        ).apply(inst, (tagKey, fallback) -> DefaultedTagResult.ofHolder(registry, tagKey, fallback)));
    }
}
