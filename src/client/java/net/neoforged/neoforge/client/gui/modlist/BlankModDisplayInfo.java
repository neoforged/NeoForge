/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.modlist;

import java.net.URI;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/// A blank [mod display info][ModDisplayInfo].
final class BlankModDisplayInfo implements ModDisplayInfo {
    public static final BlankModDisplayInfo INSTANCE = new BlankModDisplayInfo();

    private BlankModDisplayInfo() {}

    @Override
    public String id() {
        return "";
    }

    @Override
    public Component displayName() {
        return Component.empty();
    }

    @Override
    public String version() {
        return "";
    }

    @Override
    public Component authors() {
        return Component.empty();
    }

    @Override
    public Component credits() {
        return Component.empty();
    }

    @Override
    public Component description() {
        return Component.empty();
    }

    @Override
    public Component license() {
        return Component.empty();
    }

    @Override
    public @Nullable ImageResource banner() {
        return null;
    }

    @Override
    public @Nullable ImageResource icon() {
        return null;
    }

    @Override
    public @Nullable URI displayUrl() {
        return null;
    }

    @Override
    public @Nullable URI issuesUrl() {
        return null;
    }
}
