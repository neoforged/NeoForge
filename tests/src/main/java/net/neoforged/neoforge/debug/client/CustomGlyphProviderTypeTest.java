/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.gui.font.providers.GlyphProviderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

@ForEachTest(side = Dist.CLIENT, groups = { "client.fonts", "client" })
public class CustomGlyphProviderTypeTest {
    public static final EnumProxy<GlyphProviderType> REFERENCE_2_PARAMS = new EnumProxy<>(
            GlyphProviderType.class, "neotests:reference_2", Reference2.CODEC);

    public static final ResourceLocation LISTENER_NAME = ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "glyph_test");

    @TestHolder(description = "Tests if custom GlyphProviderTypes were used for loading resources", enabledByDefault = true)
    static void setupGlyphProviderTypeTest(DynamicTest test) {
        test.framework().modEventBus().addListener((AddClientReloadListenersEvent event) -> event.addListener(LISTENER_NAME, new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager p_10796_, ProfilerFiller p_10797_) {
                return null;
            }

            @Override
            protected void apply(Void p_10793_, ResourceManager p_10794_, ProfilerFiller p_10795_) {
                final Minecraft minecraft = Minecraft.getInstance();
                final MutableComponent component = Component.literal("iiiii");
                final int vanillaWidth = minecraft.font.width(component.withStyle(s -> s
                        .withFont(new FontDescription.Resource(ResourceLocation.withDefaultNamespace("uniform")))));
                final int moddedWidth = minecraft.font.width(component.withStyle(s -> s
                        .withFont(new FontDescription.Resource(ResourceLocation.fromNamespaceAndPath("custom_glyph_provider_type_test", "vanilla")))));

                if (moddedWidth != vanillaWidth) {
                    test.fail("Width of modded text is " + moddedWidth + ", but " + vanillaWidth + " was expected.");
                    return;
                }
                test.pass();
            }
        }));
    }

    public record Reference2(ResourceLocation what) implements GlyphProviderDefinition {
        public static final MapCodec<Reference2> CODEC = ResourceLocation.CODEC.fieldOf("what")
                .xmap(Reference2::new, Reference2::what);

        @Override
        public GlyphProviderType type() {
            return REFERENCE_2_PARAMS.getValue();
        }

        @Override
        public Either<Loader, Reference> unpack() {
            return Either.right(new Reference(what));
        }
    }
}
