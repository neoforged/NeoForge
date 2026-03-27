/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.recipebook;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.oldtest.recipebook.RecipeBookExtensionTest.RecipeBookTestContainer;
import org.jspecify.annotations.Nullable;

public class RecipeBookTestMenu extends RecipeBookMenu {
    private final RecipeBookTestContainer container = new RecipeBookTestContainer();
    private final ResultContainer resultContainer = new ResultContainer();
    final Slot resultSlot;
    private final ContainerLevelAccess access;
    private final Player player;
    private boolean placingRecipe;

    public RecipeBookTestMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, ContainerLevelAccess.NULL);
    }

    public RecipeBookTestMenu(int id, Inventory inv, ContainerLevelAccess access) {
        super(RecipeBookExtensionTest.RECIPE_BOOK_TEST_MENU_TYPE.get(), id);
        this.access = access;
        this.player = inv.player;

        /**
         * Copied from {@link ResultSlot} but not limited to {@link CraftingContainer}
         */
        this.resultSlot = new Slot(this.resultContainer, 0, 144, 35) {
            private int removeCount;

            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player passedPlayer, ItemStack stack) {
                this.checkTakeAchievements(stack);
                CraftingInput.Positioned craftinginput$positioned = RecipeBookTestMenu.this.container.asPositionedCraftInput();
                CraftingInput craftinginput = craftinginput$positioned.input();
                int i = craftinginput$positioned.left();
                int j = craftinginput$positioned.top();
                NonNullList<ItemStack> nonnulllist = this.getRemainingItems(craftinginput, passedPlayer.level());

                for (int k = 0; k < craftinginput.height(); k++) {
                    for (int l = 0; l < craftinginput.width(); l++) {
                        int i1 = l + i + (k + j) * 2;
                        ItemStack itemstack = container.getItem(i1);
                        ItemStack itemstack1 = nonnulllist.get(l + k * craftinginput.width());
                        if (!itemstack.isEmpty()) {
                            container.removeItem(i1, 1);
                            itemstack = container.getItem(i1);
                        }

                        if (!itemstack1.isEmpty()) {
                            if (itemstack.isEmpty()) {
                                container.setItem(i1, itemstack1);
                            } else if (ItemStack.isSameItemSameComponents(itemstack, itemstack1)) {
                                itemstack1.grow(itemstack.getCount());
                                container.setItem(i1, itemstack1);
                            } else if (!player.getInventory().add(itemstack1)) {
                                player.drop(itemstack1, false);
                            }
                        }
                    }
                }
            }

            private static NonNullList<ItemStack> copyAllInputItems(CraftingInput input) {
                NonNullList<ItemStack> nonnulllist = NonNullList.withSize(input.size(), ItemStack.EMPTY);

                for (int i = 0; i < nonnulllist.size(); i++) {
                    nonnulllist.set(i, input.getItem(i));
                }

                return nonnulllist;
            }

            private NonNullList<ItemStack> getRemainingItems(CraftingInput input, Level level) {
                return level instanceof ServerLevel serverlevel
                        ? serverlevel.recipeAccess()
                                .getRecipeFor(RecipeType.CRAFTING, input, serverlevel)
                                .map(recipe -> recipe.value().getRemainingItems(input))
                                .orElseGet(() -> copyAllInputItems(input))
                        : CraftingRecipe.defaultCraftingReminder(input);
            }

            @Override
            public ItemStack remove(int amount) {
                if (this.hasItem())
                    this.removeCount += Math.min(amount, this.getItem().getCount());
                return super.remove(amount);
            }

            @Override
            public void onQuickCraft(ItemStack output, int amount) {
                this.removeCount += amount;
                this.checkTakeAchievements(output);
            }

            @Override
            protected void onSwapCraft(int amount) {
                this.removeCount = amount;
            }

            @Override
            protected void checkTakeAchievements(ItemStack stack) {
                if (this.removeCount > 0)
                    stack.onCraftedBy(RecipeBookTestMenu.this.player, this.removeCount);
                if (this.container instanceof RecipeCraftingHolder recipeCraftingHolder)
                    recipeCraftingHolder.awardUsedRecipes(RecipeBookTestMenu.this.player, List.of());
                this.removeCount = 0;
            }
        };
        this.addSlot(this.resultSlot); //slot 0

        //slots 1 - 8
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 2; ++j)
                this.addSlot(new Slot(this.container, j + i * 2, 30 + j * 18, 61 - i * 18) {
                    @Override
                    public void setChanged() {
                        super.setChanged();
                        slotsChanged(this.container);
                    }
                });
        }

        //slots 9 to 35
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j)
                this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
        }

        //slots 36 to 44
        for (int i = 0; i < 9; ++i)
            this.addSlot(new Slot(inv, i, 8 + i * 18, 142));
    }

    protected static void slotChangedCraftingGrid(
            AbstractContainerMenu menu,
            ServerLevel level,
            Player player,
            RecipeBookTestContainer container,
            ResultContainer resultSlots,
            @Nullable RecipeHolder<RecipeBookTestRecipe> recipeHint) {
        CraftingInput craftinginput = container.asCraftingInput();
        ServerPlayer serverplayer = (ServerPlayer) player;
        ItemStack itemstack = ItemStack.EMPTY;
        Optional<RecipeHolder<RecipeBookTestRecipe>> optional = level.getServer()
                .getRecipeManager()
                .getRecipeFor(RecipeBookExtensionTest.RECIPE_BOOK_TEST_RECIPE_TYPE.get(), craftinginput, level, recipeHint);
        if (optional.isPresent()) {
            RecipeHolder<RecipeBookTestRecipe> recipeholder = optional.get();
            RecipeBookTestRecipe craftingrecipe = recipeholder.value();
            if (resultSlots.setRecipeUsed(serverplayer, recipeholder)) {
                ItemStack itemstack1 = craftingrecipe.assemble(craftinginput);
                if (itemstack1.isItemEnabled(level.enabledFeatures())) {
                    itemstack = itemstack1;
                }
            }
        }

        resultSlots.setItem(0, itemstack);
        menu.setRemoteSlot(0, itemstack);
        serverplayer.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), 0, itemstack));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, Blocks.GRASS_BLOCK);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.container));
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot != resultSlot;
    }

    @Override
    public void slotsChanged(Container container) {
        if (!this.placingRecipe) {
            this.access.execute((level, pos) -> {
                if (level instanceof ServerLevel serverlevel) {
                    slotChangedCraftingGrid(this, serverlevel, this.player, this.container, this.resultContainer, null);
                }
            });
        }
    }

    static final int RESULT_SLOT = 0;
    static final int CRAFTING_START = 1;
    static final int CRAFTING_STOP = 8;
    private static final int INVENTORY_START = 9;
    private static final int INVENTORY_STOP = 44;
    private static final int HOTBAR_START = 36;

    /**
     * Mostly copied from {@link CraftingMenu#quickMoveStack}
     */
    public ItemStack quickMoveStack(Player player, int idx) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(idx);
        if (!slot.hasItem())
            return ret;

        ItemStack item = slot.getItem();
        ret = item.copy();

        if (idx == RESULT_SLOT) {
            if (!this.moveItemStackTo(item, INVENTORY_START, INVENTORY_STOP + 1, true))
                return ItemStack.EMPTY;

            slot.onQuickCraft(item, ret);
        } else if (idx >= INVENTORY_START && idx < INVENTORY_STOP + 1) {
            if (!this.moveItemStackTo(item, CRAFTING_START, CRAFTING_STOP + 1, false)) {
                if (idx < HOTBAR_START) {
                    if (!this.moveItemStackTo(item, HOTBAR_START, INVENTORY_STOP + 1, false))
                        return ItemStack.EMPTY;
                } else if (!this.moveItemStackTo(item, INVENTORY_START, HOTBAR_START, false))
                    return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(item, INVENTORY_START, INVENTORY_STOP + 1, false))
            return ItemStack.EMPTY;

        if (item.isEmpty())
            slot.set(ItemStack.EMPTY);
        else
            slot.setChanged();

        if (item.getCount() == ret.getCount())
            return ItemStack.EMPTY;

        slot.onTake(player, item);
        if (idx == RESULT_SLOT)
            player.drop(item, false);

        return ret;
    }

    //RecipeBook stuff
    @Override
    public void fillCraftSlotsStackedContents(StackedItemContents contents) {
        this.container.fillStackedContents(contents);
    }

    List<Slot> getGridSlots() {
        return this.slots.subList(CRAFTING_START, CRAFTING_STOP + 1);
    }

    @Override
    public PostPlaceAction handlePlacement(boolean useMaxItems, boolean allowDroppingItemsToClear, RecipeHolder<?> rawHolder, ServerLevel serverLevel, Inventory inventory) {
        placingRecipe = true;
        RecipeHolder<RecipeBookTestRecipe> recipeHolder = (RecipeHolder<RecipeBookTestRecipe>) rawHolder;
        try {
            List<Slot> list = getGridSlots();
            return ServerPlaceRecipe.placeRecipe(new ServerPlaceRecipe.CraftingMenuAccess<>() {
                @Override
                public void fillCraftSlotsStackedContents(StackedItemContents stackedContents) {
                    RecipeBookTestMenu.this.fillCraftSlotsStackedContents(stackedContents);
                }

                @Override
                public void clearCraftingContent() {
                    container.clearContent();
                    resultContainer.clearContent();
                }

                @Override
                public boolean recipeMatches(RecipeHolder<RecipeBookTestRecipe> recipeHolder) {
                    return recipeHolder.value().matches(container.asCraftingInput(), player.level());
                }
            }, 2, 4, list, list, inventory, recipeHolder, useMaxItems, allowDroppingItemsToClear);
        } finally {
            placingRecipe = false;
            slotChangedCraftingGrid(this, serverLevel, player, container, resultContainer, recipeHolder);
        }
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookExtensionTest.TEST_TYPE;
    }
}
