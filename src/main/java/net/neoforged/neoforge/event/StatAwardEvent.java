package net.neoforged.neoforge.event;

import net.minecraft.stats.Stat;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;

/**Fired when a {@link Player} is awarded a {@link Stat}.  This event is fired in {@link Player#awardStat(Stat, int)}
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

    @ApiStatus.Internal
    public StatAwardEvent(Player player, Stat<?> stat, int value) {
        super(player);
        this.stat = stat;
        this.value = value;
    }

    /**@return the {@link Stat} being awarded*/
    public Stat<?> getStat() {
        return stat;
    }

    /**allows replacement of the {@link Stat} to be awarded*/
    public void setStat(Stat<?> stat) {
        this.stat = stat;
    }

    /**@return the current value to be awarded to the {@link Stat}*/
    public int getValue() {
        return value;
    }

    /**replaces the value to be awarded*/
    public void setValue(int value) {
        this.value = value;
    }
}