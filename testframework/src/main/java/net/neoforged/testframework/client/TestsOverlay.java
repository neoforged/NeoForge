/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.client;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.gui.overlay.ExtendedGui;
import net.neoforged.neoforge.client.gui.overlay.IGuiOverlay;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.impl.MutableTestFramework;

public final class TestsOverlay implements IGuiOverlay {
    public static final int MAX_DISPLAYED = 5;
    public static final ResourceLocation BG_TEXTURE = new ResourceLocation("testframework", "textures/gui/background.png");

    private final MutableTestFramework impl;
    private final BooleanSupplier enabled;

    private final Object2FloatMap<Test> fading = new Object2FloatOpenHashMap<>();
    private final List<Test> lastRenderedTests = new ArrayList<>(MAX_DISPLAYED);

    public TestsOverlay(MutableTestFramework impl, BooleanSupplier enabled) {
        this.impl = impl;
        this.enabled = enabled;
        fading.defaultReturnValue(1f);
    }

    @Override
    public void render(ExtendedGui gui, GuiGraphics poseStack, float partialTick, int screenWidth, int screenHeight) {
        if (!enabled.getAsBoolean()) return;

        List<Test> enabled = impl.tests().enabled().collect(Collectors.toCollection(ArrayList::new));
        if (enabled.isEmpty()) return;

        final Font font = gui.getFont();
        final int startX = 10, startY = 10;
        final int maxWidth = screenWidth / 3;
        int x = startX, y = startY;
        int maxX = x;

        final CommitBasedList<Runnable> renderingQueue = new CommitBasedList<>(new ArrayList<>());
        final Component title = Component.literal("Tests overlay for ").append(Component.literal(impl.id().toString()).withStyle(ChatFormatting.AQUA));
        renderingQueue.addDirectly(withXY(x, y, (x$, y$) -> poseStack.drawString(font, title, x$, y$, 0xffffff)));
        y += font.lineHeight + 5;
        maxX += font.width(title);

        if (enabled.size() > MAX_DISPLAYED) {
            // In this case, we only render the first 5 which are NOT passed
            // But keeping the last completed ones, if present, and fading them out
            // TODO - may need to tweak this logic to only fade ONLY IF the amount of tests not passed is >= 5
            final Map<Test, Integer> lastCompleted = lastRenderedTests.stream()
                    .filter(it -> impl.tests().getStatus(it.id()).result() == Test.Result.PASSED)
                    .collect(Collectors.toMap(Function.identity(), lastRenderedTests::indexOf));

            List<Test> actuallyToRender = new ArrayList<>(MAX_DISPLAYED);
            List<Test> finalActuallyToRender = actuallyToRender;
            for (int i = 0; i < MAX_DISPLAYED; i++) actuallyToRender.add(null);
            lastCompleted.forEach((test, index) -> finalActuallyToRender.set(index, test));
            enabled.stream()
                    .filter(it -> impl.tests().getStatus(it.id()).result() != Test.Result.PASSED)
                    .limit(MAX_DISPLAYED - lastCompleted.size())
                    .forEach(it -> finalActuallyToRender.set(finalActuallyToRender.indexOf(null), it));

            int nullIndex;
            while ((nullIndex = actuallyToRender.indexOf(null)) >= 0) {
                actuallyToRender.remove(nullIndex);
            }

            for (final Test test : List.copyOf(actuallyToRender)) {
                // If we find one that isn't passed, we need to start fading it out
                renderingQueue.push();
                int lastY = y;
                int lastMaxX = maxX;
                if (impl.tests().getStatus(test.id()).result() == Test.Result.PASSED) {
                    final float fade = fading.computeIfAbsent(test, it -> 1f) - 0.005f;
                    if (fade <= 0) {
                        fading.removeFloat(test);
                        actuallyToRender.remove(test);
                        continue; // We don't need to render this one anymore, hurray!
                    }

                    renderingQueue.add(() -> {
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                    });

                    final XY xy = renderTest(gui, font, test, poseStack, maxWidth, x, y, ((int) (fade * 255f) << 24) | 0xffffff, renderingQueue.currentProgress());
                    y = xy.y() + 5;
                    maxX = Math.max(maxX, xy.x());

                    renderingQueue.add(RenderSystem::disableBlend);
                    fading.put(test, fade);
                } else {
                    final XY xy = renderTest(gui, font, test, poseStack, maxWidth, x, y, 0xffffff, renderingQueue.currentProgress());
                    y = xy.y() + 5;
                    maxX = Math.max(maxX, xy.x());
                }

                if (y >= screenHeight) {
                    int endIndex = actuallyToRender.indexOf(test) + 1;
                    // If the y is greater than the height, don't render this test at all
                    if (y > screenHeight) {
                        endIndex--;
                        renderingQueue.revert();
                        y = lastY;
                        maxX = lastMaxX;
                    }
                    // Otherwise, break and render this test
                    else {
                        renderingQueue.popAndCommit();
                    }
                    actuallyToRender = actuallyToRender.subList(0, endIndex);
                    break;
                } else {
                    renderingQueue.popAndCommit();
                }
            }

            lastRenderedTests.clear();
            lastRenderedTests.addAll(actuallyToRender);
        } else {
            for (final Test test : enabled) {
                int lastY = y;
                int lastMaxX = maxX;
                renderingQueue.push();
                final XY xy = renderTest(gui, font, test, poseStack, maxWidth, x, y, 0xffffff, renderingQueue.currentProgress());
                y = xy.y() + 5;
                maxX = Math.max(maxX, xy.x());

                if (y >= screenHeight) {
                    int endIndex = enabled.indexOf(test) + 1;
                    // If the y is greater than the height, don't render this test at all
                    if (y > screenHeight) {
                        renderingQueue.revert();
                        y = lastY;
                        maxX = lastMaxX;
                        endIndex--;
                    }
                    // Otherwise, break and render this test
                    else {
                        renderingQueue.popAndCommit();
                    }
                    enabled = enabled.subList(0, endIndex);
                    break;
                } else {
                    renderingQueue.popAndCommit();
                }
            }
            lastRenderedTests.clear();
            lastRenderedTests.addAll(enabled);
        }

        maxX += 3;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        renderTilledTexture(poseStack, BG_TEXTURE, startX - 4, startY - 4, (maxX - startX) + 4 + 4, (y - startY) + 4, 4, 4, 256, 256, .5f);
        renderingQueue.forEach(Runnable::run);

        RenderSystem.disableBlend();
    }

