/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.modlist;

import static net.minecraft.network.chat.Component.translatable;

import com.mojang.logging.LogUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.VersionChecker;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@ApiStatus.Internal
class ChangelogScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final @Nullable Screen lastScreen;
    private final ModDisplayInfo info;
    private final VersionChecker.CheckResult checkResult;

    @Nullable
    private HeaderAndFooterLayout layout;

    protected ChangelogScreen(@Nullable Screen lastScreen, ModDisplayInfo info, VersionChecker.CheckResult checkResult) {
        super(Component.translatable("neoforge.screen.mods.changelog.title", info.displayName()));
        this.lastScreen = lastScreen;
        this.info = info;
        this.checkResult = checkResult;
    }

    @Override
    protected void init() {
        super.init();

        this.layout = new HeaderAndFooterLayout(this);
        this.layout.addTitleHeader(Component.translatable("neoforge.screen.mods.changelog.title", info.displayName()), this.font);

        // Footer
        final LinearLayout footer = layout.addToFooter(new LinearLayout(0, 0, LinearLayout.Orientation.HORIZONTAL));
        footer.spacing(4).defaultCellSetting().paddingTop(5);

        final Button updateSiteButton = footer.addChild(Button.builder(translatable("neoforge.screen.mods.changelog.open_site"),
                _ -> clickUrlAction(minecraft, this, URI.create(this.checkResult.url()))).build());
        updateSiteButton.active = false;
        if (checkResult.url() != null) {
            String rawUrl = checkResult.url();
            try {
                new URI(rawUrl);
                updateSiteButton.active = true;
            } catch (URISyntaxException exception) {
                LOGGER.warn("Failed to create update site URI for mod {} update checker: {}", info.id(), rawUrl);
            }
        }
        footer.addChild(Button.builder(CommonComponents.GUI_BACK, _ -> this.onClose()).build());

        // Contents
        final LinearLayout body = new LinearLayout(0, 0, LinearLayout.Orientation.VERTICAL).spacing(4);

        if (checkResult.changes().isEmpty()) {
            body.addChild(FocusableTextWidget.builder(Component.translatable("neoforge.screen.mods.changelog.no_changelog").withStyle(ChatFormatting.ITALIC), this.font)
                    .alwaysShowBorder(false)
                    .backgroundFill(FocusableTextWidget.BackgroundFill.NEVER)
                    .maxWidth(310)
                    .build()
                    .setCentered(false));
        } else {
            for (Map.Entry<ComparableVersion, String> updateEntry : checkResult.changes().entrySet()) {
                body.addChild(FocusableTextWidget.builder(Component.translatable("neoforge.screen.mods.changelog.entry",
                        Component.literal(updateEntry.getKey().toString()).withStyle(ChatFormatting.BOLD),
                        Component.literal(updateEntry.getValue())),
                        this.font)
                        .alwaysShowBorder(false)
                        .backgroundFill(FocusableTextWidget.BackgroundFill.NEVER)
                        .maxWidth(310)
                        .build()
                        .setCentered(false));
            }
        }

        layout.addToContents(new ScrollableLayout(this.minecraft, body, this.layout.getContentHeight())).setMinWidth(310);

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        assert this.layout != null;
        this.layout.arrangeElements();
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(lastScreen);
    }
}
