package net.neoforged.neoforge.registries;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.neoforged.neoforge.registries.callback.AddCallback;
import net.neoforged.neoforge.registries.callback.BakeCallback;
import net.neoforged.neoforge.registries.callback.ClearCallback;

class NeoForgeRegistryCallbacks {
    static class BlockCallbacks implements ClearCallback<Block>, BakeCallback<Block> {
        static final BlockCallbacks INSTANCE = new BlockCallbacks();
        static final ClearableObjectIntIdentityMap<BlockState> BLOCKSTATE_TO_ID_MAP = new ClearableObjectIntIdentityMap<>();

        @Override
        public void onClear(Registry<Block> registry, boolean full) {
            BLOCKSTATE_TO_ID_MAP.clear();
        }

        @Override
        public void onBake(Registry<Block> registry) {
            for (Block block : registry) {
                for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                    BLOCKSTATE_TO_ID_MAP.add(state);
                    state.initCache();
                }

                block.getLootTable();
            }
            DebugLevelSource.initValidStates();
        }

        private static class ClearableObjectIntIdentityMap<T> extends IdMapper<T> {
            void clear() {
                this.tToId.clear();
                this.idToT.clear();
                this.nextId = 0;
            }
        }
    }

    static class ItemCallbacks implements AddCallback<Item>, ClearCallback<Item> {
        static final ItemCallbacks INSTANCE = new ItemCallbacks();
        static final Map<Block, Item> BLOCK_TO_ITEM_MAP = new HashMap<>();

        @Override
        public void onAdd(Registry<Item> registry, int id, ResourceKey<Item> key, Item item) {
            if (item instanceof BlockItem blockItem)
                blockItem.registerBlocks(BLOCK_TO_ITEM_MAP, item);
        }

        @Override
        public void onClear(Registry<Item> registry, boolean full) {
            if (full)
                BLOCK_TO_ITEM_MAP.clear();
        }
    }

    static class AttributeCallbacks implements BakeCallback<Attribute> {
        static final AttributeCallbacks INSTANCE = new AttributeCallbacks();

        public void onBake(Registry<Attribute> registry) {
            DefaultAttributes.validate();
        }
    }

    static class PoiTypeCallbacks implements AddCallback<PoiType>, ClearCallback<PoiType> {
        static final PoiTypeCallbacks INSTANCE = new PoiTypeCallbacks();
        static final Map<BlockState, PoiType> BLOCKSTATE_TO_POI_TYPE_MAP = new HashMap<>();

        @Override
        public void onAdd(Registry<PoiType> registry, int id, ResourceKey<PoiType> key, PoiType value) {
            value.matchingStates().forEach(state -> {
                PoiType oldType = BLOCKSTATE_TO_POI_TYPE_MAP.put(state, value);
                if (oldType != null) {
                    throw new IllegalStateException(String.format(Locale.ENGLISH,
                            "Point of interest types %s and %s both list %s in their blockstates, this is not allowed. Blockstates can only have one point of interest type each.",
                            oldType, value, state));
                }
            });
        }

        @Override
        public void onClear(Registry<PoiType> registry, boolean full) {
            if (full)
                BLOCKSTATE_TO_POI_TYPE_MAP.clear();
        }
    }
}
