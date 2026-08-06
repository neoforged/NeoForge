package org.bukkit.craftbukkit.inventory.components;

import com.google.common.base.Preconditions;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.world.item.component.UseEffects;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.craftbukkit.inventory.SerializableMeta;
import org.bukkit.inventory.meta.components.UseEffectsComponent;

@SerializableAs("UseEffects")
public final class CraftUseEffectsComponent implements UseEffectsComponent {

    private UseEffects handle;

    public CraftUseEffectsComponent(UseEffects handle) {
        this.handle = handle;
    }

    public CraftUseEffectsComponent(CraftUseEffectsComponent component) {
        this.handle = component.handle;
    }

    public CraftUseEffectsComponent(Map<String, Object> map) {
        boolean canSprint = SerializableMeta.getBoolean(map, "can-sprint");
        boolean interactVibrations = SerializableMeta.getBoolean(map, "interact-vibrations");
        Float speedMultiplier = SerializableMeta.getObject(Float.class, map, "speed-multiplier", false);

        this.handle = new UseEffects(canSprint, interactVibrations, speedMultiplier);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("can-sprint", canSprint());
        result.put("interact-vibrations", isInteractVibrations());
        result.put("speed-multiplier", getSpeedMultiplier());

        return result;
    }

    public UseEffects getHandle() {
        return handle;
    }

    @Override
    public boolean canSprint() {
        return handle.canSprint();
    }

    @Override
    public void setCanSprint(boolean sprint) {
        handle = new UseEffects(sprint, handle.interactVibrations(), handle.speedMultiplier());
    }

    @Override
    public boolean isInteractVibrations() {
        return handle.interactVibrations();
    }

    @Override
    public void setInteractVibrations(boolean interactVibrations) {
        handle = new UseEffects(handle.canSprint(), interactVibrations, handle.speedMultiplier());
    }

    @Override
    public float getSpeedMultiplier() {
        return handle.speedMultiplier();
    }

    @Override
    public void setSpeedMultiplier(float multiplier) {
        Preconditions.checkArgument(0.0F <= multiplier && multiplier <= 1.0F, "multiplier must be in range [0,1]");

        handle = new UseEffects(handle.canSprint(), handle.interactVibrations(), multiplier);
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
        final CraftUseEffectsComponent other = (CraftUseEffectsComponent) obj;
        return Objects.equals(this.handle, other.handle);
    }

    @Override
    public String toString() {
        return "CraftUseEffectsComponent{" + "handle=" + handle + '}';
    }
}
