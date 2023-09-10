package net.feltmc.neoforge.patches.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.client.extensions.IForgePoseStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PoseStack.class)
public class PoseStackMixin implements IForgePoseStack {
}
