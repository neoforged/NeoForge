package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import net.minecraft.core.registries.Registries;
import org.bukkit.GameEvent;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.util.Handleable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CraftGameRule<T> implements Handleable<net.minecraft.world.level.gamerules.GameRule<T>>, GameRule<T> {

    public static <T> GameRule<T> minecraftToBukkit(net.minecraft.world.level.gamerules.GameRule<T> minecraft) {
        return CraftRegistry.minecraftToBukkit(minecraft, Registries.GAME_RULE, Registry.GAME_RULE);
    }

    public static <T> net.minecraft.world.level.gamerules.GameRule<T> bukkitToMinecraft(GameRule<T> bukkit) {
        return CraftRegistry.bukkitToMinecraft(bukkit);
    }

    private final NamespacedKey key;
    private final net.minecraft.world.level.gamerules.GameRule<T> handle;

    public CraftGameRule(NamespacedKey key, net.minecraft.world.level.gamerules.GameRule<T> handle) {
        this.key = key;
        this.handle = handle;
    }

    @Override
    public net.minecraft.world.level.gamerules.GameRule<T> getHandle() {
        return handle;
    }

    @NotNull
    @Override
    public T getDefaultValue() {
        return this.handle.defaultValue();
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return getKeyOrThrow();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof CraftGameEvent)) {
            return false;
        }

        return getKey().equals(((GameEvent) other).getKey());
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

    @Override
    public String toString() {
        return "CraftGameRule{key=" + key + "}";
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

    @Override
    public String getName() {
        return getKeyOrThrow().getKey();
    }

    @Override
    public Class<T> getType() {
        return getHandle().valueClass();
    }
}
