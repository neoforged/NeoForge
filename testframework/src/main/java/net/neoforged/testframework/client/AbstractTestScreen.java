/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.client;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.group.Group;
import net.neoforged.testframework.impl.MutableTestFramework;
import org.lwjgl.glfw.GLFW;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class AbstractTestScreen extends Screen {
    protected final MutableTestFramework framework;
    private final Screen outer = this;

    public AbstractTestScreen(Component title, MutableTestFramework framework) {
        super(title);
        this.framework = framework;
    }

    protected final class GroupableList extends ObjectSelectionList<GroupableList.Entry> {
        private final Function<String, List<? extends Entry>> entryGetter;

        public GroupableList(Function<String, List<? extends Entry>> entryGetter, Minecraft pMinecraft, int pWidth, int pHeight, int pY, int pItemHeight) {
            super(pMinecraft, pWidth, pHeight, pY, pItemHeight);
            this.entryGetter = entryGetter;
        }

        public GroupableList(BooleanSupplier isGrouped, List<Group> groups, Supplier<Stream<Test>> tests, Minecraft pMinecraft, int pWidth, int pHeight, int pY, int pItemHeight) {
            super(pMinecraft, pWidth, pHeight, pY, pItemHeight);
            this.entryGetter = search -> isGrouped.getAsBoolean()
                    ? groups.stream()
                            .filter(it -> it.title().getString().toLowerCase(Locale.ROOT).contains(search))
                            .sorted(Comparator.comparing(gr -> gr.title().getString()))
                            .map(GroupEntry::new).toList()
                    : withGroups(tests.get()
                            .filter(it -> it.visuals().title().getString().toLowerCase(Locale.ROOT).contains(search))
                            .sorted(Comparator.comparing(test -> test.visuals().title().getString())), groups).toList();
        }

        public void resetRows(String search) {
            this.clearEntries();
            entryGetter.apply(search).forEach(this::addEntry);
        }

        @Override
        protected boolean isValidClickButton(MouseButtonInfo buttonInfo) {
            int button = buttonInfo.button();
            return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT || button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
        }

        @Override
        protected int scrollBarX() {
            return this.width / 2 + 144;
        }

        @Override
        public int getRowWidth() {
            return 260;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
            super.renderWidget(graphics, pMouseX, pMouseY, pPartialTick);
            renderTooltips(graphics, pMouseX, pMouseY);
        }

        private void renderTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
            if (this.isMouseOver(mouseX, mouseY)) {
                Entry entry = this.getEntryAtPosition(mouseX, mouseY);
                if (entry != null) {
                    entry.renderTooltips(graphics, mouseX, mouseY);
                }
            }
        }

        private Stream<? extends Entry> withGroups(Stream<Test> tests, List<Group> groups) {
            final Group parent = groups.size() == 1 ? groups.get(0) : null;
            final ListMultimap<String, Test> withParent = tests.collect(Multimaps.toMultimap(
                    test -> test.groups().size() < 1 ? "ungrouped" : test.groups().get(0),
                    Function.identity(),
                    () -> Multimaps.newListMultimap(new LinkedHashMap<>(), ArrayList::new)));
            final Predicate<String> isUngrouped = it -> it.equals("ungrouped") || (parent != null && it.equals(parent.id()));
            final Comparator<String> stringComparator = Comparator.naturalOrder();
            return withParent.asMap().entrySet()
                    .stream().<Map.Entry<Group, Collection<Test>>>map(entry -> new AbstractMap.SimpleEntry<>(framework.tests().getOrCreateGroup(entry.getKey()), entry.getValue()))
                    .sorted((o1, o2) -> {
                        if (isUngrouped.test(o1.getKey().id())) return -1;
                        else if (isUngrouped.test(o2.getKey().id())) return 1;
                        return stringComparator.compare(o1.getKey().title().getString(), o2.getKey().title().getString());
                    })
                    .flatMap(entry -> {
                        if (isUngrouped.test(entry.getKey().id())) {
                            return entry.getValue().stream().map(TestEntry::new);
                        } else {
                            return Stream.concat(
                                    Stream.of(new GroupEntry(entry.getKey(), true)),
                                    entry.getValue().stream().map(TestEntry::new));
                        }
                    });
        }

        protected abstract sealed class Entry extends ObjectSelectionList.Entry<Entry> permits TestEntry, GroupEntry {
            @Override
            public Component getNarration() {
                return Component.empty();
            }

            public abstract boolean isEnabled();

            public boolean canDisable() {
                return isEnabled();
            }

            public boolean canEnable() {
                return !isEnabled();
            }

            public abstract void enable(boolean enable);

            public abstract void reset();

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean p_432750_) {
                if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    setSelected(this);
                    return true;
                } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    enable(!isEnabled());
                } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                    reset();
                }
                return false;
            }

            protected void renderTooltips(GuiGraphics graphics, int mouseX, int mouseY) {}
        }

        protected final class TestEntry extends Entry {
            private final Test test;

            private TestEntry(Test test) {
                this.test = test;
            }

            @Override
            public void renderContent(GuiGraphics graphics, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
                final Test.Status status = framework.tests().getStatus(test.id());

                final int alpha = 0x73000000;
                final boolean renderTransparent = !isEnabled();

                ResourceLocation icon = TestsOverlay.ICON_BY_RESULT.get(status.result());
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, getContentX(), getContentY(), 9, 9, renderTransparent ? (alpha | 0x00FFFFFF) : 0xFFFFFFFF);

                final Component title = TestsOverlay.statusColoured(test.visuals().title(), status);
                graphics.drawString(font, title, getContentX() + 11, getContentY(), renderTransparent ? (alpha | 0x00FFFFFF) : 0xFFFFFFFF);
            }

            @Override
            protected void renderTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
                final List<FormattedCharSequence> tooltip = new ArrayList<>();
                if (!isEnabled()) {
                    tooltip.add(Component.literal("DISABLED").withStyle(ChatFormatting.GRAY).getVisualOrderText());
                }

                final Test.Status status = framework.tests().getStatus(test.id());
                if (!status.message().isBlank()) {
                    tooltip.add(Component.literal("!!! ").append(status.message()).withStyle(ChatFormatting.RED).getVisualOrderText());
                }

                for (final Component desc : test.visuals().description()) {
                    tooltip.addAll(font.split(desc, 200));
                }

                if (!tooltip.isEmpty()) {
                    graphics.setTooltipForNextFrame(font, tooltip, mouseX, mouseY);
                }
            }

            @Override
            public boolean isEnabled() {
                return framework.tests().isEnabled(test.id());
            }

            @Override
            public void enable(boolean enable) {
                framework.setEnabled(test, enable, minecraft.player);
            }

            @Override
            public void reset() {
                framework.changeStatus(test, Test.Status.DEFAULT, null);
            }
        }

        protected final class GroupEntry extends Entry {
            private final Group group;
            private final Button browseButton;
            private final boolean isTitle;

            private GroupEntry(Group group) {
                this(group, false);
            }

            private GroupEntry(Group group, boolean isTitle) {
                this.group = group;
                this.isTitle = isTitle;
                this.browseButton = Button.builder(Component.literal("Browse"), button -> openBrowseGUI())
                        .size(50, 12)
                        .pos(0, 0).build();
                if (isTitle) {
                    browseButton.active = false;
                    browseButton.visible = false;
                }
            }

            @Override
            public void renderContent(GuiGraphics graphics, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
                if (isTitle) {
                    graphics.drawCenteredString(font, getTitle(), getContentXMiddle(), getContentY(), 0xffffffff);
                } else {
                    graphics.drawString(font, getTitle(), getX() + 11, getContentY(), 0xffffffff);
                    this.browseButton.setX(getX() + getWidth() - 53);
                    this.browseButton.setY(getY() - 1);
                    // TODO 1.21.6: nextStratum instead? Was increasing z by 100 before.
                    browseButton.render(graphics, pMouseX, pMouseY, pPartialTick);
                }
            }

            @Override
            protected void renderTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
                if (isTitle) return;
                final List<Test> all = group.resolveAll();
                final int enabledCount = (int) all.stream().filter(it -> framework.tests().isEnabled(it.id())).count();
                if (enabledCount == all.size()) {
                    graphics.setTooltipForNextFrame(font, Component.literal("All tests in group (" + all.size() + ") are enabled!").withStyle(ChatFormatting.GREEN), mouseX, mouseY);
                } else if (enabledCount == 0) {
                    graphics.setTooltipForNextFrame(font, Component.literal("All tests in group (" + all.size() + ") are disabled!").withStyle(ChatFormatting.GRAY), mouseX, mouseY);
                } else {
                    graphics.setTooltipForNextFrame(font, Component.literal(enabledCount + "/" + all.size() + " tests enabled!").withStyle(ChatFormatting.BLUE), mouseX, mouseY);
                }
            }

            private Component getTitle() {
                return group.title();
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean p_432750_) {
                if (isTitle) return false;

                if (browseButton.isMouseOver(event.x(), event.y())) return browseButton.mouseClicked(event, p_432750_);
                if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && (event.hasShiftDown() || event.hasControlDown())) {
                    openBrowseGUI();
                    return false;
                }
                return super.mouseClicked(event, p_432750_);
            }

            private void openBrowseGUI() {
                Minecraft.getInstance().setScreen(new TestScreen(
                        Component.literal("Tests of group ").append(getTitle()),
                        framework, List.of(group)) {
                    @Override
                    protected void init() {
                        super.init();
                        showAsGroup.visible = false;
                        showAsGroup.active = false;
                        showAsGroup.setValue(false);
                        groupableList.resetRows("");

                        addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (p_97691_) -> minecraft.setScreen(outer))
                                .size(60, 20)
                                .pos(this.width - 20 - 60, this.height - 29)
                                .build());
                    }
                });
            }

            @Override
            public boolean isEnabled() {
                return group.resolveAll().stream().allMatch(it -> framework.tests().isEnabled(it.id()));
            }

            @Override
            public boolean canDisable() {
                return group.resolveAll().stream().anyMatch(it -> framework.tests().isEnabled(it.id()));
            }

            @Override
            public boolean canEnable() {
                return group.resolveAll().stream().anyMatch(it -> !framework.tests().isEnabled(it.id()));
            }

            @Override
            public void enable(boolean enable) {
                group.resolveAll().forEach(test -> framework.setEnabled(test, enable, null));
            }

            @Override
            public void reset() {
                group.resolveAll().forEach(test -> framework.changeStatus(test, Test.Status.DEFAULT, null));
            }
        }
    }
}
