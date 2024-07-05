/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public interface IAdvancementBuilderExtension {
    private Advancement.Builder self() {
        return (Advancement.Builder) this;
    }

    /**
     * Saves this builder with the given id using the {@link ExistingFileHelper} to check if the parent is already known.
     *
     * @param saver      a {@link Consumer} which saves any advancements provided
     * @param id         the {@link ResourceLocation} id for the new advancement
     * @param fileHelper the {@link ExistingFileHelper} where all known advancements are registered
     * @return the built advancement
     * @throws IllegalStateException if the parent of the advancement is not known
     */
    default AdvancementHolder save(Consumer<AdvancementHolder> saver, ResourceLocation id, ExistingFileHelper fileHelper) {
        AdvancementHolder advancementholder = self().build(id);

        Optional<ResourceLocation> parent = advancementholder.value().parent();
        if (parent.isPresent() && !fileHelper.exists(parent.get(), PackType.SERVER_DATA, ".json", "advancement")) {
            throw new IllegalStateException("The parent: '%s' of advancement '%s', has not been saved yet!".formatted(
                    parent.orElseThrow(),
                    id));
        }

        saver.accept(advancementholder);
        fileHelper.trackGenerated(id, PackType.SERVER_DATA, ".json", "advancement");
        return advancementholder;
    }
}
