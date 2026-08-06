package org.bukkit.craftbukkit.inventory.components;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.item.component.KineticWeapon;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.craftbukkit.CraftSound;
import org.bukkit.craftbukkit.inventory.SerializableMeta;
import org.bukkit.inventory.meta.components.KineticWeaponComponent;

@SerializableAs("KineticWeapon")
public final class CraftKineticWeaponComponent implements KineticWeaponComponent {

    private KineticWeapon handle;

    public CraftKineticWeaponComponent(KineticWeapon handle) {
        this.handle = handle;
    }

    public CraftKineticWeaponComponent(CraftKineticWeaponComponent craft) {
        this.handle = craft.handle;
    }

    public CraftKineticWeaponComponent(Map<String, Object> map) {
        Integer contactCooldownTicks = SerializableMeta.getObject(Integer.class, map, "contact-cooldown-ticks", true);
        Integer delayTicks = SerializableMeta.getObject(Integer.class, map, "delay-ticks", true);

        Condition dismountConditions = SerializableMeta.getObject(Condition.class, map, "dismount-conditions", true);
        Condition knockbackConditions = SerializableMeta.getObject(Condition.class, map, "knockback-conditions", true);
        Condition damageConditions = SerializableMeta.getObject(Condition.class, map, "damage-conditions", true);

        Float forwardMovement = SerializableMeta.getObject(Float.class, map, "forward-movement", true);
        Float damageMultiplier = SerializableMeta.getObject(Float.class, map, "damage-multiplier", true);

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

        handle = new KineticWeapon(
                (contactCooldownTicks != null) ? contactCooldownTicks : 10,
                (delayTicks != null) ? delayTicks : 0,
                Optional.ofNullable(dismountConditions).map(CraftKineticWeaponCondition::new).map(CraftKineticWeaponCondition::getHandle),
                Optional.ofNullable(knockbackConditions).map(CraftKineticWeaponCondition::new).map(CraftKineticWeaponCondition::getHandle),
                Optional.ofNullable(damageConditions).map(CraftKineticWeaponCondition::new).map(CraftKineticWeaponCondition::getHandle),
                (forwardMovement != null) ? forwardMovement : 0.0F,
                (damageMultiplier != null) ? damageMultiplier : 1.0F,
                Optional.ofNullable(sound).map(CraftSound::bukkitToMinecraftHolder),
                Optional.ofNullable(hitSound).map(CraftSound::bukkitToMinecraftHolder)
        );
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("contact-cooldown-ticks", getContactCooldownTicks());
        result.put("delay-ticks", getDelayTicks());

        Condition dismountConditions = getDismountConditions();
        if (dismountConditions != null) {
            result.put("dismount-conditions", dismountConditions);
        }

        Condition knockbackConditions = getKnockbackConditions();
        if (knockbackConditions != null) {
            result.put("knockback-conditions", null);
        }

        Condition damageConditions = getDamageConditions();
        if (damageConditions != null) {
            result.put("damage-conditions", null);
        }

        result.put("forward-movement", getForwardMovement());
        result.put("damage-multiplier", getDamageMultiplier());

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

    public KineticWeapon getHandle() {
        return handle;
    }

    @Override
    public int getContactCooldownTicks() {
        return handle.contactCooldownTicks();
    }

    @Override
    public void setContactCooldownTicks(int ticks) {
        handle = new KineticWeapon(ticks, handle.delayTicks(), handle.dismountConditions(), handle.knockbackConditions(), handle.damageConditions(), handle.forwardMovement(), handle.damageMultiplier(), handle.sound(), handle.hitSound());
    }

    @Override
    public int getDelayTicks() {
        return handle.delayTicks();
    }

    @Override
    public void setDelayTicks(int ticks) {
        handle = new KineticWeapon(handle.contactCooldownTicks(), ticks, handle.dismountConditions(), handle.knockbackConditions(), handle.damageConditions(), handle.forwardMovement(), handle.damageMultiplier(), handle.sound(), handle.hitSound());
    }

    @Override
    public Condition getDismountConditions() {
        return handle.dismountConditions().map(CraftKineticWeaponCondition::new).orElse(null);
    }

    @Override
    public void setDismountConditions(Condition condition) {
        handle = new KineticWeapon(handle.contactCooldownTicks(), handle.delayTicks(), Optional.ofNullable(condition).map(CraftKineticWeaponCondition::new).map(CraftKineticWeaponCondition::getHandle), handle.knockbackConditions(), handle.damageConditions(), handle.forwardMovement(), handle.damageMultiplier(), handle.sound(), handle.hitSound());
    }

    @Override
    public Condition getKnockbackConditions() {
        return handle.knockbackConditions().map(CraftKineticWeaponCondition::new).orElse(null);
    }

    @Override
    public void setKnockbackConditions(Condition condition) {
        handle = new KineticWeapon(handle.contactCooldownTicks(), handle.delayTicks(), handle.dismountConditions(), Optional.ofNullable(condition).map(CraftKineticWeaponCondition::new).map(CraftKineticWeaponCondition::getHandle), handle.damageConditions(), handle.forwardMovement(), handle.damageMultiplier(), handle.sound(), handle.hitSound());
    }

    @Override
    public Condition getDamageConditions() {
        return handle.damageConditions().map(CraftKineticWeaponCondition::new).orElse(null);
    }

    @Override
    public void setDamageConditions(Condition condition) {
        handle = new KineticWeapon(handle.contactCooldownTicks(), handle.delayTicks(), handle.dismountConditions(), handle.knockbackConditions(), Optional.ofNullable(condition).map(CraftKineticWeaponCondition::new).map(CraftKineticWeaponCondition::getHandle), handle.forwardMovement(), handle.damageMultiplier(), handle.sound(), handle.hitSound());
    }

    @Override
    public float getForwardMovement() {
        return handle.forwardMovement();
    }

    @Override
    public void setForwardMovement(float movement) {
        handle = new KineticWeapon(handle.contactCooldownTicks(), handle.delayTicks(), handle.dismountConditions(), handle.knockbackConditions(), handle.damageConditions(), movement, handle.damageMultiplier(), handle.sound(), handle.hitSound());
    }

    @Override
    public float getDamageMultiplier() {
        return handle.damageMultiplier();
    }

    @Override
    public void setDamageMultipler(float multiplier) {
        handle = new KineticWeapon(handle.contactCooldownTicks(), handle.delayTicks(), handle.dismountConditions(), handle.knockbackConditions(), handle.damageConditions(), handle.forwardMovement(), multiplier, handle.sound(), handle.hitSound());
    }

    @Override
    public Sound getSound() {
        return handle.sound().map(CraftSound::minecraftHolderToBukkit).orElse(null);
    }

    @Override
    public void setSound(Sound sound) {
        handle = new KineticWeapon(handle.contactCooldownTicks(), handle.delayTicks(), handle.dismountConditions(), handle.knockbackConditions(), handle.damageConditions(), handle.forwardMovement(), handle.damageMultiplier(), Optional.ofNullable(sound).map(CraftSound::bukkitToMinecraftHolder), handle.hitSound());
    }

    @Override
    public Sound getHitSound() {
        return handle.hitSound().map(CraftSound::minecraftHolderToBukkit).orElse(null);
    }

    @Override
    public void setHitSound(Sound sound) {
        handle = new KineticWeapon(handle.contactCooldownTicks(), handle.delayTicks(), handle.dismountConditions(), handle.knockbackConditions(), handle.damageConditions(), handle.forwardMovement(), handle.damageMultiplier(), handle.sound(), Optional.ofNullable(sound).map(CraftSound::bukkitToMinecraftHolder));
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
        final CraftKineticWeaponComponent other = (CraftKineticWeaponComponent) obj;
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
        return "CraftKineticWeaponComponent{" + "handle=" + handle + '}';
    }

    @SerializableAs("KineticWeaponCondition")
    public static class CraftKineticWeaponCondition implements Condition {

        private KineticWeapon.Condition handle;

        public CraftKineticWeaponCondition(KineticWeapon.Condition handle) {
            this.handle = handle;
        }

        public CraftKineticWeaponCondition(Condition bukkit) {
            KineticWeapon.Condition toCopy = ((CraftKineticWeaponCondition) bukkit).handle;
            this.handle = new KineticWeapon.Condition(toCopy.maxDurationTicks(), toCopy.minSpeed(), toCopy.minRelativeSpeed());
        }

        public CraftKineticWeaponCondition(Map<String, Object> map) {
            Integer maxDurationTicks = SerializableMeta.getObject(Integer.class, map, "max-duration-ticks", false);
            Integer minSpeed = SerializableMeta.getObject(Integer.class, map, "min-speed", true);
            Integer minRelativeSpeed = SerializableMeta.getObject(Integer.class, map, "min-relative-speed", true);

            handle = new KineticWeapon.Condition(
                    maxDurationTicks,
                    (minSpeed != null) ? minSpeed : 0.0F,
                    (minRelativeSpeed != null) ? minRelativeSpeed : 0.0F
            );
        }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> result = new LinkedHashMap<>();

            result.put("max-duration-ticks", getMaxDurationTicks());
            result.put("min-speed", getMinSpeed());
            result.put("min-relative-speed", getMinRelativeSpeed());

            return result;
        }

        public KineticWeapon.Condition getHandle() {
            return handle;
        }

        @Override
        public int getMaxDurationTicks() {
            return handle.maxDurationTicks();
        }

        @Override
        public void setMaxDurationTicks(int ticks) {
            handle = new KineticWeapon.Condition(ticks, handle.minSpeed(), handle.minRelativeSpeed());
        }

        @Override
        public float getMinSpeed() {
            return handle.minSpeed();
        }

        @Override
        public void setMinSpeed(float speed) {
            handle = new KineticWeapon.Condition(handle.maxDurationTicks(), speed, handle.minRelativeSpeed());
        }

        @Override
        public float getMinRelativeSpeed() {
            return handle.minRelativeSpeed();
        }

        @Override
        public void setMinRelativeSpeed(float speed) {
            handle = new KineticWeapon.Condition(handle.maxDurationTicks(), handle.minSpeed(), speed);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + Objects.hashCode(this.handle);
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
            final CraftKineticWeaponCondition other = (CraftKineticWeaponCondition) obj;
            return Objects.equals(this.handle, other.handle);
        }

        @Override
        public String toString() {
            return "CraftKineticWeaponCondition{" + "handle=" + handle + '}';
        }
    }
}
