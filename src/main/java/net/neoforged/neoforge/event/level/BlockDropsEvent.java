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
 * It is safe to modify the Block in this event, as it has already been replaced.
 */
public class BlockDropsEvent extends BlockEvent implements ICancellableEvent {

    private final List<ItemStack> drops;
    private final Entity destroyingEntity;
    private final ItemStack tool;

    private boolean dropXpWhenCancelled;

    /**
     * Cancellation will result in the drops not being spawned.
     */
    public BlockDropsEvent(LevelAccessor level, BlockPos pos, BlockState state, List<ItemStack> drops, @Nullable Entity destroyer, ItemStack tool) {
        super(level, pos, state);
        this.drops = drops;
        this.destroyingEntity = destroyer;
        this.tool = tool;
        this.dropXpWhenCancelled = true;
    }

    /**
     * Sets whether XP Orb Entities should still be spawned when the event is cancelled.
     * @param shouldDrop Whether XP should be spawned.
     */
    public void setDropXpWhenCancelled(boolean shouldDrop) {
        this.dropXpWhenCancelled = shouldDrop;
    }

    /**
     * Returns a list of drops determined for this broken block.
     * @return An immutable list of ItemStacks.
     */
    public List<ItemStack> getDrops() {
        return drops;
    }

    /**
     * Returns the entity associated with this broken block. Might be null.
     * @return The entity responsible for the breaking of this block, or null.
     */
    public @Nullable Entity getDestroyingEntity() {
        return destroyingEntity;
    }

    /**
     * Returns the tool associated with this broken block.
     * @return The used tool as ItemStack, or ItemStack.EMPTY
     */
    public ItemStack getTool() {
        return tool;
    }

    public boolean isDropXpWhenCancelled() {
        return dropXpWhenCancelled;
    }
}
