package net.neoforged.neoforge.event.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Fired when a block is destroyed AFTER drops have been determined, but have not yet spawned.
 * - Cancellation will result in the drops not being spawned.
 * - Breaker may be null if the block was not destroyed by an entity.
 * - The drops list is immutable and cannot be modified. Use LootTableModifiers instead
 * - It is safe to modify the Block in this event, as it has already been replaced.
 */
public class BlockDropsEvent extends BlockEvent implements ICancellableEvent {

    private final List<ItemStack> drops;
    private final Entity destroyingEntity;
    private final ItemStack tool;

    private boolean dropXpWhenCancelled;

    public BlockDropsEvent(LevelAccessor level, BlockPos pos, BlockState state, List<ItemStack> drops, @Nullable Entity destroyer, ItemStack tool) {
        super(level, pos, state);
        this.drops = drops;
        this.destroyingEntity = destroyer;
        this.tool = tool;
        this.dropXpWhenCancelled = true;
    }

    public void setDropXpWhenCancelled(boolean shouldDrop) {
        this.dropXpWhenCancelled = shouldDrop;
    }

    public List<ItemStack> getDrops() {
        return drops;
    }

    public Entity getDestroyingEntity() {
        return destroyingEntity;
    }

    public ItemStack getTool() {
        return tool;
    }

    public boolean isDropXpWhenCancelled() {
        return dropXpWhenCancelled;
    }
}
