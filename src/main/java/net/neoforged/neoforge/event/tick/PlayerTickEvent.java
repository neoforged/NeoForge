package net.neoforged.neoforge.event.tick;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Base class of the two player tick events.
 * 
 * @see Pre
 * @see Post
 */
public abstract class PlayerTickEvent extends Event {

    private final Player player;

    @ApiStatus.Internal
    public PlayerTickEvent(Player player) {
        this.player = player;
    }

    /**
     * {@return the player being ticked}
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * {@link PlayerTickEvent.Pre} is fired once per game tick, per player, before the player performs work for the current tick.
     * <p>
     * This event may fire on both the logical server and logical client, for all subclasses of {@link Player} on their respective sides.
     * <p>
     * As such, be sure to check {@link Player#isClientSide()} before performing any operations.
     */
    public static class Pre extends PlayerTickEvent {

        public Pre(Player player) {
            super(player);
        }

    }

    /**
     * {@link PlayerTickEvent.Post} is fired once per game tick, per player, after the player performs work for the current tick.
     * <p>
     * This event may fire on both the logical server and logical client, for all subclasses of {@link Player} on their respective sides.
     * <p>
     * As such, be sure to check {@link Player#isClientSide()} before performing any operations.
     */
    public static class Post extends PlayerTickEvent {

        public Post(Player player) {
            super(player);
        }

    }
}
