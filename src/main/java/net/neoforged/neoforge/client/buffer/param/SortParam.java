/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import net.neoforged.neoforge.client.buffer.param.general.BooleanParam;

public class SortParam {
    public static final class Vanilla {
        public static final BooleanParam SORT_ON_UPLOAD = new BooleanParam(true);
        public static final BooleanParam DONT_SORT_ON_UPLOAD = new BooleanParam(false);
    }
}
