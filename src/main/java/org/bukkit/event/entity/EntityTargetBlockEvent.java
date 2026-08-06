package org.bukkit.event.entity;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a creature targets or untargets a block, for example when a
 * copper golem decides whether to walk to a given chest.
 */
@ApiStatus.Experimental
public class EntityTargetBlockEvent extends EntityEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;
    private Block target;

    public EntityTargetBlockEvent(@NotNull final Entity entity, @Nullable final Block target) {
        super(entity);
        this.target = target;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    /**
     * Get the block that this is targeting.
     * <p>
     * This will be null in the case that the event is called when the mob
     * forgets its target.
     *
     * @return The block
     */
    @Nullable
    public Block getTarget() {
        return target;
    }

    /**
     * Set the block that you want the mob to target instead.
     * <p>
     * It is possible to be null, null will cause the entity to be target-less.
     * <p>
     * This is different from cancelling the event. Cancelling the event will
     * cause the entity to keep an original target or attempt to find an
     * alternative target, while setting to be null will cause the target to be
     * cleared.
     *
     * @param target The block to target
     */
    public void setTarget(@Nullable Block target) {
        this.target = target;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
