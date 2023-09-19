package net.feltmc.neoforge.patches.mixin;

import net.feltmc.neoforge.patches.interfaces.RenderTypeInterface;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(RenderType.class)
public abstract class RenderTypeMixin implements RenderTypeInterface {
    @Shadow
    public static List<RenderType> chunkBufferLayers() { return null; }

    public int chunkLayerId = -1;
    static {
        int i = 0;
        for (var layer : chunkBufferLayers())
            //Somehow we have to inject this var? no idea how atm
            //Possibly we could just introduce a setter?
            layer.chunkLayerId = i++;
    }

    @Override
    public final int getChunkLayerId() {
        return chunkLayerId;
    }
}
