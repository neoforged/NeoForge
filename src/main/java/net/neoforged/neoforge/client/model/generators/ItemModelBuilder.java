/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Builder for item models, adds the ability to build overrides via
 * {@link #override()}.
 */
public class ItemModelBuilder extends ModelBuilder<ItemModelBuilder> {

    private static final List<ResourceKey<TrimMaterial>> VANILLA_TRIM_MATERIALS = List.of(TrimMaterials.QUARTZ, TrimMaterials.IRON, TrimMaterials.NETHERITE, TrimMaterials.REDSTONE, TrimMaterials.COPPER, TrimMaterials.GOLD, TrimMaterials.EMERALD, TrimMaterials.DIAMOND, TrimMaterials.LAPIS, TrimMaterials.AMETHYST);
    protected List<OverrideBuilder> overrides = new ArrayList<>();

    public ItemModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper) {
        super(outputLocation, existingFileHelper);
        //add vanilla armor trims to the existing file helper
        for (ResourceKey<TrimMaterial> trim : VANILLA_TRIM_MATERIALS) {
            this.existingFileHelper.trackGenerated(new ResourceLocation("trims/items/boots_trim_" + trim.location().getPath()), ModelProvider.TEXTURE);
            this.existingFileHelper.trackGenerated(new ResourceLocation("trims/items/chestplate_trim_" + trim.location().getPath()), ModelProvider.TEXTURE);
            this.existingFileHelper.trackGenerated(new ResourceLocation("trims/items/helmet_trim_" + trim.location().getPath()), ModelProvider.TEXTURE);
            this.existingFileHelper.trackGenerated(new ResourceLocation("trims/items/leggings_trim_" + trim.location().getPath()), ModelProvider.TEXTURE);
        }
    }

    public OverrideBuilder override() {
        OverrideBuilder ret = new OverrideBuilder();
        overrides.add(ret);
        return ret;
    }

    /**
     * Get an existing override builder
     * 
     * @param index the index of the existing override builder
     * @return the override builder
     * @throws IndexOutOfBoundsException if {@code} index is out of bounds
     */
    public OverrideBuilder override(int index) {
        Preconditions.checkElementIndex(index, overrides.size(), "override");
        return overrides.get(index);
    }

    @Override
    public JsonObject toJson() {
        JsonObject root = super.toJson();
        if (!overrides.isEmpty()) {
            JsonArray overridesJson = new JsonArray();
            overrides.stream().map(OverrideBuilder::toJson).forEach(overridesJson::add);
            root.add("overrides", overridesJson);
        }
        return root;
    }

    public class OverrideBuilder {

        private ModelFile model;
        private final Map<ResourceLocation, Float> predicates = new LinkedHashMap<>();

        public OverrideBuilder model(ModelFile model) {
            this.model = model;
            model.assertExistence();
            return this;
        }

        public OverrideBuilder predicate(ResourceLocation key, float value) {
            this.predicates.put(key, value);
            return this;
        }

        public ItemModelBuilder end() {
            return ItemModelBuilder.this;
        }

        JsonObject toJson() {
            JsonObject ret = new JsonObject();
            JsonObject predicatesJson = new JsonObject();
            predicates.forEach((key, val) -> predicatesJson.addProperty(key.toString(), val));
            ret.add("predicate", predicatesJson);
            ret.addProperty("model", model.getLocation().toString());
            return ret;
        }
    }

}
