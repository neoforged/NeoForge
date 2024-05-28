package net.neoforged.neoforge.common.util;

public class NeoForgeCollectors {

   /**
	* Stream support for converting a stream of NBT tags into an NBT ListTag.
	* Useful for stream operations and mapping functions.
	* Usage: {@code Stream.doStuff().collect(NeoForgeCollectors.toNbtList())}
	*/
   public static NbtListCollector toNbtList() {
	  return new NbtListCollector();
   }
}
