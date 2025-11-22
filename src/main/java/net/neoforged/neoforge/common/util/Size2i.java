/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class Size2i {
    public final int width;
    public final int height;

    @SuppressWarnings("ConstantConditions")
    public Size2i(int width, int height) {
        Preconditions.checkArgument(width >= 0, "width must be greater or equal 0");
        Preconditions.checkArgument(height >= 0, "height must be greater or equal 0");
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Size2i) {
            Size2i other = (Size2i) obj;
            return (width == other.width) && (height == other.height);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + width;
        hash = hash * 31 + height;
        return hash;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("width", width)
                .add("height", height)
                .toString();
    }
}
