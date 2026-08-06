package org.bukkit.event.player;

import com.google.gson.JsonElement;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called after a player runs a custom action from a chat event or form
 * submission.
 */
@ApiStatus.Experimental
public class PlayerCustomClickEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    @NotNull
    private final NamespacedKey id;
    @Nullable
    private final JsonElement data;

    public PlayerCustomClickEvent(@NotNull Player player, @NotNull NamespacedKey id, @Nullable JsonElement data) {
        super(player);
        this.id = id;
        this.data = data;
    }

    /**
     * Gets the ID of the custom action.
     *
     * @return custom action ID
     */
    @NotNull
    public NamespacedKey getId() {
        return id;
    }

    /**
     * Gets the data of the custom action as a {@link JsonElement}, or null if
     * not available.
     * <br>
     * If not a form submission, then may be null.
     *
     * @return data as JSON or null
     */
    @Nullable
    public JsonElement getData() {
        return data;
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
