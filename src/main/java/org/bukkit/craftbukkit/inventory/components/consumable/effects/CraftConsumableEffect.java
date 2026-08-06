package org.bukkit.craftbukkit.inventory.components.consumable.effects;

import java.util.Map;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.consume_effects.PlaySoundConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.TeleportRandomlyConsumeEffect;
import org.bukkit.inventory.meta.components.consumable.effects.ConsumableEffect;

public abstract class CraftConsumableEffect<T extends ConsumeEffect> implements ConsumableEffect {

    public static <T extends CraftConsumableEffect<?>> T minecraftToBukkitSpecific(ConsumeEffect effect) {
        if (effect instanceof ApplyStatusEffectsConsumeEffect nmsEffect) {
            return ((T) new CraftConsumableApplyEffects(nmsEffect));
        } else if (effect instanceof RemoveStatusEffectsConsumeEffect nmsEffect) {
            return ((T) new CraftConsumableRemoveEffect(nmsEffect));
        } else if (effect instanceof ClearAllStatusEffectsConsumeEffect nmsEffect) {
            return ((T) new CraftConsumableClearEffects(nmsEffect));
        } else if (effect instanceof TeleportRandomlyConsumeEffect nmsEffect) {
            return ((T) new CraftConsumableTeleportRandomly(nmsEffect));
        } else if (effect instanceof PlaySoundConsumeEffect nmsEffect) {
            return ((T) new CraftConsumablePlaySound(nmsEffect));
        }
        throw new IllegalStateException("Unexpected value: " + effect.getType());
    }

    public static <T extends ConsumeEffect> T bukkitToMinecraftSpecific(CraftConsumableEffect<T> effect) {
        return effect.getHandle();
    }

    T handle;

    public CraftConsumableEffect(T consumeEffect) {
        this.handle = consumeEffect;
    }

    public CraftConsumableEffect(CraftConsumableEffect<T> consumeEffect) {
        this.handle = consumeEffect.handle;
    }

    public CraftConsumableEffect(Map<String, Object> map) {
    }

    public T getHandle() {
        return handle;
    }
}
