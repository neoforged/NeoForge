/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.assertj.core.api.Assertions.assertThat;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.junit.jupiter.api.Test;

class KeyMappingOrderTest {
    @Test
    void sortsModdedMappingsByOrderThenNameWithinCategory() {
        var category = KeyMapping.Category.MISC;
        var context = KeyConflictContext.GUI;
        var key = InputConstants.UNKNOWN;
        var type = key.getType();
        int code = key.getValue();
        String prefix = "key.neoforge.unittest.order.";

        // Names sort in the opposite direction to the explicit orders.
        var first = new KeyMapping(prefix + "z", context, type, code, category, -2);
        var second = new KeyMapping(prefix + "y", context, key, category, -1);
        var defaultByType = new KeyMapping(prefix + "m1", context, type, code, category);
        var defaultByKey = new KeyMapping(prefix + "m2", context, key, category);
        var defaultWithModifierByType = new KeyMapping(prefix + "m3", context, KeyModifier.SHIFT, type, code, category);
        var defaultWithModifierByKey = new KeyMapping(prefix + "m4", context, KeyModifier.SHIFT, key, category);
        var next = new KeyMapping(prefix + "b", context, KeyModifier.SHIFT, type, code, category, 1);
        var last = new KeyMapping(prefix + "a", context, KeyModifier.SHIFT, key, category, 2);

        var mappings = List.of(last, next, defaultWithModifierByKey, defaultWithModifierByType, defaultByKey, defaultByType, second, first);
        assertThat(mappings.stream().sorted()).containsExactly(first, second, defaultByType, defaultByKey, defaultWithModifierByType, defaultWithModifierByKey, next, last);
        assertThat(last.getKeyConflictContext()).isSameAs(context);
        assertThat(last.getKeyModifier()).isEqualTo(KeyModifier.SHIFT);
    }
}
