/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.energy;

import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public final class SimpleEnergyContainer extends EnergyContainer {
    private SimpleEnergyContainer(int[] energyValues, int capacity, int maxInsertRate, int maxExtractRate, @Nullable Runnable updateCallback) {
        super(energyValues, capacity, maxInsertRate, maxExtractRate, updateCallback);
    }

    public static Builder builder(int size, int capacity) {
        return new Builder().size(size).capacity(capacity).maxInsertRate(Mth.ceil(capacity * 0.01f)).maxExtractRate(capacity);
    }

    public static Builder from(int[] values, int capacity) {
        return new Builder().from(values).capacity(capacity).maxInsertRate(Mth.ceil(capacity * 0.01f)).maxExtractRate(capacity);
    }

    public static class Builder extends EnergyContainer.Builder<Builder> {}
}
