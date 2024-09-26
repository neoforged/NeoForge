/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import java.util.List;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.flag.Flag;
import net.neoforged.neoforge.flag.RequiredFlagsCondition;

public interface IConditionBuilder {
    default ICondition and(ICondition... values) {
        return new AndCondition(List.of(values));
    }

    default ICondition FALSE() {
        return FalseCondition.INSTANCE;
    }

    default ICondition TRUE() {
        return TrueCondition.INSTANCE;
    }

    default ICondition not(ICondition value) {
        return new NotCondition(value);
    }

    default ICondition or(ICondition... values) {
        return new OrCondition(List.of(values));
    }

    default ICondition itemExists(String namespace, String path) {
        return new ItemExistsCondition(namespace, path);
    }

    default ICondition modLoaded(String modid) {
        return new ModLoadedCondition(modid);
    }

    default ICondition tagEmpty(TagKey<Item> tag) {
        return new TagEmptyCondition(tag.location());
    }

    default ICondition requiredFlags(Flag... requiredFlags) {
        return new RequiredFlagsCondition(requiredFlags);
    }
}
