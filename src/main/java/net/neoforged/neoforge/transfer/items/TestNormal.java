package net.neoforged.neoforge.transfer.items;

import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;

public class TestNormal implements IResourceHandler<ItemResource> {

    @Override
    public int insert(int index, ItemResource resource, int amount, TransferAction action) {
        return 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransferAction action) {
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
    public int size() {
        return 0;
    }

    @Override
    public ItemResource getResource(int index) {
        return null;
    }

    @Override
    public int getAmount(int index) {
        return 0;
    }

    @Override
    public int getCapacity(int index, ItemResource resource) {
        return 0;
    }

    @Override
    public int getCapacity(int index) {
        return 0;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return false;
    }

    @Override
    public boolean allowsInsertion(int index) {
        return false;
    }

    @Override
    public boolean allowsExtraction(int index) {
        return false;
    }
}
