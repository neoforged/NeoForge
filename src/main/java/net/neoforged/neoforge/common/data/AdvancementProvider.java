/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.extensions.IAdvancementBuilderExtension;

/**
 * An extension of the vanilla {@code AdvancementProvider} to provide a feature-complete
 * experience to generate modded advancements.
 */
public class AdvancementProvider extends net.minecraft.data.advancements.AdvancementProvider {
    /**
     * Constructs an advancement provider using the generators to write the
     * advancements to a file.
     *
     * @param output       the target directory of the data generator
     * @param registries   a future of a lookup for registries and their objects
     * @param subProviders the generators used to create the advancements
     */
    public AdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, List<AdvancementGenerator> subProviders) {
        super(output, registries, subProviders.stream().map(AdvancementGenerator::toSubProvider).toList());
    }

    /**
     * An interface used to generated modded advancements. This is parallel to
     * vanilla's {@link AdvancementSubProvider}.
     *
     * @see AdvancementSubProvider
     */
    public interface AdvancementGenerator {
        /**
         * A method used to generate advancements for a mod. Advancements should be
         * built via {@link IAdvancementBuilderExtension#save(Consumer, ResourceLocation)}.
         *
         * @param registries a lookup for registries and their objects
         * @param saver      a consumer used to write advancements to a file
         */
        void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver);

        /**
         * Creates an {@link AdvancementSubProvider} from this generator.
         *
         * @return a sub provider wrapping this generator
         */
        default AdvancementSubProvider toSubProvider() {
            return this::generate;
        }
    }
}
