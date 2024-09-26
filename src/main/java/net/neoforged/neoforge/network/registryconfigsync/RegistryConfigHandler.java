/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registryconfigsync;

import com.google.gson.JsonElement;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * A handler to:
 * <li>Collect registry-affecting configs on server and parse them to json.</li>
 * <li>Verify if received configs are compatible with the client config. If not, explain the difference.</li>
 * <li>Overwrite local configs with received server configs.</li>
 * This allows mods to have configs that affect the way items and blocks are registered,
 * and inform client when those configs are different, preventing misleading registry conflict information.
 */
public interface RegistryConfigHandler {
    /**
     * Called when players are connecting to server.
     * Collect all config values (in any form) that could affect static registry contents and encode them to json.
     */
    JsonElement serializeConfig();

    /**
     * Called only when client attempts to join a server. Receive server config produced by {@link RegistryConfigHandler#serializeConfig}.
     * 
     * @param value the server side config received
     * @return null when the server side config is compatible with currently loaded client side config.<br>
     *         When local config is not compatible with server config,
     *         return a brief explanation about what is different and what would happen if local config is replaced by the server side config.
     *         It will be displayed on {@link net.neoforged.neoforge.client.gui.RegistryConfigMismatchScreen}
     */
    @Nullable
    Component verifyConfig(JsonElement value);

    /**
     * Overwrite local config with received server side config
     * 
     * @param value the server side config received
     */
    void applyConfig(JsonElement value);
}
