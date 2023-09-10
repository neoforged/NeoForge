package net.feltmc.neoforge.patches.interfaces;

public interface RenderTargetInterface {
    public boolean stencilEnabled = false;

    default public void enableStencil() {
        //TODO log error
    }

    default public boolean isStencilEnabled() {
        //TODO log error
        return false;
    }
}
