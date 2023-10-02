package net.minecraftforge.common.capabilities;

import net.minecraft.nbt.CompoundTag;

/**
 * @author TT432
 */
public interface ISyncCapabilityProvider<T extends ICapabilityProvider> {
    /**
     * write all the sync capability data
     *
     * @param tag   tag
     * @param owner owner of the capabilities
     */
    void writeAllSyncCapabilityData(CompoundTag tag, T owner);

    /**
     * read all the sync capability data
     *
     * @param tag tag
     */
    void readAllSyncCapabilityData(CompoundTag tag);
}
