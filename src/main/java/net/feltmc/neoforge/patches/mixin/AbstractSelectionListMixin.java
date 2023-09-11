package net.feltmc.neoforge.patches.mixin;

import net.feltmc.neoforge.patches.interfaces.AbstractSelectionListInterface;
import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractSelectionList.class)
public class AbstractSelectionListMixin implements AbstractSelectionListInterface {
    @Shadow private int width;
    @Shadow private int height;
    @Shadow private int y0;
    @Shadow private int y1;
    @Shadow private int x0;
    @Shadow private int x1;
    @Override public int getWidth() { return this.width; }
    @Override public int getHeight() { return this.height; }
    @Override public int getTop() { return this.y0; }
    @Override public int getBottom() { return this.y1; }
    @Override public int getLeft() { return this.x0; }
    @Override public int getRight() { return this.x1; }
}
