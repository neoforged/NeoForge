package net.neoforged.neoforge.event.tick;

import java.util.function.BooleanSupplier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Base class of the two level tick events.
 * 
 * @see Pre
 * @see Post
 */
public abstract class LevelTickEvent extends Event {

    private final BooleanSupplier hasTime;
    private final Level level;

    @ApiStatus.Internal
    public LevelTickEvent(BooleanSupplier hasTime, Level level) {
        this.hasTime = hasTime;
        this.level = level;
    }

    /**
     * {@return true if the server has enough time to perform any
     * additional tasks (usually IO related) during the current tick,
     * otherwise false}
     */
    public boolean hasTime() {
        return this.hasTime.getAsBoolean();
    }

    /**
     * {@return the level being ticked}
     */
    public Level getLevel() {
        return level;
    }

    /**
     * {@link LevelTickEvent.Pre} is fired once per game tick, per level, before the level performs work for the current tick.
     * <p>
     * This event may fire on both the logical server and logical client, since both {@link ClientLevel} and {@link ServerLevel} tick.
     * <p>
     * As such, be sure to check {@link Level#isClientSide()} before performing any operations.
     */
    public static class Pre extends LevelTickEvent {

        public Pre(BooleanSupplier haveTime, Level level) {
            super(haveTime, level);
        }

    }

    /**
     * {@link LevelTickEvent.Post} is fired once per game tick, per level, after the level performs work for the current tick.
     * <p>
     * This event may fire on both the logical server and logical client, since both {@link ClientLevel} and {@link ServerLevel} tick.
     * <p>
     * As such, be sure to check {@link Level#isClientSide()} before performing any operations.
     */
    public static class Post extends LevelTickEvent {

        public Post(BooleanSupplier haveTime, Level level) {
            super(haveTime, level);
        }

    }
}
