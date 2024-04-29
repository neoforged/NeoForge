/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.client;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.group.Group;
import net.neoforged.testframework.impl.MutableTestFramework;

public class TestScreen extends AbstractTestScreen {
    protected EditBox searchTextField;

    private final List<Group> groups;
    protected GroupableList groupableList;
    private Consumer<String> suggestionProvider;
    protected CycleButton<Boolean> showAsGroup;
    protected CycleButton<FilterMode> filterMode;

    public TestScreen(Component title, MutableTestFramework framework, List<Group> groups) {
        super(title, framework);
        this.groups = groups;
    }

    private static boolean isGroup = true;

    @Override
    protected void init() {
        final Runnable reloader = () -> {
            groupableList.resetRows(searchTextField.getValue());
            this.groupableList.setScrollAmount(0);
        };
        this.showAsGroup = addRenderableWidget(CycleButton.booleanBuilder(Component.literal("Show groups"),
                Component.literal("Show all tests"))
                .displayOnlyValue().withInitialValue(isGroup)
                .create(20, this.height - 26, 100, 20, Component.empty(), (pCycleButton, pValue) -> {
                    reloader.run();
                    isGroup = pValue;
                }));
        this.filterMode = addRenderableWidget(CycleButton.<FilterMode>builder(mode -> mode.name)
                .withValues(FilterMode.values()).create((this.width - 160) / 2, this.height - 26, 150, 20, Component.literal("Filter"), (pCycleButton, pValue) -> reloader.run()));

        final List<Test> tests = groups.stream().flatMap(it -> it.resolveAll().stream()).distinct().toList();
        this.groupableList = new GroupableList(() -> showAsGroup.getValue(), groups, () -> tests.stream()
                .filter(test -> filterMode.getValue().test(framework, test)), minecraft, width, height - 90, 50, 9 + 2 + 2 + 2);
        this.suggestionProvider = s -> {
            if (showAsGroup.getValue()) {
                updateSearchTextFieldSuggestion(this.searchTextField, s, groups, gr -> gr.title().getString());
            } else {
                updateSearchTextFieldSuggestion(this.searchTextField, s, tests, test -> test.visuals().title().getString());
            }
        };

        groupableList.resetRows("");
        this.addRenderableWidget(groupableList);

        this.searchTextField = new EditBox(this.font, this.width / 2 - 110, 22, 220, 20, Component.literal("Search"));
        this.searchTextField.setResponder(s -> {
            suggestionProvider.accept(s);
            groupableList.resetRows(s.toLowerCase(Locale.ROOT));
            if (!s.isEmpty()) {
                this.groupableList.setScrollAmount(0);
            }
        });
        this.addWidget(searchTextField);

        addRenderableWidget(Button.builder(Component.literal("Disable"), pButton -> groupableList.getSelected().enable(false)).bounds(searchTextField.getX() - 43, searchTextField.getY(), 40, 20).build(builder -> new Button(builder) {
            @Override
            public void renderWidget(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
                this.active = groupableList != null && groupableList.getSelected() != null && groupableList.getSelected().canDisable();
                super.renderWidget(pPoseStack, pMouseX, pMouseY, pPartialTick);
            }
        }));
        addRenderableWidget(Button.builder(Component.literal("Enable"), pButton -> groupableList.getSelected().enable(true)).bounds(searchTextField.getX() + searchTextField.getWidth() + 3, searchTextField.getY(), 40, 20).build(builder -> new Button(builder) {
            @Override
            public void renderWidget(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
                this.active = groupableList != null && groupableList.getSelected() != null && groupableList.getSelected().canEnable();
                super.renderWidget(pPoseStack, pMouseX, pMouseY, pPartialTick);
            }
        }));
    }

    @Override
    public void render(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (showAsGroup.getValue()) {
            filterMode.visible = false;
            filterMode.active = false;
        } else {
            filterMode.visible = true;
            filterMode.active = true;
        }

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        searchTextField.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        pPoseStack.drawCenteredString(font, getTitle(), this.width / 2, 7, 0xffffff);
    }

    public static <T> void updateSearchTextFieldSuggestion(EditBox editBox, String value, List<T> entries, Function<T, String> nameProvider) {
        if (!value.isEmpty()) {
            Optional<? extends String> optional = entries.stream().filter(info -> nameProvider.apply(info).toLowerCase(Locale.ROOT).startsWith(value.toLowerCase(Locale.ROOT))).map(nameProvider).min(Comparator.naturalOrder());
            if (optional.isPresent()) {
                int length = value.length();
                String displayName = optional.get();
                editBox.setSuggestion(displayName.substring(length));
            } else {
                editBox.setSuggestion("");
            }
        } else {
            editBox.setSuggestion("Search");
        }
    }

    public enum FilterMode implements BiPredicate<MutableTestFramework, Test> {
        ALL("All") {
            @Override
            public boolean test(MutableTestFramework framework, Test test) {
                return true;
            }
        },
        NOT_PROCESSED("Not Processed") {
            @Override
            public boolean test(MutableTestFramework framework, Test test) {
                return framework.tests().getStatus(test.id()).result() == Test.Result.NOT_PROCESSED;
            }
        },
        PASSED("Passed") {
            @Override
            public boolean test(MutableTestFramework framework, Test test) {
                return framework.tests().getStatus(test.id()).result().passed();
            }
        },
        FAILED("Failed") {
            @Override
            public boolean test(MutableTestFramework framework, Test test) {
                return framework.tests().getStatus(test.id()).result() == Test.Result.FAILED;
            }
        },
        ENABLED("Enabled") {
            @Override
            public boolean test(MutableTestFramework framework, Test test) {
                return framework.tests().isEnabled(test.id());
            }
        },
        DISABLED("Disabled") {
            @Override
            public boolean test(MutableTestFramework framework, Test test) {
                return !framework.tests().isEnabled(test.id());
            }
        };

        private Component name;

        FilterMode(String name) {
            this.name = Component.literal(name);
        }
    }
}
