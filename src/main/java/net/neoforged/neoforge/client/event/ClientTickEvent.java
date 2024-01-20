package net.neoforged.neoforge.client.event;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Base class of the two client tick events.
 * <p>
 * Note, these events do not provide a reference to {@link Minecraft} so they do not have to be isolated. Use {@link Minecraft#getInstance()} to retrieve it.
 * 
 * @see Pre
 * @see Post
 */
public abstract class ClientTickEvent extends Event {

    @ApiStatus.Internal
    public ClientTickEvent() {}

    /**
     * {@link ClientTickEvent.Pre} is fired once per client tick, before the client performs work for the current tick.
     * <p>
     * This event only fires on the physical client.
     */
    public static class Pre extends ClientTickEvent {

        public Pre() {}

    }

    /**
     * {@link ClientTickEvent.Post} is fired once per client tick, after the client performs work for the current tick.
     * <p>
     * This event only fires on the physical client.
     */
    public static class Post extends ClientTickEvent {

        public Post() {}

    }
}
