/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.obj.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.MatrixUtil;
import com.mojang.math.Transformation;
import java.io.IOException;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.AbstractUnbakedModel;
import net.neoforged.neoforge.client.model.NeoForgeModelProperties;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import net.neoforged.neoforge.client.model.obj.ObjLoader;
import net.neoforged.neoforge.client.model.obj.ObjModel;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.MutableQuad;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public class ObjModelLoader implements UnbakedModelLoader<ObjModelLoader.Unbaked> {
    private ObjModelLoader() {};

    public static ObjModelLoader INSTANCE = new ObjModelLoader();

    @Override
    public Unbaked read(final JsonObject jsonObject, final JsonDeserializationContext deserializationContext) throws JsonParseException {
        if (!jsonObject.has("model"))
            throw new JsonParseException("OBJ Loader requires a 'model' key that points to a valid .OBJ model.");

        var resourceMapper = getResourceMapper();
        var origin = Identifier.parse(jsonObject.get("model").getAsString());

        // TODO: a geometry cache may be necessary here
        return new Unbaked(
                () -> {
                    var resource = resourceMapper.apply(origin, origin.getPath());
                    return ObjLoader.load(origin, resource, resourceMapper);
                },
                StandardModelParameters.parse(jsonObject, deserializationContext));
    }

    private static ObjLoader.ResourceLocator getResourceMapper() {
        var manager = Minecraft.getInstance().getResourceManager();
        return (origin, name) -> {
            // Assume it's a resource location with an absolute location if possible.
            if (name.indexOf(Identifier.NAMESPACE_SEPARATOR) > 0) {
                return manager.getResourceOrThrow(Identifier.parse(name));
            }

            // Otherwise, attempt to load it as if it were a relative path
            var loc = origin.withPath(path -> path.substring(0, path.lastIndexOf('/') + 1) + name);
            var resource = manager.getResource(loc).orElse(null);
            if (resource == null) {
                // If that fails, attempt to load it as if it were an absolute path.
                loc = origin.withPath(name);
                resource = manager.getResourceOrThrow(loc);
            }

            return resource;
        };
    }

    @FunctionalInterface
    private interface ObjModelSupplier {
        ObjModel apply() throws IOException;
    }

    public static class Unbaked extends AbstractUnbakedModel {
        private ObjModelSupplier _geometry;
        private @Nullable Geometry _loadedModel;

        private Unbaked(ObjModelSupplier geometry, final StandardModelParameters parameters) {
            super(parameters);
            _geometry = geometry;
            _loadedModel = null;
        }

        @Override
        public @Nullable UnbakedGeometry geometry() {
            if (_loadedModel == null) {
                try {
                    _loadedModel = new Geometry(_geometry.apply());
                } catch (IOException e) {
                    throw new RuntimeException("Failed to load geometry", e);
                }
            }

            return _loadedModel;
        }
    }

    public static class Geometry implements UnbakedGeometry {
        private final ObjModel _rawModel;

        private final MutableQuad _scratchQuad = new MutableQuad();
        private final Vector3f _overallNormal = new Vector3f();
        private final Vector3f _transformedUv = new Vector3f();
        private final Matrix4f _overallTransform = new Matrix4f();

        private Geometry(final ObjModel rawModel) {
            _rawModel = rawModel;
        }

        @Override
        public QuadCollection bake(
                final TextureSlots textureSlots,
                final ModelBaker modelBaker,
                final ModelState modelState,
                final ModelDebugName modelDebugName) {
            return bake(textureSlots, modelBaker, modelState, modelDebugName, ContextMap.EMPTY);
        }

        @Override
        public QuadCollection bake(
                final TextureSlots textureSlots,
                final ModelBaker baker,
                final ModelState state,
                final ModelDebugName debugName,
                final ContextMap additionalProperties) {
            var result = new QuadCollection.Builder();
            var visibilityMap = additionalProperties.getOrDefault(NeoForgeModelProperties.PART_VISIBILITY, Map.of());
            var transform = additionalProperties.getOrDefault(NeoForgeModelProperties.TRANSFORM, Transformation.IDENTITY);
            transform = state.transformation().compose(transform).blockCenterToCorner();

            for (var object : _rawModel.objects()) {
                bakeObject(result, object, textureSlots, baker, state, debugName, visibilityMap, transform);
            }

            return result.build();
        }

        private void bakeObject(
                QuadCollection.Builder result,
                ObjModel.Object object,
                TextureSlots slots,
                ModelBaker baker,
                ModelState state,
                ModelDebugName debugName,
                Map<String, Boolean> visibilityMap,
                Transformation transform) {
            if (!visibilityMap.getOrDefault(object.name, true)) return;

            for (var face : object.faces()) {
                bakeFace(result, object, face, slots, baker, state, debugName, transform);
            }
        }

        private void bakeFace(
                QuadCollection.Builder result,
                ObjModel.Object object,
                ObjModel.Face face,
                TextureSlots slots,
                ModelBaker baker,
                ModelState state,
                ModelDebugName debugName,
                Transformation transform) {
            var numberOfFaces = face.vertices().size();
            if (numberOfFaces != 3 && numberOfFaces != 4)
                throw new IllegalStateException("Only triangles and quads are permitted");

            Material.Baked material = null;

            var materialName = object.material;
            if (materialName == null) materialName = object.name;

            if (_rawModel.materialLibrary != null) {
                // If the OBJ has an associated material library, apply its properties.
                var mtlMaterial = _rawModel.materialLibrary.getMaterial(materialName);
                if (mtlMaterial != null) {
                    var diffuseMap = mtlMaterial.diffuseReflectivityMap();
                    if (diffuseMap != null) {
                        material = baker.materials().resolveSlot(slots, diffuseMap, debugName);
                    }

                    if (mtlMaterial.tintIndex() != null) {
                        _scratchQuad.setTintIndex(mtlMaterial.tintIndex());
                    }

                    if (mtlMaterial.ambientReflectivity() != null) {
                        var avgLight = (mtlMaterial.ambientReflectivity().x() + mtlMaterial.ambientReflectivity().y() + mtlMaterial.ambientReflectivity().z()) / 3;
                        _scratchQuad.setLightEmission((int) (avgLight * 15));
                    }

                    float r = 1, g = 1, b = 1, a = mtlMaterial.dissolve();
                    if (mtlMaterial.diffuseReflectivity() != null) {
                        r *= mtlMaterial.diffuseReflectivity().x();
                        g *= mtlMaterial.diffuseReflectivity().y();
                        b *= mtlMaterial.diffuseReflectivity().z();
                    }

                    _scratchQuad.setColor(BakedColors.of(ARGB.colorFromFloat(a, r, g, b)));
                }
            }

            if (material == null) {
                material = baker.materials().resolveSlot(slots, materialName, debugName);
            }

            _scratchQuad.setSprite(material);
            var direction = face.computeApproximateDirection(object, _overallNormal);
            _scratchQuad.setDirection(direction);

            for (int i = 0; i < 4; i++) {
                var vertexIndex = i;
                if (numberOfFaces == 3 && i == 3) vertexIndex--;

                var vertex = face.vertices().get(vertexIndex);
                var position = object.position(vertex.positionReference());
                _scratchQuad.setPosition(i, position);

                if (vertex.normalReference() != null) {
                    _scratchQuad.setNormal(i, object.normal(vertex.normalReference()));
                }

                if (vertex.textureCoordinateReference() != null) {
                    var uv = object.uv(vertex.textureCoordinateReference());

                    // Apply UV transformations in order to support uvlock
                    var uvTransform = state.inverseFaceTransformation(direction);
                    if (!MatrixUtil.isIdentity(uvTransform)) {
                        _transformedUv.set(uv.x() - 0.5f, (1 - uv.y()) - 0.5f, 0.0f);
                        uvTransform.transformPosition(_transformedUv);
                        _scratchQuad.setUvFromSprite(i, _transformedUv.x() + 0.5f, _transformedUv.y() + 0.5f);
                    } else {
                        _scratchQuad.setUvFromSprite(i, uv.x(), 1 - uv.y());
                    }
                }
            }

            // Apply the model transform from the model state.
            if (!transform.isIdentity()) {
                _overallTransform.set(transform.getMatrix());
                _scratchQuad.transform(_overallTransform);
            }

            // TODO: cull
            result.addUnculledFace(_scratchQuad.toBakedQuad());
        }
    }
}
