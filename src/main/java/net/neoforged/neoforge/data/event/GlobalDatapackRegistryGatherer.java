/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data.event;

// TODO-ASH: Document
public interface GlobalDatapackRegistryGatherer extends DatapackRegistryGatherer {
    DatapackRegistryGatherer generateFor(String modId, String... modIds);
}
