/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapCodec.MapCodecCodec;
import com.mojang.serialization.codecs.KeyDispatchCodec;
import net.minecraft.util.ExtraCodecs;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.stream.Stream;

public class NeoForgeExtraCodecs
{
   public static <T> MapCodec<T> aliasedFieldOf(final Codec<T> codec, final String... names)
   {
      if (names.length == 0)
         throw new IllegalArgumentException("Must have at least one name!");
      MapCodec<T> mapCodec = codec.fieldOf(names[0]);
      for (int i = 1; i < names.length; i++)
         mapCodec = mapWithAlternative(mapCodec, codec.fieldOf(names[i]));
      return mapCodec;
   }
   
   public static <T> MapCodec<T> mapWithAlternative(final MapCodec<T> mapCodec, final MapCodec<? extends T> alternative)
   {
      return Codec.mapEither(mapCodec, alternative).xmap(either -> either.map(Function.identity(), Function.identity()), Either::left);
   }
   
   public static <T> MapCodec<Set<T>> singularOrPluralCodec(final Codec<T> codec, final String singularName)
   {
      return singularOrPluralCodec(codec, singularName, "%ss".formatted(singularName));
   }
   
   public static <T> MapCodec<Set<T>> singularOrPluralCodec(final Codec<T> codec, final String singularName, final String pluralName)
   {
      return Codec.mapEither(codec.fieldOf(singularName), setOf(codec).fieldOf(pluralName)).xmap(
            either -> either.map(ImmutableSet::of, ImmutableSet::copyOf),
            set -> set.size() == 1 ? Either.left(set.iterator().next()) : Either.right(set)
      );
   }
   
   public static <T> MapCodec<Set<T>> singularOrPluralCodecNotEmpty(final Codec<T> codec, final String singularName)
   {
      return singularOrPluralCodecNotEmpty(codec, singularName, "%ss".formatted(singularName));
   }
   
   public static <T> MapCodec<Set<T>> singularOrPluralCodecNotEmpty(final Codec<T> codec, final String singularName, final String pluralName)
   {
      return Codec.mapEither(codec.fieldOf(singularName), setOf(codec).fieldOf(pluralName)).xmap(
            either -> either.map(ImmutableSet::of, ImmutableSet::copyOf),
            set -> set.size() == 1 ? Either.left(set.iterator().next()) : Either.right(set)
      ).flatXmap(ts -> {
         if (ts.isEmpty())
            return DataResult.error(() -> "The set for: %s can not be empty!".formatted(singularName));
         return DataResult.success(ts);
      }, ts -> {
         if (ts.isEmpty())
            return DataResult.error(() -> "The set for: %s can not be empty!".formatted(singularName));
         return DataResult.success(ImmutableSet.copyOf(ts));
      });
   }
   
   public static <T> Codec<Set<T>> setOf(final Codec<T> codec)
   {
      return Codec.list(codec).xmap(ImmutableSet::copyOf, ImmutableList::copyOf);
   }

   /**
    * Version of {@link Codec#dispatch(Function, Function)} that always writes the dispatched codec inline,
    * i.e. at the same nesting level as the {@code "type": ...}.
    * <p>
    * Note: the codec produced by {@code .dispatch()} inlines the dispatched codec ONLY if it is a {@link MapCodecCodec}.
    * This function always inlines.
    */
   public static <K, V> Codec<V> dispatchUnsafe(final Codec<K> keyCodec, final Function<? super V, ? extends K> type, final Function<? super K, ? extends Codec<? extends V>> codec)
   {
      return dispatchUnsafe(keyCodec, "type", type, codec);
   }

   /**
    * Version of {@link Codec#dispatch(String, Function, Function)} that always writes the dispatched codec inline,
    * i.e. at the same nesting level as the {@code "type": ...}.
    * <p>
    * Note: the codec produced by {@code .dispatch()} inlines the dispatched codec ONLY if it is a {@link MapCodecCodec}.
    * This function always inlines.
    */
   public static <K, V> Codec<V> dispatchUnsafe(final Codec<K> keyCodec, final String typeKey, final Function<? super V, ? extends K> type, final Function<? super K, ? extends Codec<? extends V>> codec)
   {
      return partialDispatchUnsafe(keyCodec, typeKey, type.andThen(DataResult::success), codec.andThen(DataResult::success));
   }

   /**
    * Version of {@link Codec#partialDispatch(String, Function, Function)} that always writes the dispatched codec inline,
    * i.e. at the same nesting level as the {@code "type": ...}.
    * <p>
    * Note: the codec produced by {@code .dispatch()} inlines the dispatched codec ONLY if it is a {@link MapCodecCodec}.
    * This function always inlines.
    */
   public static <K, V> Codec<V> partialDispatchUnsafe(final Codec<K> keyCodec, final String typeKey, final Function<? super V, ? extends DataResult<? extends K>> type, final Function<? super K, ? extends DataResult<? extends Codec<? extends V>>> codec)
   {
      return KeyDispatchCodec.unsafe(typeKey, keyCodec, type, codec, v -> type.apply(v).<Encoder<? extends V>>flatMap(k -> codec.apply(k).map(Function.identity())).map(e -> ((Encoder<V>) e))).codec();
   }
   
