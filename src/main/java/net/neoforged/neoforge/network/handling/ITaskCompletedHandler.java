/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import net.minecraft.server.network.ConfigurationTask;

/**
 * Handler which is called when a task is completed.
 */
@FunctionalInterface
public interface ITaskCompletedHandler {
    /**
     * Called when a task is completed.
     *
     * @param type The type of task that was completed.
     */
    void onTaskCompleted(ConfigurationTask.Type type);
}
