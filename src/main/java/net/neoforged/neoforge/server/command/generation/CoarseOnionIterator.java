/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command.generation;

import com.google.common.collect.AbstractIterator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import net.minecraft.world.level.ChunkPos;

public class CoarseOnionIterator extends AbstractIterator<ChunkPos> {
    private final int radius;
    private final int cellSize;

    private final OnionIterator cells;
    private CellIterator cell;

    public CoarseOnionIterator(int radius, int cellSize) {
        this.radius = radius;
        this.cellSize = cellSize;

        this.cells = new OnionIterator((radius + cellSize - 1) / cellSize);
    }

    @Override
    protected ChunkPos computeNext() {
        OnionIterator cells = this.cells;
        CellIterator cell = this.cell;
        while (cell == null || !cell.hasNext()) {
            if (!cells.hasNext()) {
                return this.endOfData();
            }

            ChunkPos cellPos = cells.next();
            this.cell = cell = this.createCellIterator(cellPos);
        }

        return cell.next();
    }

    private CellIterator createCellIterator(ChunkPos pos) {
        int size = this.cellSize;
        int radius = this.radius;

        int x0 = pos.x * size;
        int z0 = pos.z * size;
        int x1 = x0 + size - 1;
        int z1 = z0 + size - 1;
        return new CellIterator(
                Math.max(x0, -radius), Math.max(z0, -radius),
                Math.min(x1, radius), Math.min(z1, radius));
    }

    private static final class CellIterator implements Iterator<ChunkPos> {
        private final int x0;
        private final int x1;
        private final int z1;

        private int x;
        private int z;

        private CellIterator(int x0, int z0, int x1, int z1) {
            this.x = x0;
            this.z = z0;
            this.x0 = x0;
            this.x1 = x1;
            this.z1 = z1;
        }

        @Override
        public boolean hasNext() {
            return this.z <= this.z1;
        }

        @Override
        public ChunkPos next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }

            ChunkPos pos = new ChunkPos(this.x, this.z);
            if (this.x++ >= this.x1) {
                this.x = this.x0;
                this.z++;
            }

            return pos;
        }
    }
}
