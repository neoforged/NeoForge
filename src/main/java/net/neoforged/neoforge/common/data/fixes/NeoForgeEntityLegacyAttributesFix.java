/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.fixes;

import com.mojang.datafixers.*;
import com.mojang.datafixers.schemas.Schema;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mojang.datafixers.types.Type;
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
        Type<?> itemStackType = schema.getType(References.ITEM_STACK);
        OpticFinder<?> itemStackTag = itemStackType.findField("tag");
        return TypeRewriteRule.seq(
                this.fixTypeEverywhereTyped(this.name + " (ItemStack)", itemStackType, typed -> typed.updateTyped(itemStackTag, this::fixItemStackTag)),
                this.fixTypeEverywhereTyped(name + " (Entity)", schema.getType(References.ENTITY), this::removeLegacyAttributes),
                this.fixTypeEverywhereTyped(name + " (Player)", schema.getType(References.PLAYER), this::removeLegacyAttributes));
    }

    private Typed<?> fixItemStackTag(Typed<?> typed) {
        return typed.update(
                DSL.remainderFinder(),
                tagDynamic -> tagDynamic.update(
                        "AttributeModifiers",
                        modifiersDynamic -> DataFixUtils.orElse(
                                modifiersDynamic.asStreamOpt()
                                        .result()
                                        .map(dynamics -> dynamics.filter(dynamic -> dynamic.get("AttributeName").asString().result().map(str -> !legacyAttributes.contains(str)).orElse(true)))
                                        .map(modifiersDynamic::createList),
                                modifiersDynamic
                        )
                )
        );
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
