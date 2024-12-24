/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;

public interface IAdvancementBuilderExtension {
    private Advancement.Builder self() {
        return (Advancement.Builder) this;
    }

    /**
     * Saves this builder with the given id.
     *
     * @param saver a {@link Consumer} which saves any advancements provided
     * @param id    the {@link ResourceLocation} id for the new advancement
     * @return the built advancement
     * @throws IllegalStateException if the parent of the advancement is not known
     */
    default AdvancementHolder save(Consumer<AdvancementHolder> saver, ResourceLocation id) {
        AdvancementHolder advancementholder = self().build(id);
        saver.accept(advancementholder);
        return advancementholder;
    }
}
