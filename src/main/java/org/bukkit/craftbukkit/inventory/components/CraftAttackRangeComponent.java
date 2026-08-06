package org.bukkit.craftbukkit.inventory.components;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.world.item.component.AttackRange;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.craftbukkit.inventory.SerializableMeta;
import org.bukkit.inventory.meta.components.AttackRangeComponent;

@SerializableAs("AttackRange")
public final class CraftAttackRangeComponent implements AttackRangeComponent {

    private AttackRange handle;

    public CraftAttackRangeComponent(AttackRange handle) {
        this.handle = handle;
    }

    public CraftAttackRangeComponent(CraftAttackRangeComponent craft) {
        this.handle = craft.handle;
    }

    public CraftAttackRangeComponent(Map<String, Object> map) {
        Float minReach = SerializableMeta.getObject(Float.class, map, "min-reach", true);
        Float maxReach = SerializableMeta.getObject(Float.class, map, "max-reach", true);
        Float minCreativeReach = SerializableMeta.getObject(Float.class, map, "min-creative-reach", true);
        Float maxCreativeReach = SerializableMeta.getObject(Float.class, map, "max-creative-reach", true);
        Float hitboxMargin = SerializableMeta.getObject(Float.class, map, "hitbox-margin", true);
        Float mobFactor = SerializableMeta.getObject(Float.class, map, "mob-factor", true);

        handle = new AttackRange(
                (minReach != null) ? minReach : 0.0F,
                (maxReach != null) ? maxReach : 3.0F,
                (minCreativeReach != null) ? minCreativeReach : 0.0F,
                (maxCreativeReach != null) ? maxCreativeReach : 3.0F,
                (hitboxMargin != null) ? hitboxMargin : 0.3F,
                (mobFactor != null) ? mobFactor : 1.0F
        );
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("min-reach", getMinReach());
        result.put("max-reach", getMaxReach());
        result.put("min-creative-reach", getMinCreativeReach());
        result.put("max-creative-reach", getMaxCreativeReach());
        result.put("hitbox-margin", getHitboxMargin());
        result.put("mob-factor", getMobFactor());

        return result;
    }

    public AttackRange getHandle() {
        return handle;
    }

    @Override
    public float getMinReach() {
        return handle.minReach();
    }

    @Override
    public void setMinReach(float reach) {
        handle = new AttackRange(reach, handle.maxReach(), handle.minCreativeReach(), handle.maxCreativeReach(), handle.hitboxMargin(), handle.mobFactor());
    }

    @Override
    public float getMaxReach() {
        return handle.maxReach();
    }

    @Override
    public void setMaxReach(float reach) {
        handle = new AttackRange(handle.minReach(), reach, handle.minCreativeReach(), handle.maxCreativeReach(), handle.hitboxMargin(), handle.mobFactor());
    }

    @Override
    public float getMinCreativeReach() {
        return handle.minCreativeReach();
    }

    @Override
    public void setMinCreativeReach(float reach) {
        handle = new AttackRange(handle.minReach(), handle.maxReach(), reach, handle.maxCreativeReach(), handle.hitboxMargin(), handle.mobFactor());
    }

    @Override
    public float getMaxCreativeReach() {
        return handle.maxCreativeReach();
    }

    @Override
    public void setMaxCreativeReach(float reach) {
        handle = new AttackRange(handle.minReach(), handle.maxReach(), handle.minCreativeReach(), reach, handle.hitboxMargin(), handle.mobFactor());
    }

    @Override
    public float getHitboxMargin() {
        return handle.hitboxMargin();
    }

    @Override
    public void setHitboxMargin(float margin) {
        handle = new AttackRange(handle.minReach(), handle.maxReach(), handle.minCreativeReach(), handle.maxCreativeReach(), margin, handle.mobFactor());
    }

    @Override
    public float getMobFactor() {
        return handle.mobFactor();
    }

    @Override
    public void setMobFactor(float factor) {
        handle = new AttackRange(handle.minReach(), handle.maxReach(), handle.minCreativeReach(), handle.maxCreativeReach(), handle.hitboxMargin(), handle.mobFactor());
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
        final CraftAttackRangeComponent other = (CraftAttackRangeComponent) obj;
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
        return "CraftAttackRangeComponent{" + "handle=" + handle + '}';
    }
}
