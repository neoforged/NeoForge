/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.brewing;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForgeMod;

public interface IBrewingRecipe extends Recipe<IBrewingRecipe.IBrewingContainer> {
    /**
     * Returns true is the passed ItemStack is an input for this recipe. "Input"
     * being the item that goes in one of the three bottom slots of the brewing
     * stand (e.g: water bottle)
     */
    boolean isInput(ItemStack input);

    /**
     * Returns true if the passed ItemStack is an ingredient for this recipe.
     * "Catalyst" being the item that goes in the top slot of the brewing
     * stand (e.g: nether wart)
     */
    boolean isCatalyst(ItemStack catalyst);

    /**
     * Returns the output when the passed input is brewed with the passed
     * catalyst. Empty if invalid input or catalyst.
     */
    ItemStack getOutput(ItemStack input, ItemStack catalyst);

    @Override
    default boolean matches(IBrewingContainer container, Level level) {
        return isCatalyst(container.getCatalyst()) && isInput(container.getInput());
    }

    @Override
    default ItemStack assemble(IBrewingContainer container, RegistryAccess access) {
        return getOutput(container.getInput(), container.getCatalyst());
    }

    @Override
    default boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    default ItemStack getResultItem(RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    @Override
    default RecipeType<IBrewingRecipe> getType() {
        return NeoForgeMod.BREWING_RECIPE_TYPE.value();
    }

    interface IBrewingContainer extends Container {
        ItemStack getInput();

        ItemStack getCatalyst();

        void setResult(ItemStack result);

        class Wrapper implements IBrewingContainer {
            private final Container delegate;
            private final int inputSlot;
            private final int catalystSlot;

            public Wrapper(Container delegate, int inputSlot, int catalystSlot) {
                this.delegate = delegate;
                this.inputSlot = inputSlot;
                this.catalystSlot = catalystSlot;
            }

            @Override
            public void clearContent() {
                this.removeItemNoUpdate(this.inputSlot);
                this.removeItemNoUpdate(this.catalystSlot);
            }

            @Override
            public int getContainerSize() {
                return 2;
            }

            @Override
            public boolean isEmpty() {
                return getInput().isEmpty() && getCatalyst().isEmpty();
            }

            @Override
            public ItemStack getItem(int slot) {
                if (isValidSlot(slot)) {
                    return this.delegate.getItem(mapSlot(slot));
                }
                return ItemStack.EMPTY;
            }

            @Override
            public ItemStack removeItem(int slot, int count) {
                if (isValidSlot(slot)) {
                    return this.delegate.removeItem(mapSlot(slot), count);
                }
                return ItemStack.EMPTY;
            }

            @Override
            public ItemStack removeItemNoUpdate(int slot) {
                if (isValidSlot(slot)) {
                    return this.delegate.removeItemNoUpdate(mapSlot(slot));
                }
                return ItemStack.EMPTY;
            }

            @Override
            public void setItem(int slot, ItemStack stack) {
                if (isValidSlot(slot)) {
                    this.delegate.setItem(mapSlot(slot), stack);
                }
            }

            @Override
            public void setChanged() {
                this.delegate.setChanged();
            }

            @Override
            public boolean stillValid(Player player) {
                return this.delegate.stillValid(player);
            }

            private int mapSlot(int slot) {
                return switch (slot) {
                    case 0 -> this.inputSlot;
                    case 1 -> this.catalystSlot;
                    default -> -1;
                };
            }

            private boolean isValidSlot(int slot) {
                return slot == 0 || slot == 1;
            }

            @Override
            public ItemStack getInput() {
                return this.delegate.getItem(this.inputSlot);
            }

            @Override
            public ItemStack getCatalyst() {
                return this.delegate.getItem(this.catalystSlot);
            }

            @Override
            public void setResult(ItemStack result) {
                this.delegate.setItem(this.inputSlot, result);
            }
        }
    }
}
