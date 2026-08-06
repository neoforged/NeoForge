package org.bukkit.event.player;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event that is called when a player right clicks an entity that
 * also contains the location where the entity was clicked.
 */
public class PlayerInteractAtEntityEvent extends PlayerInteractEntityEvent {
    private final Vector position;

    public PlayerInteractAtEntityEvent(@NotNull Player who, @NotNull Entity clickedEntity, @NotNull Vector position) {
        this(who, clickedEntity, position, EquipmentSlot.HAND);
    }

    public PlayerInteractAtEntityEvent(@NotNull Player who, @NotNull Entity clickedEntity, @NotNull Vector position, @NotNull EquipmentSlot hand) {
        super(who, clickedEntity, hand);
        this.position = position;
    }

    @NotNull
    public Vector getClickedPosition() {
        return position.clone();
    }
}
