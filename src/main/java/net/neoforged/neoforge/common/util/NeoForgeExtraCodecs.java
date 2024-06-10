/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.booleans.BooleanImmutableList;
import it.unimi.dsi.fastutil.bytes.ByteImmutableList;
import it.unimi.dsi.fastutil.doubles.DoubleImmutableList;
import it.unimi.dsi.fastutil.floats.FloatImmutableList;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.longs.LongImmutableList;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.shorts.ShortImmutableList;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.util.ExtraCodecs;

/**
 * {@link Codec}-related helper functions that are not in {@link ExtraCodecs}, but useful to NeoForge and other mods.
 * 
 * @see ExtraCodecs
 */
@SuppressWarnings("unused")
public class NeoForgeExtraCodecs {
    public static final Codec<boolean[]> BOOLEAN_ARRAY = booleanArray(0, Integer.MAX_VALUE);
    public static final Codec<byte[]> BYTE_ARRAY = Codec.BYTE_BUFFER.xmap(ByteBuffer::array, ByteBuffer::wrap);
    public static final Codec<char[]> CHAR_ARRAY = Codec.STRING.xmap(String::toCharArray, String::valueOf);
    public static final Codec<short[]> SHORT_ARRAY = shortArray(0, Integer.MAX_VALUE);
    public static final Codec<int[]> INT_ARRAY = Codec.INT_STREAM.xmap(IntStream::toArray, IntStream::of);
    public static final Codec<long[]> LONG_ARRAY = Codec.LONG_STREAM.xmap(LongStream::toArray, LongStream::of);
    public static final Codec<float[]> FLOAT_ARRAY = floatArray(0, Integer.MAX_VALUE);
    public static final Codec<double[]> DOUBLE_ARRAY = doubleArray(0, Integer.MAX_VALUE);

    private static abstract class AliasedFieldMapCodec<T> extends MapCodec<T> {
        private final List<String> names;
        private final String encodeName;
        private final Encoder<? super T> encoder;

        private AliasedFieldMapCodec(List<String> names, String encodeName, Encoder<? super T> encoder) {
            this.names = names;
            this.encodeName = encodeName;
            this.encoder = encoder;
        }

        abstract Decoder<T> decoder(String name);

        @Override
        public <T1> Stream<T1> keys(DynamicOps<T1> ops) {
            return names.stream().map(ops::createString);
        }

        @Override
        public <T1> DataResult<T> decode(DynamicOps<T1> ops, MapLike<T1> input) {
            DataResult<T> result = DataResult.error(() -> "None of keys [" + String.join(", ", names) + "] in " + input);
            StringJoiner errors = new StringJoiner("\n");
            for (final String name : names) {
                T1 field = input.get(name);
                if (field != null) {
                    result = decoder(name).parse(ops, field);
                    if (result.isSuccess())
                        return result;
                    result = result.promotePartial(error -> errors.add(name + ": " + error));
                }
            }
            if (errors.length() > 0) {
                result.mapError(ignored -> errors.toString());
            }
            return result;
        }

        @Override
        public <T1> RecordBuilder<T1> encode(T input, DynamicOps<T1> ops, RecordBuilder<T1> prefix) {
            return prefix.add(encodeName, encoder.encodeStart(ops, input));
        }
    }

    /**
     * Creates a {@link MapCodec} which accepts multiple aliases.
     * 
     * @param codec the backing {@link Codec}
     * @param names the aliases
     */
    public static <T> MapCodec<T> aliasedFieldOf(final Codec<T> codec, final String... names) {
        if (names.length == 0)
            throw new IllegalArgumentException("Must have at least one name!");
        return new AliasedFieldMapCodec<>(List.of(names), names[0], codec) {
            @Override
            Decoder<T> decoder(String name) {
                return codec;
            }
        };
    }

