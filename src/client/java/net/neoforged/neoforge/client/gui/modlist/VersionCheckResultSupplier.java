/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.modlist;

import net.neoforged.fml.VersionChecker;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
@ApiStatus.Internal
public interface VersionCheckResultSupplier {
    VersionChecker.@Nullable CheckResult get(String modId);
}
