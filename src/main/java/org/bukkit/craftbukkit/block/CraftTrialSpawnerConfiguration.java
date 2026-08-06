package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerStateData;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.craftbukkit.CraftLootTable;
import org.bukkit.craftbukkit.entity.CraftEntitySnapshot;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.bukkit.loot.LootTable;
import org.bukkit.spawner.TrialSpawnerConfiguration;

public class CraftTrialSpawnerConfiguration implements TrialSpawnerConfiguration {
    private final TrialSpawnerBlockEntity snapshot;
    private final RegistryAccess registry;

    private int spawnRange;
    private float totalMobs;
    private float simultaneousMobs;
    private float totalMobsAddedPerPlayer;
    private float simultaneousMobsAddedPerPlayer;
    private int ticksBetweenSpawn;
    private WeightedList<SpawnData> spawnPotentialsDefinition;
    private WeightedList<ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> lootTablesToEject;
    private ResourceKey<net.minecraft.world.level.storage.loot.LootTable> itemsToDropWhenOminous;

    public CraftTrialSpawnerConfiguration(TrialSpawnerBlockEntity snapshot, RegistryAccess registry) {
        this.snapshot = snapshot;
        this.registry = registry;
    }

    void loadFromConfig(TrialSpawnerConfig minecraft) {
        this.spawnRange = minecraft.spawnRange();
        this.totalMobs = minecraft.totalMobs();
        this.simultaneousMobs = minecraft.simultaneousMobs();
        this.totalMobsAddedPerPlayer = minecraft.totalMobsAddedPerPlayer();
        this.simultaneousMobsAddedPerPlayer = minecraft.simultaneousMobsAddedPerPlayer();
        this.ticksBetweenSpawn = minecraft.ticksBetweenSpawn();
        this.spawnPotentialsDefinition = minecraft.spawnPotentialsDefinition();
        this.lootTablesToEject = minecraft.lootTablesToEject();
        this.itemsToDropWhenOminous = minecraft.itemsToDropWhenOminous();
    }

    @Override
    public EntityType getSpawnedType() {
        if (spawnPotentialsDefinition.isEmpty()) {
            return null;
        }

        Optional<net.minecraft.world.entity.EntityType<?>> type = spawnPotentialsDefinition.unwrap().get(0).value().getEntityToSpawn().read("id", net.minecraft.world.entity.EntityType.CODEC);
        return type.map(CraftEntityType::minecraftToBukkit).orElse(null);
    }

