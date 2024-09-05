/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelReader;
import net.neoforged.neoforge.flag.Flag;
import net.neoforged.neoforge.flag.FlagManager;
import org.apache.commons.lang3.NotImplementedException;

public interface ILevelReaderExtension {
    private LevelReader self() {
        return (LevelReader) this;
    }

    default boolean isAreaLoaded(BlockPos center, int range) {
        return self().hasChunksAt(center.offset(-range, -range, -range), center.offset(range, range, range));
    }

    /**
     * Shortcut method to get a holder from a ResourceKey.
     * see {@link IHolderLookupProviderExtension}
     *
     * @throws IllegalStateException if the registry or key is not found.
     */
    default <T> Holder<T> holderOrThrow(ResourceKey<T> key) {
        return this.self().registryAccess().holderOrThrow(key);
    }

    /**
     * Shortcut method to get an optional holder from a ResourceKey.
     * see {@link IHolderLookupProviderExtension}
     */
    default <T> Optional<Holder.Reference<T>> holder(ResourceKey<T> key) {
        return this.self().registryAccess().holder(key);
    }

    /**
     * Returns a valid and sycned {@link FlagManager}.
     * <p>
     * Modders may use this for all their {@link Flag flag} state testing needs.
     *
     * @return Valid and synced {@link FlagManager}.
     */
    default FlagManager getModdedFlagManager() {
        throw new NotImplementedException("ILevelReaderExtension#getModdedFlagManager must be implemented to lookup a valid FlagManager");
    }
}
