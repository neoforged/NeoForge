/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.crafting;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/** Ingredient that matches everything from the first ingredient that is not included in the second ingredient */
public class DifferenceIngredient extends Ingredient
{
    
    public static final Codec<DifferenceIngredient> CODEC = RecordCodecBuilder.create(builder -> builder.group(
          Ingredient.CODEC.fieldOf("base").forGetter(DifferenceIngredient::getBase),
          Ingredient.CODEC.fieldOf("subtracted").forGetter(DifferenceIngredient::getSubtracted)
    ).apply(builder, DifferenceIngredient::new));
    
    
    public static final Codec<DifferenceIngredient> CODEC_NONEMPTY = RecordCodecBuilder.create(builder -> builder.group(
          Ingredient.CODEC_NONEMPTY.fieldOf("base").forGetter(DifferenceIngredient::getBase),
          Ingredient.CODEC_NONEMPTY.fieldOf("subtracted").forGetter(DifferenceIngredient::getSubtracted)
    ).apply(builder, DifferenceIngredient::new));
    
    private final Ingredient base;
    private final Ingredient subtracted;
    
    protected DifferenceIngredient(Ingredient base, Ingredient subtracted)
    {
        super(Arrays.stream(base.getValues()).map(value -> new SubtractingValue(value, subtracted)), ForgeMod.DIFFERENCE_INGREDIENT_TYPE::get);
        
        this.base = base;
        this.subtracted = subtracted;
    }
    
    public Ingredient getBase() {
        return base;
    }
    
    public Ingredient getSubtracted() {
        return subtracted;
    }
    
    @Override
    public boolean isSimple() {
        return false;
    }
    
    @Override
    public ItemStack[] getItems() {
        if (synchronizeWithContents())
            return super.getItems();
            
        final var list = Lists.newArrayList(base.getItems());
        for (ItemStack item : subtracted.getItems()) {
            list.removeIf(i -> areStacksEqual(i, item));
        }
        return list.toArray(ItemStack[]::new);
    }
    
    @Override
    public boolean test(@Nullable ItemStack p_43914_) {
        if (synchronizeWithContents())
            return super.test(p_43914_);
        
        return base.test(p_43914_) && !subtracted.test(p_43914_);
    }
    
    @Override
    public IntList getStackingIds() {
        return super.getStackingIds();
    }
    
    @Override
    public boolean synchronizeWithContents() {
        return base.synchronizeWithContents() && subtracted.synchronizeWithContents();
    }

    /**
     * Gets the difference from the two ingredients
     * @param base        Ingredient the item must match
     * @param subtracted  Ingredient the item must not match
     * @return  Ingredient that {@code base} anything in base that is not in {@code subtracted}
     */
    public static DifferenceIngredient of(Ingredient base, Ingredient subtracted)
    {
        return new DifferenceIngredient(base, subtracted);
    }
    
    private record SubtractingValue(Value inner, Ingredient subtracted) implements Ingredient.Value {
        
        @Override
        public Collection<ItemStack> getItems() {
            final Collection<ItemStack> innerItems = new ArrayList<>(inner().getItems());
            innerItems.removeIf(subtracted);
            return innerItems;
        }
    }
}
