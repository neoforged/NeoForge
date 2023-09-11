package net.feltmc.neoforge.patches.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.GlStateManager;
import net.feltmc.neoforge.patches.interfaces.RenderTargetInterface;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderTarget.class)
public abstract class RenderTargetMixin implements RenderTargetInterface {
    @Shadow protected int depthBufferId;
    @Shadow public int width;
    @Shadow public int height;
    @Shadow public int viewWidth;
    @Shadow public int viewHeight;

    @Shadow public abstract void resize(int i, int j, boolean bl);

    @WrapOperation(
            method = "createBuffers",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D (IIIIIIIILjava/nio/IntBuffer;)V", ordinal = 0, remap = false)
    )
    private void createBuffersMixin0(RenderTarget instance, boolean flag, Operation<Integer> original) {
        if (!stencilEnabled) {
            original.call(instance, flag);
        } else {
            GlStateManager._texImage2D(3553, 0, GL30.GL_DEPTH32F_STENCIL8, this.width, this.height, 0, GL30.GL_DEPTH_STENCIL, GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV, null);
        }
    }

    @WrapOperation(
            method = "createBuffers",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V", ordinal = 1, remap = false)
    )
    private void createBuffersMixin1(RenderTarget instance, boolean flag, Operation<Integer> original) {
        if (!stencilEnabled) {
            original.call(instance, flag);
        } else if(net.minecraftforge.common.ForgeConfig.CLIENT.useCombinedDepthStencilAttachment.get()) {
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, 3553, this.depthBufferId, 0);
        } else {
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, 3553, this.depthBufferId, 0);
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, 3553, this.depthBufferId, 0);
        }

    }

    //TODO inject field for more compat?
    private boolean stencilEnabled = false;

    @Override
    public void enableStencil() {
        if(stencilEnabled) return;
        stencilEnabled = true;
        this.resize(this.viewWidth, this.viewHeight, Minecraft.ON_OSX);
    }

    @Override
    public boolean isStencilEnabled() {
        return this.stencilEnabled;
    }
}