    /**
     * Creates a {@link MapCodec} which accepts multiple aliases and alias-specific decoders.
     * 
     * @param codecs the {@link Map} of {@link Codec}s by alias, the first key will be used as the field name in encode.
     */
    public static <T> MapCodec<T> aliasedFieldOf(final Map<String, Codec<T>> codecs) {
        if (codecs.isEmpty())
            throw new IllegalArgumentException("Must have at least one name!");
        final List<String> names = List.copyOf(codecs.keySet());
        final String encoderName = names.getFirst();
        return new AliasedFieldMapCodec<>(names, encoderName, codecs.get(encoderName)) {
            @Override
            Decoder<T> decoder(String name) {
                return codecs.get(name);
            }
        };
    }

    /**
     * Creates a {@link MapCodec} which accepts multiple aliases and alias-specific decoders.
     * 
     * @param name     the field name used in encode
     * @param encoder  the encoder
     * @param decoders the {@link Map} of {@link Codec}s by alias, must contain the {@code name}
     */
    public static <T> MapCodec<T> aliasedFieldOf(final String name, final Encoder<T> encoder, final Map<String, ? extends Decoder<T>> decoders) {
        final List<String> names = new ArrayList<>(decoders.keySet());
        if (!names.remove(name))
            throw new IllegalArgumentException("No decoder for name " + name);
        names.addFirst(name);
        return new AliasedFieldMapCodec<>(names, name, encoder) {
            @Override
            Decoder<T> decoder(String name) {
                return decoders.get(name);
            }
        };
    }

    /**
     * Similar to {@link Codec#optionalFieldOf(String, Object)}, except that the default value is always written.
     */
    public static <T> MapCodec<T> optionalFieldAlwaysWrite(final Codec<T> codec, String name, T defaultValue) {
        return codec.optionalFieldOf(name).xmap(o -> o.orElse(defaultValue), Optional::of);
    }

    public static <T> MapCodec<T> mapWithAlternative(final MapCodec<T> mapCodec, final MapCodec<? extends T> alternative) {
        return Codec.mapEither(mapCodec, alternative).xmap(either -> either.map(Function.identity(), Function.identity()), Either::left);
    }

    public static <T> MapCodec<Set<T>> singularOrPluralCodec(final Codec<T> codec, final String singularName) {
        return singularOrPluralCodec(codec, singularName, setOf(codec), "%ss".formatted(singularName), ImmutableSet::of);
    }

    public static <T> MapCodec<Set<T>> singularOrPluralCodec(final Codec<T> codec, final Codec<Set<T>> setCodec, final String singularName) {
        return singularOrPluralCodec(codec, singularName, setCodec, "%ss".formatted(singularName), ImmutableSet::of);
    }

    public static <T> MapCodec<Set<T>> singularOrPluralCodec(final Codec<T> codec, final String singularName, final String pluralName) {
        return singularOrPluralCodec(codec, singularName, setOf(codec), pluralName, ImmutableSet::of);
    }

    public static <T> MapCodec<Set<T>> singularOrPluralCodec(final Codec<T> codec, final String singularName, final Codec<Set<T>> setCodec, final String pluralName) {
        return singularOrPluralCodec(codec, singularName, setCodec, pluralName, ImmutableSet::of);
    }

    public static <T> MapCodec<List<T>> singularOrPluralListCodec(final Codec<T> codec, final String singularName) {
        return singularOrPluralCodec(codec, singularName, codec.listOf(), "%ss".formatted(singularName), ImmutableList::of);
    }

    public static <T> MapCodec<List<T>> singularOrPluralListCodec(final Codec<T> codec, final Codec<List<T>> listCodec, final String singularName) {
        return singularOrPluralCodec(codec, singularName, listCodec, "%ss".formatted(singularName), ImmutableList::of);
    }

    public static <T> MapCodec<List<T>> singularOrPluralListCodec(final Codec<T> codec, final String singularName, final String pluralName) {
        return singularOrPluralCodec(codec, singularName, codec.listOf(), pluralName, ImmutableList::of);
    }

    public static <T> MapCodec<List<T>> singularOrPluralListCodec(final Codec<T> codec, final String singularName, final Codec<List<T>> listCodec, final String pluralName) {
        return singularOrPluralCodec(codec, singularName, listCodec, pluralName, ImmutableList::of);
    }

