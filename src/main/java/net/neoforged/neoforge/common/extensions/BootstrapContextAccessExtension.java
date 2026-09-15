/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface BootstrapContextAccessExtension {
    default <S> Optional<HolderLookup<S>> holderLookup(ResourceKey<? extends Registry<? extends S>> registry) {
        return Optional.empty();
    }
}
