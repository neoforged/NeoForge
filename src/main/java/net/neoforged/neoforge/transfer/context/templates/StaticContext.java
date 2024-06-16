package net.neoforged.neoforge.transfer.context.templates;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.items.ItemResource;

import java.util.Objects;

/**
 * A static context that holds a fixed amount of a single item. Operations on this context will still perform as if the
 * item is mutable, but the amount or resource will never change.
 */
public class StaticContext implements IItemContext {
    private final ItemResource resource;
    private final int amount;

    public StaticContext(ItemResource resource, int amount) {
        this.resource = resource;
        this.amount = amount;
    }

    public StaticContext(ItemStack stack) {
        this(ItemResource.of(stack), stack.getCount());
    }

    @Override
    public ItemResource getResource() {
        return resource;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public int insert(ItemResource resource, int amount, TransferAction action) {
        if (resource.isBlank() || amount <= 0) return 0;
        return insertOverflow(resource, amount, action);
    }

    protected int insertOverflow(ItemResource resource, int amount, TransferAction action) {
        return amount;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransferAction action) {
        return Objects.equals(this.resource, resource) ? Math.min(this.amount, amount) : 0;
    }

    @Override
    public int exchange(ItemResource resource, int amount, TransferAction action) {
        return amount;
    }
}
