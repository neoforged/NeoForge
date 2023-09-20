package net.feltmc.neoforge.patches.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraftforge.client.extensions.IForgeVertexConsumer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(VertexConsumer.class)
public class VertexConsumerMixin implements IForgeVertexConsumer {
}
