/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.loading.earlydisplay;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.GpuSurface;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.SurfaceException;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.earlydisplay.render.ElementShader;
import net.neoforged.fml.earlydisplay.render.backend.ELSBuffer;
import net.neoforged.fml.earlydisplay.render.backend.ELSBufferSlice;
import net.neoforged.fml.earlydisplay.render.backend.ELSRenderBackend;
import net.neoforged.fml.earlydisplay.render.backend.ELSRenderPass;
import net.neoforged.fml.earlydisplay.render.backend.ELSRenderPipeline;
import net.neoforged.fml.earlydisplay.render.backend.ELSTexture;
import net.neoforged.fml.earlydisplay.render.backend.TextureFormat;
import net.neoforged.fml.earlydisplay.theme.NativeBuffer;
import net.neoforged.fml.earlydisplay.theme.ThemeColor;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@SuppressWarnings("UnstableApiUsage")
public final class Blaze3DRenderBackend extends ELSRenderBackend {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int TEX_USAGE = GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_RENDER_ATTACHMENT;
    private static final ColorTargetState COLOR_STATE = new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA, BlendFactor.ZERO, BlendFactor.ONE));
    private static final VertexFormat FORMAT_POS = VertexFormat.builder(0)
            .addAttribute("position", GpuFormat.RG32_FLOAT)
            .build();
    private static final VertexFormat FORMAT_POS_TEX = VertexFormat.builder(0)
            .addAttribute("position", GpuFormat.RG32_FLOAT)
            .addAttribute("uv", GpuFormat.RG32_FLOAT)
            .build();
    private static final VertexFormat FORMAT_POS_COLOR = VertexFormat.builder(0)
            .addAttribute("position", GpuFormat.RG32_FLOAT)
            .addAttribute("color", GpuFormat.RGBA8_UNORM)
            .build();
    private static final VertexFormat FORMAT_POS_TEX_COLOR = VertexFormat.builder(0)
            .addAttribute("position", GpuFormat.RG32_FLOAT)
            .addAttribute("uv", GpuFormat.RG32_FLOAT)
            .addAttribute("color", GpuFormat.RGBA8_UNORM)
            .build();

    private final GpuDevice device;
    private final Window window;
    private final Map<ELSRenderPipeline, RenderPipeline> pipelines = new IdentityHashMap<>();

    public Blaze3DRenderBackend(Window window) {
        this.device = RenderSystem.getDevice();
        this.window = window;
        super();
    }

    @Override
    public void preloadPipelines(Collection<ELSRenderPipeline> pipelines) {
        for (ELSRenderPipeline elsPipeline : pipelines) {
            ElementShader shader = elsPipeline.shader();
            BindGroupLayout.Builder layoutBuilder = BindGroupLayout.builder();
            if (elsPipeline.sampler() != null) {
                layoutBuilder.withSampler(elsPipeline.sampler());
            }
            for (String uniform : elsPipeline.uniforms()) {
                layoutBuilder.withUniform(uniform, UniformType.UNIFORM_BUFFER);
            }
            RenderPipeline pipeline = RenderPipeline.builder()
                    .withLocation(makeId(shader.getName()))
                    .withVertexShader(makeId(shader.getVertexShaderPath()))
                    .withFragmentShader(makeId(shader.getFragmentShaderPath()))
                    .withPrimitiveTopology(switch (elsPipeline.vertexMode()) {
                        case TRIANGLES -> PrimitiveTopology.TRIANGLES;
                        case QUADS -> PrimitiveTopology.QUADS;
                    })
                    .withColorTargetState(COLOR_STATE)
                    .withVertexBinding(0, switch (elsPipeline.vertexFormat()) {
                        case POS -> FORMAT_POS;
                        case POS_TEX -> FORMAT_POS_TEX;
                        case POS_COLOR -> FORMAT_POS_COLOR;
                        case POS_TEX_COLOR -> FORMAT_POS_TEX_COLOR;
                    })
                    .withBindGroupLayout(layoutBuilder.build())
                    .build();
            this.device.precompilePipeline(pipeline, makeShaderSource(shader));
            this.pipelines.put(elsPipeline, pipeline);
        }
    }

    @Override
    public ELSTexture createTexture(String debugName, int width, int height, TextureFormat format, boolean linearFilter) {
        GpuFormat b3dFormat = switch (format) {
            case RGBA -> GpuFormat.RGBA8_UNORM;
            case RED -> GpuFormat.R8_UNORM;
        };
        GpuTexture texture = this.device.createTexture(debugName, TEX_USAGE, b3dFormat, width, height, 1, 1);
        GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(linearFilter ? FilterMode.LINEAR : FilterMode.NEAREST);
        return new Blaze3DTexture(format, texture, this.device.createTextureView(texture), sampler);
    }

    @Override
    public void writeToTexture(ELSTexture texture, ByteBuffer pixels) {
        this.device.createCommandEncoder().writeToTexture(((Blaze3DTexture) texture).unwrap(), pixels, 0, 0, 0, 0, texture.width(), texture.height());
    }

    @Override
    public ELSBuffer createBuffer(String label, Set<ELSBuffer.Usage> usage, long size) {
        int usageMask = Blaze3DConst.elsUsageToB3D(usage);
        return new Blaze3DBuffer(this.device.createBuffer(() -> label, usageMask, size), usage, usageMask);
    }

    @Override
    public ELSBuffer createBuffer(String label, Set<ELSBuffer.Usage> usage, ByteBuffer data) {
        int usageMask = Blaze3DConst.elsUsageToB3D(usage);
        return new Blaze3DBuffer(this.device.createBuffer(() -> label, usageMask, data), usage, usageMask);
    }

    @Override
    public void writeToBuffer(ELSBufferSlice buffer, ByteBuffer data) {
        this.device.createCommandEncoder().writeToBuffer(((Blaze3DBufferSlice) buffer).unwrap(), data);
    }

    @Override
    public void copyBufferToBuffer(ELSBufferSlice source, ELSBufferSlice destination) {
        this.device.createCommandEncoder().copyToBuffer(
                ((Blaze3DBufferSlice) source).unwrap(),
                ((Blaze3DBufferSlice) destination).unwrap());
    }

    @Override
    public ELSBuffer getQuadAutoIndexBuffer(int indexCount) {
        return new Blaze3DBuffer(RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS).getBuffer(indexCount), Set.of(ELSBuffer.Usage.INDEX), GpuBuffer.USAGE_INDEX);
    }

    @Override
    public ELSRenderPass createRenderPass(String label, ELSTexture target, ThemeColor clearColor) {
        GpuTextureView texture = ((Blaze3DTexture) target).view();
        Optional<Vector4fc> clearColorVec = Optional.of(new Vector4f(clearColor.r(), clearColor.g(), clearColor.b(), clearColor.a()));
        return new Blaze3DRenderPass(this, this.device.createCommandEncoder().createRenderPass(() -> label, texture, clearColorVec));
    }

    @Override
    public boolean startFrame(FramebufferSizeListener listener) {
        GpuSurface gpuSurface = Minecraft.getInstance().windowSurface();
        if (gpuSurface.isAcquired()) {
            return false;
        }

        if (Minecraft.getInstance().windowSurfaceNeedsReconfiguring) {
            int[] width = new int[1];
            int[] height = new int[1];
            GLFW.glfwGetFramebufferSize(this.window.handle(), width, height);
            listener.accept(width[0], height[0]);

            if (width[0] != 0 || height[0] != 0) {
                GpuSurface.PresentMode presentMode = GpuSurface.PresentMode.getSupportedVsyncMode(
                        gpuSurface.supportedPresentModes(), Minecraft.getInstance().options.enableVsync().get());
                GpuSurface.Configuration config = new GpuSurface.Configuration(width[0], height[0], presentMode);

                try {
                    gpuSurface.configure(config);
                    Minecraft.getInstance().surfaceIsInvalid = false;
                } catch (SurfaceException exception) {
                    LOGGER.warn("Couldn't configure surface to {}: {}", config, exception);
                    Minecraft.getInstance().surfaceIsInvalid = true;
                }
            }
            Minecraft.getInstance().windowSurfaceNeedsReconfiguring = false;
        }

        if (!Minecraft.getInstance().surfaceIsInvalid && !this.window.isMinimized()) {
            try {
                gpuSurface.acquireNextTexture();
            } catch (SurfaceException ex) {
                LOGGER.warn("Couldn't acquire next surface texture with config {}: {}", gpuSurface.currentConfiguration(), ex);
                Minecraft.getInstance().surfaceIsInvalid = true;
                Minecraft.getInstance().windowSurfaceNeedsReconfiguring = true;
            }
        }

        return true;
    }

    @Override
    public void presentTexture(ELSTexture texture, ThemeColor backgroundColor, int windowFBWidth, int windowFBHeight) {
        GpuSurface gpuSurface = Minecraft.getInstance().windowSurface();
        if (gpuSurface.isAcquired()) {
            gpuSurface.blitFromTexture(this.device.createCommandEncoder(), ((Blaze3DTexture) texture).view());
        }
        this.device.createCommandEncoder().submit();
        if (gpuSurface.isAcquired()) {
            gpuSurface.present();
        }
    }

    @Override
    public int getMaxTextureSize() {
        return this.device.getDeviceInfo().limits().maxTextureSize();
    }

    @Override
    public long getWindowHandle() {
        return this.window.handle();
    }

    @Override
    public void acquireContextOwnership(boolean createContext) {}

    @Override
    public void releaseContextOwnership() {}

    @Override
    public void guardResourceCleanup(Runnable cleanupTask) {
        cleanupTask.run();
    }

    @Override
    public void close() {}

    @Override
    public String name() {
        return "Blaze3D";
    }

    RenderPipeline getPipeline(ELSRenderPipeline elsPipeline) {
        RenderPipeline pipeline = this.pipelines.get(elsPipeline);
        if (pipeline == null) {
            throw new IllegalArgumentException("Unrecognized pipeline: " + elsPipeline);
        }
        return pipeline;
    }

    private static Identifier makeId(String path) {
        return Identifier.fromNamespaceAndPath("neoforge", "fml_els/" + path);
    }

    private static ShaderSource makeShaderSource(ElementShader shader) {
        return (_, type) -> {
            try {
                NativeBuffer buffer = switch (type) {
                    case VERTEX -> shader.loadVertexShader();
                    case FRAGMENT -> shader.loadFragmentShader();
                };
                return new String(buffer.toByteArray());
            } catch (IOException e) {
                return null;
            }
        };
    }
}
