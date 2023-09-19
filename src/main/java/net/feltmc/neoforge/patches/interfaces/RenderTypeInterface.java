package net.feltmc.neoforge.patches.interfaces;

import net.feltmc.neoforge.FeltVars;

public interface RenderTypeInterface {
        default int getChunkLayerId() {
            throw new RuntimeException(FeltVars.mixinOverrideException);
        }
}
