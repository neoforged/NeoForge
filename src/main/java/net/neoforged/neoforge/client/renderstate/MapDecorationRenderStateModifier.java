package net.neoforged.neoforge.client.renderstate;

import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public interface MapDecorationRenderStateModifier {
    void accept(MapItemSavedData mapItemSavedData, MapRenderState mapRenderState, MapRenderState.MapDecorationRenderState mapDecorationRenderState);
}