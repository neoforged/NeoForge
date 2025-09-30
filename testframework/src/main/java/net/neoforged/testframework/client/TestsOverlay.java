/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.client;

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
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.impl.MutableTestFramework;

public final class TestsOverlay implements GuiLayer {
    public static final int MAX_DISPLAYED = 5;
    public static final ResourceLocation BG_TEXTURE = ResourceLocation.fromNamespaceAndPath("testframework", "background");

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
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!enabled.getAsBoolean()) return;

        List<Test> enabled = impl.tests().enabled().collect(Collectors.toCollection(ArrayList::new));
        if (enabled.isEmpty()) return;

        final Font font = Minecraft.getInstance().font;
        final int startX = 10, startY = 10;
        final int maxWidth = graphics.guiWidth() / 3;
        int x = startX, y = startY;
        int maxX = x;

        final CommitBasedList<Runnable> renderingQueue = new CommitBasedList<>(new ArrayList<>());
        final Component title = Component.literal("Tests overlay for ").append(Component.literal(impl.id().toString()).withStyle(ChatFormatting.AQUA));
        renderingQueue.addDirectly(withXY(x, y, (x$, y$) -> graphics.drawString(font, title, x$, y$, 0xffffffff)));
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

                    final XY xy = renderTest(font, test, graphics, maxWidth, x, y, ((int) (fade * 255f) << 24) | 0xffffff, renderingQueue.currentProgress());
                    y = xy.y() + 5;
                    maxX = Math.max(maxX, xy.x());

                    fading.put(test, fade);
                } else {
                    final XY xy = renderTest(font, test, graphics, maxWidth, x, y, 0xffffffff, renderingQueue.currentProgress());
                    y = xy.y() + 5;
                    maxX = Math.max(maxX, xy.x());
                }

                if (y >= graphics.guiHeight()) {
                    int endIndex = actuallyToRender.indexOf(test) + 1;
                    // If the y is greater than the height, don't render this test at all
                    if (y > graphics.guiHeight()) {
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
                final XY xy = renderTest(font, test, graphics, maxWidth, x, y, 0xffffffff, renderingQueue.currentProgress());
                y = xy.y() + 5;
                maxX = Math.max(maxX, xy.x());

                if (y >= graphics.guiHeight()) {
                    int endIndex = enabled.indexOf(test) + 1;
                    // If the y is greater than the height, don't render this test at all
                    if (y > graphics.guiHeight()) {
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

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BG_TEXTURE, startX - 4, startY - 4, (maxX - startX) + 4 + 4, (y - startY) + 4, 0x7FFFFFFF);
        renderingQueue.forEach(Runnable::run);
    }

    static final Map<Test.Result, ResourceLocation> ICON_BY_RESULT = new EnumMap<>(Map.of(
            Test.Result.FAILED, ResourceLocation.fromNamespaceAndPath("testframework", "test_failed"),
            Test.Result.PASSED, ResourceLocation.fromNamespaceAndPath("testframework", "test_passed"),
            Test.Result.NOT_PROCESSED, ResourceLocation.fromNamespaceAndPath("testframework", "test_not_processed")));

    // TODO - maybe "group" together tests in the same group?
    private XY renderTest(Font font, Test test, GuiGraphics graphics, int maxWidth, int x, int y, int colour, List<Runnable> rendering) {
        final Test.Status status = impl.tests().getStatus(test.id());
        final FormattedCharSequence bullet = Component.literal("- ").withStyle(ChatFormatting.BLACK).getVisualOrderText();
        rendering.add(withXY(x, y, (x$, y$) -> graphics.drawString(font, bullet, x$, y$ - 1, colour)));
        x += font.width(bullet) + 1;

        rendering.add(withXY(x, y, (x$, y$) -> graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ICON_BY_RESULT.get(status.result()), x$, y$, 9, 9)));
        x += 11;

        final Component title = statusColoured(test.visuals().title(), status);
        rendering.add(withXY(x, y, (x$, y$) -> graphics.drawString(font, title, x$, y$, colour)));

        final List<Component> extras = new ArrayList<>();
        if (Minecraft.getInstance().hasShiftDown()) extras.addAll(test.visuals().description());
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
                rendering.add(withXY(x, y, (x$, y$) -> graphics.drawString(font, extra, x$, y$, 0xffffffff)));
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

    @FunctionalInterface
    public interface IntBiConsumer {
        void accept(int x, int y);
    }
}
