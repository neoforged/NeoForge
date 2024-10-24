/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import net.neoforged.neoforge.client.buffer.param.general.BooleanParam;

public class CrumblingParam {
    public static final class Vanilla {
        public static final BooleanParam AFFECTS_CRUMBLING = new BooleanParam(true);
        public static final BooleanParam NOT_AFFECTS_CRUMBLING = new BooleanParam(false);
    }
}
