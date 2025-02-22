package net.neoforged.neoforge.client.renderer.transparency;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_R;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT1;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_HALF_FLOAT;
import static org.lwjgl.opengl.GL30.GL_NONE;
import static org.lwjgl.opengl.GL30.GL_R32F;
import static org.lwjgl.opengl.GL30.GL_RGBA32F;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL30.glDrawBuffer;
import static org.lwjgl.opengl.GL30.glReadBuffer;

public class OITMomentTarget extends RenderTarget {

    public OITMomentTarget(int width, int height)
    {
        super(false);
        createBuffers(width, height);
    }

    @Override
    public void createBuffers(int p_83951_, int p_83952_) {
        RenderSystem.assertOnRenderThreadOrInit();
        this.frameBufferId = GlStateManager.glGenFramebuffers();
        MainTarget.Dimension maintarget$dimension = this.allocateAttachments(width, height);
        GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, this.frameBufferId);
        GlStateManager._glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, this.colorTextureId, 0); // opaque framebuffer's depth texture
        GlStateManager._glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, this.depthBufferId, 0); // opaque framebuffer's depth texture
        this.viewWidth = maintarget$dimension.width;
        this.viewHeight = maintarget$dimension.height;
        this.width = maintarget$dimension.width;
        this.height = maintarget$dimension.height;
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        this.checkStatus();
        GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private MainTarget.Dimension allocateAttachments(int width, int height) {
        RenderSystem.assertOnRenderThreadOrInit();
        this.colorTextureId = TextureUtil.generateTextureId();
        this.depthBufferId = TextureUtil.generateTextureId();
        AttachmentState maintarget$attachmentstate = AttachmentState.NONE;

        for(MainTarget.Dimension maintarget$dimension : MainTarget.Dimension.listWithFallback(width, height)) {
            maintarget$attachmentstate = AttachmentState.NONE;
            if (this.allocateFirstMomentsAttachment(maintarget$dimension)) {
                maintarget$attachmentstate = maintarget$attachmentstate.with(AttachmentState.FIRST_MOMENTS);
            }

            if (this.allocateZerothAttachment(maintarget$dimension)) {
                maintarget$attachmentstate = maintarget$attachmentstate.with(AttachmentState.ZEROTH_MOMENT);
            }

            if (maintarget$attachmentstate == AttachmentState.ZEROTH_MOMENT_AND_FIRST_MOMENTS) {
                return maintarget$dimension;
            }
        }

        throw new RuntimeException("Unrecoverable GL_OUT_OF_MEMORY (allocated attachments = " + maintarget$attachmentstate.name() + ")");
    }

    private boolean allocateZerothAttachment(MainTarget.Dimension dimension) {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._getError();
        GlStateManager._bindTexture(this.depthBufferId);
        GlStateManager._texImage2D(GL_TEXTURE_2D, 0, GL_R32F, dimension.width, dimension.height, 0, GL_R, GL_HALF_FLOAT, (IntBuffer)null);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GlStateManager._bindTexture(0);
        return GlStateManager._getError() != 1285;
    }

    private boolean allocateFirstMomentsAttachment(MainTarget.Dimension dimension) {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._getError();
        GlStateManager._bindTexture(this.colorTextureId);
        GlStateManager._texImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, dimension.width, dimension.height, 0, GL_BGRA, GL_HALF_FLOAT, (IntBuffer)null);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GlStateManager._bindTexture(0);
        return GlStateManager._getError() != 1285;
    }

    enum AttachmentState {
        NONE,
        ZEROTH_MOMENT,
        FIRST_MOMENTS,
        ZEROTH_MOMENT_AND_FIRST_MOMENTS;

        private static final AttachmentState[] VALUES = values();

        AttachmentState with(AttachmentState p_166164_) {
            return VALUES[this.ordinal() | p_166164_.ordinal()];
        }
    }
}
