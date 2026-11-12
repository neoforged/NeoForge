/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world;

import java.util.List;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;

/** Implement this interface in a {@link net.minecraft.world.level.levelgen.structure.StructurePiece} class extension to modify its {@link net.minecraft.world.level.levelgen.Beardifier} behavior. */
public interface PieceBeardifierModifier {
    BoundingBox getBeardifierBox();

    TerrainAdjustment getTerrainAdjustment();

    /// @deprecated To be removed in 26.4. Override [#getBeardifierGroundLevelDelta()] instead.
    @Deprecated(forRemoval = true, since = "26.3")
    int getGroundLevelDelta();

    /// {@return how far above the bottom of [the beardifier box][#getBeardifierBox()] the ground lies}
    default int getBeardifierGroundLevelDelta() {
        return getGroundLevelDelta();
    }

    /// {@return the junctions which the beardifier blends the terrain around}
    /// 
    /// @implNote Defaults to the junctions of a [PoolElementStructurePiece], or otherwise returns an empty list.
    default List<JigsawJunction> getBeardifierJunctions() {
        return this instanceof PoolElementStructurePiece poolPiece ? poolPiece.getJunctions() : List.of();
    }
}
