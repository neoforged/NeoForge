package net.neoforged.neoforge.client.model.obj;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ObjModel {
    private final List<Object> _objects;

    @Nullable
    public final MaterialLibrary materialLibrary;

    private ObjModel(@Nullable MaterialLibrary materialLibrary, List<Object> objects) {
        this.materialLibrary = materialLibrary;

        _objects = objects;
    }

    public List<Object> objects() {
        return _objects;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Object {
        private final List<Vector3fc> _vertexPositions;
        private final List<Vector3fc> _vertexNormals;
        private final List<Vector3fc> _vertexUvs;
        private final List<Face> _faces;

        public final String name;
        public final @Nullable String material;
        public final int smoothingGroup;

        private Object(String name, @Nullable String material, int smoothingGroup,
            List<Vector3fc> positions, List<Vector3fc> normals, List<Vector3fc> uvs,
            List<Face> faces) {
            this.name = name;
            this.material = material;
            this.smoothingGroup = smoothingGroup;

            _vertexPositions = positions;
            _vertexNormals = normals;
            _vertexUvs = uvs;
            _faces = faces;
        }

        public List<Face> faces() {
            return _faces;
        }

        public Vector3fc position(int reference) {
            return _vertexPositions.get(reference - 1);
        }

        public Vector3fc normal(int reference) {
            return _vertexNormals.get(reference - 1);
        }

        public Vector3fc uv(int reference) {
            return _vertexUvs.get(reference - 1);
        }
    }

    public record Face(
        List<Vertex> vertices
    ) {
        // TODO: This should normalize the references such that there are no negative numbers.
        public record Vertex(int positionReference,
            @Nullable Integer textureCoordinateReference,
            @Nullable Integer normalReference
        ) { }

        public Direction computeApproximateDirection(Object object, Vector3f overallNormal) {
            overallNormal.zero();

            for (var vertex : vertices) {
                if (vertex.normalReference() != null) overallNormal.add(object.normal(vertex.normalReference()));
            }

            overallNormal.div(vertices.size());

            return Direction.getApproximateNearest(overallNormal.x(), overallNormal.y(), overallNormal.z());
        }
    }

    public static class Builder {
        @Nullable
        private String _materialLibrary = null;

        private final List<Vector3fc> _vertexPositions = new ReferenceArrayList<>();
        private final List<Vector3fc> _vertexNormals = new ReferenceArrayList<>();
        private final List<Vector3fc> _vertexUvs = new ReferenceArrayList<>();
        private final List<ObjModel.Object> _objects = new ReferenceArrayList<>();

        private @Nullable String _objectName = null;
        private final List<Face> _objectFaces = new ReferenceArrayList<>();
        private String _material = null;
        private int _smoothingGroup;

        private Builder() { }

        public ObjModel build() {
            return new ObjModel(null, _objects);
        }

        public ObjModel build(MaterialLibrary materialLibrary) {
            if (_objectName != null) {
                _objects.add(new Object(
                    _objectName, _material, _smoothingGroup,
                    _vertexPositions, _vertexNormals, _vertexUvs,
                    new ReferenceArrayList<>(_objectFaces)
                ));
            }

            return new ObjModel(materialLibrary, _objects);
        }

        public Builder setMaterialLibrary(String materialLibrary) {
            _materialLibrary = materialLibrary;
            return this;
        }

        @Nullable
        public String materialLibrary() {
            return _materialLibrary;
        }

        public Builder beginObject(String name) {
            if (_objectName != null) {
                _objects.add(new Object(
                    _objectName, _material, _smoothingGroup,
                    _vertexPositions, _vertexNormals, _vertexUvs,
                    new ReferenceArrayList<>(_objectFaces)
                ));
            }

            _objectName = name;
            _objectFaces.clear();
            _smoothingGroup = 0;
            return this;
        }

        public Builder addFace(Face face) {
            _objectFaces.add(face);
            return this;
        }

        public Builder setMaterial(String material) {
            _material = material;
            return this;
        }

        public Builder setSmoothingGroup(int group) {
            _smoothingGroup = group;
            return this;
        }

        public Builder addVertexPosition(Vector3fc position) {
            _vertexPositions.add(position);
            return this;
        }

        public Builder addVertexNormal(Vector3fc normal) {
            _vertexNormals.add(normal);
            return this;
        }

        public Builder addVertexTextureCoordinate(Vector3fc texCoord) {
            _vertexUvs.add(texCoord);
            return this;
        }

        public int vertexPositionCount() {
            return _vertexPositions.size();
        }

        public int vertexNormalCount() {
            return _vertexNormals.size();
        }

        public int vertexTextureCoordinateCount() {
            return _vertexUvs.size();
        }
    }
}