    public static <T, C extends Collection<T>> MapCodec<C> singularOrPluralCodec(
            final Codec<T> codec, final Codec<C> collectionCodec,
            final String singularName, final Function<? super T, ? extends C> fromSingleton) {
        return singularOrPluralCodec(codec, singularName, collectionCodec, "%ss".formatted(singularName), fromSingleton);
    }

    public static <T, C extends Collection<T>> MapCodec<C> singularOrPluralCodec(
            final Codec<T> codec, final String singularName,
            final Codec<C> collectionCodec, final String pluralName,
            final Function<? super T, ? extends C> fromSingleton) {
        return Codec.mapEither(codec.fieldOf(singularName), collectionCodec.fieldOf(pluralName)).xmap(
                either -> either.map(fromSingleton, Function.identity()),
                collection -> collection.size() == 1 ? Either.left(Iterables.getOnlyElement(collection)) : Either.right(collection));
    }

    public static <T> MapCodec<Set<T>> singularOrPluralCodecNotEmpty(final Codec<T> codec, final String singularName) {
        return singularOrPluralCodecNotEmpty(codec, singularName, setOf(codec), "%ss".formatted(singularName), ImmutableSet::of);
    }

    public static <T> MapCodec<Set<T>> singularOrPluralCodecNotEmpty(final Codec<T> codec, final Codec<Set<T>> setCodec, final String singularName) {
        return singularOrPluralCodecNotEmpty(codec, singularName, setCodec, "%ss".formatted(singularName), ImmutableSet::of);
    }

    public static <T> MapCodec<Set<T>> singularOrPluralCodecNotEmpty(final Codec<T> codec, final String singularName, final String pluralName) {
        return singularOrPluralCodecNotEmpty(codec, singularName, setOf(codec), pluralName, ImmutableSet::of);
    }

    public static <T> MapCodec<Set<T>> singularOrPluralCodecNotEmpty(final Codec<T> codec, final String singularName, final Codec<Set<T>> setCodec, final String pluralName) {
        return singularOrPluralCodecNotEmpty(codec, singularName, setCodec, pluralName, ImmutableSet::of);
    }

    public static <T> MapCodec<List<T>> singularOrPluralListCodecNotEmpty(final Codec<T> codec, final String singularName) {
        return singularOrPluralCodecNotEmpty(codec, singularName, codec.listOf(), "%ss".formatted(singularName), ImmutableList::of);
    }

    public static <T> MapCodec<List<T>> singularOrPluralListCodecNotEmpty(final Codec<T> codec, final Codec<List<T>> listCodec, final String singularName) {
        return singularOrPluralCodecNotEmpty(codec, singularName, listCodec, "%ss".formatted(singularName), ImmutableList::of);
    }

    public static <T> MapCodec<List<T>> singularOrPluralListCodecNotEmpty(final Codec<T> codec, final String singularName, final String pluralName) {
        return singularOrPluralCodecNotEmpty(codec, singularName, codec.listOf(), pluralName, ImmutableList::of);
    }

    public static <T> MapCodec<List<T>> singularOrPluralListCodecNotEmpty(final Codec<T> codec, final String singularName, final Codec<List<T>> listCodec, final String pluralName) {
        return singularOrPluralCodecNotEmpty(codec, singularName, listCodec, pluralName, ImmutableList::of);
    }

    public static <T, C extends Collection<T>> MapCodec<C> singularOrPluralCodecNotEmpty(
            final Codec<T> codec, final Codec<C> collectionCodec,
            final String singularName, final Function<? super T, ? extends C> fromSingleton) {
        return singularOrPluralCodecNotEmpty(codec, singularName, collectionCodec, "%ss".formatted(singularName), fromSingleton);
    }

