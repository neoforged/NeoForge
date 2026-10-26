package net.neoforged.neoforge.client.model.obj;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.neoforge.client.model.obj.ObjModel.Builder;
import net.neoforged.neoforge.client.model.obj.ObjModel.Face;
import net.neoforged.neoforge.client.model.obj.ObjModel.Face.Vertex;

import java.io.IOException;
import java.util.Collections;

public final class ObjLoader {
    private ObjLoader() { }

    @FunctionalInterface
    public interface ResourceLocator {
        Resource apply(Identifier origin, String name) throws IOException;
    }

    public static ObjModel load(Identifier origin, Resource resource, ResourceLocator locator) throws IOException {
        Builder model;
        try (var reader = new ObjReader(resource)) {
            model = process(reader);
        }

        if (model.materialLibrary() != null) {
            MaterialLibrary materials;
            try (var reader = new MtlReader(locator.apply(origin, model.materialLibrary()))) {
                materials = process(reader).build();
            }

            return model.build(materials);
        }

        return model.build();
    }

    private static Builder process(ObjReader reader) throws IOException {
        var builder = ObjModel.builder();
        while (reader.hasMoreData()) {
            switch (reader.readCommand()) {
                case MaterialLibraryReference -> builder.setMaterialLibrary(reader.readString());
                case Object -> builder.beginObject(reader.readString());
                case VertexPosition -> builder.addVertexPosition(reader.readVertexPosition());
                case VertexNormal -> builder.addVertexNormal(reader.readVertexNormal());
                case VertexTexture -> builder.addVertexTextureCoordinate(reader.readTextureCoordinate());
                case MaterialReference -> builder.setMaterial(reader.readString());
                case SmoothingGroup -> builder.setSmoothingGroup(reader.readSmoothingGroup());

                case Face -> {
                    var face = reader.readFace();

                    // Ensure we're only permitting OBJs with triangles or quads.
                    var numberOfVertices = face.vertices().length;
                    var vertices = new ReferenceArrayList<Vertex>(numberOfVertices);
                    for (int i = 0; i < numberOfVertices; i++) {
                        var originalVertex = face.vertices()[i];

                        if (originalVertex.positionIndex() < 0 || originalVertex.positionIndex() > builder.vertexPositionCount())
                            throw new IllegalStateException("Invalid face: vertex index " + i + " references invalid position " + originalVertex.positionIndex());

                        if (originalVertex.normalIndex() != null &&
                            (originalVertex.normalIndex() < 0 || originalVertex.normalIndex() > builder.vertexNormalCount()))

                            throw new IllegalStateException("Invalid face: vertex index " + i + " references invalid normal " + originalVertex.positionIndex());

                        if (originalVertex.textureCoordinateIndex() != null &&
                            (originalVertex.textureCoordinateIndex() < 0 || originalVertex.textureCoordinateIndex() > builder.vertexTextureCoordinateCount()))

                            throw new IllegalStateException("Invalid face: vertex index " + i + " references invalid texture coordinate " + originalVertex.positionIndex());

                        vertices.add(new Vertex(
                            originalVertex.positionIndex(),
                            originalVertex.textureCoordinateIndex(),
                            originalVertex.normalIndex()
                        ));
                    }

                    builder.addFace(new Face(Collections.unmodifiableList(vertices)));
                }

                case null -> { /* no-op, likely means we reached EOF */ }
            }
        }

        return builder;
    }

    private static MaterialLibrary.Builder process(MtlReader reader) throws IOException {
        var builder = MaterialLibrary.builder();
        while (reader.hasMoreData()) {
            switch (reader.readCommand()) {
                case NewMaterial -> builder.addMaterial(reader.readString());
                case SpecularExponent -> builder.setSpecularExponent(reader.readFloat());
                case OpticalDensity -> builder.setOpticalDensity(reader.readFloat());
                case AmbientReflectivity -> builder.setAmbientReflectivity(reader.readColor());
                case DiffuseReflectivity -> builder.setDiffuseReflectivity(reader.readColor());
                case SpecularReflectivity -> builder.setSpecularReflectivity(reader.readColor());
                case SpecularEmission -> builder.setSpecularEmission(reader.readColor());
                case Dissolve -> builder.setDissolve(reader.readFloat());
                case IlluminationModel -> builder.setIlluminationModel(reader.readInteger());
                case MappedSpecularityExponent -> builder.setSpecularityExponentMap(reader.readString());
                case MappedAmbientReflectivity -> builder.setAmbientReflectivityMap(reader.readString());
                case MappedDiffuseReflectivity -> builder.setDiffuseReflectivityMap(reader.readString());
                case MappedSpecularReflectivity -> builder.setSpecularReflectivityMap(reader.readString());
                case MappedSpecularEmission -> builder.setSpecularEmissionMap(reader.readString());
                case DisplacementMap -> builder.setDisplacementMap(reader.readString());
                case BumpMap -> builder.setBumpMap(reader.readString());
                case ReflectionMap -> builder.setReflectionMap(reader.readString());
                case NeoForgeTintIndex -> builder.setNeoForgeTintIndex(reader.readInteger());
                case null -> { /* no-op, likely means we reached EOF */ }
            }
        }

        return builder;
    }
}
