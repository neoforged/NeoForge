package org.bukkit.craftbukkit.generator.structure;

import com.google.common.base.Preconditions;
import net.minecraft.core.registries.Registries;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.util.Handleable;
import org.bukkit.generator.structure.StructureType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CraftStructureType extends StructureType implements Handleable<net.minecraft.world.level.levelgen.structure.StructureType<?>> {

    public static StructureType minecraftToBukkit(net.minecraft.world.level.levelgen.structure.StructureType<?> minecraft) {
        return CraftRegistry.minecraftToBukkit(minecraft, Registries.STRUCTURE_TYPE, Registry.STRUCTURE_TYPE);
    }

    public static net.minecraft.world.level.levelgen.structure.StructureType<?> bukkitToMinecraft(StructureType bukkit) {
        return CraftRegistry.bukkitToMinecraft(bukkit);
    }

    private final NamespacedKey key;
    private final net.minecraft.world.level.levelgen.structure.StructureType<?> structureType;

    public CraftStructureType(NamespacedKey key, net.minecraft.world.level.levelgen.structure.StructureType<?> structureType) {
        this.key = key;
        this.structureType = structureType;
    }

    @Override
    public net.minecraft.world.level.levelgen.structure.StructureType<?> getHandle() {
        return structureType;
    }

    @Override
    public NamespacedKey getKey() {
        return getKeyOrThrow();
    }

    @NotNull
    @Override
    public NamespacedKey getKeyOrThrow() {
        Preconditions.checkState(isRegistered(), "Cannot get key of this registry item, because it is not registered. Use #isRegistered() before calling this method.");
        return this.key;
    }

    @Nullable
    @Override
    public NamespacedKey getKeyOrNull() {
        return this.key;
    }

    @Override
    public boolean isRegistered() {
        return this.key != null;
    }
}
