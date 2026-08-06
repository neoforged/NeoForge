package org.bukkit.craftbukkit.inventory.components.consumable.effects;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.craftbukkit.inventory.ItemMetaKey;
import org.bukkit.craftbukkit.inventory.SerializableMeta;
import org.bukkit.craftbukkit.potion.CraftPotionEffectType;
import org.bukkit.inventory.meta.components.consumable.effects.ConsumableRemoveEffect;
import org.bukkit.potion.PotionEffectType;

@SerializableAs("ConsumableRemoveEffect")
public class CraftConsumableRemoveEffect extends CraftConsumableEffect<RemoveStatusEffectsConsumeEffect> implements ConsumableRemoveEffect {

    static final ItemMetaKey POTION_TYPES = new ItemMetaKey("effects");

    public CraftConsumableRemoveEffect(RemoveStatusEffectsConsumeEffect consumeEffect) {
        super(consumeEffect);
    }

    public CraftConsumableRemoveEffect(CraftConsumableRemoveEffect consumeEffect) {
        super(consumeEffect);
    }

    public CraftConsumableRemoveEffect(Map<String, Object> map) {
        super(map);

        List<PotionEffectType> effectTypeList = new ArrayList<>();
        Iterable<?> rawEffectTypeList = SerializableMeta.getObject(Iterable.class, map, POTION_TYPES.BUKKIT, true);
        if (rawEffectTypeList == null) {
            return;
        }

        for (Object obj : rawEffectTypeList) {
            Preconditions.checkArgument(obj instanceof PotionEffectType, "Object (%s) in effect type list is not valid", obj.getClass());
            effectTypeList.add((PotionEffectType) obj);
        }

        super.handle = new RemoveStatusEffectsConsumeEffect(HolderSet.direct(effectTypeList.stream().map(CraftPotionEffectType::bukkitToMinecraftHolder).collect(Collectors.toList())));
    }

    public RemoveStatusEffectsConsumeEffect getHandle() {
        return (RemoveStatusEffectsConsumeEffect) super.getHandle();
    }

    @Override
    public List<PotionEffectType> getEffectTypes() {
        return this.getHandle().effects().stream().map(CraftPotionEffectType::minecraftHolderToBukkit).collect(Collectors.toList());
    }

    @Override
    public void setEffectTypes(List<PotionEffectType> effectTypeList) {
        this.handle = new RemoveStatusEffectsConsumeEffect(HolderSet.direct(effectTypeList.stream().map(CraftPotionEffectType::bukkitToMinecraftHolder).collect(Collectors.toList())));
    }

    @Override
    public PotionEffectType addEffectType(PotionEffectType potionEffect) {
        List<PotionEffectType> list = this.getEffectTypes();
        list.add(potionEffect);
        this.setEffectTypes(list);
        return potionEffect;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(POTION_TYPES.BUKKIT, ImmutableList.copyOf(this.getEffectTypes()));

        return result;
    }
}
