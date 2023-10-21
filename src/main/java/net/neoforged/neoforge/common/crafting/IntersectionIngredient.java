/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/** Ingredient that matches if all child ingredients match */
public class IntersectionIngredient extends Ingredient
{
    public static final Codec<IntersectionIngredient> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Ingredient.LIST_CODEC.fieldOf("children").forGetter(IntersectionIngredient::getChildren)
    ).apply(builder, IntersectionIngredient::new));

    public static final Codec<IntersectionIngredient> CODEC_NONEMPTY = RecordCodecBuilder.create(builder -> builder.group(
            Ingredient.LIST_CODEC_NONEMPTY.fieldOf("children").forGetter(IntersectionIngredient::getChildren)
    ).apply(builder, IntersectionIngredient::new));

    private final List<Ingredient> children;

    protected IntersectionIngredient(List<Ingredient> children)
    {
        super(children.stream().flatMap(ingredient -> Arrays.stream(ingredient.getValues()).map(value -> {
            final List<Ingredient> matchers = new ArrayList<>(children);
            matchers.remove(ingredient);
            
            return new IntersectionValue(value, matchers);
        })), ForgeMod.INTERSECTION_INGREDIENT_TYPE::get);
        
        this.children = Collections.unmodifiableList(children);
    }
    
    public List<Ingredient> getChildren() {
        return children;
    }
    
    /**
     * Gets an intersection ingredient
     * @param ingredients  List of ingredients to match
     * @return  Ingredient that only matches if all the passed ingredients match
     */
    public static Ingredient of(Ingredient... ingredients)
    {
        if (ingredients.length == 0)
            throw new IllegalArgumentException("Cannot create an IntersectionIngredient with no children, use Ingredient.of() to create an empty ingredient");
        if (ingredients.length == 1)
            return ingredients[0];

        return new IntersectionIngredient(Arrays.asList(ingredients));
    }
    
    @Override
    public ItemStack[] getItems() {
        if (synchronizeWithContents())
            return super.getItems();
        
        final List<ItemStack> list = Lists.newArrayList();
        for (Ingredient child : children) {
            final var stacks = child.getItems();
            Arrays.stream(stacks).filter(this).forEach(list::add);
        }
        
        return list.toArray(ItemStack[]::new);
    }
    
    @Override
    public boolean test(@Nullable ItemStack p_43914_) {
        if (synchronizeWithContents())
            return super.test(p_43914_);
        
        return children.stream().allMatch(c -> c.test(p_43914_));
    }
    
    @Override
    public boolean synchronizeWithContents() {
        return children.stream().allMatch(Ingredient::synchronizeWithContents);
    }

    @Override
    public boolean isSimple()
    {
        return false;
    }
    
    public record IntersectionValue(Value inner, List<Ingredient> other) implements Ingredient.Value {
        
        @Override
        public Collection<ItemStack> getItems() {
            final Collection<ItemStack> inner = new ArrayList<>(inner().getItems());
            
            inner.removeIf(stack -> {
                return !other().stream().allMatch(ingredient -> ingredient.test(stack));
            });
            
            return inner;
        }
    }
}
