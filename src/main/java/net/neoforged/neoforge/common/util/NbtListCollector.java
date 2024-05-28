package net.neoforged.neoforge.common.util;

import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Stream support for converting a stream of NBT tags into an NBT ListTag.
 * Useful for stream operations and mapping functions.
 *
 * Usage: {@code Stream.doStuff().collect(NbtListCollector.toNbtList())}
 */
public class NbtListCollector implements Collector<Tag, ListTag, ListTag> {

   @Override
   public Supplier<ListTag> supplier() {
	  return ListTag::new;
   }

   @Override
   public BiConsumer<ListTag, Tag> accumulator() {
	  return ListTag::add;
   }

   @Override
   public BinaryOperator<ListTag> combiner() {
	  return (res1, res2) -> {
		 res1.addAll(res2);
		 return res1;
	  };
   }

   @Override
   public Function<ListTag, ListTag> finisher() {
	  return Function.identity();
   }

   @Override
   public Set<Characteristics> characteristics() {
	  return ImmutableSet.of(Collector.Characteristics.CONCURRENT, Collector.Characteristics.UNORDERED);
   }
}
