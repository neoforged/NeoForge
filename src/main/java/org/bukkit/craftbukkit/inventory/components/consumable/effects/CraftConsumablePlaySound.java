package org.bukkit.craftbukkit.inventory.components.consumable.effects;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.item.consume_effects.PlaySoundConsumeEffect;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.craftbukkit.CraftSound;
import org.bukkit.inventory.meta.components.consumable.effects.ConsumablePlaySound;

@SerializableAs("ConsumablePlaySound")
public class CraftConsumablePlaySound extends CraftConsumableEffect<PlaySoundConsumeEffect> implements ConsumablePlaySound {

    public CraftConsumablePlaySound(PlaySoundConsumeEffect consumeEffect) {
        super(consumeEffect);
    }

    public CraftConsumablePlaySound(CraftConsumableEffect<PlaySoundConsumeEffect> consumeEffect) {
        super(consumeEffect);
    }

    public CraftConsumablePlaySound(Map<String, Object> map) {
        super(map);
    }

    @Override
    public Sound getSound() {
        return CraftSound.minecraftHolderToBukkit(this.getHandle().sound());
    }

    @Override
    public void setSound(Sound sound) {
        this.handle = new PlaySoundConsumeEffect(CraftSound.bukkitToMinecraftHolder(sound));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sound", getSound());

        return result;
    }
}
