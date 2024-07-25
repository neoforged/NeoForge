package net.neoforged.neoforge.transfer.fluids.templates;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.ResourceStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.handlers.templates.SingleResourceStorageItem;
import net.neoforged.neoforge.transfer.items.ItemResource;

public class SingleFluidStorageItem extends SingleResourceStorageItem<FluidResource> {
    public SingleFluidStorageItem(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit) {
        super(context, componentType, FluidResource.NONE, singleItemLimit);
    }

    public static class Consumable extends SingleFluidStorageItem {
        public Consumable(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit) {
            super(context, componentType, singleItemLimit);
        }

        @Override
        protected int empty(int count, TransferAction action) {
            return context.extract(context.getResource(), count, action);
        }
    }

    public static class SwapEmpty extends SingleFluidStorageItem {
        protected final ItemResource emptyContainer;

        public SwapEmpty(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit, ItemResource emptyContainer) {
            super(context, componentType, singleItemLimit);
            this.emptyContainer = emptyContainer;
        }

        @Override
        protected int empty(int count, TransferAction action) {
            return context.exchange(emptyContainer, count, action);
        }
    }
}
