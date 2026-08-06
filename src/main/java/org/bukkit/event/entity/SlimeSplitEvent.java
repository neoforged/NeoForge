package org.bukkit.event.entity;

import org.bukkit.entity.Slime;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a Slime splits into smaller Slimes upon death
 *
 * @deprecated use parent class {@link CubeMobSplitEvent}
 */
@Deprecated(since = "26.2")
public class SlimeSplitEvent extends CubeMobSplitEvent {

    public SlimeSplitEvent(@NotNull final Slime slime, final int count) {
        super(slime, count);
    }

    @NotNull
    @Override
    public Slime getEntity() {
        return (Slime) super.getEntity();
    }
}
