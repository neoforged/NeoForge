/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Manages individual components rendered as part of {@linkplain Gui the hud}, allowing
 * modders to intercept rendering of these components and add their own in-between.
 *
 * <p>Overlays can be registered using the {@link RegisterGuiLayersEvent} event.
 */
@ApiStatus.Internal
public class GuiLayerManager {
    private final List<NamedLayer> layers = new ArrayList<>();
    private boolean initialized = false;

    public record NamedLayer(ResourceLocation name, GuiLayer layer) {}

    public GuiLayerManager add(ResourceLocation name, GuiLayer layer) {
        this.layers.add(new NamedLayer(name, layer));
        return this;
    }

    public GuiLayerManager add(ResourceLocation name, Consumer<GuiGraphics> layer, BooleanSupplier shouldRender) {
        add(name, (guiGraphics, deltaTracker) -> layer.accept(guiGraphics), shouldRender);
        return this;
    }

    public GuiLayerManager add(ResourceLocation name, GuiLayer layer, BooleanSupplier shouldRender) {
        this.layers.add(new NamedLayer(name, (guiGraphics, deltaTracker) -> {
            if (shouldRender.getAsBoolean()) {
                layer.render(guiGraphics, deltaTracker);
            }
        }));
        return this;
    }

    public GuiLayerManager add(GuiLayerManager child, BooleanSupplier shouldRender) {
        // Flatten the layers to allow mods to insert layers between vanilla layers.
        for (var entry : child.layers) {
            add(entry.name(), entry.layer, shouldRender);
        }
        return this;
    }

    public void render(GuiGraphics guiGraphics, DeltaTracker partialTick) {
        if (NeoForge.EVENT_BUS.post(new RenderGuiEvent.Pre(guiGraphics, partialTick)).isCanceled()) {
            return;
        }

        renderInner(guiGraphics, partialTick);

        NeoForge.EVENT_BUS.post(new RenderGuiEvent.Post(guiGraphics, partialTick));
    }

    private void renderInner(GuiGraphics guiGraphics, DeltaTracker partialTick) {
        for (var layer : this.layers) {
            if (!NeoForge.EVENT_BUS.post(new RenderGuiLayerEvent.Pre(guiGraphics, partialTick, layer.name(), layer.layer())).isCanceled()) {
                layer.layer().render(guiGraphics, partialTick);
                NeoForge.EVENT_BUS.post(new RenderGuiLayerEvent.Post(guiGraphics, partialTick, layer.name(), layer.layer()));
            }
        }
    }

    public void initModdedLayers() {
        if (initialized) {
            throw new IllegalStateException("Duplicate initialization of NamedLayeredDraw");
        }
        initialized = true;
        ModLoader.postEvent(new RegisterGuiLayersEvent(this.layers));
    }

    public int getLayerCount() {
        return this.layers.size();
    }
}
