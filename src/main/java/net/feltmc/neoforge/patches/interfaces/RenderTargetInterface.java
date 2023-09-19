package net.feltmc.neoforge.patches.interfaces;

import net.feltmc.neoforge.FeltVars;

public interface RenderTargetInterface {
    public boolean stencilEnabled = false;

    default public void enableStencil() {
        throw new RuntimeException(FeltVars.mixinOverrideException);
    }

    default public boolean isStencilEnabled() {
        throw new RuntimeException(FeltVars.mixinOverrideException);
    }
}
