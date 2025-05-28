package net.neoforged.neoforge.transfer.item.base;

import net.neoforged.neoforge.transfer.item.ItemVariant;
import net.neoforged.neoforge.transfer.storage.base.EmptyStorage;

public final class EmptyItemStorage extends EmptyStorage<ItemVariant> {
    public static final EmptyItemStorage INSTANCE = new EmptyItemStorage();

    @Override
    protected ItemVariant getBlankResource() {
        return ItemVariant.EMPTY;
    }
}
