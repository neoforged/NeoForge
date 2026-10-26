package net.neoforged.neoforge.client.model.obj;

import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.stream.Collector;

public class MaterialLibrary {
    private final Object2ReferenceMap<String, Material> _materialMap;

    private MaterialLibrary(Object2ReferenceMap<String, Material> materials) {
        _materialMap = materials;
    }

    public static Builder builder() {
        return new Builder();
    }

    public @Nullable Material getMaterial(String name) {
        return _materialMap.getOrDefault(name, null);
    }
    
    public record Material(String name,
        float specularExponent,
        float opticalDensity,
        @Nullable
        Vector3fc ambientReflectivity,
        @Nullable
        Vector3fc diffuseReflectivity,
        @Nullable
        Vector3fc specularReflectivity,
        @Nullable
        Vector3fc specularEmission,
        float dissolve,
        int illuminationModel,

        @Nullable
        String specularExponentMap,
        @Nullable
        String opticalDensityMap,
        @Nullable
        String ambientReflectivityMap,
        @Nullable
        String diffuseReflectivityMap,
        @Nullable
        String specularReflectivityMap,
        @Nullable
        String specularEmissionMap,

        @Nullable
        String displacementMap,
        @Nullable
        String bumpMap,
        @Nullable
        String reflectionMap,

        @Nullable
        Integer tintIndex
    ) {}

    public static class Builder {
        private final List<Material> _materials = new ReferenceArrayList<>();

        private String _materialName;
        private float _specularExponent;
        private float _opticalDensity;
        @Nullable
        private Vector3fc _ambientReflectivity;
        @Nullable
        private Vector3fc _diffuseReflectivity;
        @Nullable
        private Vector3fc _specularReflectivity;
        @Nullable
        private Vector3fc _specularEmission;
        private float _dissolve;
        private int _illuminationModel;

        @Nullable
        private String _specularExponentMap;
        @Nullable
        private String _opticalDensityMap;
        @Nullable
        private String _ambientReflectivityMap;
        @Nullable
        private String _diffuseReflectivityMap;
        @Nullable
        private String _specularReflectivityMap;
        @Nullable
        private String _specularEmissionMap;

        @Nullable
        private String _displacementMap;
        @Nullable
        private String _bumpMap;
        @Nullable
        private String _reflectionMap;

        @Nullable
        private Integer _tintIndex;

        private Builder() { }

        public MaterialLibrary build() {
            if (_materialName != null) {
                _materials.add(new Material(
                    _materialName,
                    _specularExponent,
                    _opticalDensity,
                    _ambientReflectivity,
                    _diffuseReflectivity,
                    _specularReflectivity,
                    _specularEmission,
                    _dissolve,
                    _illuminationModel,
                    _specularExponentMap,
                    _opticalDensityMap,
                    _ambientReflectivityMap,
                    _diffuseReflectivityMap,
                    _specularReflectivityMap,
                    _specularEmissionMap,
                    _displacementMap,
                    _bumpMap,
                    _reflectionMap,
                    _tintIndex
                ));

                _materialName = null;
            }

            var materials = new Object2ReferenceOpenHashMap<String, Material>();
            for (var material : _materials) {
                materials.put(material.name(), material);
            }

            _materials.clear();
            return new MaterialLibrary(materials);
        }

        public Builder addMaterial(final String name) {
            if (_materialName != null) {
                _materials.add(new Material(
                    _materialName,
                    _specularExponent,
                    _opticalDensity,
                    _ambientReflectivity,
                    _diffuseReflectivity,
                    _specularReflectivity,
                    _specularEmission,
                    _dissolve,
                    _illuminationModel,
                    _specularExponentMap,
                    _opticalDensityMap,
                    _ambientReflectivityMap,
                    _diffuseReflectivityMap,
                    _specularReflectivityMap,
                    _specularEmissionMap,
                    _displacementMap,
                    _bumpMap,
                    _reflectionMap,
                    _tintIndex
                ));
            }

            _materialName = name;
            _specularExponent = 0;
            _opticalDensity = 1;
            _ambientReflectivity = null;
            _diffuseReflectivity = null;
            _specularReflectivity = null;
            _specularEmission = null;
            _dissolve = 1;
            _illuminationModel = 2;

            _specularExponentMap = null;
            _opticalDensityMap = null;
            _ambientReflectivityMap = null;
            _diffuseReflectivityMap = null;
            _specularReflectivityMap = null;
            _specularEmissionMap = null;

            _displacementMap = null;
            _bumpMap = null;
            _reflectionMap = null;

            _tintIndex = null;

            return this;
        }

        public Builder setSpecularExponent(final float exponent) {
            _specularExponent = exponent;
            return this;
        }
        public Builder setOpticalDensity(final float density) {
            _opticalDensity = density;
            return this;
        }
        public Builder setAmbientReflectivity(final Vector3fc ambient) {
            _ambientReflectivity = ambient;
            return this;
        }
        public Builder setDiffuseReflectivity(final Vector3fc diffuse) {
            _diffuseReflectivity = diffuse;
            return this;
        }
        public Builder setSpecularReflectivity(final Vector3fc specular) {
            _specularReflectivity = specular;
            return this;
        }
        public Builder setSpecularEmission(final Vector3fc emission) {
            _specularEmission = emission;
            return this;
        }
        public Builder setDissolve(final float dissolve) {
            _dissolve = dissolve;
            return this;
        }
        public Builder setIlluminationModel(final int model) {
            _illuminationModel = model;
            return this;
        }
        public Builder setSpecularityExponentMap(final String map) {
            _specularExponentMap = map;
            return this;
        }
        public Builder setAmbientReflectivityMap(final String map) {
            _ambientReflectivityMap = map;
            return this;
        }
        public Builder setDiffuseReflectivityMap(final String map) {
            _diffuseReflectivityMap = map;
            return this;
        }
        public Builder setSpecularReflectivityMap(final String map) {
            _specularReflectivityMap = map;
            return this;
        }
        public Builder setSpecularEmissionMap(final String map) {
            _specularEmissionMap = map;
            return this;
        }
        public Builder setDisplacementMap(final String map) {
            _displacementMap = map;
            return this;
        }
        public Builder setBumpMap(final String map) {
            _bumpMap = map;
            return this;
        }
        public Builder setReflectionMap(final String map) {
            _reflectionMap = map;
            return this;
        }
        public Builder setNeoForgeTintIndex(final int index) {
            _tintIndex = index;
            return this;
        }
    }
}
