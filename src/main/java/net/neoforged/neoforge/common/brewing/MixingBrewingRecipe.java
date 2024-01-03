/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.brewing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.NeoForgeMod;

public record MixingBrewingRecipe(
        Potion potionIn,
        List<MobEffectInstance> effectsIn,
        Ingredient catalyst,
        Potion potionOut,
        List<MobEffectInstance> effectsOut
) implements IBrewingRecipe {

    @Override
    public RecipeSerializer<MixingBrewingRecipe> getSerializer() {
        return NeoForgeMod.MIXING_BREWING_RECIPE_SERIALIZER.get();
    }

    @Override
    public boolean isInput(ItemStack input) {
        Potion potion = PotionUtils.getPotion(input);
        List<MobEffectInstance> customEffects = PotionUtils.getCustomEffects(input);
        return potion == this.potionIn() && new HashSet<>(customEffects).containsAll(effectsIn());
    }

    @Override
    public boolean isCatalyst(ItemStack catalyst) {
        return this.catalyst().test(catalyst);
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack catalyst) {
        return isInput(input) && isCatalyst(catalyst) ? makeOutput(input) : ItemStack.EMPTY;
    }

    private ItemStack makeOutput(ItemStack input) {
        ItemStack stack = new ItemStack(input.getItem());
        if (this.potionOut() != Potions.EMPTY) {
            PotionUtils.setPotion(stack, this.potionOut());
        }
        if (!this.effectsOut().isEmpty()) {
            List<MobEffectInstance> effects = PotionUtils.getCustomEffects(input);
            effects.removeAll(this.effectsIn());
            effects.addAll(this.effectsOut());
            PotionUtils.setCustomEffects(stack, effects);
        }
        return stack;
    }

    public static class Serializer implements RecipeSerializer<MixingBrewingRecipe> {
        private static final Codec<MobEffectInstance> MOB_EFFECT_INSTANCE_CODEC = ExtraCodecs.converter(NbtOps.INSTANCE).comapFlatMap(tag -> tag instanceof CompoundTag compoundTag ? DataResult.success(MobEffectInstance.load(compoundTag)) : DataResult.error(() -> "Not an object!"), m -> m.save(new CompoundTag()));
        private static final Codec<MixingBrewingRecipe> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                BuiltInRegistries.POTION.byNameCodec().optionalFieldOf("potionIn", Potions.EMPTY).forGetter(MixingBrewingRecipe::potionIn),
                MOB_EFFECT_INSTANCE_CODEC.listOf().optionalFieldOf("effectsIn", List.of()).forGetter(MixingBrewingRecipe::effectsIn),
                Ingredient.CODEC_NONEMPTY.fieldOf("catalyst").forGetter(MixingBrewingRecipe::catalyst),
                BuiltInRegistries.POTION.byNameCodec().optionalFieldOf("potionOut", Potions.EMPTY).forGetter(MixingBrewingRecipe::potionOut),
                MOB_EFFECT_INSTANCE_CODEC.listOf().optionalFieldOf("effectsOut", List.of()).forGetter(MixingBrewingRecipe::effectsOut)).apply(inst, MixingBrewingRecipe::new));

        @Override
        public Codec<MixingBrewingRecipe> codec() {
            return CODEC;
        }

        @Override
        public MixingBrewingRecipe fromNetwork(FriendlyByteBuf buf) {
            return new MixingBrewingRecipe(
                    buf.readById(BuiltInRegistries.POTION),
                    buf.readList(b -> b.readWithCodecTrusted(NbtOps.INSTANCE, MOB_EFFECT_INSTANCE_CODEC)),
                    Ingredient.fromNetwork(buf),
                    buf.readById(BuiltInRegistries.POTION),
                    buf.readList(b -> b.readWithCodecTrusted(NbtOps.INSTANCE, MOB_EFFECT_INSTANCE_CODEC)));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, MixingBrewingRecipe recipe) {
            buf.writeId(BuiltInRegistries.POTION, recipe.potionIn());
            buf.writeCollection(recipe.effectsIn(), (b, eff) -> b.writeWithCodec(NbtOps.INSTANCE, MOB_EFFECT_INSTANCE_CODEC, eff));
            recipe.catalyst().toNetwork(buf);
            buf.writeId(BuiltInRegistries.POTION, recipe.potionOut());
            buf.writeCollection(recipe.effectsOut(), (b, eff) -> b.writeWithCodec(NbtOps.INSTANCE, MOB_EFFECT_INSTANCE_CODEC, eff));
        }
    }
}
