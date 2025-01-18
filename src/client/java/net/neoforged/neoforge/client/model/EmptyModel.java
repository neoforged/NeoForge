/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.textures.UnitTextureAtlasSprite;

/**
 * A completely empty model with no quads or texture dependencies.
 * <p>
 * You can access it as a {@link BakedModel}.
 */
public class EmptyModel implements UnbakedModel, JsonDeserializer<EmptyModel> {
    public static final BakedModel BAKED = new Baked();
    public static final EmptyModel INSTANCE = new EmptyModel();
    public static final UnbakedModelLoader<EmptyModel> LOADER = (object, context) -> INSTANCE;

    private EmptyModel() {}

    @Override
    public BakedModel bake(TextureSlots p_386641_, ModelBaker p_250133_, ModelState p_119536_, boolean p_387129_, boolean p_388638_, ItemTransforms p_386911_) {
        return BAKED;
    }

    @Override
    public void resolveDependencies(Resolver p_387087_) {}

    @Override
    public EmptyModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return INSTANCE;
    }

    private static class Baked extends SimpleBakedModel {
        private static final Material MISSING_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation());

        // SimpleBakedModel must have a quad list per face in its map.
        private static Map<Direction, List<BakedQuad>> makeEmptyCulledFaces() {
            Map<Direction, List<BakedQuad>> map = new EnumMap<>(Direction.class);
            for (Direction direction : Direction.values()) {
                map.put(direction, List.of());
            }
            return map;
        }

        public Baked() {
            super(List.of(), makeEmptyCulledFaces(), false, false, false, UnitTextureAtlasSprite.INSTANCE, ItemTransforms.NO_TRANSFORMS, RenderTypeGroup.EMPTY);
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return MISSING_TEXTURE.sprite();
        }
    }
}
