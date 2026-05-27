/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.common.extensions.IBlockStateExtension;

/// Declares whether some block is relocatable by mechanics which cut/copy and paste blocks,
/// typically with some translation and possibly a rotation and/or mirror,
/// and including any blockentity data.
///
/// Relocator mechanics may query {@link IBlockStateExtension#getRelocability} for each block in some area being moved.
public sealed interface BlockRelocability permits BlockRelocability.Never, BlockRelocability.Always, BlockRelocability.Multiblock {
    /// {@return true if the block is relocatable from a given Set of block positions, false otherwise}
    /// @param relocatingPositions Set of positions blocks are being relocated from
    public abstract boolean isRelocatable(Set<BlockPos> relocatingPositions);

    /// BlockRelocability indicating some block may never be relocated.
    /// "Never" only indicates that this BlockRelocability instance produced by some context
    /// will always return false for isRelocatable; it does not imply that the relevant block
    /// will always produce a Never instance in any context.
    public static enum Never implements BlockRelocability {
        /** Singleton instance **/
        INSTANCE;

        @Override
        public boolean isRelocatable(Set<BlockPos> relocatingPositions) {
            return false;
        }
    }

    /// BlockRelocability indicating some block is movable from any position.
    /// "Always" only indicates that this BlockRelocability instance produced by some context
    /// will always return true for isRelocatable; it does not imply that the relevant block
    /// will always produce an Always instance in any context.
    public static enum Always implements BlockRelocability {
        /** Singleton instance **/
        INSTANCE;

        @Override
        public boolean isRelocatable(Set<BlockPos> relocatingPositions) {
            return true;
        }
    }

    /// BlockRelocability indicating a block may be moved if and only if some set of other block positions is being
    /// moved with it.
    ///
    /// Large multiblocks with a "core" block may reduce the number of necessary checks by having the non-core blocks
    /// only require themselves and the core, and having the core block require all non-core positions.
    ///
    /// @param requiredPositions Set of block positions which must be moved with this block to allow moving it.
    public static record Multiblock(Set<BlockPos> requiredPositions) implements BlockRelocability {
        @Override
        public boolean isRelocatable(Set<BlockPos> relocatingPositions) {
            return relocatingPositions.containsAll(requiredPositions);
        }
    }
}
