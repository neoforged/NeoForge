package net.neoforged.neoforge.client.model.ao;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.Direction;

class AoCalculatedFace {
    float brightness0;
    float brightness1;
    float brightness2;
    float brightness3;
    int lightmap0;
    int lightmap1;
    int lightmap2;
    int lightmap3;

    void copyToResult(float[] brightness, int[] lightmap, Direction direction) {
        var ambientRemap = ModelBlockRenderer.AmbientVertexRemap.fromFacing(direction);
        brightness[ambientRemap.vert0] = brightness0;
        brightness[ambientRemap.vert1] = brightness1;
        brightness[ambientRemap.vert2] = brightness2;
        brightness[ambientRemap.vert3] = brightness3;
        lightmap[ambientRemap.vert0] = lightmap0;
        lightmap[ambientRemap.vert1] = lightmap1;
        lightmap[ambientRemap.vert2] = lightmap2;
        lightmap[ambientRemap.vert3] = lightmap3;
    }
}
