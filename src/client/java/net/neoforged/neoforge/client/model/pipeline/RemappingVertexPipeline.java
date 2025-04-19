/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.pipeline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Vector3f;

/**
 * Vertex pipeline element that remaps incoming data to another format.
 */
public class RemappingVertexPipeline implements VertexConsumer {
    private static final Set<VertexFormatElement> KNOWN_ELEMENTS = Set.of(VertexFormatElement.POSITION,
            VertexFormatElement.COLOR, VertexFormatElement.UV, VertexFormatElement.UV1,
            VertexFormatElement.UV2, VertexFormatElement.NORMAL);
    private static final int[] EMPTY_INT_ARRAY = new int[0];

    private final VertexConsumer parent;
    private final VertexFormat targetFormat;

    private final Vector3f position = new Vector3f();
    private final Vector3f normal = new Vector3f();
    private final int[] color = new int[] { 255, 255, 255, 255 };
    private final float[] uv0 = new float[] { 0, 0 };
    private final int[] uv1 = new int[] { OverlayTexture.NO_WHITE_U, OverlayTexture.WHITE_OVERLAY_V };
    private final int[] uv2 = new int[] { 0, 0 };

    private final Map<VertexFormatElement, Integer> miscElementIds;
    private final int[][] misc;

    public RemappingVertexPipeline(VertexConsumer parent, VertexFormat targetFormat) {
        this.parent = parent;
        this.targetFormat = targetFormat;

        this.miscElementIds = new IdentityHashMap<>();
        int i = 0;
        for (var element : targetFormat.getElements())
            if (!KNOWN_ELEMENTS.contains(element))
                this.miscElementIds.put(element, i++);
        this.misc = new int[i][];
        Arrays.fill(this.misc, EMPTY_INT_ARRAY);
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        position.set(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        normal.set(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = a;
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        uv0[0] = u;
        uv0[1] = v;
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        uv1[0] = u;
        uv1[1] = v;
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        uv2[0] = u;
        uv2[1] = v;
        return this;
    }

    @Override
    public VertexConsumer misc(VertexFormatElement element, int... values) {
        Integer id = miscElementIds.get(element);
        if (id != null)
            misc[id] = Arrays.copyOf(values, values.length);
        return this;
    }

    public void endVertex() {
        // TODO 1.21: The interface no longer includes an endVertex method because
        // vanilla now automatically ends the vertex either when a new vertex is started
        // or a finishing method like build is called
        for (var element : targetFormat.getElements()) {

            // Try to match and output any of the supported elements, and if that fails, treat as misc
            if (element.equals(VertexFormatElement.POSITION))
                parent.addVertex(position.x, position.y, position.z);
            else if (element.equals(VertexFormatElement.NORMAL))
                parent.setNormal(normal.x(), normal.y(), normal.z());
            else if (element.equals(VertexFormatElement.COLOR))
                parent.setColor(color[0], color[1], color[2], color[3]);
            else if (element.equals(VertexFormatElement.UV0))
                parent.setUv(uv0[0], uv0[1]);
            else if (element.equals(VertexFormatElement.UV1))
                parent.setUv1(uv1[0], uv1[1]);
            else if (element.equals(VertexFormatElement.UV2))
                parent.setUv2(uv2[0], uv2[1]);
            else
                parent.misc(element, misc[miscElementIds.get(element)]);
        }
    }
}
