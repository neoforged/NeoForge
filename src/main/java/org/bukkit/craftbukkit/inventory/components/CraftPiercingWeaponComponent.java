package org.bukkit.craftbukkit.inventory.components;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.item.component.PiercingWeapon;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.craftbukkit.CraftSound;
import org.bukkit.craftbukkit.inventory.SerializableMeta;
import org.bukkit.inventory.meta.components.PiercingWeaponComponent;

@SerializableAs("PiercingWeapon")
public final class CraftPiercingWeaponComponent implements PiercingWeaponComponent {

    private PiercingWeapon handle;

    public CraftPiercingWeaponComponent(PiercingWeapon handle) {
        this.handle = handle;
    }

    public CraftPiercingWeaponComponent(CraftPiercingWeaponComponent craft) {
        this.handle = craft.handle;
    }

    public CraftPiercingWeaponComponent(Map<String, Object> map) {
        Boolean dealsKnockback = SerializableMeta.getObject(Boolean.class, map, "deals-knockback", true);
        Boolean dismounts = SerializableMeta.getObject(Boolean.class, map, "dismounts", true);

        Sound sound = null;
        String snd = SerializableMeta.getString(map, "sound", true);
        if (snd != null) {
            sound = Registry.SOUNDS.get(NamespacedKey.fromString(snd));
        }

        Sound hitSound = null;
        String hitSnd = SerializableMeta.getString(map, "hit-sound", true);
        if (hitSnd != null) {
            hitSound = Registry.SOUNDS.get(NamespacedKey.fromString(hitSnd));
        }

        handle = new PiercingWeapon(
                (dealsKnockback != null) ? dealsKnockback : true,
                (dismounts != null) ? dismounts : false,
                Optional.ofNullable(sound).map(CraftSound::bukkitToMinecraftHolder),
                Optional.ofNullable(hitSound).map(CraftSound::bukkitToMinecraftHolder)
        );
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deals-knockback", isDealsKnockback());
        result.put("dismounts", isDismounts());

        Sound sound = getSound();
        if (sound != null) {
            result.put("sound", sound.getKey().toString());
        }

        Sound hitSound = getHitSound();
        if (hitSound != null) {
            result.put("hit-sound", hitSound.getKey().toString());
        }

        return result;
    }

    public PiercingWeapon getHandle() {
        return handle;
    }

    @Override
    public boolean isDealsKnockback() {
        return handle.dealsKnockback();
    }

    @Override
    public void setDealsKnockback(boolean knockback) {
        handle = new PiercingWeapon(knockback, handle.dismounts(), handle.sound(), handle.hitSound());
    }

    @Override
    public boolean isDismounts() {
        return handle.dismounts();
    }

    @Override
    public void setDismounts(boolean dismounts) {
        handle = new PiercingWeapon(handle.dealsKnockback(), dismounts, handle.sound(), handle.hitSound());
    }

    @Override
    public Sound getSound() {
        return handle.sound().map(CraftSound::minecraftHolderToBukkit).orElse(null);
    }

    @Override
    public void setSound(Sound sound) {
        handle = new PiercingWeapon(handle.dealsKnockback(), handle.dismounts(), Optional.ofNullable(sound).map(CraftSound::bukkitToMinecraftHolder), handle.hitSound());
    }

    @Override
    public Sound getHitSound() {
        return handle.hitSound().map(CraftSound::minecraftHolderToBukkit).orElse(null);
    }

    @Override
    public void setHitSound(Sound sound) {
        handle = new PiercingWeapon(handle.dealsKnockback(), handle.dismounts(), handle.sound(), Optional.ofNullable(sound).map(CraftSound::bukkitToMinecraftHolder));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CraftPiercingWeaponComponent other = (CraftPiercingWeaponComponent) obj;
        return Objects.equals(this.handle, other.handle);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.handle);
        return hash;
    }

    @Override
    public String toString() {
        return "CraftPiercingWeaponComponent{" + "handle=" + handle + '}';
    }
}
