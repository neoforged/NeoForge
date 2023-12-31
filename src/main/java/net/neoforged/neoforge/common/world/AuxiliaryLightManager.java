package net.neoforged.neoforge.common.world;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.ApiStatus;

/**
 * Manager for light values controlled by dynamic data in {@link BlockEntity}s.
 */
public final class AuxiliaryLightManager implements INBTSerializable<ListTag> {
    public static final String LIGHT_NBT_KEY = "neoforge:aux_lights";

    private final LevelChunk owner;
    private final Map<BlockPos, Integer> lights = new ConcurrentHashMap<>();

    @ApiStatus.Internal
    public AuxiliaryLightManager(LevelChunk owner) {
        this.owner = owner;
    }

    /**
     * Set the light value at the given position to the given value
     */
    public void setLightAt(BlockPos pos, int value) {
        if (value > 0) {
            lights.put(pos, value);
        } else {
            lights.remove(pos);
        }
        owner.setUnsaved(true);
    }

    /**
     * Remove the light value at the given position
     */
    public void removeLightAt(BlockPos pos) {
        lights.remove(pos);
        owner.setUnsaved(true);
    }

    /**
     * {@return the light value at the given position or 0 if none is present}
     */
    public int getLightAt(BlockPos pos) {
        return lights.getOrDefault(pos, 0);
    }

    @Override
    @ApiStatus.Internal
    public ListTag serializeNBT() {
        if (lights.isEmpty()) {
            return null;
        }

        ListTag list = new ListTag();
        lights.forEach((pos, light) -> {
            CompoundTag tag = new CompoundTag();
            tag.putLong("pos", pos.asLong());
            tag.putByte("level", light.byteValue());
            list.add(tag);
        });
        return list;
    }

    @Override
    @ApiStatus.Internal
    public void deserializeNBT(ListTag list) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            lights.put(BlockPos.of(tag.getLong("pos")), (int) tag.getByte("level"));
        }
    }
}
