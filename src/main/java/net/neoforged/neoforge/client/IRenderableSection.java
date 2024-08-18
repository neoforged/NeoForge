package net.neoforged.neoforge.client;

import java.util.Set;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.UnmodifiableView;

/**
 * Describes a chunk section that may be rendered on the GPU.
 */
public interface IRenderableSection {
    /**
     * {@return the block position at the origin of the section}
     */
    BlockPos getOrigin();

    /**
     * {@return the bounding box of the section}
     */
    AABB getBoundingBox();

    /**
     * {@return true if the compiled section contains no chunk render layers}
     */
    boolean isEmpty();

    /**
     * {@return the set of compiled render layers for the section}
     */
    @UnmodifiableView
    Set<RenderType> getCompiledRenderLayers();
}
