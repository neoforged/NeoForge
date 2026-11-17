/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import net.minecraft.data.recipes.RecipeOutput;

/// A [RecipeOutput] scoped to a specific context, used within a
/// `try`-with-resources block.
public interface ScopedRecipeOutput extends RecipeOutput, AutoCloseable {
    @Override
    void close();
}
