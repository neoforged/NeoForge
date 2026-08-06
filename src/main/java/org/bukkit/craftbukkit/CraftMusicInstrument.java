package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Instrument;
import org.bukkit.MusicInstrument;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.util.HolderHandleable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CraftMusicInstrument extends MusicInstrument implements HolderHandleable<Instrument> {

    public static MusicInstrument minecraftToBukkit(Instrument minecraft) {
        return CraftRegistry.minecraftToBukkit(minecraft, Registries.INSTRUMENT, Registry.INSTRUMENT);
    }

    public static MusicInstrument minecraftHolderToBukkit(Holder<Instrument> minecraft) {
        Preconditions.checkArgument(minecraft != null);

        if (minecraft instanceof Holder.Reference<Instrument> holder) {
            MusicInstrument bukkit = Registry.INSTRUMENT.get(CraftNamespacedKey.fromMinecraft(holder.key().identifier()));

            Preconditions.checkArgument(bukkit != null);

            return bukkit;
        }

        return new CraftMusicInstrument(null, minecraft);
    }

    public static Instrument bukkitToMinecraft(MusicInstrument bukkit) {
        return CraftRegistry.bukkitToMinecraft(bukkit);
    }

    public static Holder<Instrument> bukkitToMinecraftHolder(MusicInstrument bukkit) {
        return ((HolderHandleable<Instrument>) bukkit).getHandleHolder();
    }

    public static String bukkitToString(MusicInstrument bukkit) {
        Preconditions.checkArgument(bukkit != null);

        Holder<Instrument> holder = bukkitToMinecraftHolder(bukkit);

        return Instrument.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, CraftRegistry.getMinecraftRegistry()), holder).result().get().toString();
    }

    public static MusicInstrument stringToBukkit(String string) {
        Preconditions.checkArgument(string != null);

        NamespacedKey key = NamespacedKey.fromString(string);
        if (key != null) {
            MusicInstrument bukkit = Registry.INSTRUMENT.get(key);

            if (bukkit != null) {
                return bukkit;
            }
        }

        JsonElement json = JsonParser.parseString(string);
        DataResult<Pair<Holder<Instrument>, JsonElement>> result = Instrument.CODEC.decode(RegistryOps.create(JsonOps.INSTANCE, CraftRegistry.getMinecraftRegistry()), json);

        return CraftMusicInstrument.minecraftHolderToBukkit(result.getOrThrow().getFirst());
    }

    private final NamespacedKey key;
    private final Holder<Instrument> handle;

    public CraftMusicInstrument(NamespacedKey key, Holder<Instrument> handle) {
        this.key = key;
        this.handle = handle;
    }

    @Override
    public Instrument getHandle() {
        return handle.value();
    }

    @Override
    public Holder<Instrument> getHandleHolder() {
        return handle;
    }

    @Override
    public String getDescription() {
        return CraftChatMessage.fromComponent(this.getHandle().description());
    }

    @Override
    public float getDuration() {
        return this.getHandle().useDuration();
    }

    @Override
    public float getRange() {
        return this.getHandle().range();
    }

    @Override
    public Sound getSoundEvent() {
        return CraftSound.minecraftHolderToBukkit(this.getHandle().soundEvent());
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return getKeyOrThrow();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof CraftMusicInstrument other)) {
            return false;
        }

        if (this.key != null && other.key != null) {
            return this.key.equals(other.key);
        }

        return getHandle().equals(other.getHandle());
    }

    @Override
    public int hashCode() {
        if (key != null) {
            return key.hashCode();
        }

        return getHandle().hashCode();
    }

    @Override
    public String toString() {
        return "CraftMusicInstrument{key=" + key + ", handle=" + handle + "}";
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
