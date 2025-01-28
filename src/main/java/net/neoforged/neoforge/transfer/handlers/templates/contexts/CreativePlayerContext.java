package net.neoforged.neoforge.transfer.handlers.templates.contexts;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.PlayerInventoryHandler;

public class CreativePlayerContext extends StaticContext {
    protected final PlayerInventoryHandler handler;

    public CreativePlayerContext(ItemResource resource, int amount, Player player) {
        super(resource, amount);
        this.handler = new PlayerInventoryHandler(player);
    }

    @Override
    protected int insertOverflow(ItemResource resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isEmpty()) return 0;
        int testIfPresent = handler.extract(resource, 1, TransferAction.SIMULATE);
        if (testIfPresent == 0) {
            return handler.insert(resource, 1, action);
        }
        return amount;
    }
}