    @Override
    public void setSpawnedType(EntityType entityType) {
        if (entityType == null) {
            getTrialData().nextSpawnData = Optional.empty();
            spawnPotentialsDefinition = WeightedList.of(); // need clear the spawnPotentials to avoid nextSpawnData being replaced later
            return;
        }
        Preconditions.checkArgument(entityType != EntityType.UNKNOWN, "Can't spawn EntityType %s from mob spawners!", entityType);

        SpawnData data = new SpawnData();
        data.getEntityToSpawn().putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(CraftEntityType.bukkitToMinecraft(entityType)).toString());
        getTrialData().nextSpawnData = Optional.of(data);
        spawnPotentialsDefinition = WeightedList.of(data);
    }

    @Override
    public float getBaseSpawnsBeforeCooldown() {
        return totalMobs;
    }

    @Override
    public void setBaseSpawnsBeforeCooldown(float amount) {
        totalMobs = amount;
    }

    @Override
    public float getBaseSimultaneousEntities() {
        return simultaneousMobs;
    }

    @Override
    public void setBaseSimultaneousEntities(float amount) {
        simultaneousMobs = amount;
    }

    @Override
    public float getAdditionalSpawnsBeforeCooldown() {
        return totalMobsAddedPerPlayer;
    }

    @Override
    public void setAdditionalSpawnsBeforeCooldown(float amount) {
        totalMobsAddedPerPlayer = amount;
    }

    @Override
    public float getAdditionalSimultaneousEntities() {
        return simultaneousMobsAddedPerPlayer;
    }

    @Override
    public void setAdditionalSimultaneousEntities(float amount) {
        simultaneousMobsAddedPerPlayer = amount;
    }

    @Override
    public int getDelay() {
      return ticksBetweenSpawn;
    }

    @Override
    public void setDelay(int delay) {
        Preconditions.checkArgument(delay >= 0, "Delay cannot be less than 0");

        ticksBetweenSpawn = delay;
    }

    @Override
    public int getSpawnRange() {
        return spawnRange;
    }

    @Override
    public void setSpawnRange(int spawnRange) {
        this.spawnRange = spawnRange;
    }

    @Override
    public EntitySnapshot getSpawnedEntity() {
        WeightedList<SpawnData> potentials = spawnPotentialsDefinition;
        if (potentials.isEmpty()) {
            return null;
        }

        return CraftEntitySnapshot.create(potentials.unwrap().get(0).value().getEntityToSpawn());
    }

    @Override
    public void setSpawnedEntity(EntitySnapshot snapshot) {
        setSpawnedEntity(snapshot, null, null);
    }

    @Override
    public void setSpawnedEntity(SpawnerEntry spawnerEntry) {
        Preconditions.checkArgument(spawnerEntry != null, "Entry cannot be null");

        setSpawnedEntity(spawnerEntry.getSnapshot(), spawnerEntry.getSpawnRule(), spawnerEntry.getEquipment());
    }

    private void setSpawnedEntity(EntitySnapshot snapshot, SpawnRule spawnRule, SpawnerEntry.Equipment equipment) {
        if (snapshot == null) {
            getTrialData().nextSpawnData = Optional.empty();
            spawnPotentialsDefinition = WeightedList.of(); // need clear the spawnPotentials to avoid nextSpawnData being replaced later
            return;
        }

        CompoundTag compoundTag = ((CraftEntitySnapshot) snapshot).getData();
        SpawnData data = new SpawnData(compoundTag, Optional.ofNullable(CraftCreatureSpawner.toMinecraftRule(spawnRule)), CraftCreatureSpawner.getEquipment(equipment));

        getTrialData().nextSpawnData = Optional.of(data);
        spawnPotentialsDefinition = WeightedList.of(data);
    }

    @Override
    public void addPotentialSpawn(EntitySnapshot snapshot, int weight, SpawnRule spawnRule) {
        addPotentialSpawn(snapshot, weight, spawnRule, null);
    }

    private void addPotentialSpawn(EntitySnapshot snapshot, int weight, SpawnRule spawnRule, SpawnerEntry.Equipment equipment) {
        Preconditions.checkArgument(snapshot != null, "Snapshot cannot be null");

        CompoundTag compoundTag = ((CraftEntitySnapshot) snapshot).getData();

        WeightedList.Builder<SpawnData> builder = WeightedList.builder(); // PAIL rename Builder
        spawnPotentialsDefinition.unwrap().forEach(entry -> builder.add(entry.value(), entry.weight()));
        builder.add(new SpawnData(compoundTag, Optional.ofNullable(CraftCreatureSpawner.toMinecraftRule(spawnRule)), CraftCreatureSpawner.getEquipment(equipment)), weight);
        spawnPotentialsDefinition = builder.build();
    }

    @Override
    public void addPotentialSpawn(SpawnerEntry spawnerEntry) {
        Preconditions.checkArgument(spawnerEntry != null, "Entry cannot be null");

        addPotentialSpawn(spawnerEntry.getSnapshot(), spawnerEntry.getSpawnWeight(), spawnerEntry.getSpawnRule(), spawnerEntry.getEquipment());
    }

    @Override
    public void setPotentialSpawns(Collection<SpawnerEntry> entries) {
        Preconditions.checkArgument(entries != null, "Entries cannot be null");

        WeightedList.Builder<SpawnData> builder = WeightedList.builder();
        for (SpawnerEntry spawnerEntry : entries) {
            CompoundTag compoundTag = ((CraftEntitySnapshot) spawnerEntry.getSnapshot()).getData();
            builder.add(new SpawnData(compoundTag, Optional.ofNullable(CraftCreatureSpawner.toMinecraftRule(spawnerEntry.getSpawnRule())), CraftCreatureSpawner.getEquipment(spawnerEntry.getEquipment())), spawnerEntry.getSpawnWeight());
        }
        spawnPotentialsDefinition = builder.build();
    }

    @Override
    public List<SpawnerEntry> getPotentialSpawns() {
        List<SpawnerEntry> entries = new ArrayList<>();

        for (Weighted<SpawnData> entry : spawnPotentialsDefinition.unwrap()) {
            CraftEntitySnapshot snapshot = CraftEntitySnapshot.create(entry.value().getEntityToSpawn());

            if (snapshot != null) {
                SpawnRule rule = entry.value().customSpawnRules().map(CraftCreatureSpawner::fromMinecraftRule).orElse(null);
                entries.add(new SpawnerEntry(snapshot, entry.weight(), rule));
            }
        }
        return entries;
    }

    @Override
    public Map<LootTable, Integer> getPossibleRewards() {
        Map<LootTable, Integer> tables = new HashMap<>();

        for (Weighted<ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> entry : lootTablesToEject.unwrap()) {
            LootTable table = CraftLootTable.minecraftToBukkit(entry.value());
            if (table != null) {
                tables.put(table, entry.weight());
            }
        }

        return tables;
    }

    @Override
    public void addPossibleReward(LootTable table, int weight) {
        Preconditions.checkArgument(table != null, "Table cannot be null");
        Preconditions.checkArgument(weight >= 1, "Weight must be at least 1");

        WeightedList.Builder<ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> builder = WeightedList.builder();
        lootTablesToEject.unwrap().forEach(entry -> builder.add(entry.value(), entry.weight()));
        builder.add(CraftLootTable.bukkitToMinecraft(table), weight);
        lootTablesToEject = builder.build();
    }

    @Override
    public void removePossibleReward(LootTable table) {
        Preconditions.checkArgument(table != null, "Key cannot be null");

        ResourceKey<net.minecraft.world.level.storage.loot.LootTable> minecraftKey = CraftLootTable.bukkitToMinecraft(table);
        WeightedList.Builder<ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> builder = WeightedList.builder();

        for (Weighted<ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> entry : lootTablesToEject.unwrap()) {
            if (!entry.value().equals(minecraftKey)) {
                builder.add(entry.value(), entry.weight());
            }
        }
        lootTablesToEject = builder.build();
    }

    @Override
    public void setPossibleRewards(Map<LootTable, Integer> rewards) {
        if (rewards == null || rewards.isEmpty()) {
            lootTablesToEject = WeightedList.of();
            return;
        }

        WeightedList.Builder<ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> builder = WeightedList.builder();
        rewards.forEach((table, weight) -> {
            Preconditions.checkArgument(table != null, "Table cannot be null");
            Preconditions.checkArgument(weight >= 1, "Weight must be at least 1");

            builder.add(CraftLootTable.bukkitToMinecraft(table), weight);
        });

        lootTablesToEject = builder.build();
    }

    @Override
    public int getRequiredPlayerRange() {
        return snapshot.trialSpawner.getRequiredPlayerRange();
    }

    @Override
    public void setRequiredPlayerRange(int requiredPlayerRange) {
        TrialSpawner.FullConfig oldConfig = snapshot.trialSpawner.config;
        snapshot.trialSpawner.config = new TrialSpawner.FullConfig(oldConfig.normal(), oldConfig.ominous(), oldConfig.targetCooldownLength(), requiredPlayerRange);
    }

    private TrialSpawnerStateData getTrialData() {
        return snapshot.getTrialSpawner().getStateData();
    }

    protected TrialSpawnerConfig toMinecraft() {
        return new TrialSpawnerConfig(spawnRange, totalMobs, simultaneousMobs, totalMobsAddedPerPlayer, simultaneousMobsAddedPerPlayer, ticksBetweenSpawn, spawnPotentialsDefinition, lootTablesToEject, itemsToDropWhenOminous);
    }
}