    public static <T, C extends Collection<T>> MapCodec<C> singularOrPluralCodecNotEmpty(
            final Codec<T> codec, final String singularName,
            final Codec<C> collectionCodec, final String pluralName,
            final Function<? super T, ? extends C> fromSingleton) {
        return Codec.mapEither(codec.fieldOf(singularName), collectionCodec.fieldOf(pluralName)).xmap(
                either -> either.map(fromSingleton, Function.identity()),
                collection -> collection.size() == 1 ? Either.left(collection.iterator().next()) : Either.right(collection)).flatXmap(ts -> {
                    if (ts.isEmpty())
                        return DataResult.error(() -> "The collection for: %s can not be empty!".formatted(singularName));
                    return DataResult.success(ts);
                }, ts -> {
                    if (ts.isEmpty())
                        return DataResult.error(() -> "The collection for: %s can not be empty!".formatted(singularName));
                    return DataResult.success(ts);
                });
    }

    public static Codec<boolean[]> booleanArray(final int maxSize) {
        return booleanArray(0, maxSize);
    }

    public static Codec<boolean[]> booleanArray(final int minSize, final int maxSize) {
        return Codec.BOOL.listOf(minSize, maxSize)
                .xmap(list -> new BooleanImmutableList(list).toBooleanArray(), BooleanImmutableList::new);
    }

    public static Codec<byte[]> byteArray(final int maxSize) {
        return byteArray(0, maxSize);
    }

    public static Codec<byte[]> byteArray(final int minSize, final int maxSize) {
        return Codec.BYTE.listOf(minSize, maxSize)
                .xmap(list -> new ByteImmutableList(list).toByteArray(), ByteImmutableList::new);
    }

    public static Codec<char[]> charArray(final int maxSize) {
        return charArray(0, maxSize);
    }

    public static Codec<char[]> charArray(final int minSize, final int maxSize) {
        final String tooShort = "\"%s\" is too short: %d, expected length of range [" + minSize + "-" + maxSize + "]";
        final String tooLong = "\"%s\" is too long: %d, expected length of range [" + minSize + "-" + maxSize + "]";
        return Codec.STRING.flatXmap(
                s -> {
                    final int length = s.length();
                    if (length < minSize)
                        return DataResult.error(() -> tooShort.formatted(s, length));
                    if (length > maxSize)
                        return DataResult.error(() -> tooLong.formatted(s, length));
                    return DataResult.success(s.toCharArray());
                },
                chars -> {
                    final String s = String.valueOf(chars);
                    if (chars.length < minSize)
                        return DataResult.error(() -> tooShort.formatted(s, chars.length));
                    if (chars.length > maxSize)
                        return DataResult.error(() -> tooLong.formatted(s, chars.length));
                    return DataResult.success(s);
                });
    }

    public static Codec<short[]> shortArray(final int maxSize) {
        return shortArray(0, maxSize);
    }

    public static Codec<short[]> shortArray(final int minSize, final int maxSize) {
        return Codec.SHORT.listOf(minSize, maxSize)
                .xmap(list -> new ShortImmutableList(list).toShortArray(), ShortImmutableList::new);
    }

    public static Codec<int[]> intArray(final int maxSize) {
        return intArray(0, maxSize);
    }

    public static Codec<int[]> intArray(final int minSize, final int maxSize) {
        return Codec.INT.listOf(minSize, maxSize)
                .xmap(list -> new IntImmutableList(list).toIntArray(), IntImmutableList::new);
    }

    public static Codec<long[]> longArray(final int maxSize) {
        return longArray(0, maxSize);
    }

    public static Codec<long[]> longArray(final int minSize, final int maxSize) {
        return Codec.LONG.listOf(minSize, maxSize)
                .xmap(list -> new LongImmutableList(list).toLongArray(), LongImmutableList::new);
    }

    public static Codec<float[]> floatArray(final int maxSize) {
        return floatArray(0, maxSize);
    }

    public static Codec<float[]> floatArray(final int minSize, final int maxSize) {
        return Codec.FLOAT.listOf(minSize, maxSize)
                .xmap(list -> new FloatImmutableList(list).toFloatArray(), FloatImmutableList::new);
    }

    public static Codec<double[]> doubleArray(final int maxSize) {
        return doubleArray(0, maxSize);
    }

