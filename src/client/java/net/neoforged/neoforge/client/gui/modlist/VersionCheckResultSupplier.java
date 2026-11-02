/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.modlist;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.VersionChecker;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// Version check result supplier used by the mod list screen. For internal use only.
@FunctionalInterface
@ApiStatus.Internal
interface VersionCheckResultSupplier {
    VersionChecker.@Nullable CheckResult get(String modId);

    VersionCheckResultSupplier DEFAULT = modId -> ModList.get().getModContainerById(modId)
            .map(ModContainer::getModInfo)
            .map(VersionChecker::getResult)
            .filter(result -> result.status() == VersionChecker.Status.OUTDATED || result.status() == VersionChecker.Status.BETA_OUTDATED)
            .orElse(null);
}
