package net.neoforged.testframework.gametest;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("unchecked")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface TemplateBuilderHelper<T extends TemplateBuilderHelper<T>> {
    T set(int x, int y, int z, BlockState state, @Nullable CompoundTag nbt);

    default T placeFloorLever(int x, int y, int z, boolean powered) {
        set(x, y, z, Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.FLOOR).setValue(LeverBlock.POWERED, powered), null);
        set(x, y - 1, z, Blocks.STONE.defaultBlockState(), null);
        return (T) this;
    }
}
