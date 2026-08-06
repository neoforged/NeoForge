package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import net.minecraft.world.level.storage.loot.LootTable;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Vault;
import org.bukkit.craftbukkit.CraftLootTable;
import org.bukkit.craftbukkit.inventory.CraftItemStack;

public class CraftVault extends CraftBlockEntityState<VaultBlockEntity> implements Vault {
    private CraftVaultConfiguration config;

    public CraftVault(World world, VaultBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftVault(CraftVault state, Location location) {
        super(state, location);
    }

    @Override
    public CraftVault copy() {
        return new CraftVault(this, null);
    }

    @Override
    public CraftVault copy(Location location) {
        return new CraftVault(this, location);
    }

    @Override
    public double getActivationRange() {
        return config.activationRange;
    }

    @Override
    public void setActivationRange(double range) {
        config.activationRange = range;
    }

    @Override
    public double getDeactivationRange() {
        return config.deactivationRange;
    }

    @Override
    public void setDeactivationRange(double range) {
        config.deactivationRange = range;
    }

    @Override
    public org.bukkit.loot.LootTable getLootTable() {
        return CraftLootTable.minecraftToBukkit(config.lootTable);
    }

    @Override
    public void setLootTable(org.bukkit.loot.LootTable lootTable) {
        Preconditions.checkArgument(lootTable != null, "LootTable cannot be null");
        config.lootTable = CraftLootTable.bukkitToMinecraft(lootTable);
    }

    @Override
    public org.bukkit.loot.LootTable getDisplayLootTable() {
        return config.overrideLootTableToDisplay.map(CraftLootTable::minecraftToBukkit).orElse(null);
    }

    @Override
    public void setDisplayLootTable(org.bukkit.loot.LootTable lootTable) {
        config.overrideLootTableToDisplay = Optional.ofNullable(CraftLootTable.bukkitToMinecraft(lootTable));
    }

    @Override
    public org.bukkit.inventory.ItemStack getKeyItem() {
        return CraftItemStack.asBukkitCopy(config.keyItem);
    }

    @Override
    public void setKeyItem(org.bukkit.inventory.ItemStack keyItem) {
        Preconditions.checkArgument(keyItem != null, "Key item cannot be null");
        config.keyItem = CraftItemStack.asNMSCopy(keyItem);
    }

    @Override
    protected void load(VaultBlockEntity tileEntity) {
        super.load(tileEntity);
        if (config == null) {
            config = new CraftVaultConfiguration();
        }
        if (tileEntity != null) {
            config.loadFromConfig(tileEntity.getConfig());
        }
    }

    @Override
    protected void applyTo(VaultBlockEntity tileEntity) {
        super.applyTo(tileEntity);

        tileEntity.setConfig(this.config.toMinecraft());
    }

    @Override
    public Set<UUID> getRewardedPlayers() {
        requirePlaced();

        VaultServerData serverData = getTileEntity().getServerData();
        Objects.requireNonNull(serverData, "serverData should not be null for placed Vault");

        return Collections.unmodifiableSet(serverData.getRewardedPlayers());
    }

    static class CraftVaultConfiguration {
        private ResourceKey<LootTable> lootTable;
        private double activationRange;
        private double deactivationRange;
        private ItemStack keyItem;
        private Optional<ResourceKey<LootTable>> overrideLootTableToDisplay;
        private PlayerDetector playerDetector;
        private PlayerDetector.EntitySelector entitySelector;

        private void loadFromConfig(VaultConfig minecraft) {
            this.lootTable = minecraft.lootTable();
            this.activationRange = minecraft.activationRange();
            this.deactivationRange = minecraft.deactivationRange();
            this.keyItem = minecraft.keyItem();
            this.overrideLootTableToDisplay = minecraft.overrideLootTableToDisplay();
            this.playerDetector = minecraft.playerDetector();
            this.entitySelector = minecraft.entitySelector();
        }

        private VaultConfig toMinecraft() {
            return new VaultConfig(lootTable, activationRange, deactivationRange, keyItem, overrideLootTableToDisplay, playerDetector, entitySelector);
        }
    }
}
