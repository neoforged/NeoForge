#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:fog.glsl>
#include <minecraft:globals.glsl>
#include <minecraft:projection.glsl>
#include <minecraft:sample_lightmap.glsl>
#include <minecraft:terrainglobals.glsl>
#ifndef MULTIDRAW_TERRAIN
    #include <minecraft:chunksection.glsl>
#endif

#ifdef MULTIDRAW_TERRAIN
layout(location = 0) in ivec3 ChunkPosition;
layout(location = 1) in float ChunkVisibility;
layout(location = 2) in ivec2 BufferStartOffsets;
#endif

#ifndef OIT_ALPHA_ONLY
uniform sampler2D Sampler2;
#endif

uniform usamplerBuffer TerrainVertexBuffer;

layout(location = 0) out float sphericalVertexDistance;
layout(location = 1) out float cylindricalVertexDistance;
layout(location = 2) out vec4 vertexColor;
layout(location = 3) out vec2 texCoord0;
layout(location = 4) out float chunkVisibility;

vec3 unpackPosition(int packed) {
    return vec3((packed & 0xFF) - 7, ((packed >> 8) & 0xFF) - 7, ((packed >> 16) & 0xFF) - 7) / 15.0f;
}

// unpackUnorm4x8 intrinsic requires OpenGL 4.0+, this "should" be lowered down to same gpu instructions as unpackUnorm4x8
vec4 unpackUnorm(uint packed) {
    return vec4((packed & 0xFFu), ((packed >> 8u) & 0xFFu), ((packed >> 16u) & 0xFFu), ((packed >> 24u) & 0xFFu)) / 255.0f;
}

struct VertexData {
    vec3 position;
    vec4 color;
    vec2 UV0;
    vec2 UV2;
};

VertexData decodeVertex(usamplerBuffer vertexBuffer, ivec2 vertexBufferOffsets, int vertexID) {
    int faceID = vertexID >> 2; // gl_VertexIndex / 4
    int cornerID = vertexID & 3; // gl_VertexIndex % 4

    int startOffset = vertexBufferOffsets.x + faceID * PER_FACE_INCREMENT;

    uvec4 packedData1 = texelFetch(vertexBuffer, startOffset);
    uvec4 packedData2 = texelFetch(vertexBuffer, startOffset + 1);

    int posPacked = int(packedData1.x);
    uint colorPacked = packedData1.y;
    int bakedQuadIndex = int(packedData2.y);

    uint brightnessPacked = packedData1.z;
    uint brightnessShiftVal = 8u * uint(cornerID);
    uint brightnessUint = (brightnessPacked >> brightnessShiftVal) & 0xFFu;
    float brightness = brightnessUint / 255.0;

    int lightCoordIndex = startOffset + 3 + (cornerID >> 1);
    uint lightCoord01 = packedData1.w;
    uint lightCoord23 = packedData2.r;
    uint lightCoordUint = ((cornerID >> 1) == 1) ? lightCoord23 : lightCoord01;

    uint shiftVal = 16u * (uint(cornerID) & 1u);
    lightCoordUint = (lightCoordUint >> shiftVal) & 0xFFFFu;
    vec2 UV2 = unpackUnorm(lightCoordUint).xy;

    int bakedQuadBufferIndex = vertexBufferOffsets.y + (bakedQuadIndex * PER_BAKED_FACE_INCREMENT) + cornerID * PER_BAKED_FACE_VERTEX_INCREMENT;
    vec4 bakedPackedData1 = uintBitsToFloat(texelFetch(vertexBuffer, bakedQuadBufferIndex));
    uvec4 bakedPackedData2 = texelFetch(vertexBuffer, bakedQuadBufferIndex + 1);
    float uv0V = uintBitsToFloat(bakedPackedData2.r);

    vec3 position = unpackPosition(posPacked) + vec3(bakedPackedData1.x, bakedPackedData1.y, bakedPackedData1.z);
    vec4 unpackedColor = unpackUnorm(colorPacked);
    vec4 color = vec4(unpackedColor.yzw * brightness, unpackedColor.x) * unpackUnorm(bakedPackedData2.y);
    vec2 UV0 = vec2(bakedPackedData1.w, uv0V);

    return VertexData(position, color, UV0, UV2);
}

void main() {
    VertexData vertexData = decodeVertex(TerrainVertexBuffer, BufferStartOffsets, gl_VertexIndex);

    vec3 pos = vertexData.position + (ChunkPosition - CameraBlockPos) + CameraOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    sphericalVertexDistance = fog_spherical_distance(pos);
    cylindricalVertexDistance = fog_cylindrical_distance(pos);
    #ifndef OIT_ALPHA_ONLY
    vertexColor = vertexData.color * sample_lightmap(Sampler2, vertexData.UV2);
    #else
    vertexColor = vertexData.color;
    #endif
    texCoord0 = vertexData.UV0;

    const float chunkFullyVisibleRange = 16.0;
    float dist = length(pos);
    chunkVisibility = mix(1.0, ChunkVisibility, clamp((dist - chunkFullyVisibleRange) / chunkFullyVisibleRange, 0.0, 1.0));
}
