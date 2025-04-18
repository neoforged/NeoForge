/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockGetterExtension;
import net.neoforged.neoforge.common.extensions.IBlockStateExtension;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface BlockStateModelExtension {
    private BlockStateModel self() {
        return (BlockStateModel) this;
    }

    /**
     * Collects all the data used by the model to
     * {@linkplain #collectParts(BlockAndTintGetter, BlockPos, BlockState, RandomSource, List) produce renderable geometry}.
     * The returned object encapsulates which parts of the world state the model depends on.
     *
     * <p>This allows the geometry produced previously by a model to be reused, provided that the key matches.
     * <b>The key can be used to compare the geometry of different models.</b>
     * If this model forwards to a single other model, it can directly return its geometry key.
     * Otherwise, it should use a custom type specific to the model.
     *
     * <p>The passed in {@code level}, {@code pos} and {@code random} parameters should not be
     * put directly into the returned key. Only the relevant information should be extracted
     * so that the same key is yielded as often as possible.
     *
     * <p>The default implementation returns {@code null}, meaning the model does not implement this method.
     * A model that wishes to override this method must therefore not return null,
     * even if the model does not use any passed in state.
     *
     * @return an object collecting all the data that influences the geometry of this model;
     *         can be any object as long as it implements {@link Object#hashCode()} and {@link Object#equals(Object)} correctly.
     */
    @Nullable
    default Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return null;
    }

    /**
     * Collects the parts of the model that should be rendered.
     *
     * <p>Typically called on a meshing worker thread, with a snapshot of world state.
     * To access block entity data, use {@link IBlockGetterExtension#getModelData(BlockPos)} on the passed level.
     * Avoid accessing or manipulating the block entities directly as they are not thread safe.
     * Other world state is safe to access.
     *
     * <p>The parameters passed to this method might not be what the model expects.
     * For example, the model data might be missing or coming from a different/outdated block entity,
     * the block state in the level might be different from the one passed in, and so on...
     * The model should handle these cases gracefully and return the best model it can
     * (e.g. by returning a completely unconnected model, in the case of connected textures).
     *
     * <p>Calling {@link IBlockStateExtension#getAppearance} before accessing the block state is recommended.
     *
     * @param level  a level to query block entity data or other world state
     * @param pos    the position of the block being rendered
     * @param state  the state of the block being rendered
     * @param random a random source for random model variations
     * @param parts  the list that should receive all parts to be rendered
     */
    default void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        self().collectParts(random, parts);
    }

    /**
     * Helper to collects the parts of the model into a new list.
     */
    @ApiStatus.NonExtendable
    default List<BlockModelPart> collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        List<BlockModelPart> parts = new ObjectArrayList<>();
        this.collectParts(level, pos, state, random, parts);
        return parts;
    }

    /**
     * Returns the particle icon.
     *
     * <p>Block entity data can be accessed using {@link IBlockGetterExtension#getModelData(BlockPos)}.
     */
    default TextureAtlasSprite particleIcon(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return self().particleIcon();
    }
}
