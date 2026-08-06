package org.bukkit.block;

import java.util.Set;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a captured state of a vault.
 */
public interface Vault extends TileState {

    /**
     * Gets the distance at which a player must enter for this vault to
     * activate.
     *
     * @return the distance at which a player must enter for this vault
     * to activate.
     */
    double getActivationRange();

    /**
     * Sets the distance at which a player must enter for this vault to
     * activate.
     *
     * @param range the distance at which a player must enter for this
     * vault to activate.
     */
    void setActivationRange(double range);

    /**
     * Gets the distance at which a player must exit for the vault to
     * deactivate.
     *
     * @return the distance at which a player must exit for the vault
     * to deactivate.
     */
    double getDeactivationRange();

    /**
     * Sets the distance at which a player must exit for this vault to
     * deactivate.
     *
     * @param range the distance at which a player must exit for this
     * vault to deactivate.
     */
    void setDeactivationRange(double range);

    /**
     * Gets the {@link LootTable} this vault will pick rewards from.
     *
     * @return the loot table
     */
    @NotNull
    LootTable getLootTable();

    /**
     * Sets the {@link LootTable} this vault will pick rewards from.
     *
     * @param table the loot table
     */
    void setLootTable(@NotNull LootTable table);

    /**
     * Gets the {@link LootTable} this vault will display items from. <br>
     * If this value is null the regular loot table will be used to display
     * items.
     *
     * @return the loot table to display items from
     */
    @Nullable
    LootTable getDisplayLootTable();

    /**
     * Sets the {@link LootTable} this vault will display items from. <br>
     * If this value is set to null the regular loot table will be used to
     * display items.
     *
     * @param table the loot table to display items from
     */
    void setDisplayLootTable(@Nullable LootTable table);

    /**
     * Gets the {@link ItemStack} players must use to unlock this vault.
     *
     * @return the key item
     */
    @NotNull
    ItemStack getKeyItem();

    /**
     * Sets the {@link ItemStack} players must use to unlock this vault.
     *
     * @param keyItem the key item
     */
    void setKeyItem(@NotNull ItemStack keyItem);

    /**
     * Gets the players who have already received rewards from this vault.
     *
     * @return unmodifiable set of player UUIDs
     * @throws IllegalStateException if this block state is not placed
     */
    @NotNull
    Set<UUID> getRewardedPlayers();
}
