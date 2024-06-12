package net.neoforged.neoforge.capabilities.color.vanilla;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.capabilities.color.ColorApplicationResult;
import net.neoforged.neoforge.capabilities.color.IColorable;

public record SheepDyeConsumer(Sheep sheep) implements IColorable {
   public ColorApplicationResult apply(DyeColor dye) {
	  if(!sheep.isAlive() || sheep.isSheared()) {
		 return ColorApplicationResult.CANNOT_APPLY;
	  }

	  if(sheep.getColor() != dye) {
		 // TODO: Player context? sheep.level().playSound(p_41086_, sheep, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
		 sheep.setColor(dye);
		 return ColorApplicationResult.APPLIED;
	  } else {
		 return ColorApplicationResult.ALREADY_APPLIED;
	  }
   }
}
