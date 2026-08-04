/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.modlist;

import static net.minecraft.network.chat.Component.translatable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.time.Month;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.EqualSpacingLayout;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.LinearLayout.Orientation;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonLinks;
import net.minecraft.util.SpecialDates;
import net.minecraft.util.Util;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.VersionChecker;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.widget.BackgroundWithPipingWidget;
import net.neoforged.neoforge.client.gui.widget.ResizableTextureImageWidget;
import net.neoforged.neoforge.client.gui.widget.SolidColorWidget;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@ApiStatus.Internal
public class ModListScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int SIDEBAR_CONTROLS_WIDTH = 150;
    private static final int SIDEBAR_SORT_BUTTON_WIDTH = 50;
    private static final int SIDEBAR_CONTROLS_HEIGHT = Button.DEFAULT_HEIGHT;
    private static final int SIDEBAR_MODS_LIST_WIDTH = 150;
    static final int INFO_PANEL_WIDTH = 250;
    private static final int INFO_PANEL_FRAME_PADDING = 2;
    static final int ICON_SIZE = 24;
    static final int BANNER_HEIGHT = 50;

    private final @Nullable Screen lastScreen;
    private final ImmutableList<ModDisplayInfo> mods;
    private final Path modsFolder;
    private final ConfigurationScreenFactory configFactory;
    private final VersionCheckResultSupplier versionCheck;

    private final List<ModsList.Entry> allEntries;
    private SortType currentSort = SortType.A_TO_Z;

    @Nullable
    private HeaderAndFooterLayout layout;
    @Nullable
    private EqualSpacingLayout fixedSidebarLayout;
    @Nullable
    private ModsList displayList;
    private ModListScreen.@Nullable ModInfoPanel displayPanel;
    @Nullable
    private EditBox search;

    public static ModListScreen create(@Nullable Screen lastScreen) {
        final Builder<ModDisplayInfo> mods = ImmutableList.builder();

        for (ModContainer container : ModList.get().getSortedMods()) {
            final ModDisplayInfo displayInfo;
            final String modId = container.getModId();
            if (!FMLEnvironment.isProduction() && modId.equals(Identifier.DEFAULT_NAMESPACE)) {
                // This is a special case in development because the Minecraft mods.toml information is hardcoded in FML
                // TODO: remove in the future once FML is updated to match
                displayInfo = new DefaultModDisplayInfo(container) {
                    @Override
                    public ImageResource icon() {
                        return ImageResource.packRoot("vanilla", "pack.png");
                    }

                    @Override
                    public ImageResource banner() {
                        return ImageResource.packAsset(LogoRenderer.MINECRAFT_LOGO);
                    }

                    @Override
                    public Component authors() {
                        return Component.literal("Mojang Studios");
                    }

                    @Override
                    public Component license() {
                        return Component.literal("Minecraft EULA").withStyle(style -> style
                                .withUnderlined(true)
                                .withClickEvent(new ClickEvent.OpenUrl(CommonLinks.EULA)));
                    }

                    @Override
                    public URI displayUrl() {
                        return URI.create("https://minecraft.net");
                    }

                    @Override
                    public URI issuesUrl() {
                        return URI.create("https://bugs.mojang.com");
                    }

                    @Override
                    public Component description() {
                        return Component.empty();
                    }
                };
            } else {
                displayInfo = container.getCustomExtension(ModDisplayInfo.class)
                        .orElseGet(() -> new DefaultModDisplayInfo(container));
            }
            mods.add(displayInfo);
        }

        return new ModListScreen(lastScreen, mods.build(), FMLPaths.MODSDIR.get(), ConfigurationScreenFactory.DEFAULT, VersionCheckResultSupplier.DEFAULT);
    }

    private ModListScreen(@Nullable Screen lastScreen, ImmutableList<ModDisplayInfo> mods, Path modsFolder, ConfigurationScreenFactory configFactory, VersionCheckResultSupplier versionCheck) {
        super(translatable("neoforge.screen.mods.title"));
        this.lastScreen = lastScreen;
        this.mods = mods;
        this.modsFolder = modsFolder;
        this.allEntries = new ArrayList<>(mods.size());
        this.configFactory = configFactory;
        this.versionCheck = versionCheck;
    }

    @Override
    protected void init() {
        super.init();
        this.layout = new HeaderAndFooterLayout(this, 8, 38);

        // Footer
        final LinearLayout footer = layout.addToFooter(new LinearLayout(0, 0, Orientation.HORIZONTAL));
        footer.spacing(4).defaultCellSetting().paddingTop(5);

        footer.addChild(Button.builder(Component.translatable("neoforge.screen.mods.button.open_folder"),
                _ -> Util.getPlatform().openPath(modsFolder)).build());
        footer.addChild(Button.builder(CommonComponents.GUI_BACK, _ -> ModListScreen.this.onClose()).build());

        // Content
        final GridLayout contentBase = layout.addToContents(new GridLayout()).columnSpacing(-3);
        contentBase.defaultCellSetting().alignVerticallyTop().alignHorizontallyCenter().padding(0);
        final RowHelper contentBaseHelper = contentBase.createRowHelper(3);

        final LinearLayout sidebar = contentBaseHelper.addChild(LinearLayout.vertical(), contentBaseHelper.newCellSettings().paddingVertical(-INFO_PANEL_FRAME_PADDING));
        sidebar.spacing(6).defaultCellSetting().alignVerticallyTop();
        final LinearLayout main = contentBaseHelper.addChild(LinearLayout.vertical(), 2);

        this.fixedSidebarLayout = sidebar.addChild(new EqualSpacingLayout(SIDEBAR_CONTROLS_WIDTH, SIDEBAR_CONTROLS_HEIGHT, EqualSpacingLayout.Orientation.HORIZONTAL));

        this.search = this.fixedSidebarLayout.addChild(new EditBox(this.font, SIDEBAR_CONTROLS_WIDTH - SIDEBAR_SORT_BUTTON_WIDTH - 4, SIDEBAR_CONTROLS_HEIGHT, Component.translatable("neoforge.screen.mods.search")));
        this.search.setHint(Component.translatable("neoforge.screen.mods.search").withStyle(ChatFormatting.GRAY));
        this.search.setFocused(false);
        this.search.setCanLoseFocus(true);
        this.search.setResponder(_ -> this.updateModsList());

        this.fixedSidebarLayout.addChild(CycleButton.builder(SortType::getName, this.currentSort)
                .displayOnlyValue()
                .withValues(SortType.values())
                .withTooltip(sort -> Tooltip.create(sort.getHover()))
                // 20 is the default button height
                .create(0, 0, SIDEBAR_SORT_BUTTON_WIDTH, SIDEBAR_CONTROLS_HEIGHT, translatable("neoforge.screen.mods.button.sort"), (_, newValue) -> {
                    this.currentSort = newValue;
                    this.updateModsList();
                }));

        this.fixedSidebarLayout.arrangeElements(); // Arrange to figure out the height
        this.displayList = sidebar.addChild(new ModsList(this.layout.getContentHeight() - this.fixedSidebarLayout.getHeight() - 5));

        this.displayPanel = new ModInfoPanel(INFO_PANEL_WIDTH, this.layout.getContentHeight());
        main.addChild(this.displayPanel.getMainLayout());

        this.allEntries.clear();
        for (ModDisplayInfo mod : mods) {
            this.allEntries.add(this.displayList.new Entry(this.versionCheck.get(mod.id()), mod));
        }
        this.updateModsList();

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        assert this.layout != null;
        assert this.displayList != null;
        assert this.displayPanel != null;
        assert this.fixedSidebarLayout != null;
        this.displayPanel.updateHeight(this.layout.getContentHeight());
        this.displayList.updateSizeAndPosition(SIDEBAR_MODS_LIST_WIDTH, this.layout.getContentHeight() - this.fixedSidebarLayout.getHeight() - 5, 0);
        this.displayPanel.getMainLayout().arrangeElements();
        this.layout.arrangeElements();
        this.displayList.setScrollAmount(this.displayList.scrollAmount());
    }

    @Override
    public void onClose() {
        final TextureManager textureManager = this.minecraft.getTextureManager();
        for (ModsList.Entry entry : this.allEntries) {
            if (entry.iconData != null) {
                textureManager.release(entry.iconData.sprite);
            }
        }
        assert this.displayPanel != null;
        this.displayPanel.close();
        this.minecraft.gui.setScreen(this.lastScreen);
    }

    private void updateModsList() {
        if (this.displayList == null) return;

        final ArrayList<ModsList.Entry> entries = new ArrayList<>(this.allEntries);
        this.currentSort.sort(entries);
        assert this.search != null;
        final String filter = this.search.getValue().toLowerCase(Locale.ROOT);
        if (!filter.isEmpty()) {
            entries.removeIf(entry -> !entry.displayInfo.displayName().getString().toLowerCase(Locale.ROOT).contains(filter));
        }
        final ModsList.Entry selected = this.displayList.getSelected();
        this.displayList.replaceEntries(entries);
        if (entries.contains(selected)) {
            // Reselect the previously-selected if possible
            this.displayList.setSelected(selected);
        } else if (entries.size() == 1) {
            // If there is only one entry, select that
            this.displayList.setSelected(entries.getFirst());
        }
    }

    private record ImageData(Identifier sprite, int width, int height) {}

    @Nullable
    private ImageData loadImage(String type, String modId, ImageResource imageResource) {
        final IoSupplier<InputStream> resource = imageResource.get(this.minecraft.getResourceManager());

        if (resource == null) {
            LOGGER.warn("Failed to find {} resource {} for mod ID {} as it did not exist", type, imageResource, modId);
            return null;
        }

        final NativeImage image;
        try (InputStream imageStream = resource.get()) {
            image = NativeImage.read(imageStream);
        } catch (IOException e) {
            LOGGER.warn("Failed to load {} resource {} for mod ID {}", type, imageResource, modId);
            return null;
        }

        final TextureManager textureManager = this.minecraft.getTextureManager();
        final Identifier sprite = Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "mod/" + type + "/" + modId);
        textureManager.register(sprite, new DynamicTexture(sprite::toString, image));

        return new ImageData(sprite, image.getWidth(), image.getHeight());
    }

    private class ModsList extends ObjectSelectionList<ModsList.Entry> {
        public ModsList(int height) {
            // minecraft, width, height, y, itemHeight
            super(ModListScreen.this.minecraft, SIDEBAR_MODS_LIST_WIDTH, height, 0, ICON_SIZE + 4);
            // 24 pixels for the icon, 4 pixels padding for top and bottom (2 pixels each)
        }

        @Override
        public void clearEntries() {
            super.clearEntries();
            this.setSelected(null);
        }

        // Make the entries fit the container, and shrink if the scrollbar is present
        @Override
        public int getRowLeft() {
            return this.getX() + 3;
        }

        @Override
        public int getRowWidth() {
            int rowWidth = this.getWidth() - 6;
            if (this.scrollable()) {
                // Shrink if scrollbar is present
                rowWidth -= this.scrollbarWidth();
            }
            return rowWidth;
        }

        @Override
        protected int scrollBarX() {
            return this.getRight() - this.scrollbarWidth();
        }

        @Override
        public void setSelected(ModsList.@Nullable Entry selectedEntry) {
            super.setSelected(selectedEntry);
            assert ModListScreen.this.displayPanel != null;
            ModListScreen.this.displayPanel.updateSelected(selectedEntry);
        }

        class Entry extends ObjectSelectionList.Entry<ModsList.Entry> {
            private static final Identifier VERSION_CHECK_ICONS = Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "textures/gui/version_check_icons.png");
            final VersionChecker.@Nullable CheckResult checkResult;
            final ModDisplayInfo displayInfo;
            @Nullable
            final ImageData iconData;

            Entry(VersionChecker.@Nullable CheckResult checkResult, ModDisplayInfo displayInfo) {
                this.checkResult = checkResult;
                this.displayInfo = displayInfo;
                if (displayInfo.icon() != null) {
                    this.iconData = ModListScreen.this.loadImage("icon", displayInfo.id(), Objects.requireNonNull(displayInfo.icon()));
                } else {
                    this.iconData = null;
                }
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", Component.translatable("neoforge.screen.mods.list.narration", displayInfo.displayName(), displayInfo.version()));
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
                int left = this.getContentX();
                int top = this.getContentY();
                int textLeft = left + 2;

                if (iconData != null) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, iconData.sprite, left, top, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
                    textLeft += ICON_SIZE + 4;
                }
                int maxTextWidth = getRowWidth() - textLeft + left - 4;

                if (checkResult != null && checkResult.status().shouldDraw() && FMLConfig.getBoolConfigValue(FMLConfig.ConfigValue.VERSION_CHECK)) {
                    graphics.blit(
                            RenderPipelines.GUI_TEXTURED,
                            VERSION_CHECK_ICONS,
                            this.getContentRight() - 10,
                            this.getContentYMiddle() - (8 / 2),
                            checkResult.status().getSheetOffset() * 8,
                            (checkResult.status().isAnimated() && ((System.currentTimeMillis() / 800 & 1) == 1)) ? 8 : 0,
                            8,
                            8,
                            64,
                            16);
                    maxTextWidth -= 14;
                }

                top += 4; // padding

                final Language language = Language.getInstance();
                graphics.text(ModListScreen.this.font, language.getVisualOrder(ModListScreen.this.font.ellipsize(displayInfo.displayName(), maxTextWidth)), textLeft, top, 0xFFFFFFFF);
                top += ModListScreen.this.font.lineHeight;
                graphics.text(ModListScreen.this.font, language.getVisualOrder(ModListScreen.this.font.ellipsize(Component.literal(displayInfo.version()), maxTextWidth)), textLeft, top, 0xFF000000 + TextColor.GRAY.getValue());
            }
        }
    }

    private class ModInfoPanel implements Closeable {
        private static final MonthDay APRIL_FOOLS = MonthDay.of(Month.APRIL, 1);
        private static final int MAIN_PADDING = 4; // Padding between sections

        private final int width;
        private final ResizableTextureImageWidget bannerWidget;
        private final MultiLineTextWidget displayNameWidget;
        private final MultiLineTextWidget idAndVersionWidget;
        private final MultiLineTextWidget newerVersionWidget;
        private final Button newerVersionButton;
        private final MultiLineTextWidget licenseWidget;
        private final MultiLineTextWidget authorsWidget;
        private final MultiLineTextWidget creditsWidget;
        private final SolidColorWidget separator;
        private final Button homepageButton;
        private final Button issuesButton;
        private final Button configButton;
        private final MultiLineTextWidget descriptionWidget;
        private ModsList.@Nullable Entry selected;
        @Nullable
        private ImageData bannerData;
        @Nullable
        private UnaryOperator<Screen> configScreenFactory;

        private static final int BUTTON_PANEL_HEIGHT = 26;

        private final LinearLayout mainLayout;
        private final FrameLayout contentFrame;
        private final BackgroundWithPipingWidget backgroundWithPipingWidget;
        private final ScrollableLayout scrollableContentContainer;

        private final ImageWidget squirr;

        public ModInfoPanel(int width, int height) {
            this.mainLayout = LinearLayout.vertical();
            this.mainLayout.defaultCellSetting().alignHorizontallyCenter();

            this.contentFrame = this.mainLayout.addChild(new FrameLayout(width, height - BUTTON_PANEL_HEIGHT));

            LinearLayout contentLayout = new LinearLayout(0, 0, Orientation.VERTICAL);
            this.width = width;

            // Ensure layout width fills the whole width
            contentLayout.addChild(SpacerElement.width(width));

            this.bannerWidget = contentLayout.addChild(
                    new ResizableTextureImageWidget(0, 0, 0, 0, MissingTextureAtlasSprite.getLocation(), 0, 0),
                    contentLayout.newCellSettings().paddingTop(INFO_PANEL_FRAME_PADDING).alignHorizontallyCenter());

            contentLayout.addChild(SpacerElement.height(MAIN_PADDING));

            this.displayNameWidget = buildTextWidget(contentLayout).setCentered(true);
            this.displayNameWidget.setComponentClickHandler(null); // Disallow clicks for the display name
            this.idAndVersionWidget = buildTextWidget(contentLayout).setCentered(true);

            this.newerVersionWidget = contentLayout.addChild(FocusableTextWidget.builder(Component.empty(), font, 2)
                    .alwaysShowBorder(false)
                    .backgroundFill(FocusableTextWidget.BackgroundFill.NEVER)
                    .maxWidth(width)
                    .build()
                    .setCentered(true),
                    contentLayout.newCellSettings().paddingVertical(3));
            this.newerVersionWidget.setComponentClickHandler(style -> {
                ClickEvent clickEvent = style.getClickEvent();
                if (clickEvent != null) {
                    defaultHandleClickEvent(clickEvent, ModListScreen.this.minecraft, ModListScreen.this);
                }
            });
            this.newerVersionButton = contentLayout.addChild(Button.builder(translatable("neoforge.screen.mods.button.changelog"),
                    _ -> {
                        if (this.selected != null && this.selected.checkResult != null) {
                            this.openChangelogScreen(this.selected.checkResult);
                        }
                    }).build(),
                    contentLayout.newCellSettings().alignHorizontallyCenter());

            contentLayout.addChild(SpacerElement.height(MAIN_PADDING));

            this.licenseWidget = buildTextWidget(contentLayout);
            this.authorsWidget = buildTextWidget(contentLayout);
            this.creditsWidget = buildTextWidget(contentLayout);

            this.separator = contentLayout.addChild(
                    new SolidColorWidget(width - 8, 1).setColor(ARGB.opaque(TextColor.GRAY.getValue())).calculateShadow(),
                    contentLayout.newCellSettings().paddingVertical(MAIN_PADDING).alignHorizontallyCenter());

            this.descriptionWidget = buildTextWidget(contentLayout);

            this.backgroundWithPipingWidget = this.contentFrame.addChild(new BackgroundWithPipingWidget(
                    ModListScreen.this.minecraft,
                    0, 0,
                    INFO_PANEL_WIDTH + INFO_PANEL_FRAME_PADDING * 2, height - BUTTON_PANEL_HEIGHT + INFO_PANEL_FRAME_PADDING * 2),
                    this.contentFrame.newChildLayoutSettings().padding(-INFO_PANEL_FRAME_PADDING));

            this.scrollableContentContainer = this.contentFrame.addChild(
                    new ScrollableLayout(ModListScreen.this.minecraft, contentLayout, height - BUTTON_PANEL_HEIGHT),
                    this.contentFrame.newChildLayoutSettings().alignVerticallyTop());

            this.squirr = this.contentFrame.addChild(ImageWidget.texture(
                    32, 30,
                    Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "textures/gui/bigsquirr.png"),
                    32, 30), this.contentFrame.newChildLayoutSettings().alignVerticallyBottom().alignHorizontallyRight().paddingRight(16).paddingBottom(-1));

            EqualSpacingLayout buttonsLayout = this.mainLayout.addChild(new EqualSpacingLayout(width, BUTTON_PANEL_HEIGHT, EqualSpacingLayout.Orientation.HORIZONTAL));
            buttonsLayout.defaultChildLayoutSetting().alignVerticallyBottom().paddingHorizontal(-INFO_PANEL_FRAME_PADDING);

            final int buttonSpacing = 3;
            final int buttonWidth = width / 3 - (buttonSpacing / 2);
            final int buttonHeight = Button.DEFAULT_HEIGHT;

            this.homepageButton = buttonsLayout.addChild(Button.builder(Component.translatable("neoforge.screen.mods.button.homepage"),
                    _ -> {
                        if (displayInfo().displayUrl() != null) {
                            ConfirmLinkScreen.confirmLinkNow(ModListScreen.this, Objects.requireNonNull(displayInfo().displayUrl()));
                        }
                    }).size(buttonWidth, buttonHeight).build());
            this.issuesButton = buttonsLayout.addChild(Button.builder(Component.translatable("neoforge.screen.mods.button.issues"),
                    _ -> {
                        if (displayInfo().issuesUrl() != null) {
                            ConfirmLinkScreen.confirmLinkNow(ModListScreen.this, Objects.requireNonNull(displayInfo().issuesUrl()));
                        }
                    }).size(buttonWidth, buttonHeight).build());
            this.configButton = buttonsLayout.addChild(Button.builder(Component.translatable("neoforge.screen.mods.button.config"),
                    _ -> {
                        if (this.configScreenFactory != null) {
                            ModListScreen.this.minecraft.gui.setScreen(this.configScreenFactory.apply(ModListScreen.this));
                        }
                    }).size(buttonWidth, buttonHeight).build());

            this.reset();
        }

        private MultiLineTextWidget buildTextWidget(LinearLayout layout) {
            MultiLineTextWidget widget = layout.addChild(FocusableTextWidget.builder(Component.empty(), font, 2)
                    .alwaysShowBorder(false)
                    .backgroundFill(FocusableTextWidget.BackgroundFill.NEVER)
                    .maxWidth(width)
                    .build()
                    .setCentered(false));
            widget.setComponentClickHandler(style -> {
                ClickEvent clickEvent = style.getClickEvent();
                if (clickEvent != null) {
                    defaultHandleClickEvent(clickEvent, ModListScreen.this.minecraft, ModListScreen.this);
                }
            });
            return widget;
        }

        public LinearLayout getMainLayout() {
            return mainLayout;
        }

        private ModDisplayInfo displayInfo() {
            if (this.selected == null) return BlankModDisplayInfo.INSTANCE;
            return this.selected.displayInfo;
        }

        public void updateHeight(int height) {
            int infoHeight = height - BUTTON_PANEL_HEIGHT;
            this.contentFrame.setMinHeight(infoHeight);
            this.backgroundWithPipingWidget.setHeight(infoHeight);
            this.scrollableContentContainer.setMaxHeight(infoHeight);
        }

        public void updateSelected(ModsList.@Nullable Entry newSelected) {
            if (newSelected == this.selected) return; // No change in selection
            this.selected = newSelected;
            this.update();
            assert ModListScreen.this.layout != null;
            assert ModListScreen.this.displayPanel != null;
            ModListScreen.this.displayPanel.getMainLayout().arrangeElements();
            ModListScreen.this.layout.arrangeElements();
            ModListScreen.this.repositionElements();
        }

        /// Resets layout to a blank state.
        private void reset() {
            if (this.bannerData != null) {
                final TextureManager textureManager = ModListScreen.this.minecraft.getTextureManager();
                textureManager.release(bannerData.sprite);
                this.bannerData = null;
            }
            this.bannerWidget.updateResource(MissingTextureAtlasSprite.getLocation(), 0, 0);

            hideTextWidget(this.displayNameWidget);
            hideTextWidget(this.idAndVersionWidget);
            hideTextWidget(this.newerVersionWidget);
            this.newerVersionButton.visible = this.newerVersionButton.active = false;
            this.newerVersionButton.setHeight(0);
            hideTextWidget(this.licenseWidget);
            hideTextWidget(this.authorsWidget);
            hideTextWidget(this.creditsWidget);

            this.homepageButton.active = false;
            this.issuesButton.active = false;
            this.configScreenFactory = null;
            this.configButton.active = false;
            this.separator.visible = false;
            hideTextWidget(this.descriptionWidget);

            this.squirr.visible = false;
        }

        private void hideTextWidget(AbstractStringWidget widget) {
            widget.setMessage(Component.empty());
            widget.visible = false;
            widget.setHeight(0);
        }

        /// Updates the layout based on the selected info.
        public void update() {
            reset();
            if (this.selected == null) return; // Do nothing if nothing is selected
            ModDisplayInfo displayInfo = this.selected.displayInfo;

            final ImageResource bannerResource = displayInfo.banner();
            if (bannerResource != null) {
                // Load new banner data
                if (displayInfo.id().equals(Identifier.DEFAULT_NAMESPACE)) {
                    // Special-case for the 'minecraft' mod: render the logo using LogoRenderer
                    float scaleFactor = Math.min(1F, (float) width / LogoRenderer.LOGO_TEXTURE_WIDTH);
                    int bannerWidth = (int) (LogoRenderer.LOGO_TEXTURE_WIDTH * scaleFactor);
                    this.bannerWidget.useMinecraftLogo(bannerWidth);
                } else {
                    this.bannerData = loadImage("banner", displayInfo.id(), bannerResource);
                    if (this.bannerData != null) {
                        float widthScaleFactor = Math.min(1F, (float) this.width / this.bannerData.width());
                        float heightScaleFactor = Math.min(1F, (float) BANNER_HEIGHT / this.bannerData.height());
                        float scaleFactor = Math.min(widthScaleFactor, heightScaleFactor);
                        int bannerWidth = (int) (this.bannerData.width() * scaleFactor);
                        int bannerHeight = (int) (this.bannerData.height() * scaleFactor);
                        this.bannerWidget.updateResource(this.bannerData.sprite(), bannerWidth, bannerHeight);
                    }
                }
            }

            this.displayNameWidget.setMessage(displayInfo.displayName());
            this.displayNameWidget.visible = true;
            this.idAndVersionWidget.setMessage(Component.translatable(
                    "neoforge.screen.mods.info.subtitle",
                    Component.literal(displayInfo.id()).withStyle(style -> style
                            .withItalic(true)
                            .withHoverEvent(new HoverEvent.ShowText(Component.translatable("neoforge.screen.mods.info.subtitle.modid.click")))
                            .withClickEvent(new ClickEvent.CopyToClipboard(displayInfo.id()))),
                    Component.literal(displayInfo.version()).withStyle(style -> style
                            .withHoverEvent(new HoverEvent.ShowText(Component.translatable("neoforge.screen.mods.info.subtitle.version.click")))
                            .withClickEvent(new ClickEvent.CopyToClipboard(displayInfo.version()))))
                    .withStyle(ChatFormatting.GRAY));
            this.idAndVersionWidget.visible = true;
            VersionChecker.CheckResult checkResult = this.selected.checkResult;
            if (checkResult != null) {
                this.newerVersionWidget.setMessage(Component.translatable(
                        "neoforge.screen.mods.info.update",
                        Component.literal(checkResult.target().toString())
                                .withStyle(style -> style.withItalic(false)))
                        .withStyle(style -> style.withItalic(true)));
                this.newerVersionWidget.visible = true;

                this.newerVersionButton.setHeight(Button.DEFAULT_HEIGHT);
                this.newerVersionButton.visible = this.newerVersionButton.active = true;
            }

            this.licenseWidget.setMessage(Component.translatable(
                    "neoforge.screen.mods.info.license",
                    displayInfo.license().copy().withStyle(ChatFormatting.WHITE).withStyle(style -> style.withBold(false))).withStyle(ChatFormatting.GRAY).withStyle(style -> style.withBold(true)));
            this.licenseWidget.visible = true;
            if (containsText(displayInfo.authors())) {
                this.authorsWidget.setMessage(Component.translatable(
                        "neoforge.screen.mods.info.authors",
                        displayInfo.authors().copy().withStyle(ChatFormatting.WHITE).withStyle(style -> style.withBold(false))).withStyle(ChatFormatting.GRAY).withStyle(style -> style.withBold(true)));
                this.authorsWidget.visible = true;
            }
            if (containsText(displayInfo.credits())) {
                this.creditsWidget.setMessage(Component.translatable(
                        "neoforge.screen.mods.info.credits",
                        displayInfo.credits().copy().withStyle(ChatFormatting.WHITE).withStyle(style -> style.withBold(false))).withStyle(ChatFormatting.GRAY).withStyle(style -> style.withBold(true)));
                this.creditsWidget.visible = true;
            }

            this.homepageButton.active = displayInfo.displayUrl() != null;
            this.issuesButton.active = displayInfo.issuesUrl() != null;
            this.configScreenFactory = ModListScreen.this.configFactory.create(displayInfo);
            this.configButton.active = this.configScreenFactory != null;

            // Hardcoded case from ModInfo
            if (containsText(displayInfo.description()) && !displayInfo.description().getString().equals("MISSING DESCRIPTION")) {
                this.separator.visible = true;
                this.descriptionWidget.setMessage(displayInfo.description());
                this.descriptionWidget.visible = true;
            }

            if (displayInfo.id().equals(NeoForgeMod.MOD_ID) && SpecialDates.dayNow().equals(APRIL_FOOLS)) {
                this.squirr.visible = true;
            }
        }

        private void openChangelogScreen(VersionChecker.CheckResult checkResult) {
            ModListScreen.this.minecraft.gui.setScreen(new ChangelogScreen(ModListScreen.this, displayInfo(), checkResult));
        }

        private static boolean containsText(Component component) {
            return !component.getContents().equals(PlainTextContents.EMPTY) && !component.getString().isEmpty();
        }

        @Override
        public void close() {
            this.reset();
        }
    }

    private static final Comparator<ModsList.Entry> COMPARATOR_BY_NAME = Comparator.comparing(c -> c.displayInfo.displayName().getString().toLowerCase(Locale.ROOT));

    private enum SortType {
        A_TO_Z("neoforge.screen.mods.sort.a_to_z", list -> list.sort(COMPARATOR_BY_NAME)),
        Z_TO_A("neoforge.screen.mods.sort.z_to_a", list -> list.sort(COMPARATOR_BY_NAME.reversed()));

        private final String translationKey;
        private final Consumer<List<ModsList.Entry>> sorter;

        SortType(String translationKey, Consumer<List<ModsList.Entry>> sorter) {
            this.translationKey = translationKey;
            this.sorter = sorter;
        }

        public Component getName() {
            return Component.translatable(this.translationKey);
        }

        public Component getHover() {
            return Component.translatable(this.translationKey + ".hover");
        }

        public void sort(List<ModsList.Entry> list) {
            sorter.accept(list);
        }
    }
}
