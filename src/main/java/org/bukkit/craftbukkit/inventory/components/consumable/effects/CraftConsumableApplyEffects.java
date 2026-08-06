package org.bukkit.craftbukkit.inventory.components.consumable.effects;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.craftbukkit.inventory.ItemMetaKey;
import org.bukkit.craftbukkit.inventory.SerializableMeta;
import org.bukkit.craftbukkit.potion.CraftPotionUtil;
import org.bukkit.inventory.meta.components.consumable.effects.ConsumableApplyEffects;
import org.bukkit.potion.PotionEffect;

@SerializableAs("ConsumableApplyEffects")
public class CraftConsumableApplyEffects extends CraftConsumableEffect<ApplyStatusEffectsConsumeEffect> implements ConsumableApplyEffects {

    static final ItemMetaKey POTIONS = new ItemMetaKey("effects");

    public CraftConsumableApplyEffects(ApplyStatusEffectsConsumeEffect consumeEffect) {
        super(consumeEffect);
    }

    public CraftConsumableApplyEffects(CraftConsumableApplyEffects consumeEffect) {
        super(consumeEffect);
    }

    public CraftConsumableApplyEffects(Map<String, Object> map) {
        super(map);

        List<PotionEffect> effectList = new ArrayList<>();
        Iterable<?> rawEffectTypeList = SerializableMeta.getObject(Iterable.class, map, POTIONS, true);
        if (rawEffectTypeList == null) {
            return;
        }

        for (Object obj : rawEffectTypeList) {
            Preconditions.checkArgument(obj instanceof PotionEffect, "Object (%s) in effect list is not valid", obj.getClass());
            effectList.add((PotionEffect) obj);
        }

        Float probability = SerializableMeta.getObject(Float.class, map, "probability", false);

        this.handle = new ApplyStatusEffectsConsumeEffect(effectList.stream().map(CraftPotionUtil::fromBukkit).toList(), probability);
    }

    @Override
    public List<PotionEffect> getEffects() {
        List<MobEffectInstance> mobEffectList = this.getHandle().effects();
        return mobEffectList.stream().map(CraftPotionUtil::toBukkit).toList();
    }

    @Override
    public void setEffects(List<PotionEffect> list) {
        this.handle = new ApplyStatusEffectsConsumeEffect(list.stream().map(CraftPotionUtil::fromBukkit).toList());
    }

    @Override
    public PotionEffect addEffect(PotionEffect potionEffect) {
        List<MobEffectInstance> mobEffectList = this.getHandle().effects();
        mobEffectList.add(CraftPotionUtil.fromBukkit(potionEffect));
        this.handle = new ApplyStatusEffectsConsumeEffect(mobEffectList, this.handle.probability());
        return potionEffect;
    }

    @Override
    public float getProbability() {
        return this.getHandle().probability();
    }

    @Override
    public void setProbability(float probability) {
        Preconditions.checkArgument(probability >= 0.0f && probability <= 1.0f, "Probability must be between 0.0f and 1.0f but is %s", probability);
        this.handle = new ApplyStatusEffectsConsumeEffect(this.getHandle().effects(), probability);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(POTIONS.BUKKIT, ImmutableList.copyOf(this.getEffects()));

        return result;
    }
}
