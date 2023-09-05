package net.minecraftforge.common.advancements.critereon;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Predicate;

/**
 * Interface that mods can use to define {@link ItemPredicate}s with custom matching logic.
 */
public interface ICustomItemPredicate extends Predicate<ItemStack> {
   /**
    * {@return the codec for this predicate}
    * <p>
    * The codec must be registered to {@link ForgeRegistries#ITEM_PREDICATE_SERIALIZERS}.
    */
   Codec<? extends ICustomItemPredicate> codec();

   /**
    * Convert to a vanilla {@link ItemPredicate}.
    */
   default ItemPredicate toVanilla() {
      return new ItemPredicate(this);
   }
}
