/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.standalone;

import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ResolvedModel;
import net.neoforged.neoforge.client.event.ModelEvent;

/**
 * Baker implementation for standalone models registered to {@link ModelEvent.RegisterStandalone}.
 *
 * <p>Depending on the context where it is used, different parts of a {@link ResolvedModel}s might be baked.
 * For example, block models query ambient occlusion, item models query transforms,
 * and both query baked geometry.
 * Each standalone model baker can therefore bake exactly the properties it needs,
 * and store them in an object of arbitrary type {@code T}.
 *
 * <p>The baked object can be retrieved later using {@link ModelManager#getStandaloneModel(StandaloneModelKey)}.
 *
 * @param <T> the type of the baked object, which contains some properties baked from the {@link ResolvedModel}
 * @see StandaloneModelKey
 */
@FunctionalInterface
public interface StandaloneModelBaker<T> {
    T bake(ResolvedModel model, ModelBaker baker);
}
