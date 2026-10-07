/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.conditions.ICondition;

public interface IAdvancementBuilderExtension {
    private Advancement.Builder self() {
        return (Advancement.Builder) this;
    }

    /// Saves this builder with the given id.
    ///
    /// @param output a [BootstrapContext] which saves any advancements provided
    /// @param id     the [Identifier] id for the new advancement
    /// @return the built advancement
    /// @throws IllegalStateException if the parent of the advancement is not known
    default AdvancementHolder save(BootstrapContext<Advancement> output, Identifier id) {
        return save(output, id, new ICondition[0]);
    }

    /// Saves this builder with the given id.
    ///
    /// @param output a [BootstrapContext] which saves any advancements provided
    /// @param id     the [Identifier] id for the new advancement
    /// @return the built advancement
    /// @throws IllegalStateException if the parent of the advancement is not known
    default AdvancementHolder save(BootstrapContext<Advancement> output, Identifier id, ICondition... conditions) {
        AdvancementHolder advancementholder = self().build(id);
        advancementholder.register(output, conditions);
        return advancementholder;
    }
}
