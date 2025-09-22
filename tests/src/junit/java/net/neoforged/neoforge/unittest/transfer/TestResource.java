/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import net.neoforged.neoforge.transfer.resource.Resource;

public enum TestResource implements Resource {
    EMPTY,
    SOME,
    OTHER_1,
    OTHER_2,
    OTHER_3,
    OTHER_4;

    static TestResource otherFromIndex(int index) {
        return values()[OTHER_1.ordinal() + index];
    }

    @Override
    public boolean isEmpty() {
        return this == EMPTY;
    }
}
