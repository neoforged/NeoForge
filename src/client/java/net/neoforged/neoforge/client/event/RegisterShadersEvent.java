/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.List;
import net.minecraft.client.renderer.ShaderProgram;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired to allow mods to register custom {@linkplain ShaderProgram shaders}.
 * This event is fired after the default Minecraft shaders have been registered.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterShadersEvent extends Event implements IModBusEvent {
    private final List<ShaderProgram> shaderList;

    @ApiStatus.Internal
    public RegisterShadersEvent(List<ShaderProgram> shaderList) {
        this.shaderList = shaderList;
    }

    /**
     * Registers a {@link ShaderProgram}
     *
     * @param program a shader
     */
    public void registerShader(ShaderProgram program) {
        shaderList.add(program);
    }
}