    static final Map<Test.Result, ResourceLocation> ICON_BY_RESULT = new EnumMap<>(Map.of(
            Test.Result.FAILED, new ResourceLocation("testframework", "textures/gui/test_failed.png"),
            Test.Result.PASSED, new ResourceLocation("testframework", "textures/gui/test_passed.png"),
            Test.Result.NOT_PROCESSED, new ResourceLocation("testframework", "textures/gui/test_not_processed.png")));

    // TODO - maybe "group" together tests in the same group?
    private XY renderTest(ExtendedGui gui, Font font, Test test, GuiGraphics stack, int maxWidth, int x, int y, int colour, List<Runnable> rendering) {
        final Test.Status status = impl.tests().getStatus(test.id());
        final FormattedCharSequence bullet = Component.literal("- ").withStyle(ChatFormatting.BLACK).getVisualOrderText();
        rendering.add(withXY(x, y, (x$, y$) -> stack.drawString(font, bullet, x$, y$ - 1, colour)));
        x += font.width(bullet) + 1;

        rendering.add(withXY(x, y, (x$, y$) -> stack.blit(ICON_BY_RESULT.get(status.result()), x$, y$, 0, 0, 9, 9, 9, 9)));
        x += 11;

        final Component title = statusColoured(test.visuals().title(), status);
        rendering.add(withXY(x, y, (x$, y$) -> stack.drawString(font, title, x$, y$, colour)));

        final List<Component> extras = new ArrayList<>();
        if (Screen.hasShiftDown()) extras.addAll(test.visuals().description());
        if (status.result() != Test.Result.PASSED && !status.message().isBlank()) {
            extras.add(Component.literal("!!! " + status.message()).withStyle(ChatFormatting.RED));
        }

        int maxX = x;
        y += font.lineHeight + 2;
        if (!extras.isEmpty()) {
            x += 6;
            Iterator<FormattedCharSequence> charSequences = extras.stream()
                    .flatMap(it -> font.split(it, maxWidth).stream())
                    .iterator();
            while (charSequences.hasNext()) {
                final FormattedCharSequence extra = charSequences.next();
                rendering.add(withXY(x, y, (x$, y$) -> stack.drawString(font, extra, x$, y$, 0xffffff)));
                y += font.lineHeight;
                maxX = Math.max(maxX, x + font.width(extra));
            }
        }
        return new XY(maxX, y);
    }

    private record XY(int x, int y) {}

    private Runnable withXY(int x, int y, IntBiConsumer consumer) {
        return () -> consumer.accept(x, y);
    }

    static MutableComponent statusColoured(Component input, Test.Status status) {
        return switch (status.result()) {
            case PASSED -> input.copy().withStyle(ChatFormatting.GREEN);
            case FAILED -> input.copy().withStyle(ChatFormatting.RED);
            case NOT_PROCESSED -> input.copy();
        };
    }

