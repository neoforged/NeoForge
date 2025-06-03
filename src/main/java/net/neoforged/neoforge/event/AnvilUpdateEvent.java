/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

/**
 * This event is fired when an anvil's left input is not empty, and any of the inputs (left, right, or name) are changed.
 * <p>
 * It is fired after the vanilla result has been computed, and allows for modification (or overriding) of the result.
 * <p>
 * This event is fired on both the logical server and logical client.
 */
public class AnvilUpdateEvent extends Event implements ICancellableEvent {
    private final ItemStack left;
    private final ItemStack right;
    @Nullable
    private final String name;
    private final VanillaResult vanillaResult;
    private final Player player;

    private ItemStack output;
    private int xpCost;
    private int materialCost;

    public AnvilUpdateEvent(ItemStack left, ItemStack right, @Nullable String name, ItemStack result, int xpCost, int materialCost, Player player) {
        this.left = left;
        this.right = right;
        this.name = name;
        this.vanillaResult = new VanillaResult(result, xpCost, materialCost);
        this.player = player;
        this.output = result.copy();
        this.xpCost = xpCost;
        this.materialCost = materialCost;
    }

    /**
     * {@return a copy of the item in the left input slot}
     */
    public ItemStack getLeft() {
        return this.left.copy();
    }

    /**
     * {@return a copy of the item in the right input slot}
     */
    public ItemStack getRight() {
        return this.right.copy();
    }

    /**
     * This is the name as sent by the client. It may be null if none has been sent.
     * If empty, it indicates the user wishes to clear the custom name from the item.
     * <p>
     * The server is unable to change the name field, as there is no S2C packet for it.
     * 
     * @return The name that the output item should be set to, if applicable.
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * {@return a view of the vanilla result for the given anvil inputs}
     * 
     * @see {@link VanillaResult}
     */
    public VanillaResult getVanillaResult() {
        return vanillaResult;
    }

    /**
     * Returns a mutable reference to the current output stack, defaulting to the vanilla output.
     * <p>
     * If this event is cancelled, this output is ignored.
     * 
     * @return The item that will be set in the output slot.
     */
    public ItemStack getOutput() {
        return output;
    }

    /**
     * Sets the output to the given item stack.
     * 
     * @param output The stack to change the output to.
     */
    public void setOutput(ItemStack output) {
        this.output = output;
    }

    /**
     * Returns the level cost of the anvil operation, defaulting to the vanilla cost.
     * <p>
     * If this event is cancelled, the level cost is ignored.
     * 
     * @return The level cost of the anvil operation.
     */
    public int getXpCost() {
        return this.xpCost;
    }

    /**
     * Sets the level cost of the anvil operation.
     * 
     * @param cost The new level cost.
     */
    public void setXpCost(int xpCost) {
        this.xpCost = xpCost;
    }

    /**
     * Returns the material cost of the anvil operation, defaulting to the vanilla cost.
     * <p>
     * The material cost is how many units of the right input stack are consumed.
     * 
     * @return The material cost of the anvil operation.
     */
    public int getMaterialCost() {
        return materialCost;
    }

    /**
     * Sets the material cost (how many right inputs are consumed).
     * <p>
     * A material cost of zero consumes the entire stack (due to vanilla behavior).
     * <p>
     * A material cost higher than the count of the right stack consumes the entire stack.
     * <p>
     * The material cost being higher than the right item count does not prevent the output from being available.
     * 
     * @param materialCost The new material cost.
     */
    public void setMaterialCost(int materialCost) {
        this.materialCost = materialCost;
    }

    /**
     * @return The player using this anvil container.
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Cancels the event, preventing any further handling.
     * <p>
     * When the event is cancelled, the outputs set by this event are ignored, and the anvil will produce no output.
     */
    @Override
    public void setCanceled(boolean canceled) {
        ICancellableEvent.super.setCanceled(canceled);
    }

    /**
     * A record packing all the vanilla result data.
     * 
     * @param output       The result of the vanilla anvil operation.
     * @param xpCost       The experience cost of the vanilla anvil operation.
     * @param materialCost The material (right input) cost of the vanilla anvil operation.
     */
    public static record VanillaResult(ItemStack output, int xpCost, int materialCost) {
        public ItemStack output() {
            return this.output.copy(); // Always copy the result to prevent accidental modification.
        }
    }
}
