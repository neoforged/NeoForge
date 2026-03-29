package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public interface ICropBlockExtension {

	private CropBlock self() {
		return (CropBlock) this;
	}

	/// state sensitive variant of [#getStateForAge(int)]
	///
	/// Override if the crop has block state properties other than age.
	default BlockState getStateForAge(LevelReader level, BlockPos pos, BlockState state, int age) {
		return self().getStateForAge(age);
	}

}
