package net.neoforged.neoforge.transfer.items.templates;

import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.items.ItemResource;
import net.neoforged.neoforge.transfer.storage.IStorage;

import java.util.function.Supplier;

public class ItemStorageAttachment implements IStorage<ItemResource> {
    private final Supplier<AttachmentType<ItemContainerContents>> attachmentType;
    private final AttachmentHolder holder;
    private final int slotCount;

    public ItemStorageAttachment(Supplier<AttachmentType<ItemContainerContents>> attachmentType, AttachmentHolder holder, int slotCount) {
        this.attachmentType = attachmentType;
        this.holder = holder;
        this.slotCount = slotCount;
    }

    @Override
    public int getSlotCount() {
        return slotCount;
    }

    @Override
    public int getSlotLimit(int slot) {
        return holder.getData(attachmentType).get
    }

    @Override
    public ItemResource getResource(int slot) {
        return null;
    }

    @Override
    public int getAmount(int slot) {
        return 0;
    }

    @Override
    public boolean isResourceValid(int slot, ItemResource resource) {
        return false;
    }

    @Override
    public boolean canInsert() {
        return false;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    public boolean isSlotEmpty(int slot) {
        return getAmount(slot) == 0 || getResource(slot).isBlank();
    }

    @Override
    public int insert(int slot, ItemResource resource, int amount, TransferAction action) {
        return 0;
    }

    @Override
    public int insert(ItemResource resource, int amount, TransferAction action) {
        return 0;
    }

    @Override
    public int extract(int slot, ItemResource resource, int amount, TransferAction action) {
        return 0;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransferAction action) {
        return 0;
    }
}
