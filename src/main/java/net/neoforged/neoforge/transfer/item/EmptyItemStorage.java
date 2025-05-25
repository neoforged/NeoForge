package net.neoforged.neoforge.transfer.item;

import net.neoforged.neoforge.transfer.storage.EmptyStorage;

public final class EmptyItemStorage extends EmptyStorage<ItemVariant> {
    public static final EmptyItemStorage INSTANCE = new EmptyItemStorage();

    @Override
    protected ItemVariant getBlankResource() {
        return ItemVariant.EMPTY;
    }
}
