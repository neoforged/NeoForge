/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.buffer.param.IBufferDefinitionParam;
import net.neoforged.neoforge.client.buffer.param.IBufferDefinitionParamType;
import net.neoforged.neoforge.client.buffer.param.general.IGeneralUsageParam;
import net.neoforged.neoforge.client.event.RegisterBufferDefinitionParamTypeAliasesEvent;

public interface IBufferDefinitionBuilder {
    /**
     * Add a {@link IBufferDefinitionParam} to the {@link IBufferDefinition} with a specified {@link IBufferDefinitionParamType}.
     * Can be used for {@link IGeneralUsageParam}.
     * 
     * @param type  the param type
     * @param param the param value
     * @return the builder itself
     */
    IBufferDefinitionBuilder withParam(IBufferDefinitionParamType<?, ?> type, IBufferDefinitionParam<?> param);

    /**
     * Add a {@link IBufferDefinitionParam} to the {@link IBufferDefinition} with a param type described in a {@link ResourceLocation}.
     * It can specify param type without accessing the {@link IBufferDefinitionParamType} instance. (e.g. provided by other mods)
     * Generally used for {@link IGeneralUsageParam}.
     * 
     * @param resourceLocation the registered location of the param type
     * @param param            the param value
     * @return the builder itself
     * @see RegisterBufferDefinitionParamTypeAliasesEvent
     */
    IBufferDefinitionBuilder withParam(ResourceLocation resourceLocation, IBufferDefinitionParam<?> param);

    /**
     * Add a {@link IBufferDefinitionParam} to the {@link IBufferDefinition} with {@link IBufferDefinitionParamType} automatically determined by {@link IBufferDefinitionParam#getType()}.
     * <STRONG>CANNOT</STRONG> be used for {@link IGeneralUsageParam}.
     * 
     * @param param the param value
     * @return the builder itself
     * @see IGeneralUsageParam#getType()
     */
    IBufferDefinitionBuilder withParam(IBufferDefinitionParam<?> param);

    /**
     * Build the {@link IBufferDefinition} with params declared.
     * 
     * @return the built buffer definition
     */
    IBufferDefinition build();
}
