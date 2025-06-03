package net.neoforged.neoforge.transfer.fluid;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.transfer.initem.InItemStorageContext;
import net.neoforged.neoforge.transfer.item.ItemVariant;

public class VanillaBucketFluidStorage extends DiscreteInItemStorage<FluidVariant> {
    public VanillaBucketFluidStorage(InItemStorageContext context) {
        super(context);
    }

    @Override
    protected ItemVariant getEmptyItem() {
        return ItemVariant.of(Items.BUCKET);
    }

    @Override
    protected int getItemVolume() {
        return FluidType.BUCKET_VOLUME;
    }

    @Override
    protected ItemVariant getFilledItem(FluidVariant fluidContent) {
        return FluidUtil.getFilledBucket(fluidContent);
    }

    @Override
    protected FluidVariant getContainedResource(ItemVariant filledItem) {
        var item = filledItem.getItem();
        if (item instanceof BucketItem bucketItem) {
            return FluidVariant.of(bucketItem.content);
        } else if (item instanceof MilkBucketItem && NeoForgeMod.MILK.isBound()) {
            return FluidVariant.of(NeoForgeMod.MILK.get());
        } else {
            return FluidVariant.EMPTY;
        }
    }
}
