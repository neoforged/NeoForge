package org.bukkit.craftbukkit.inventory.components;

import com.google.common.base.Preconditions;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.world.item.component.Weapon;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.craftbukkit.inventory.SerializableMeta;
import org.bukkit.inventory.meta.components.WeaponComponent;

@SerializableAs("Weapon")
public final class CraftWeaponComponent implements WeaponComponent {

    private Weapon handle;

    public CraftWeaponComponent(Weapon weapon) {
        this.handle = weapon;
    }

    public CraftWeaponComponent(CraftWeaponComponent tool) {
        this.handle = tool.handle;
    }

    public CraftWeaponComponent(Map<String, Object> map) {
        Integer itemDamagePerAttack = SerializableMeta.getObject(Integer.class, map, "item-damage-per-attack", false);
        Float disableBlockingForSeconds = SerializableMeta.getObject(Float.class, map, "disable-blocking-for-seconds", true);

        this.handle = new Weapon(itemDamagePerAttack, (disableBlockingForSeconds != null) ? disableBlockingForSeconds : 0.0F);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("item-damage-per-attack", getItemDamagePerAttack());
        if (getDisableBlockingForSeconds() != 0.0F) {
            result.put("disable-blocking-for-seconds", getDisableBlockingForSeconds());
        }
        return result;
    }

    public Weapon getHandle() {
        return handle;
    }

    @Override
    public int getItemDamagePerAttack() {
        return getHandle().itemDamagePerAttack();
    }

    @Override
    public void setItemDamagePerAttack(int damage) {
        Preconditions.checkArgument(damage >= 0, "damage must be >= 0");

        this.handle = new Weapon(damage, handle.disableBlockingForSeconds());
    }

    @Override
    public float getDisableBlockingForSeconds() {
        return getHandle().disableBlockingForSeconds();
    }

    @Override
    public void setDisableBlockingForSeconds(float time) {
        Preconditions.checkArgument(time >= 0, "time must be >= 0");

        this.handle = new Weapon(handle.itemDamagePerAttack(), time);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.handle);
        return hash;
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
        final CraftWeaponComponent other = (CraftWeaponComponent) obj;
        return Objects.equals(this.handle, other.handle);
    }

    @Override
    public String toString() {
        return "CraftWeaponComponent{" + "handle=" + handle + '}';
    }
}
