package net.neoforged.neoforge.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public record IndexedNonNullListCodec<E>(MapCodec<E> elementCodec, E defaultValue, Predicate<E> skipPredicate, String indexKey) implements Codec<NonNullList<E>> {

   public IndexedNonNullListCodec(MapCodec<E> elementCodec, E defaultValue, Predicate<E> skipPredicate) {
	  this(elementCodec, defaultValue, skipPredicate, "index");
   }

   @Override
   public <T> DataResult<Pair<NonNullList<E>, T>> decode(DynamicOps<T> ops, T input) {
	  var asMap = ops.getMap(input);
	  if (asMap.isError()) {
		 String err = asMap.error().map(DataResult.Error::message).orElse("Not a map.");
		 return DataResult.error(() -> err, Pair.of(NonNullList.create(), input));
	  }

	  return asMap.map(map -> {
		 int maxSize = Codec.INT.fieldOf("size")
			 .decode(ops, map)
			 .getOrThrow();

		 var finalList = NonNullList.<E>withSize(maxSize, defaultValue);
		 final var decoder = new NonNullElementDecoder<T, E>(ops, elementCodec, indexKey, finalList::set, (el) -> {
		 });

		 ops.getList(map.get("items")).ifSuccess(stream -> {
			stream.accept(decoder::process);
		 });

		 return Pair.of(finalList, input);
	  });
   }

   @Override
   public <T> DataResult<T> encode(NonNullList<E> input, DynamicOps<T> ops, T prefix) {
	  var list = ops.listBuilder();

	  for (int i = 0; i < input.size(); i++) {
		 if (!skipPredicate.test(input.get(i))) {
			var res = elementCodec.encode(input.get(i), ops, ops.mapBuilder());
			res.add(indexKey, ops.createInt(i));
			list.add(res.build(ops.empty()));
		 }
	  }

	  return ops.mapBuilder()
		  .add("items", list.build(ops.empty()))
		  .add("size", ops.createInt(input.size()))
		  .build(prefix);
   }

   private record NonNullElementDecoder<T, E>(DynamicOps<T> ops, MapCodec<E> elementCodec, String indexKey,
											  BiConsumer<Integer, E> items, Consumer<T> failed) {

	  public void process(final T value) {
		 var itemAsMap = ops.getMap(value);
		 if (itemAsMap.isError()) {
			failed.accept(value);
			return;
		 }

		 var item = itemAsMap.result().orElseThrow();
		 var index = ops.getNumberValue(item.get(indexKey));
		 var data = elementCodec.decode(ops, item);

		 if (index.isSuccess() && data.isSuccess()) {
			items.accept(index.getOrThrow().intValue(), data.getOrThrow());
			return;
		 }

		 failed.accept(value);
	  }
   }
}