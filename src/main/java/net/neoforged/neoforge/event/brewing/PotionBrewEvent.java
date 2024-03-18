/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.brewing;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;

public abstract class PotionBrewEvent extends Event {
    private final Level level;
    private final IBrewingRecipe.IBrewingContainer brewingContainer;
    private final RecipeHolder<IBrewingRecipe> recipe;

    protected PotionBrewEvent(Level level, IBrewingRecipe.IBrewingContainer brewingContainer, RecipeHolder<IBrewingRecipe> recipe) {
        this.level = level;
        this.brewingContainer = brewingContainer;
        this.recipe = recipe;
    }

    public Level getLevel() {
        return this.level;
    }

    public IBrewingRecipe.IBrewingContainer getBrewingContainer() {
        return this.brewingContainer;
    }

    public ItemStack getInput() {
        return this.getBrewingContainer().getInput();
    }

    public ItemStack getCatalyst() {
        return this.getBrewingContainer().getCatalyst();
    }

    public RecipeHolder<IBrewingRecipe> getRecipe() {
        return this.recipe;
    }

    public ResourceLocation getRecipeId() {
        return this.getRecipe().id();
    }

    /**
     * PotionBrewEvent.Pre is fired before brewing takes place.
     * <p>
     * This event is {@link net.neoforged.bus.api.ICancellableEvent}.<br>
     * If the event is canceled, the brewing will not take place.
     * <p>
     * This event does not have a result. {@link HasResult}<br>
     * <p>
     * This event is fired on the {@link NeoForge#EVENT_BUS}.<br>
     **/
    public static class Pre extends PotionBrewEvent implements ICancellableEvent {
        public Pre(Level level, IBrewingRecipe.IBrewingContainer brewingContainer, RecipeHolder<IBrewingRecipe> recipe) {
            super(level, brewingContainer, recipe);
        }
    }

    /**
     * PotionBrewEvent.Post is fired when a potion is brewed in the brewing stand.<br>
     * If the output is changed the result of the brewing will be the newly set output.
     * <p>
     * This event is not {@link net.neoforged.bus.api.ICancellableEvent}.
     * <p>
     * This event does not have a result. {@link HasResult}
     * <p>
     * This event is fired on the {@link NeoForge#EVENT_BUS}.
     **/
    public static class Post extends PotionBrewEvent {
        private ItemStack output;

        public Post(Level level, IBrewingRecipe.IBrewingContainer brewingContainer, RecipeHolder<IBrewingRecipe> recipe, ItemStack output) {
            super(level, brewingContainer, recipe);
            this.output = output;
        }

        public ItemStack getOutput() {
            return output;
        }

        public void setOutput(ItemStack output) {
            this.output = output;
        }
    }
}
