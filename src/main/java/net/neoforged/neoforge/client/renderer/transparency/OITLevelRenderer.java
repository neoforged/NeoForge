package net.neoforged.neoforge.client.renderer.transparency;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeConfig;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.mojang.blaze3d.platform.GlStateManager.glBlendFuncSeparate;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glClearBufferfv;

@ApiStatus.Internal
public final class OITLevelRenderer
{
    private static final OITLevelRenderer INSTANCE = new OITLevelRenderer();
    public static final ShaderProgram WBOIT_COMPOSITION_SHADER = new ShaderProgram(
            ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "oit/blit_screen_oit"),
            DefaultVertexFormat.POSITION_TEX_COLOR,
            ShaderDefines.EMPTY);

    public static final ShaderProgram MBOIT_COMPOSITION_SHADER = new ShaderProgram(
            ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "oit_moments/blit_screen_oit"),
            DefaultVertexFormat.POSITION_TEX_COLOR,
            ShaderDefines.EMPTY);

    public static OITLevelRenderer getInstance()
    {
        return INSTANCE;
    }

    private boolean levelRenderingInProgress;
    private OITRenderTarget transparentOITRenderTarget;
    private OITPreDepthTarget shadowMapTarget;
    private OITMomentTarget momentTarget;


    private final List<IRenderCall> queuedRenderCallList = new ArrayList<>();

    private OITLevelRenderer()
    {
    }

    /**
     * Initializes the OIT system.
     */
    public void initialize(final int width, final int height) {
        this.transparentOITRenderTarget = new OITRenderTarget(width, height);
        this.transparentOITRenderTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.transparentOITRenderTarget.clear();

        this.shadowMapTarget = new OITPreDepthTarget(width, height);
        this.shadowMapTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.shadowMapTarget.clear();

        /*
        this.momentTarget = new OITMomentTarget(width, height);
        this.momentTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.momentTarget.clear();*/
    }

    /**
     * Gives access to the render target that is used for OIT rendering.
     *
     * @return The render target. This is a custom implementation.
     */
    public RenderTarget getTransparentOITRenderTarget()
    {
        return transparentOITRenderTarget;
    }

    /**
     * The final shader used to composite the OIT render target to the screen.
     *
     * @return The shader instance.
     */
    public CompiledShaderProgram getCompositionShader()
    {
        if (NeoForgeConfig.CLIENT.usesMomentBasedOIT.get())
            return Minecraft.getInstance().getShaderManager().getProgram(MBOIT_COMPOSITION_SHADER);
        else
            return Minecraft.getInstance().getShaderManager().getProgram(WBOIT_COMPOSITION_SHADER);
    }

    /**
     * Indicates if the OIT rendering for level rendering is being handled.
     * <p>
     * When true, then render calls can be captured for transparent render types which require sorting.
     *
     * @return True, when level rendering is active, false when not.
     */
    public boolean isLevelRenderingInProgress()
    {
        return levelRenderingInProgress;
    }

    /**
     * Sets the OIT rendering indicator for level rendering.
     *
     * @param levelRenderingInProgress True enables OIT rendering capture for level rendering, false disables it.
     */
    private void setLevelRenderingInProgress(boolean levelRenderingInProgress)
    {
        this.levelRenderingInProgress = levelRenderingInProgress;
    }

    /**
     * Performs a check if the given cpu bound buffer render type is used during level rendering and requires transparency sorting.
     * <p>
     * If this is the case then the render call is postponed and queued up to be rendered through OIT.
     * If not then the render is immediately queued on the GPU.
     *
     * @param renderType The render type with which the rendering should happen.
     * @param renderedBuffer The CPU side payload buffer.
     */
    public void checkHandlesThenQueueOrRender(RenderType renderType, MeshData renderedBuffer)
    {
        final IRenderTypeBasedRenderCall call = new RenderedBufferRenderCall(renderType, renderedBuffer, new Matrix4f(RenderSystem.getModelViewMatrix()), new Matrix4f(RenderSystem.getProjectionMatrix()));
        checkHandlesThenQueueOrRender(call);
    }

    /**
     * Performs a check if the given chunk geometry render type is used during level rendering and requires transparency sorting.
     * <p>
     * If this is the case then the render call is postponed and queued up to be rendered through OIT.
     * If not then the render is immediately queued on the GPU.
     *
     * @param renderType The render type with which the rendering should happen.
     * @param cameraX The camera X position.
     * @param cameraY The camera Y position.
     * @param cameraZ The camera Z position.
     * @param modelViewMatrix The model view matrix.
     * @param projectionMatrix The projection matrix.
     */
    public void checkHandlesThenQueueOrRender(RenderType renderType, double cameraX, double cameraY, double cameraZ, Matrix4f modelViewMatrix, Matrix4f projectionMatrix)
    {
        //We make a defensive copy so that the caller can't modify the matrix after we've captured it.
        final IRenderTypeBasedRenderCall call = new ChunkGeometryRenderCall(
                renderType,
                cameraX,
                cameraY,
                cameraZ,
                Util.make(() -> {
                    final Matrix4f matrix = new Matrix4f();
                    matrix.set(modelViewMatrix);
                    return matrix;
                }),
                Util.make(() -> {
                    final Matrix4f matrix = new Matrix4f();
                    matrix.set(projectionMatrix);
                    return matrix;
                }));
        checkHandlesThenQueueOrRender(call);
    }

    /**
     * Performs a check if the given particle render type is used during level rendering and requires transparency sorting.
     * <p>
     * If this is the case then the render call is postponed and queued up to be rendered through OIT.
     * If not then the render is immediately queued on the GPU.
     *
     * @param particleRenderType The render type with which the rendering should happen.
     * @param toRender The particles to render.
     * @param clippingHelper The clipping helper.
     * @param camera The camera.
     * @param partialTickTime The partial tick time.
     */
    public void checkHandlesThenQueueOrRender(final ParticleRenderType particleRenderType, final Iterable<Particle> toRender, @Nullable final Frustum clippingHelper, Camera camera, float partialTickTime) {
        final IParticleRenderCall call = new ParticleRenderCall(particleRenderType, toRender, clippingHelper, camera, partialTickTime);
        checkHandlesThenQueueOrRender(call);
    }

    /**
     * Internal method to check if the given render call based on a render type is handled by the OIT Handler.
     * <p>
     * If handled, the call will be queued up for later rendering.
     * If not handled, the call will be rendered directly.
     *
     * @param renderCall The render call.
     */
    private void checkHandlesThenQueueOrRender(IRenderTypeBasedRenderCall renderCall)
    {
        if (handles(renderCall.renderType()))
        {
            queuedRenderCallList.add(renderCall);
        }
        else
        {
            renderCall.drawDirect();
        }
    }

    /**
     * Internal method to check if the given render call based on a particle render type is handled by the OIT Handler.
     * <p>
     * If handled, the call will be queued up for later rendering.
     * If not handled, the call will be rendered directly.
     *
     * @param renderCall The render call.
     */
    private void checkHandlesThenQueueOrRender(IParticleRenderCall renderCall)
    {
        if (handles(renderCall.particleRenderType()))
        {
            queuedRenderCallList.add(renderCall);
        }
        else
        {
            renderCall.drawDirect();
        }
    }

    /**
     * Indicates whether the OIT Handler handles the given render type's render call.
     * This will only return true if {@link #willHandle(RenderType)} and {@link #isLevelRenderingInProgress()} are both true.
     *
     * @param renderType The render type to check.
     * @return True when the handler will queue the render type's render call for OIT handler, false when rendering is immediately queued on the GPU.
     */
    public boolean handles(RenderType renderType)
    {
        return willHandle(renderType) && isLevelRenderingInProgress();
    }

    /**
     * Indicates whether the OIT Handler handles the given render type's render call.
     * This will only return true if {@link #willHandle(RenderType)} and {@link #isLevelRenderingInProgress()} are both true.
     *
     * @param renderType The render type to check.
     * @return True when the handler will queue the render type's render call for OIT handler, false when rendering is immediately queued on the GPU.
     */
    public boolean handles(ParticleRenderType renderType)
    {
        return willHandle(renderType) && isLevelRenderingInProgress();
    }

    /**
     * Indicates whether the OIT Handler will handle the given render type's render call.
     * For now this is only true when the render type uses translucent transparency and requires sorting.
     * We are still researching options for other transparency types.
     *
     * @param renderType The render type to check.
     * @return True when the handler will queue the render type's render call for OIT handler, false when rendering is immediately queued on the GPU.
     */
    public boolean willHandle(RenderType renderType) {
        if (!NeoForgeConfig.CLIENT.orderIndependentTransparentRendering.get())
            return false;

        if (this.transparentOITRenderTarget == null)
            return false;

        if (this.shadowMapTarget == null)
            return false;

        if (!renderType.sortOnUpload)
            return false;

        if (!(renderType instanceof RenderType.CompositeRenderType compositeRenderType))
            return false;

        //We can only handle ALPHA Transparency for now.
        if(compositeRenderType.state().transparencyState != RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            return false;

        //We need an adapted shader, which is indicated by the presence of the OIT_ENABLE uniform.
        return compositeRenderType.state().shaderState.shader
                .map(Minecraft.getInstance().getShaderManager()::getProgram)
                .filter(shaderInstanceSupplier -> shaderInstanceSupplier.OIT_ENABLE != null).isPresent();
    }

    /**
     * Indicates whether the OIT Handler will handle the given render type's render call.
     * For now this is only true when the render type uses translucent transparency and requires sorting.
     * We are still researching options for other transparency types.
     *
     * @param renderType The render type to check.
     * @return True when the handler will queue the render type's render call for OIT handler, false when rendering is immediately queued on the GPU.
     */
    public boolean willHandle(ParticleRenderType renderType) {
        if (!NeoForgeConfig.CLIENT.orderIndependentTransparentRendering.get())
            return false;

        //We can only handle the types for which OIT is explicitly enabled.
        //Additionally check for the particle shader to be adapted.
        return this.transparentOITRenderTarget != null &&
                this.shadowMapTarget != null &&
                renderType.isTranslucent() &&
                Minecraft.getInstance().getShaderManager().getProgram(CoreShaders.PARTICLE) != null
                && Minecraft.getInstance().getShaderManager().getProgram(CoreShaders.PARTICLE).OIT_ENABLE != null
                && Minecraft.getInstance().getShaderManager().getProgram(CoreShaders.PARTICLE).OIT_ENABLE.getLocation() != -1;
    }

    /**
     * Called to start the level rendering process.
     * Initializes the OIT rendering process.
     */
    public void startLevelRendering(ProfilerFiller profilerfiller)
    {
        if (isLevelRenderingInProgress())
            return;

        if (!NeoForgeConfig.CLIENT.orderIndependentTransparentRendering.get())
            return;

        profilerfiller.popPush("oit_rendering_setup");

        setLevelRenderingInProgress(true);

        transparentOITRenderTarget.bindWrite(true);
        glClearColor(0.0F, 0.0F, 0.0F, 1F);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        transparentOITRenderTarget.unbindWrite();
        shadowMapTarget.bindWrite(true);
        glClearBufferfv(GL_DEPTH, 0, new float[]{1f});
        shadowMapTarget.unbindWrite();
        /*momentTarget.bindWrite(true);
        glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        glClear(GL_COLOR_BUFFER_BIT);
        momentTarget.unbindWrite();*/
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
    }

    /**
     * Called to end the level rendering process.
     * Finalizes the OIT rendering process.
     * Should be called immediately after the transparent rendering is done.
     */
    public void endLevelRendering(ProfilerFiller profilerfiller)
    {
        if (!isLevelRenderingInProgress())
            return;

        if (!NeoForgeConfig.CLIENT.orderIndependentTransparentRendering.get())
            return;

        profilerfiller.popPush("oit_rendering_finalize");

        setLevelRenderingInProgress(false);
        renderQueue(profilerfiller);
        queuedRenderCallList.clear();
    }

    /**
     * Renders the queued render calls.
     *
     * @param profilerfiller The profiler to use.
     */
    private void renderQueue(ProfilerFiller profilerfiller) {
        if (NeoForgeConfig.CLIENT.usesMomentBasedOIT.get())
            renderMBOITQueue(profilerfiller);
        else
            renderWBOITQueue(profilerfiller);
    }

    private void renderWBOITQueue(ProfilerFiller profilerfiller) {
        if (queuedRenderCallList.isEmpty()) {
            return;
        }

        renderPreDepthMap(profilerfiller);
        renderOITTransparent(profilerfiller);
        renderComposed(profilerfiller);
    }

    private void renderMBOITQueue(ProfilerFiller profilerfiller) {
        if (queuedRenderCallList.isEmpty()) {
            return;
        }

    }

    private void renderComposed(ProfilerFiller profilerfiller) {
        profilerfiller.push("oit_rendering_compose");

        transparentOITRenderTarget.unbindWrite();

        transparentOITRenderTarget.blitToScreen(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight());

        profilerfiller.pop();
    }

    private void renderOITTransparent(ProfilerFiller profilerfiller) {
        profilerfiller.push("oit_rendering_collect");

        transparentOITRenderTarget.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());

        for (IRenderCall renderCall : queuedRenderCallList) {
            renderCall.drawOIT();
        }

        profilerfiller.pop();
    }

    private void renderPreDepthMap(ProfilerFiller profilerfiller) {
        profilerfiller.push("oit_rendering_pre_depth");

        shadowMapTarget.bindWrite(true);

        for (IRenderCall renderCall : queuedRenderCallList) {
            renderCall.drawPreDepth();
        }

        shadowMapTarget.unbindWrite();

        profilerfiller.pop();
    }

    public boolean configureUniforms(CompiledShaderProgram shaderInstance, boolean shouldUpload, boolean isOIT) {
        if (shaderInstance.OIT_ENABLE != null) {
            shaderInstance.OIT_ENABLE.set(isOIT ? 1 : 0);
            if (shouldUpload) {
                shaderInstance.OIT_ENABLE.upload();
            }
        }
        return false;
    }

    /**
     * Callback to set up rendering to the OIT render target.
     *
     * @param shaderInstance The shader instance to set up.
     * @param shouldUpload Whether the uniform values should be uploaded.
     */
    private void setupShadowRendering(CompiledShaderProgram shaderInstance, boolean shouldUpload) {
        if (configureUniforms(shaderInstance, shouldUpload, false)) {
            return;
        }

        shadowMapTarget.bindWrite(true);

        RenderSystem.enableDepthTest();
        glDepthMask(true);
        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_ONE, GL_ONE, GL_ZERO, GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Callback to clean up rendering to the OIT render target.
     *
     * @param shaderInstance The shader instance to clean up.
     */
    private void cleanUpShadowRendering(CompiledShaderProgram shaderInstance) {
        configureUniforms(shaderInstance, true, false);

        glDepthMask(true);
        RenderSystem.defaultBlendFunc();
        glDisable(GL_BLEND);
    }

    /**
     * Callback to set up rendering to the OIT render target.
     *
     * @param shaderInstance The shader instance to set up.
     * @param shouldUpload Whether the uniform values should be uploaded.
     */
    private void setupTransparentRendering(CompiledShaderProgram shaderInstance, boolean shouldUpload) {
        if (configureUniforms(shaderInstance, shouldUpload, true)) {
            return;
        }

        transparentOITRenderTarget.bindWrite(true);

        shaderInstance.bindSampler(ClientHooks.SAMPLER_OIT_NAME, shadowMapTarget.getDepthTextureId());
        shaderInstance.apply();

        RenderSystem.enableDepthTest();
        glDepthMask(false);
        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_ONE, GL_ONE, GL_ZERO, GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Callback to clean up rendering to the OIT render target.
     *
     * @param shaderInstance The shader instance to clean up.
     */
    private void cleanUpTransparentRendering(CompiledShaderProgram shaderInstance) {
        configureUniforms(shaderInstance, true, false);

        glDepthMask(true);
        RenderSystem.defaultBlendFunc();
        glDisable(GL_BLEND);
    }

    /**
     * Defines a render call that can be queued up.
     */
    private interface IRenderCall {
        void drawDirect();

        void drawOIT();

        void drawPreDepth();
    }

    /**
     * Defines a render call that can be queued up, based on a render type.
     */
    private interface IRenderTypeBasedRenderCall extends IRenderCall {
        RenderType renderType();
    }

    /**
     * Defines a render call that can be queued up, based on a particle render type.
     */
    private interface IParticleRenderCall extends IRenderCall {
        ParticleRenderType particleRenderType();
    }

    /**
     * A render call for a CPU rendered buffer.
     * Captures all related information to render the buffer.
     *
     * @param renderType The render type to use.
     * @param renderedBuffer The rendered buffer to render.
     * @param modelViewMatrix The model view matrix to use.
     * @param projectionMatrix The projection matrix to use.
     */
    private record RenderedBufferRenderCall(RenderType renderType, MeshData renderedBuffer, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) implements IRenderTypeBasedRenderCall {
        @Override
        public void drawDirect()
        {
            renderType.doRender(renderedBuffer, true, (shader) -> OITLevelRenderer.getInstance().configureUniforms(shader, true, false), (shader) -> {});
        }

        @Override
        public void drawOIT()
        {
            final Matrix4f currentModelViewMatrix = new Matrix4f(RenderSystem.getModelViewMatrix());
            final Matrix4f currentProjectionMatrix = new Matrix4f(RenderSystem.getProjectionMatrix());
            renderType.doRender(renderedBuffer, true, shader ->
            {
                RenderSystem.getModelViewMatrix().set(modelViewMatrix);
                RenderSystem.getProjectionMatrix().set(projectionMatrix);
                getInstance().setupTransparentRendering(shader, false);
            }, getInstance()::cleanUpTransparentRendering);
            RenderSystem.getModelViewMatrix().set(currentModelViewMatrix);
            RenderSystem.getProjectionMatrix().set(currentProjectionMatrix);
        }

        @Override
        public void drawPreDepth() {
            final Matrix4f currentModelViewMatrix = new Matrix4f(RenderSystem.getModelViewMatrix());
            final Matrix4f currentProjectionMatrix = new Matrix4f(RenderSystem.getProjectionMatrix());
            renderType.doRender(renderedBuffer, false, shader ->
            {
                RenderSystem.getModelViewMatrix().set(modelViewMatrix);
                RenderSystem.getProjectionMatrix().set(projectionMatrix);
                getInstance().setupShadowRendering(shader, false);
            }, getInstance()::cleanUpShadowRendering);
            RenderSystem.getModelViewMatrix().set(currentModelViewMatrix);
            RenderSystem.getProjectionMatrix().set(currentProjectionMatrix);
        }
    }

    /**
     * A render call for GPU bound chunk geometry.
     * Captures all related information to render the chunk geometry.
     *
     * @param renderType The render type to use.
     * @param cameraX The camera X position.
     * @param cameraY The camera Y position.
     * @param cameraZ The camera Z position.
     * @param modelViewMatrix The model view matrix to use.
     * @param projectionMatrix The projection matrix to use.
     */
    private record ChunkGeometryRenderCall(RenderType renderType, double cameraX, double cameraY, double cameraZ, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) implements IRenderTypeBasedRenderCall {
        @Override
        public void drawDirect()
        {
            Minecraft.getInstance().levelRenderer.renderChunkGeometryForLayer(renderType, cameraX, cameraY, cameraZ, modelViewMatrix, projectionMatrix, (shader) -> OITLevelRenderer.getInstance().configureUniforms(shader, true, false), (shader) -> {});
        }

        @Override
        public void drawOIT()
        {
            Minecraft.getInstance().levelRenderer.renderChunkGeometryForLayer(renderType, cameraX, cameraY, cameraZ, modelViewMatrix, projectionMatrix, shader -> getInstance().setupTransparentRendering(shader, true), getInstance()::cleanUpTransparentRendering);
        }

        @Override
        public void drawPreDepth()
        {
            Minecraft.getInstance().levelRenderer.renderChunkGeometryForLayer(renderType, cameraX, cameraY, cameraZ, modelViewMatrix, projectionMatrix, shader -> getInstance().setupShadowRendering(shader, true), getInstance()::cleanUpShadowRendering);
        }
    }

    /**
     * A render call to render particles.
     * Captures all related information to render the particles.
     *
     * @param particleRenderType The particle render type to use.
     * @param toRender The particles to render.
     * @param clippingHelper The clipping helper to use.
     * @param camera The camera to use.
     * @param partialTickTime The partial tick time to use.
     */
    private record ParticleRenderCall(ParticleRenderType particleRenderType, Iterable<Particle> toRender, @Nullable Frustum clippingHelper, Camera camera, float partialTickTime) implements IParticleRenderCall {

        @Override
        public void drawDirect()
        {
            Minecraft.getInstance().particleEngine.renderParticlesWithType(particleRenderType, toRender, clippingHelper, camera, partialTickTime, (shader) -> OITLevelRenderer.getInstance().configureUniforms(shader, true, false), (shader) -> {});
        }

        @Override
        public void drawOIT()
        {
            Minecraft.getInstance().particleEngine.renderParticlesWithType(particleRenderType, toRender, clippingHelper, camera, partialTickTime, shader ->
            {
                //We need to explicitly enable the light layer, as it is enabled by default when rendering particles.
                Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
                getInstance().setupTransparentRendering(shader, false);
            }, getInstance()::cleanUpTransparentRendering);
        }

        @Override
        public void drawPreDepth() {
            Minecraft.getInstance().particleEngine.renderParticlesWithType(particleRenderType, toRender, clippingHelper, camera, partialTickTime, shader ->
            {
                //We need to explicitly enable the light layer, as it is enabled by default when rendering particles.
                Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
                getInstance().setupShadowRendering(shader, false);
            }, getInstance()::cleanUpShadowRendering);
        }
    }
}