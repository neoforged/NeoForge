/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.data;

import java.util.BitSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class MultipartModelData {
    private static final ModelProperty<Map<BakedModel, ModelData>> PROPERTY = new ModelProperty<>();

    /**
     * Helper to get the data from a {@link ModelData} instance.
     *
     * @param modelData The object to get data from
     * @param model     The model to get data for
     * @return The data for the part, or the one passed in if not found
     */
    public static ModelData resolve(ModelData modelData, BakedModel model) {
        var multipartData = modelData.get(PROPERTY);
        if (multipartData == null)
            return modelData;
        var partData = multipartData.get(model);
        return partData != null ? partData : modelData;
    }

    public static ModelData create(List<MultiPartBakedModel.Selector> selectors, BitSet bitset, BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData tileModelData) {
        // Don't allocate memory if no submodel changes the model data
        Map<BakedModel, ModelData> dataMap = null;

        for (int i = 0; i < bitset.length(); ++i) {
            if (bitset.get(i)) {
                var model = selectors.get(i).model();
                var data = model.getModelData(level, pos, state, tileModelData);

                if (data != tileModelData) {
                    if (dataMap == null)
                        dataMap = new IdentityHashMap<>();

                    dataMap.put(model, data);
                }
            }
        }

        return dataMap == null ? tileModelData : tileModelData.derive().with(PROPERTY, dataMap).build();
    }
}
