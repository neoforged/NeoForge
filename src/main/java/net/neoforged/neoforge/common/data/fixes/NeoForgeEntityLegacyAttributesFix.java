/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.util.datafix.fixes.References;

public class NeoForgeEntityLegacyAttributesFix extends DataFix {
    private final Set<String> legacyAttributes = new HashSet<>();
    private final String name;

    public NeoForgeEntityLegacyAttributesFix(String name, Schema outputSchema, List<String> attributesToRemove) {
        super(outputSchema, false);
        this.name = name;
        legacyAttributes.addAll(attributesToRemove);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Schema schema = this.getInputSchema();
        return TypeRewriteRule.seq(
                this.fixTypeEverywhereTyped(name + " (Entity)", schema.getType(References.ENTITY), this::removeLegacyAttributes),
                this.fixTypeEverywhereTyped(name + " (Player)", schema.getType(References.PLAYER), this::removeLegacyAttributes));
    }

    private Typed<?> removeLegacyAttributes(Typed<?> typed) {
        return typed.update(
                DSL.remainderFinder(),
                entityDynamic -> entityDynamic.update(
                        "Attributes",
                        attributesDynamic -> DataFixUtils.orElse(
                                attributesDynamic.asStreamOpt()
                                        .result()
                                        .map(dynamics -> dynamics.filter(dynamic -> dynamic.get("Name").asString().result().map(str -> !legacyAttributes.contains(str)).orElse(true)))
                                        .map(attributesDynamic::createList),
                                attributesDynamic)));
    }
}
