/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.buffer.param.state.TextureState;

public record TextureParam(List<TextureState> states) implements IBufferDefinitionParam<List<TextureState>> {

    public TextureParam(ResourceLocation texture, boolean blur, boolean mipmap) {
        this(new TextureState(texture, blur, mipmap));
    }

    public TextureParam(TextureState state) {
        this(List.of(state));
    }

    public TextureParam() {
        this(List.of());
    }

    @Override
    public List<TextureState> getValue() {
        return states;
    }

    @Override
    public IBufferDefinitionParamType<?, ?> getType() {
        return BufferDefinitionParamTypeManager.TEXTURE;
    }
    public static final class Vanilla {
        public static final TextureParam EMPTY = new TextureParam();
        public static final TextureParam BLOCK = new TextureParam(InventoryMenu.BLOCK_ATLAS, false, false);
    }
}
