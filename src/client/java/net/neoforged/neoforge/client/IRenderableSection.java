/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

/**
 * Describes a chunk section that may be rendered on the GPU.
 *
 * The renderer may choose to reuse a common backing object
 * for this interface under the hood (for performance reasons),
 * so the {@link IRenderableSection} and any objects its methods
 * return are not guaranteed to be immutable or valid after
 * exiting the scope in which its provided.
 */
public interface IRenderableSection {
    /**
     * {@return the block position at the origin of the section}
     */
    BlockPos getRenderOrigin();

    /**
     * {@return the bounding box of the section}
     */
    AABB getBoundingBox();

    /**
     * {@return true if the compiled section contains no chunk render layers}
     */
    boolean isEmpty();
}