    public static Codec<double[]> doubleArray(final int minSize, final int maxSize) {
        return Codec.DOUBLE.listOf(minSize, maxSize)
                .xmap(list -> new DoubleImmutableList(list).toDoubleArray(), DoubleImmutableList::new);
    }

    @SafeVarargs
    public static <T> Codec<T[]> array(final Codec<T> codec, final T... arr) {
        return array(codec, 0, Integer.MAX_VALUE, arr);
    }

    @SafeVarargs
    public static <T> Codec<T[]> array(final Codec<T> codec, final int minSize, final int maxSize, final T... arr) {
        if (arr.length > 0)
            throw new IllegalArgumentException("The vararg array must be of 0 length!");
        return codec.listOf(minSize, maxSize).xmap(list -> list.toArray(arr), ImmutableList::copyOf);
    }

    @SafeVarargs
    public static <T> Codec<T[]> array(final Codec<T> codec, final int maxSize, final T... arr) {
        return array(codec, 0, maxSize, arr);
    }

    public static <T> Codec<Set<T>> setOf(final Codec<T> codec) {
        return Codec.list(codec).xmap(ImmutableSet::copyOf, ImmutableList::copyOf);
    }

    /**
     * Creates a {@link Codec} for {@link Set} which only accepts known elements and thus can be compressed like {@link EnumSet}.
     * 
     * @param codec       the {@link Codec} of element, for non-compressed
     * @param universeSet the {@link Set} of known elements
     * @param generator   the array constructor of the element type
     * @return a {@link Codec} for {@link Set} which only accepts known elements and supports compress
     */
    public static <T> Codec<Set<T>> setOf(final Codec<T> codec, final Set<T> universeSet, IntFunction<T[]> generator) {
        final T[] universe = universeSet.toArray(generator);
        final Object2IntMap<T> idMap = new Object2IntArrayMap<>(universe, IntStream.range(0, universe.length).toArray());
        return ExtraCodecs.orCompressed(setOf(codec), compressedSetOf(() -> universe, idMap, Collectors.toUnmodifiableSet()));
    }

    /**
     * Creates a {@link Codec} for {@link Set} which only accepts known elements and thus can be compressed like {@link EnumSet}.
     * 
     * @param codec            the {@link Codec} of element, for non-compressed
     * @param universeSupplier the {@link Supplier} for array of known elements
     * @param idMap            the mapping from elements to its index in the known elements array
     * @return a {@link Codec} for {@link Set} which only accepts known elements and supports compress
     */
    public static <T> Codec<Set<T>> setOf(final Codec<T> codec, final Supplier<T[]> universeSupplier, final ToIntFunction<T> idMap) {
        return ExtraCodecs.orCompressed(setOf(codec), compressedSetOf(Suppliers.memoize(universeSupplier::get), idMap, Collectors.toUnmodifiableSet()));
    }

    public static <E extends Enum<E>> Codec<EnumSet<E>> enumSetOf(final Class<E> enumClass, final Codec<E> codec) {
        return ExtraCodecs.orCompressed(
                Codec.list(codec).xmap(EnumSet::copyOf, ImmutableList::copyOf),
                compressedSetOf(enumClass::getEnumConstants, E::ordinal, Collector.of(
                        () -> EnumSet.noneOf(enumClass),
                        EnumSet::add,
                        (s1, s2) -> EnumSet.copyOf(Sets.union(s1, s2)))));
    }

    private static <T, A, S extends Set<T>> Codec<S> compressedSetOf(final Supplier<T[]> universeSupplier, final ToIntFunction<T> idMap, final Collector<T, A, S> collector) {
        return ExtraCodecs.BIT_SET.flatXmap(
                elements -> {
                    final T[] universe = universeSupplier.get();
                    if (elements.length() > universe.length)
                        return DataResult.error(() -> "Illegal elements: " + elements + " out of bounds [0, " + (universe.length - 1) + "]");
                    return DataResult.success(elements.stream().mapToObj(i -> universe[i]).collect(collector));
                },
                set -> {
                    final int maxIndex = universeSupplier.get().length;
                    BitSet bits = new BitSet();
                    for (T element : set) {
                        final int index = idMap.applyAsInt(element);
                        if (index < 0 || index > maxIndex)
                            return DataResult.error(() -> "Unknown element: " + element, bits);
                    }
                    return DataResult.success(bits);
                });
    }

