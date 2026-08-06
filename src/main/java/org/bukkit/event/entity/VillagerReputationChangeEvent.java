package org.bukkit.event.entity;

import com.google.common.base.Preconditions;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called whenever an entity's reputation with a villager changes.
 */
public class VillagerReputationChangeEvent extends EntityEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final UUID targetUUID;
    private final Villager.ReputationEvent reason;
    private final Villager.ReputationType reputationType;
    private final int oldValue;
    private int newValue;
    private final int maxValue;

    public VillagerReputationChangeEvent(@NotNull Villager villager, @NotNull UUID targetUUID, @NotNull Villager.ReputationEvent reason, @NotNull Villager.ReputationType reputationType, int oldValue, int newValue, int maxValue) {
        super(villager);
        this.targetUUID = targetUUID;
        this.reason = reason;
        this.reputationType = reputationType;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.maxValue = maxValue;
    }

    /**
     * Get UUID of the entity for whom the reputation with a villager changes.
     *
     * @return UUID of the entity for whom the reputation with a villager
     *         changes
     */
    @NotNull
    public UUID getTargetUUID() {
        return targetUUID;
    }

    /**
     * Get the Entity for whom the reputation with a villager changes.
     *
     * @return the Entity for whom the reputation with a villager changes,
     *         or {@code null} if it isn't found
     */
    @Nullable
    public Entity getTarget() {
        return Bukkit.getEntity(targetUUID);
    }

    /**
     * Get the reason of this reputation change.
     *
     * @return the reason of this reputation change
     */
    @NotNull
    public Villager.ReputationEvent getReason() {
        return reason;
    }

    /**
     * Get the type of changed reputation.
     *
     * @return the type of changed reputation
     */
    @NotNull
    public Villager.ReputationType getReputationType() {
        return reputationType;
    }

    /**
     * Get the reputation value before the change.
     *
     * @return the reputation value before the change
     */
    public int getOldValue() {
        return oldValue;
    }

    /**
     * Get new reputation value after the change.
     *
     * @return the reputation value after the change
     */
    public int getNewValue() {
        return newValue;
    }

    /**
     * Set new reputation value for this event.
     *
     * <p>If the final value is below the reputation discard threshold, gossip
     * associated with this reputation type will be removed.
     *
     * <p>The provided value must be between 0 and
     * {@link VillagerReputationChangeEvent#getMaxValue()}, otherwise an
     * {@link IllegalArgumentException} will be thrown. Each reputation type
     * may have a different maximum value.
     *
     * @param newValue the reputation value after the change
     * @see Villager.ReputationType#getMaxValue()
     */
    public void setNewValue(int newValue) {
        Preconditions.checkArgument(0 <= newValue && newValue <= maxValue, "new value (%s) must be between [0, %s]", newValue, maxValue);
        this.newValue = newValue;
    }

    /**
     * Get maximum value for the reputation type affected by this event.
     *
     * @return the maximum value for the reputation type affected by this event
     * @see Villager.ReputationType#getMaxValue()
     */
    public int getMaxValue() {
        return maxValue;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @NotNull
    @Override
    public Villager getEntity() {
        return (Villager) super.getEntity();
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
