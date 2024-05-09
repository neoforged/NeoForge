package net.neoforged.neoforge.transfer.context.templates;

import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.items.ItemResource;

public class PlayerContext implements IItemContext {

    @Override
    public ItemResource getResource() {
        return null;
    }

    @Override
    public int getAmount() {
        return 0;
    }

    @Override
    public int insert(ItemResource resource, int amount, TransferAction action) {
        return 0;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransferAction action) {
        return 0;
    }

    @Override
    public int exchange(ItemResource resource, int amount, TransferAction action) {
        return 0;
    }
}