    /**
     * Encodes the enum using {@link Enum#name()}, uses {@link String#equalsIgnoreCase(String)} for matching.<p>
     * {@summary See Also:} {@link #enumCodec(Supplier, Function, BiPredicate)}
     */
    public static <E extends Enum<E>> Codec<E> enumCodec(final Supplier<E[]> valuesSupplier) {
        return enumCodec(valuesSupplier, Enum::name, (e, name) -> e.name().equalsIgnoreCase(name));
    }

    /**
     * Encodes the enum using its name in lower/upper case, uses {@link String#equalsIgnoreCase(String)} for matching.<p>
     * {@summary See Also:} {@link #enumCodec(Supplier, Function, BiPredicate)}
     */
    public static <E extends Enum<E>> Codec<E> enumCodec(final Supplier<E[]> valuesSupplier, boolean toLowerCase) {
        final Function<E, String> encoder = toLowerCase
                ? e -> e.name().toLowerCase(Locale.ROOT)
                : e -> e.name().toUpperCase(Locale.ROOT);
        return enumCodec(valuesSupplier, encoder, (e, name) -> e.name().equalsIgnoreCase(name));
    }

    /**
     * Uses {@link String#equalsIgnoreCase(String)} for matching.<p>
     * See Also: {@link #enumCodec(Supplier, Function, BiPredicate)}
     */
    public static <E extends Enum<E>> Codec<E> enumCodec(final Supplier<E[]> valuesSupplier, final Function<E, String> encoder) {
        return enumCodec(valuesSupplier, encoder, (e, name) -> e.name().equalsIgnoreCase(name));
    }

    /**
     * Creates a {@link Codec} for {@link Enum} that may not implement {@link net.minecraft.util.StringRepresentable}.<p>
     * Supports {@link net.neoforged.neoforge.common.IExtensibleEnum} as this implementation does not cache enum values on construction.
     * 
     * @param valuesSupplier the {@link Supplier} of enum constants, should be {@code E::values} or {@link Class#getEnumConstants()}
     * @param encoder        the mapping from enum instance to encoded name
     * @param matcher        the matcher matching enum instance with decoded names, could be used to allow user-friendly decoding
     * @return a {@link Codec} for regular {@link Enum} that does not implement {@link net.minecraft.util.StringRepresentable}
     */
    public static <E extends Enum<E>> Codec<E> enumCodec(final Supplier<E[]> valuesSupplier, final Function<E, String> encoder, BiPredicate<E, String> matcher) {
        final LoadingCache<String, DataResult<E>> decodeCache = CacheBuilder.newBuilder()
                // This should work for most cases supporting both lower case and upper case inputs
                // For IExtensibleEnum, 2x of the original size would likely be enough for extensions
                .maximumSize(valuesSupplier.get().length * 2L)
                .concurrencyLevel(1)
                .build(new CacheLoader<>() {
                    @Override
                    public DataResult<E> load(String key) {
                        return Arrays.stream(valuesSupplier.get())
                                .filter(e -> matcher.test(e, key))
                                .findFirst()
                                .map(DataResult::success)
                                .orElse(DataResult.error(() -> "Unknown enum name: " + key));
                    }
                });
        return ExtraCodecs.orCompressed(
                Codec.STRING.comapFlatMap(decodeCache::getUnchecked, encoder),
                Codec.INT.comapFlatMap(
                        ordinal -> {
                            final E[] values = valuesSupplier.get();
                            return ordinal > -1 && ordinal < values.length
                                    ? DataResult.success(values[ordinal])
                                    : DataResult.error(() -> "Unknown enum id: " + ordinal);
                        },
                        Enum::ordinal));
    }

