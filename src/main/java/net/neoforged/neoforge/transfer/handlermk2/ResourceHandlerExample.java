package net.neoforged.neoforge.transfer.handlermk2;

import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class ResourceHandlerExample {
    /**
     * Attempts to insert 10 apples to the handler.
     *
     * @return how many apples were inserted.
     */
    public static int addApples(IResourceHandlerTransaction<ItemResource> handler) {
        var apple = Items.APPLE.defaultResource();
        try (var tx = Transaction.open(null)) {
            int inserted = handler.insert(apple, 10, tx);
            tx.commit();
            return inserted;
        }
    }


    /**
     * Extracts 16 coal from slot 0 and inserts 1 diamond into slot 1. Only if both succeed.
     * @return {@code true} if both operations succeeded, {@code false} otherwise.
     */
    public static boolean coalToDiamonds(IResourceHandlerTransaction<ItemResource> handler, TransferAction action) {
        var coal = Items.COAL.defaultResource();
        var diamond = Items.DIAMOND.defaultResource();

        try (var tx = Transaction.open(null)) {
            if (handler.extract(0, coal, 16, tx) != 16) return false;
            if (handler.insert(1, diamond, 1, tx) != 1) return false;
            return action.commit(tx);
        }
    }
}
