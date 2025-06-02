package net.neoforged.neoforge.transfer.handlers.resources;

import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import org.jetbrains.annotations.Range;

public class TestHandler implements IResourceHandler<ItemResource> {
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
    public int getCapacity(int index) {
        return 0;
    }
    @Override
    public int getCapacity(int index, ItemResource resource) {
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
    @Override
    public int insert(int index, ItemResource resource, @Range(from = 1, to = ResourceHandlerUtil.MAX_RESOURCE_SIZE) int amount, TransferAction action) {
        return 0;
    }
    @Override
    public int insert(ItemResource resource, @Range(from = 1, to = ResourceHandlerUtil.MAX_RESOURCE_SIZE) int amount, TransferAction action) {
        return 0;
    }
    @Override
    public int extract(int index, ItemResource resource, @Range(from = 1, to = ResourceHandlerUtil.MAX_RESOURCE_SIZE) int amount, TransferAction action) {
        return 0;
    }
    @Override
    public int extract(ItemResource resource, @Range(from = 1, to = ResourceHandlerUtil.MAX_RESOURCE_SIZE) int amount, TransferAction action) {
        return 0;
    }
}
