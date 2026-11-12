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

    int getGroundLevelDelta();

    /** How far above the bottom of {@link #getBeardifierBox()} the ground lies. */
    default int getBeardifierGroundLevelDelta() {
        return getGroundLevelDelta();
    }

    /** Junctions the Beardifier blends the terrain around. Defaults to the junctions of a {@link PoolElementStructurePiece}. */
    default List<JigsawJunction> getBeardifierJunctions() {
        return this instanceof PoolElementStructurePiece poolPiece ? poolPiece.getJunctions() : List.of();
    }
}
