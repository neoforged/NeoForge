package net.neoforged.neoforge.transfer.context.templates;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.items.ItemResource;

import java.util.ArrayList;
import java.util.List;

public class DispenserContext implements IItemContext {
    protected ItemResource resource;
    protected int amount;
    protected final Object2IntMap<ItemResource> resources = new Object2IntOpenHashMap<>();

    public DispenserContext(ItemStack stack) {
        this.resource = ItemResource.of(stack);
        this.amount = stack.getCount();
    }

    @Override
    public ItemResource getResource() {
        return resource;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public int insert(ItemResource resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isBlank()) return 0;
        if (action.isSimulating()) return amount;
        int inserted = 0;
        if (getResource().isBlank()) {
            inserted = Math.min(amount, resource.getMaxStackSize());
            if (action.isExecuting()) {
                this.resource = resource;
                this.amount = inserted;
            }
        } else if (getResource().equals(resource)) {
            inserted = Math.min(amount, resource.getMaxStackSize() - getAmount());
            if (action.isExecuting()) {
                this.amount += inserted;
            }
        }
        if (action.isExecuting()) resources.put(resource, resources.getInt(resource) + amount - inserted);
        return amount;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isBlank()) return 0;
        int extracted = Math.min(amount, getAmount());
        if (action.isExecuting()) {
            this.amount -= extracted;
            if (getAmount() == 0) {
                this.resource = ItemResource.BLANK;
            }
        }
        return extracted;
    }

    @Override
    public int exchange(ItemResource resource, int amount, TransferAction action) {
        if (amount >= getAmount()) {
            if (action.isExecuting()) {
                this.resource = resource;
            }
            return getAmount();
        }
        int extracted = extract(getResource(), amount, action);
        if (extracted > 0) {
            insert(resource, extracted, action);
        }
        return extracted;
    }

    public ItemStack finalizeResult(BlockSource source) {
        ItemStack res = resource.toStack(amount);
        List<ItemStack> overflow = new ArrayList<>();
        resources.forEach((resource, amount) -> resource.toStacks(amount).forEach(stack -> {
            ItemStack notInserted = source.blockEntity().insertItem(stack);
            if (!notInserted.isEmpty()) {
                overflow.add(notInserted);
            }
        }));
        if (!overflow.isEmpty()) {
            Direction direction = source.state().getValue(DispenserBlock.FACING);
            DefaultDispenseItemBehavior.playDefaultSound(source);
            DefaultDispenseItemBehavior.playDefaultAnimation(source, direction);
            Position position = DispenserBlock.getDispensePosition(source);
            overflow.forEach(stack -> DefaultDispenseItemBehavior.spawnItem(source.level(), stack, 6, direction, position));
        }
        resources.clear();
        return res;
    }
}