    /**
     * Creates a codec from a decoder.
     * The returned codec can only decode, and will throw on any attempt to encode.
     */
    public static <A> Codec<A> decodeOnly(Decoder<A> decoder) {
        return Codec.of(Codec.unit(() -> {
            throw new UnsupportedOperationException("Cannot encode with decode-only codec! Decoder:" + decoder);
        }), decoder, "DecodeOnly[" + decoder + "]");
    }

    /**
     * Creates a codec for a list from a codec of optional elements.
     * The empty optionals are removed from the list when decoding.
     */
    public static <A> Codec<List<A>> listWithOptionalElements(Codec<Optional<A>> elementCodec) {
        return listWithoutEmpty(elementCodec.listOf());
    }

    /**
     * Creates a codec for a list of optional elements, that removes empty values when decoding.
     */
    public static <A> Codec<List<A>> listWithoutEmpty(Codec<List<Optional<A>>> codec) {
        return codec.xmap(
                list -> list.stream().filter(Optional::isPresent).map(Optional::get).toList(),
                list -> list.stream().map(Optional::of).toList());
    }

    /**
     * Codec with two alternatives.
     * <p>
     * The vanilla {@link Codec#withAlternative(Codec, Codec)} will try
     * the first codec and then the second codec for decoding, <b>but only the first for encoding</b>.
     * <p>
     * Unlike vanilla, this alternative codec also tries to encode with the second codec if the first encode fails.
     * 
     * @see #withAlternative(MapCodec, MapCodec) for keeping {@link com.mojang.serialization.MapCodec MapCodecs} as MapCodecs.
     */
    public static <T> Codec<T> withAlternative(final Codec<T> codec, final Codec<T> alternative) {
        return new AlternativeCodec<>(codec, alternative);
    }

    /**
     * MapCodec with two alternatives.
     * <p>
     * {@link #mapWithAlternative(MapCodec, MapCodec)} will try the first codec and then the second codec for decoding, <b>but only the first for encoding</b>.
     * <p>
     * Unlike {@link #mapWithAlternative(MapCodec, MapCodec)}, this alternative codec also tries to encode with the second codec if the first encode fails.
     */
    public static <T> MapCodec<T> withAlternative(final MapCodec<T> codec, final MapCodec<T> alternative) {
        return new AlternativeMapCodec<>(codec, alternative);
    }

    private record AlternativeCodec<T>(Codec<T> codec, Codec<T> alternative) implements Codec<T> {
        @Override
        public <T1> DataResult<Pair<T, T1>> decode(final DynamicOps<T1> ops, final T1 input) {
            final DataResult<Pair<T, T1>> result = codec.decode(ops, input);
            if (result.error().isEmpty()) {
                return result;
            } else {
                return alternative.decode(ops, input);
            }
        }

        @Override
        public <T1> DataResult<T1> encode(final T input, final DynamicOps<T1> ops, final T1 prefix) {
            final DataResult<T1> result = codec.encode(input, ops, prefix);
            if (result.error().isEmpty()) {
                return result;
            } else {
                return alternative.encode(input, ops, prefix);
            }
        }

        @Override
        public String toString() {
            return "Alternative[" + codec + ", " + alternative + "]";
        }
    }

    private static class AlternativeMapCodec<T> extends MapCodec<T> {
        private final MapCodec<T> codec;
        private final MapCodec<T> alternative;

        private AlternativeMapCodec(MapCodec<T> codec, MapCodec<T> alternative) {
            this.codec = codec;
            this.alternative = alternative;
        }

        @Override
        public <T1> Stream<T1> keys(DynamicOps<T1> ops) {
            return Stream.concat(codec.keys(ops), alternative.keys(ops)).distinct();
        }

        @Override
        public <T1> DataResult<T> decode(DynamicOps<T1> ops, MapLike<T1> input) {
            DataResult<T> result = codec.decode(ops, input);
            if (result.error().isEmpty()) {
                return result;
            }
            return alternative.decode(ops, input);
        }

