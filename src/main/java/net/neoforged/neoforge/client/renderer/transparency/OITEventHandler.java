package net.neoforged.neoforge.client.renderer.transparency;

import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.renderer.ShaderProgramConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.CompiledShaderEvent;
import net.neoforged.neoforge.client.event.RegisterGlslPreprocessorsEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderTargetEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.util.List;

@ApiStatus.Internal
public class OITEventHandler {
    @EventBusSubscriber(modid = NeoForgeVersion.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ModBus {

        @SubscribeEvent
        public static void onShaderRegistration(RegisterShadersEvent registerShadersEvent) throws IOException {
            registerShadersEvent.registerShader(
                    OITLevelRenderer.WBOIT_COMPOSITION_SHADER
            );
            registerShadersEvent.registerShader(
                    OITLevelRenderer.MBOIT_COMPOSITION_SHADER
            );
        }

        @SubscribeEvent
        public static void onRenderTargetSetup(RenderTargetEvent.Create event) {
            OITLevelRenderer.getInstance().initialize(event.getWidth(), event.getHeight());
        }

        @SubscribeEvent
        public static void onShaderRegisterUniforms(CompiledShaderEvent.RegisterUniforms event) {
            event.getShader().registerCustomUniform(
                    new Uniform(ClientHooks.UNIFORM_OIT_NAME, 0, 1), //BOOL -> ONCE
                    new ShaderProgramConfig.Uniform(ClientHooks.UNIFORM_OIT_NAME, ClientHooks.UNIFORM_OIT_TYPE, 1, List.of(0.0f, 1f))
            );
            event.getShader().registerSampler(
                    new ShaderProgramConfig.Sampler(ClientHooks.SAMPLER_OIT_NAME)
            );

            //throw new IllegalStateException("Missing MBOIT uniforms");
        }

        @SubscribeEvent
        public static void onShaderProgramCompile(RegisterGlslPreprocessorsEvent event) {
            event.newProcessor(new OITGlslPreprocessor());
            //throw new IllegalStateException("Missing MBOIT preprocessor");
        }
    }

    @EventBusSubscriber(modid = NeoForgeVersion.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static class GameBus {

        @SubscribeEvent
        public static void onRenderTargetResize(RenderTargetEvent.Resize event) {
            OITLevelRenderer.getInstance().getTransparentOITRenderTarget().resize(event.getWidth(), event.getHeight());
        }
    }
}
