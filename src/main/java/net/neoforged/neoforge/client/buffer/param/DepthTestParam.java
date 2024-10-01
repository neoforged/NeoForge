/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import net.neoforged.neoforge.client.buffer.param.general.IntegerParam;

public class DepthTestParam {
    public static final class Vanilla {
        public static final IntegerParam NO_DEPTH_TEST = new IntegerParam(519);
        public static final IntegerParam EQUAL_DEPTH_TEST = new IntegerParam(514);
        public static final IntegerParam LEQUAL_DEPTH_TEST = new IntegerParam(515);
        public static final IntegerParam GREATER_DEPTH_TEST = new IntegerParam(516);
    }
}
