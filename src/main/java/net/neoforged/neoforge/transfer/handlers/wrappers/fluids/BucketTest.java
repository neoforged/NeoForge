package net.neoforged.neoforge.transfer.handlers.wrappers.fluids;

import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.templates.container.SimpleItemResourceContainer;

public class BucketTest {
    public static void test() {
        var container = SimpleItemResourceContainer.builder(3).build();
        for (var index = 0; index < container.size(); index++) {
            container.set(index, Items.BUCKET.defaultResource().withMutableAmount(index));
        }
        //        ItemStackStorage outerStorage = Util.make(() -> {
        //            var storage = new ItemStackStorage(3);
        //            storage.setStackInSlot(0, new ItemStack(Items.BUCKET));
        //            return storage;
        //        });
        var storage = new BucketResourceHandler(IItemContext.ofIndex(container.asHandler(), 0));
    }
}
