package net.neoforged.neoforge.event;

import net.minecraft.stats.Stat;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**Fired when a {@link Player} is awarded a {@link Stat}.  This event is fired via {@link Player#awardStat(Stat, int)}
 * <p>
 * {@link #getStat()} contains the stat to be awarded
 * {@link #setStat(Stat)} allows replacement of the stat to be awarded
 * {@link #getValue()} contains the current value to be awarded
 * {@link #setValue(int)} replaces the value to be set
 * <p>
 * This event is NOT cancelable.
 * <p>
 * This event does not have a result.
 * <p>
 * This event is fired on the {@link NeoForge#EVENT_BUS}.
 */
public class StatAwardEvent extends PlayerEvent {
    private Stat<?> stat;
    private int value;
    public StatAwardEvent(Player player, Stat<?> stat, int value) {
        super(player);
        this.stat = stat;
        this.value = value;
    }
    public Stat<?> getStat() {
        return stat;
    }

    public void setStat(Stat<?> stat) {
        this.stat = stat;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}