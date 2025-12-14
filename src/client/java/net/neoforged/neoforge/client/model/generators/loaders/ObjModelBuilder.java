/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.loaders;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jspecify.annotations.Nullable;

public class ObjModelBuilder extends CustomLoaderBuilder {
    @Nullable
    private Identifier modelLocation;
    @Nullable
    private Boolean automaticCulling;
    @Nullable
    private Boolean shadeQuads;
    @Nullable
    private Boolean flipV;
    @Nullable
    private Boolean emissiveAmbient;
    @Nullable
    private Identifier mtlOverride;

    public ObjModelBuilder() {
        super(Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "obj"), false);
    }

    public ObjModelBuilder modelLocation(Identifier modelLocation) {
        Preconditions.checkNotNull(modelLocation, "modelLocation must not be null");
        this.modelLocation = modelLocation;
        return this;
    }

    public ObjModelBuilder automaticCulling(boolean automaticCulling) {
        this.automaticCulling = automaticCulling;
        return this;
    }

    public ObjModelBuilder shadeQuads(boolean shadeQuads) {
        this.shadeQuads = shadeQuads;
        return this;
    }

    public ObjModelBuilder flipV(boolean flipV) {
        this.flipV = flipV;
        return this;
    }

    public ObjModelBuilder emissiveAmbient(boolean ambientEmissive) {
        this.emissiveAmbient = ambientEmissive;
        return this;
    }

    public ObjModelBuilder overrideMaterialLibrary(Identifier mtlOverride) {
        Preconditions.checkNotNull(mtlOverride, "mtlOverride must not be null");
        this.mtlOverride = mtlOverride;
        return this;
    }

    @Override
    protected CustomLoaderBuilder copyInternal() {
        ObjModelBuilder builder = new ObjModelBuilder();
        builder.modelLocation = this.modelLocation;
        builder.automaticCulling = this.automaticCulling;
        builder.shadeQuads = this.shadeQuads;
        builder.flipV = this.flipV;
        builder.emissiveAmbient = this.emissiveAmbient;
        builder.mtlOverride = this.mtlOverride;
        return builder;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);

        Preconditions.checkNotNull(modelLocation, "modelLocation must not be null");

        json.addProperty("model", modelLocation.toString());

        if (automaticCulling != null)
            json.addProperty("automatic_culling", automaticCulling);

        if (shadeQuads != null)
            json.addProperty("shade_quads", shadeQuads);

        if (flipV != null)
            json.addProperty("flip_v", flipV);

        if (emissiveAmbient != null)
            json.addProperty("emissive_ambient", emissiveAmbient);

        if (mtlOverride != null)
            json.addProperty("mtl_override", mtlOverride.toString());

        return json;
    }
}
