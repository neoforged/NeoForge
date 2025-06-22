package net.neoforged.neoforge.client.gui;

import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;

public record PictureInPictureRendererRegistration<T extends PictureInPictureRenderState>(Class<T> stateClass,
                                                                                          PictureInPictureRendererFactory<T> factory) {
}