    private static void renderTilledTexture(GuiGraphics pose, ResourceLocation texture, int x, int y, int width, int height, int borderWidth, int borderHeight, int textureWidth, int textureHeight, float alpha) {
        final var sideWidth = Math.min(borderWidth, width / 2);
        final var sideHeight = Math.min(borderHeight, height / 2);

        final var leftWidth = sideWidth < borderWidth ? sideWidth + (width % 2) : sideWidth;
        final var topHeight = sideHeight < borderHeight ? sideHeight + (height % 2) : sideHeight;

        // Calculate texture centre
        final int textureCentreWidth = textureWidth - borderWidth * 2,
                textureCenterHeight = textureHeight - borderHeight * 2;
        final int centreWidth = width - leftWidth - sideWidth,
                centerHeight = height - topHeight - sideHeight;

        // Calculate the corner positions
        final var leftEdgeEnd = x + leftWidth;
        final var rightEdgeStart = leftEdgeEnd + centreWidth;
        final var topEdgeEnd = y + topHeight;
        final var bottomEdgeStart = topEdgeEnd + centerHeight;
        RenderSystem.setShaderTexture(0, texture);
        ClientUtils.setupAlpha(alpha);

        // Top Left Corner
        ClientUtils.blitAlphaSimple(pose, x, y, 0, 0, leftWidth, topHeight, textureWidth, textureHeight);
        // Bottom Left Corner
        ClientUtils.blitAlphaSimple(pose, x, bottomEdgeStart, 0, textureHeight - sideHeight, leftWidth, sideHeight, textureWidth, textureHeight);

        // Render the Middle
        if (centreWidth > 0) {
            // Top Middle
            blitTiled(pose, leftEdgeEnd, y, centreWidth, topHeight, borderWidth, 0, textureCentreWidth, borderHeight, textureWidth, textureHeight, texture);
            if (centerHeight > 0) {
                // Centre
                blitTiled(pose, leftEdgeEnd, topEdgeEnd, centreWidth, centerHeight, borderWidth, borderHeight, textureCentreWidth, textureCenterHeight, textureWidth, textureHeight, texture);
            }
            // Bottom Middle
            blitTiled(pose, leftEdgeEnd, bottomEdgeStart, centreWidth, sideHeight, borderWidth, textureHeight - sideHeight, textureCentreWidth, borderHeight, textureWidth, textureHeight, texture);
        }

        if (centerHeight > 0) {
            // Left Middle
            blitTiled(pose, x, topEdgeEnd, leftWidth, centerHeight, 0, borderHeight, borderWidth, textureCenterHeight, textureWidth, textureHeight, texture);
            // Right Middle
            blitTiled(pose, rightEdgeStart, topEdgeEnd, sideWidth, centerHeight, textureWidth - sideWidth, borderHeight, borderWidth, textureCenterHeight, textureWidth, textureHeight, texture);
        }

        // Top Right Corner
        ClientUtils.blitAlphaSimple(pose, rightEdgeStart, y, textureWidth - sideWidth, 0, sideWidth, topHeight, textureWidth, textureHeight);
        // Bottom Right Corner
        ClientUtils.blitAlphaSimple(pose, rightEdgeStart, bottomEdgeStart, textureWidth - sideWidth, textureHeight - sideHeight, sideWidth, sideHeight, textureWidth, textureHeight);
        ClientUtils.disableAlpha();
    }

    private static void blitTiled(GuiGraphics pose, int x, int y, int width, int height, int u, int v, int textureDrawWidth, int textureDrawHeight, int textureWidth, int textureHeight, ResourceLocation texture) {
        // Calculate the amount of tiles
        final int xTiles = (int) Math.ceil((float) width / textureDrawWidth),
                yTiles = (int) Math.ceil((float) height / textureDrawHeight);

        var drawWidth = width;
        var drawHeight = height;
        for (var tileX = 0; tileX < xTiles; tileX++) {
            for (var tileY = 0; tileY < yTiles; tileY++) {
                final var renderWidth = Math.min(drawWidth, textureDrawWidth);
                final var renderHeight = Math.min(drawHeight, textureDrawHeight);
                pose.blit(texture, x + textureDrawWidth * tileX, y + textureDrawHeight * tileY, u, v, renderWidth, renderHeight, textureWidth, textureHeight);
                // We rendered a tile
                drawHeight -= textureDrawHeight;
            }
            drawWidth -= textureDrawWidth;
            drawHeight = height;
        }
    }

    @FunctionalInterface
    public interface IntBiConsumer {
        void accept(int x, int y);
    }
}
