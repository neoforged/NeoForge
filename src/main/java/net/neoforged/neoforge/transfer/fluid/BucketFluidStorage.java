package net.neoforged.neoforge.transfer.fluid;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.transfer.initem.InItemStorageContext;
import net.neoforged.neoforge.transfer.item.ItemVariant;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.storage.StoragePreconditions;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class BucketFluidStorage implements Storage<FluidVariant> {
    private final InItemStorageContext context;

    public BucketFluidStorage(InItemStorageContext context) {
        this.context = context;
    }

    @Override
    public int size() {
        return 1; // we already operate on a single bucket at a time
    }

    @Override
    public boolean supportsInsertion() {
        return context.supportsModification();
    }

    @Override
    public boolean supportsExtraction() {
        return context.supportsModification();
    }

    @Override
    public long insert(int index, FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.checkSlot(index, size());
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        if (!context.getCurrent().is(Tags.Items.BUCKETS_EMPTY)) {
            return 0; // can't fill non-empty buckets
        }

        var filledBucket = FluidUtil.getFilledBucket(resource);
        if (filledBucket.isBlank()) {
            return 0; // the fluid has no associated bucket item
        }

        var bucketsToFill = maxAmount / FluidType.BUCKET_VOLUME;
        if (bucketsToFill > 0) {
            long bucketsFilled = context.exchange(filledBucket, bucketsToFill, transaction);
            return bucketsFilled * FluidType.BUCKET_VOLUME;
        } else {
            return 0;
        }
    }

    @Override
    public long extract(int index, FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.checkSlot(index, size());
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        var containedFluid = getCurrentFluid();
        if (!resource.equals(containedFluid)) {
            return 0; // Incompatible fluid
        }

        var bucketsToEmpty = maxAmount / FluidType.BUCKET_VOLUME;
        if (bucketsToEmpty > 0) {
            long bucketsEmptied = context.exchange(ItemVariant.of(Items.BUCKET), bucketsToEmpty, transaction);
            return bucketsEmptied * FluidType.BUCKET_VOLUME;
        } else {
            return 0;
        }
    }

    @Override
    public boolean isResourceBlank(int index) {
        StoragePreconditions.checkSlot(index, size());
        return getCurrentFluid().isBlank();
    }

    @Override
    public FluidVariant getResource(int index) {
        StoragePreconditions.checkSlot(index, size());
        return getCurrentFluid();
    }

    @Override
    public long getAmount(int index) {
        StoragePreconditions.checkSlot(index, size());
        return isResourceBlank(index) ? 0 : FluidType.BUCKET_VOLUME * context.getCurrentAmount();
    }

    @Override
    public long getCapacity(int index, FluidVariant resource) {
        StoragePreconditions.checkSlot(index, size());
        return FluidType.BUCKET_VOLUME * context.getCurrentAmount();
    }

    @Override
    public boolean isValid(int index, FluidVariant resource) {
        StoragePreconditions.checkSlot(index, size());
        return !FluidUtil.getFilledBucket(resource).isBlank();
    }

    private FluidVariant getCurrentFluid() {
        var current = context.getCurrent();
        var item = current.getItem();
        if (item instanceof BucketItem bucketItem) {
            return FluidVariant.of(bucketItem.content);
        } else if (item instanceof MilkBucketItem && NeoForgeMod.MILK.isBound()) {
            return FluidVariant.of(NeoForgeMod.MILK.get());
        } else {
            return FluidVariant.EMPTY;
        }
    }
}