   public static <A> Decoder<List<A>> listDecoderWithOptionalElements(final Decoder<Optional<A>> codec)
   {
      return new ListDecoderWithOptionalElements<>(codec);
   }
   
   public static <A> Decoder<List<A>> listDecoderWithIndexConsumer(final Decoder<List<A>> decoder, ObjIntConsumer<A> consumer)
   {
      return new ListDecoderWithIndexConsumer<>(decoder, consumer);
   }
   
   public static <Z, A> Decoder<List<A>> listOptionalUnwrapDecoder(final Decoder<List<Z>> decoder, Function<Z, Optional<A>> unwrap)
   {
      return new ListOptionalUnwrapDecoder<>(decoder, unwrap);
   }
   
   public static <A> Decoder<List<A>> listDecoder(final Decoder<A> decoder) {
      return Codec.of(Codec.unit(() ->
      {
         throw new UnsupportedOperationException("Cannot encode with list decoder!");
      }), decoder, decoder.toString()).listOf();
   }

   /**
    * Codec with two alternatives.
    * <p>
    * The vanilla {@link ExtraCodecs#withAlternative(Codec, Codec)} will try
    * the first codec and then the second codec for decoding, <b>but only the first for encoding</b>.
    * <p>
    * Unlike vanilla, this alternative codec also tries to encode with the second codec if the first encode fails.
    */
   public static <T> Codec<T> withAlternative(final Codec<T> codec, final Codec<T> alternative)
   {
      return new AlternativeCodec<>(codec, alternative);
   }
   
   private record ListDecoderWithOptionalElements<A>(Decoder<Optional<A>> codec) implements Decoder<List<A>>
   {
      @Override
      public <T> DataResult<Pair<List<A>, T>> decode(final DynamicOps<T> ops, final T input)
      {
         return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap(stream ->
         {
            final ImmutableList.Builder<A> read = ImmutableList.builder();
            final Stream.Builder<T> failed = Stream.builder();
            final AtomicReference<DataResult<Unit>> result = new AtomicReference<>(DataResult.success(Unit.INSTANCE, Lifecycle.stable()));
            
            stream.accept(t ->
            {
               final DataResult<Pair<Optional<A>, T>> element = codec.decode(ops, t);
               element.error().ifPresent(e -> failed.add(t));
               result.setPlain(result.getPlain().apply2stable((r, v) ->
               {
                  v.getFirst().ifPresent(read::add);
                  return r;
               }, element));
            });
            
            final ImmutableList<A> elements = read.build();
            final T errors = ops.createList(failed.build());
            
            final Pair<List<A>, T> pair = Pair.of(elements, errors);
            
            return result.getPlain().map(unit -> pair).setPartial(pair);
         });
      }
      
      @Override
      public String toString() {
         return "ListDecoderWithOptionalElements[" + codec + "]";
      }
   }
   
   private record ListDecoderWithIndexConsumer<A>(Decoder<List<A>> decoder, ObjIntConsumer<A> consumer) implements Decoder<List<A>>
   {
      
      @Override
      public <T> DataResult<Pair<List<A>, T>> decode(DynamicOps<T> ops, T input)
      {
         return decoder.decode(ops, input)
                      .map(pair ->
                      {
                         for (int i = 0; i < pair.getFirst().size(); i++)
                         {
                            consumer.accept(pair.getFirst().get(i), i);
                         }
                         return pair;
                      });
      }
      
      @Override
      public String toString()
      {
         return "ListDecoderWithIndexConsumer[" + decoder + "]";
      }
   }
   
   private record ListOptionalUnwrapDecoder<Z, A>(Decoder<List<Z>> codec, Function<Z, Optional<A>> unwrap) implements Decoder<List<A>>
   {
      
      @Override
      public <T> DataResult<Pair<List<A>, T>> decode(DynamicOps<T> ops, T input)
      {
         return codec.decode(ops, input)
                      .map(pair ->
                      {
                         final var builder = ImmutableList.<A>builder();
                         pair.getFirst().forEach(o -> unwrap.apply(o).ifPresent(builder::add));
                         return Pair.of(builder.build(), pair.getSecond());
                      });
      }
      
      @Override
      public String toString()
      {
         return "ListOptionalUnwrap[" + codec + "]";
      }
   }
   
   private record AlternativeCodec<T>(Codec<T> codec, Codec<T> alternative) implements Codec<T>
   {
      @Override
      public <T1> DataResult<Pair<T, T1>> decode(final DynamicOps<T1> ops, final T1 input)
      {
         final DataResult<Pair<T, T1>> result = codec.decode(ops, input);
         if (result.error().isEmpty())
         {
            return result;
         }
         else
         {
            return alternative.decode(ops, input);
         }
      }
      
      @Override
      public <T1> DataResult<T1> encode(final T input, final DynamicOps<T1> ops, final T1 prefix)
      {
         final DataResult<T1> result = codec.encode(input, ops, prefix);
         if (result.error().isEmpty())
         {
            return result;
         }
         else
         {
            return alternative.encode(input, ops, prefix);
         }
      }
      
      @Override
      public String toString()
      {
         return "Alternative[" + codec + ", " + alternative + "]";
      }
   }
}