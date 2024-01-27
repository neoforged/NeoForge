/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.gui.overlay.IGuiOverlay;
import net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Allows users to register custom {@link IGuiOverlay GUI overlays}.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterGuiOverlaysEvent extends Event implements IModBusEvent {
    private final Map<ResourceLocation, IGuiOverlay> overlays;
    private final List<ResourceLocation> orderedOverlays;

    @ApiStatus.Internal
    public RegisterGuiOverlaysEvent(Map<ResourceLocation, IGuiOverlay> overlays, List<ResourceLocation> orderedOverlays) {
        this.overlays = overlays;
        this.orderedOverlays = orderedOverlays;
    }

    /**
     * Registers an overlay that renders below all others.
     *
     * @param id      A unique resource id for this overlay
     * @param overlay The overlay
     * @deprecated Use {@link #registerBelowAll(ResourceLocation, IGuiOverlay) the RL-explicit variant} instead; mod ID inference will be removed in a later update, alongside the move of registration events to the NeoForge main bus
     */
    @Deprecated(forRemoval = true, since = "1.20.2")
    public void registerBelowAll(String id, IGuiOverlay overlay) {
        registerBelowAll(new ResourceLocation(id, ModLoadingContext.get().getActiveNamespace()), overlay);
    }

    /**
     * Registers an overlay that renders below all others.
     *
     * @param id      A unique resource id for this overlay
     * @param overlay The overlay
     */
    public void registerBelowAll(ResourceLocation id, IGuiOverlay overlay) {
        register(Ordering.BEFORE, null, id, overlay);
    }

    /**
     * Registers an overlay that renders below another.
     *
     * @param other   The id of the overlay to render below. This must be an overlay you have already registered or a
     *                {@link VanillaGuiOverlay vanilla overlay}. Do not use other mods' overlays.
     * @param id      A unique resource id for this overlay
     * @param overlay The overlay
     * @deprecated Use {@link #registerBelow(ResourceLocation, ResourceLocation, IGuiOverlay) the RL-explicit variant} instead; mod ID inference will be removed in a later update, alongside the move of registration events to the NeoForge main bus
     */
    @Deprecated(forRemoval = true, since = "1.20.2")
    public void registerBelow(ResourceLocation other, String id, IGuiOverlay overlay) {
        registerBelow(other, new ResourceLocation(ModLoadingContext.get().getActiveNamespace(), id), overlay);
    }

    /**
     * Registers an overlay that renders below another.
     *
     * @param other   The id of the overlay to render below. This must be an overlay you have already registered or a
     *                {@link VanillaGuiOverlay vanilla overlay}. Do not use other mods' overlays.
     * @param id      A unique resource id for this overlay
     * @param overlay The overlay
     */
    public void registerBelow(ResourceLocation other, ResourceLocation id, IGuiOverlay overlay) {
        register(Ordering.BEFORE, other, id, overlay);
    }

    /**
     * Registers an overlay that renders above another.
     *
     * @param other   The id of the overlay to render above. This must be an overlay you have already registered or a
     *                {@link VanillaGuiOverlay vanilla overlay}. Do not use other mods' overlays.
     * @param id      A unique resource id for this overlay
     * @param overlay The overlay
     * @deprecated Use {@link #registerAbove(ResourceLocation, ResourceLocation, IGuiOverlay) the RL-explicit variant} instead; mod ID inference will be removed in a later update, alongside the move of registration events to the NeoForge main bus
     */
    @Deprecated(forRemoval = true, since = "1.20.2")
    public void registerAbove(ResourceLocation other, String id, IGuiOverlay overlay) {
        registerAbove(other, new ResourceLocation(ModLoadingContext.get().getActiveNamespace(), id), overlay);
    }

    /**
     * Registers an overlay that renders above another.
     *
     * @param other   The id of the overlay to render above. This must be an overlay you have already registered or a
     *                {@link VanillaGuiOverlay vanilla overlay}. Do not use other mods' overlays.
     * @param id      A unique resource id for this overlay
     * @param overlay The overlay
     */
    public void registerAbove(ResourceLocation other, ResourceLocation id, IGuiOverlay overlay) {
        register(Ordering.AFTER, other, id, overlay);
    }

    /**
     * Registers an overlay that renders above all others.
     *
     * @param id      A unique resource id for this overlay
     * @param overlay The overlay
     * @deprecated Use {@link #registerAboveAll(ResourceLocation, IGuiOverlay) the RL-explicit variant} instead; mod ID inference will be removed in a later update, alongside the move of registration events to the NeoForge main bus
     */
    @Deprecated(forRemoval = true, since = "1.20.2")
    public void registerAboveAll(String id, IGuiOverlay overlay) {
        registerAboveAll(new ResourceLocation(ModLoadingContext.get().getActiveNamespace(), id), overlay);
    }

    /**
     * Registers an overlay that renders above all others.
     *
     * @param id      A unique resource id for this overlay
     * @param overlay The overlay
     */
    public void registerAboveAll(ResourceLocation id, IGuiOverlay overlay) {
        register(Ordering.AFTER, null, id, overlay);
    }

    private void register(Ordering ordering, @Nullable ResourceLocation other, ResourceLocation key, IGuiOverlay overlay) {
        Preconditions.checkArgument(!overlays.containsKey(key), "Overlay already registered: " + key);

        int insertPosition;
        if (other == null) {
            insertPosition = ordering == Ordering.BEFORE ? 0 : overlays.size();
        } else {
            int otherIndex = orderedOverlays.indexOf(other);
            Preconditions.checkState(otherIndex >= 0, "Attempted to order against an unregistered overlay. Only order against vanilla's and your own.");
            insertPosition = otherIndex + (ordering == Ordering.BEFORE ? 0 : 1);
        }

        overlays.put(key, overlay);
        orderedOverlays.add(insertPosition, key);
    }

    private enum Ordering {
        BEFORE, AFTER
    }
}
