package net.minecraftforge.common.capabilities;

import net.minecraft.nbt.CompoundTag;

/**
 * The interface is used to automatically implement network synchronization. <br/>
 * It will automatically find a network sync solution upon implementing the interface in {@link Capability}.<br/>
 * It can be used with all of {@link ICapabilityProvider}; if not, you need to report the bug.<br/>
 *
 * @author TT432
 */
public interface ISyncCapability<T extends ICapabilityProvider> {
    /**
     * Write data to the sync tag.
     * This method MUST be triggered on the server side.
     *
     * @param tag   buf
     * @param owner the owner of the Capability
     */
    void writeSyncTag(CompoundTag tag, T owner);

    /**
     * Read data from the sync tag.
     *
     * @param tag buf
     */
    void readSyncTag(CompoundTag tag);
}
