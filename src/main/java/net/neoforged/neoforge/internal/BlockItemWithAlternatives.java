package net.neoforged.neoforge.internal;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Map;

public final class BlockItemWithAlternatives extends BlockItem {
    private final Block[] alternatives;

    public BlockItemWithAlternatives(Block block, Block[] alternatives, Properties properties) {
        super(block, properties);
        this.alternatives = alternatives;
    }

    @Override
    public void registerBlocks(Map<Block, Item> map, Item item) {
        super.registerBlocks(map, item);
        for (Block block : this.alternatives) {
            map.put(block, item);
        }
    }
}
