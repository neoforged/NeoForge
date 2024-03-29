/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.gui.GuiLayerManager;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Allows users to register custom {@link LayeredDraw.Layer layers} for GUI rendering.
 *
 * <p>See also {@link RenderGuiLayerEvent} to intercept rendering of registered layers.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterGuiLayersEvent extends Event implements IModBusEvent {
    private final List<GuiLayerManager.NamedLayer> layers;

    @ApiStatus.Internal
    public RegisterGuiLayersEvent(List<GuiLayerManager.NamedLayer> layers) {
        this.layers = layers;
    }

    /**
     * Registers a layer that renders below all others.
     *
     * @param id    A unique resource id for this layer
     * @param layer The layer
     */
    public void registerBelowAll(ResourceLocation id, LayeredDraw.Layer layer) {
        register(Ordering.BEFORE, null, id, layer);
    }

    /**
     * Registers a layer that renders below another.
     *
     * @param other The id of the layer to render below. This must be a layer you have already registered or one of the
     *              {@link VanillaGuiLayers vanilla layers}. Do not use other mods' layers.
     * @param id    A unique resource id for this layer
     * @param layer The layer
     */
    public void registerBelow(ResourceLocation other, ResourceLocation id, LayeredDraw.Layer layer) {
        register(Ordering.BEFORE, other, id, layer);
    }

    /**
     * Registers an layer that renders above another.
     *
     * @param other The id of the layer to render above. This must be a layer you have already registered or one of the
     *              {@link VanillaGuiLayers vanilla layers}. Do not use other mods' layers.
     * @param id    A unique resource id for this layer
     * @param layer The layer
     */
    public void registerAbove(ResourceLocation other, ResourceLocation id, LayeredDraw.Layer layer) {
        register(Ordering.AFTER, other, id, layer);
    }

    /**
     * Registers a layer that renders above all others.
     *
     * @param id    A unique resource id for this layer
     * @param layer The layer
     */
    public void registerAboveAll(ResourceLocation id, LayeredDraw.Layer layer) {
        register(Ordering.AFTER, null, id, layer);
    }

    private void register(Ordering ordering, @Nullable ResourceLocation other, ResourceLocation key, LayeredDraw.Layer layer) {
        Objects.requireNonNull(key);
        for (var namedLayer : layers) {
            Preconditions.checkArgument(!namedLayer.name().equals(key), "Layer already registered: " + key);
        }

        int insertPosition;
        if (other == null) {
            insertPosition = ordering == Ordering.BEFORE ? 0 : layers.size();
        } else {
            var otherIndex = IntStream.range(0, layers.size())
                    .filter(i -> layers.get(i).name().equals(other))
                    .findFirst();
            if (otherIndex.isEmpty()) {
                throw new IllegalArgumentException("Attempted to order against an unregistered layer " + other + ". Only order against vanilla's and your own.");
            }

            insertPosition = otherIndex.getAsInt() + (ordering == Ordering.BEFORE ? 0 : 1);
        }

        layers.add(insertPosition, new GuiLayerManager.NamedLayer(key, layer));
    }

    private enum Ordering {
        BEFORE, AFTER
    }
}
