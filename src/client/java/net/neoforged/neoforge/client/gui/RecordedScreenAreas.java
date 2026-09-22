/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.ApiStatus;

/// Records the areas a UI element occupies while it renders, and stamps them with the frame they
/// were recorded in, so that the areas of an element that is not rendered anymore are dropped.
///
/// Recording elements must [clear][#clear()] the recorder when they start rendering a frame.
@ApiStatus.Internal
public class RecordedScreenAreas {
    private final List<ScreenRectangle> areas = new ArrayList<>();
    private long frame = -1;

    /// Records an area occupied by the UI element.
    ///
    /// @param area the area, in GUI-scaled screen coordinates
    public void record(ScreenRectangle area) {
        this.areas.add(area);
        this.frame = ScreenAreaManager.frameIndex();
    }

    /// Clears the recorded areas.
    public void clear() {
        this.areas.clear();
    }

    /// {@return the areas recorded during the current or previous frame, or an empty list if
    /// nothing was recorded during those frames}
    ///
    /// The areas of the previous frame remain available so that elements rendered earlier in a
    /// frame can see the areas of the elements rendered after them.
    public List<ScreenRectangle> areas() {
        if (this.frame < ScreenAreaManager.frameIndex() - 1) {
            return List.of();
        }
        return List.copyOf(this.areas);
    }
}
