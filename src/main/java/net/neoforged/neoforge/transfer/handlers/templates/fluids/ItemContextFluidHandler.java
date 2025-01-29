package net.neoforged.neoforge.transfer.handlers.templates.fluids;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.handlers.templates.storage.ItemContextResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;

import java.util.function.Predicate;

public class ItemContextFluidHandler extends ItemContextResourceHandler<FluidResource> {

    public ItemContextFluidHandler(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit, Predicate<FluidResource> validator) {
        super(context, componentType, FluidResource.NONE, singleItemLimit, validator);
    }
    public ItemContextFluidHandler(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit) {
        super(context, componentType, FluidResource.NONE, singleItemLimit);
    }
    public static class Consumable extends ItemContextFluidHandler {

        public Consumable(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit, Predicate<FluidResource> validator) {
            super(context, componentType, singleItemLimit, validator);
        }
        public Consumable(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit) {
            super(context, componentType, singleItemLimit);
        }
        @Override
        protected int empty(int count, TransferAction action) {
            return context.extract(context.getResource(), count, action);
        }
    }

    public static class SwapEmpty extends ItemContextFluidHandler {
        protected final ItemResource emptyContainer;

        public SwapEmpty(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit, Predicate<FluidResource> validator, ItemResource emptyContainer) {
            super(context, componentType, singleItemLimit, validator);
            this.emptyContainer = emptyContainer;
        }

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