        @Override
        public <T1> RecordBuilder<T1> encode(T input, DynamicOps<T1> ops, RecordBuilder<T1> prefix) {
            //Build it to see if there is an error
            DataResult<T1> result = codec.encode(input, ops, prefix).build(ops.empty());
            if (result.error().isEmpty()) {
                //But then we even if there isn't we have to encode it again so that we can actually allow the building to apply
                // as our earlier build consumes the result
                return codec.encode(input, ops, prefix);
            }
            return alternative.encode(input, ops, prefix);
        }

        @Override
        public String toString() {
            return "AlternativeMapCodec[" + codec + ", " + alternative + "]";
        }
    }

    /**
     * Map dispatch codec with an alternative.
     *
     * <p>The alternative will only be used if there is no {@code "type"} key in the serialized object.
     *
     * @param typeCodec     codec for the dispatch type
     * @param type          function to retrieve the dispatch type from the dispatched type
     * @param codec         function to retrieve the dispatched type map codec from the dispatch type
     * @param fallbackCodec fallback to use when the deserialized object does not have a {@code "type"} key
     * @param <A>           dispatch type
     * @param <E>           dispatched type
     * @param <B>           fallback type
     */
    public static <A, E, B> MapCodec<Either<E, B>> dispatchMapOrElse(Codec<A> typeCodec, Function<? super E, ? extends A> type, Function<? super A, ? extends MapCodec<? extends E>> codec, MapCodec<B> fallbackCodec) {
        var dispatchCodec = typeCodec.dispatchMap(type, codec);
        return new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.concat(dispatchCodec.keys(ops), fallbackCodec.keys(ops)).distinct();
            }

            @Override
            public <T> DataResult<Either<E, B>> decode(DynamicOps<T> ops, MapLike<T> input) {
                if (input.get("type") != null) {
                    return dispatchCodec.decode(ops, input).map(Either::left);
                } else {
                    return fallbackCodec.decode(ops, input).map(Either::right);
                }
            }

            @Override
            public <T> RecordBuilder<T> encode(Either<E, B> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return input.map(
                        dispatched -> dispatchCodec.encode(dispatched, ops, prefix),
                        fallback -> fallbackCodec.encode(fallback, ops, prefix));
            }

            @Override
            public String toString() {
                return "DispatchOrElse[" + dispatchCodec + ", " + fallbackCodec + "]";
            }
        };
    }

    /**
     * Codec that matches exactly one out of two map codecs.
     * Same as {@link Codec#xor} but for {@link MapCodec}s.
     */
    public static <F, S> MapCodec<Either<F, S>> xor(MapCodec<F> first, MapCodec<S> second) {
        return new XorMapCodec<>(first, second);
    }

    private static final class XorMapCodec<F, S> extends MapCodec<Either<F, S>> {
        private final MapCodec<F> first;
        private final MapCodec<S> second;

        private XorMapCodec(MapCodec<F> first, MapCodec<S> second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.concat(first.keys(ops), second.keys(ops)).distinct();
        }

        @Override
        public <T> DataResult<Either<F, S>> decode(DynamicOps<T> ops, MapLike<T> input) {
            DataResult<Either<F, S>> firstResult = first.decode(ops, input).map(Either::left);
            DataResult<Either<F, S>> secondResult = second.decode(ops, input).map(Either::right);
            var firstValue = firstResult.result();
            var secondValue = secondResult.result();
            if (firstValue.isPresent() && secondValue.isPresent()) {
                return DataResult.error(
                        () -> "Both alternatives read successfully, cannot pick the correct one; first: " + firstValue.get() + " second: "
                                + secondValue.get(),
                        firstValue.get());
            } else if (firstValue.isPresent()) {
                return firstResult;
            } else if (secondValue.isPresent()) {
                return secondResult;
            } else {
                return firstResult.apply2((x, y) -> y, secondResult);
            }
        }

        @Override
        public <T> RecordBuilder<T> encode(Either<F, S> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return input.map(x -> first.encode(x, ops, prefix), x -> second.encode(x, ops, prefix));
        }

        @Override
        public String toString() {
            return "XorMapCodec[" + first + ", " + second + "]";
        }
    }
}
