/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.group;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.impl.TestFrameworkImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Group implements Groupable {
    private final String id;
    private final List<Groupable> entries;
    private Component title;
    private boolean enabledByDefault;

    public Group(String id, List<Groupable> entries) {
        this.id = id;
        this.entries = entries;
        this.title = getDefaultTitle();
    }

    public String id() {
        return this.id;
    }

    public List<Groupable> entries() {
        return entries;
    }

    public boolean isEnabledByDefault() {
        return this.enabledByDefault;
    }

    public void setEnabledByDefault(boolean enabledByDefault) {
        this.enabledByDefault = enabledByDefault;
    }

    public Component title() {
        return this.title;
    }

    public void setTitle(@Nullable Component title) {
        this.title = title == null ? getDefaultTitle() : title;
    }

    public Component getDefaultTitle() {
        return Component.literal(TestFrameworkImpl.capitaliseWords(id(), "\\."));
    }

    @Override
    public @NotNull Stream<Test> resolveAsStream() {
        return entries.stream().flatMap(gr -> gr.resolveAll().stream());
    }

    public void add(Groupable entry) {
        this.entries.add(entry);
    }
}
