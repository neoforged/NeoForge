package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.registry.CraftOldEnumRegistryItem;
import org.jetbrains.annotations.NotNull;

public class CraftSound extends CraftOldEnumRegistryItem<Sound, SoundEvent> implements Sound {

    private static int count = 0;

    public static Sound minecraftToBukkit(SoundEvent minecraft) {
        return CraftRegistry.minecraftToBukkit(minecraft, Registries.SOUND_EVENT, Registry.SOUNDS);
    }

    public static Sound minecraftHolderToBukkit(Holder<SoundEvent> minecraft) {
        return minecraftToBukkit(minecraft.value());
    }

    public static SoundEvent bukkitToMinecraft(Sound bukkit) {
        return CraftRegistry.bukkitToMinecraft(bukkit);
    }

    public static Holder<SoundEvent> bukkitToMinecraftHolder(Sound bukkit) {
        Preconditions.checkArgument(bukkit != null);

        net.minecraft.core.Registry<SoundEvent> registry = CraftRegistry.getMinecraftRegistry(Registries.SOUND_EVENT);

        if (registry.wrapAsHolder(bukkitToMinecraft(bukkit)) instanceof Holder.Reference<SoundEvent> holder) {
            return holder;
        }

        throw new IllegalArgumentException("No Reference holder found for " + bukkit
                + ", this can happen if a plugin creates its own sound effect with out properly registering it.");
    }

    public CraftSound(NamespacedKey key, Holder<SoundEvent> handle) {
        super(key, handle, count++);
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return getKeyOrThrow();
    }
}
