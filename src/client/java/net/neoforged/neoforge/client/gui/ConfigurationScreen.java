/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import static net.neoforged.neoforge.common.NeoForgeConfig.CLIENT;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig.Entry;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Function4;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen.Filter;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.ListValueSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Range;
import net.neoforged.neoforge.common.ModConfigSpec.RestartType;
import net.neoforged.neoforge.common.ModConfigSpec.ValueSpec;
import net.neoforged.neoforge.common.TranslatableEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * A generic configuration UI.<p>
 * 
 * This class is the entry point for NeoForge's generic configuration UI. You can use this in two different ways:<ul>
 * 
 * <li>As an entry point for your custom configuration screen that handles fetching your configs, matching {@link Type} to the current game, enforcing level and game restarts, etc.
 * <li>As a ready-made system but extensible that works out of the box with all configs that use the {@link ModConfigSpec} system and don't do anything overly weird with it.</ul>
 * 
 * For the former one, use the 3-argument constructor {@link #ConfigurationScreen(ModContainer, Screen, Function4)} and return your own screen from the Function4. For the latter,
 * use either the 2-argument constructor {@link #ConfigurationScreen(ModContainer, Screen)} if you don't need to extend the system, or the 3-argument one and return a subclass of
 * {@link ConfigurationSectionScreen} from the Function4.<p>
 * 
 * In any case, register your configuration screen in your client mod class like this:
 * 
 * {@snippet :
 * &#64;Mod(value = "examplemod", dist = Dist.CLIENT)
 * public class ExampleMod {
 *     public ExampleMod(ModContainer container) {
 *         container.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) -> new ConfigurationScreen(container, parent));
 *     }
 * }
 * }
 * 
 * For extending the system, see the documentation on {@link ConfigurationSectionScreen}.<p>
 * 
 * If you only want to suppress certain elements from being displayed, you can also supply a {@link Filter} as the third parameter instead of subclassing the whole {@link ConfigurationSectionScreen}.
 * 
 */
public final class ConfigurationScreen extends OptionsSubScreen {
    private static final class TooltipConfirmScreen extends ConfirmScreen {
        boolean seenYes = false;

        private TooltipConfirmScreen(BooleanConsumer callback, Component title, Component message, Component yesButton, Component noButton) {
            super(callback, title, message, yesButton, noButton);
        }

        @Override
        protected void init() {
            seenYes = false;
            super.init();
        }

        @Override
        protected void addExitButton(Button button) {
            if (seenYes) {
                button.setTooltip(Tooltip.create(RESTART_NO_TOOLTIP));
            } else {
                seenYes = true;
            }
            super.addExitButton(button);
        }
    }

    public static class TranslationChecker {
        private static final Logger LOGGER = LogManager.getLogger();
        private final Set<String> untranslatables = new HashSet<>();
        private final Set<String> untranslatablesWithFallback = new HashSet<>();

        public String check(final String translationKey) {
            if (!I18n.exists(translationKey)) {
                untranslatables.add(translationKey);
            }
            return translationKey;
        }

        public String check(final String translationKey, final String fallback) {
            if (!I18n.exists(translationKey)) {
                untranslatablesWithFallback.add(translationKey);
                return check(fallback);
            }
            return translationKey;
        }

        public boolean existsWithFallback(final String translationKey) {
            if (!I18n.exists(translationKey)) {
                untranslatablesWithFallback.add(translationKey);
                return false;
            }
            return true;
        }

        /**
         * If the given translation key exists, returns it formatted with the given style(s) and as prefixed with the given component.
         * Otherwise returns an empty Component.
         */
        public Component optional(final Component prefix, final String translationKey, final ChatFormatting... style) {
            if (I18n.exists(translationKey)) {
                return Component.empty().append(prefix).append(Component.translatable(translationKey).withStyle(style));
            }
            return Component.empty();
        }

        public void finish() {
            if (CLIENT.logUntranslatedConfigurationWarnings.get() && !FMLLoader.isProduction() && (!untranslatables.isEmpty() || !untranslatablesWithFallback.isEmpty())) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("""
                        \n	Dev warning - Untranslated configuration keys encountered. Please translate your configuration keys so users can properly configure your mod.
                        """);
                if (!untranslatables.isEmpty()) {
                    stringBuilder.append("\nUntranslated keys:");
                    for (String key : untranslatables) {
                        stringBuilder.append("\n  \"").append(key).append("\": \"\",");
                    }
                }
                if (!untranslatablesWithFallback.isEmpty()) {
                    stringBuilder.append("\nThe following keys have fallbacks. Please check if those are suitable, and translate them if they're not.");
                    for (String key : untranslatablesWithFallback) {
                        stringBuilder.append("\n  \"").append(key).append("\": \"\",");
                    }
                }

                LOGGER.warn(stringBuilder);
            }
            untranslatables.clear();
        }
    }

    /**
     * Prefix for static keys the configuration screens use internally.
     */
    private static final String LANG_PREFIX = "neoforge.configuration.uitext.";
    /**
     * A wrapper for the labels of buttons that open a new screen. Default: "%s..."
     */
    private static final String SECTION = LANG_PREFIX + "section";
    /**
     * A default for the labels of buttons that open a new screen. Default: "Edit"
     */
    private static final String SECTION_TEXT = LANG_PREFIX + "sectiontext";
    /**
     * The breadcrumb separator. Default: "%s > %s"
     */
    public static final Component CRUMB_SEPARATOR = Component.translatable(LANG_PREFIX + "breadcrumb.separator").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
    private static final String CRUMB = LANG_PREFIX + "breadcrumb.order";
    /**
     * The label of list elements. Will be supplied the index into the list. Default: "%s:"
     */
    private static final String LIST_ELEMENT = LANG_PREFIX + "listelement";
    /**
     * How the range will be added to the tooltip when using translated tooltips. Mimics what the comment does in ModConfigSpec.
     */
    private static final String RANGE_TOOLTIP = LANG_PREFIX + "rangetooltip";
    private static final ChatFormatting RANGE_TOOLTIP_STYLE = ChatFormatting.GRAY;
    /**
     * How the filename will be added to the tooltip.
     */
    private static final String FILENAME_TOOLTIP = LANG_PREFIX + "filenametooltip";
    private static final ChatFormatting FILENAME_TOOLTIP_STYLE = ChatFormatting.GRAY;
    /**
     * A literal to create an empty line in a tooltip.
     */
    private static final MutableComponent EMPTY_LINE = Component.literal("\n\n");

    public static final Component TOOLTIP_CANNOT_EDIT_THIS_WHILE_ONLINE = Component.translatable(LANG_PREFIX + "notonline").withStyle(ChatFormatting.RED);
    public static final Component TOOLTIP_CANNOT_EDIT_THIS_WHILE_OPEN_TO_LAN = Component.translatable(LANG_PREFIX + "notlan").withStyle(ChatFormatting.RED);
    public static final Component TOOLTIP_CANNOT_EDIT_NOT_LOADED = Component.translatable(LANG_PREFIX + "notloaded").withStyle(ChatFormatting.RED);
    public static final Component NEW_LIST_ELEMENT = Component.translatable(LANG_PREFIX + "newlistelement");
    public static final Component MOVE_LIST_ELEMENT_UP = Component.translatable(LANG_PREFIX + "listelementup");
    public static final Component MOVE_LIST_ELEMENT_DOWN = Component.translatable(LANG_PREFIX + "listelementdown");
    public static final Component REMOVE_LIST_ELEMENT = Component.translatable(LANG_PREFIX + "listelementremove");
    public static final Component UNSUPPORTED_ELEMENT = Component.translatable(LANG_PREFIX + "unsupportedelement").withStyle(ChatFormatting.RED);
    public static final Component LONG_STRING = Component.translatable(LANG_PREFIX + "longstring").withStyle(ChatFormatting.RED);
    public static final Component GAME_RESTART_TITLE = Component.translatable(LANG_PREFIX + "restart.game.title");
    public static final Component GAME_RESTART_MESSAGE = Component.translatable(LANG_PREFIX + "restart.game.text");
    public static final Component GAME_RESTART_YES = Component.translatable("menu.quit"); // TitleScreen.init() et.al.
    public static final Component SERVER_RESTART_TITLE = Component.translatable(LANG_PREFIX + "restart.server.title");
    public static final Component SERVER_RESTART_MESSAGE = Component.translatable(LANG_PREFIX + "restart.server.text");
    public static final Component RETURN_TO_MENU = Component.translatable("menu.returnToMenu"); // PauseScreen.RETURN_TO_MENU
    public static final Component SAVING_LEVEL = Component.translatable("menu.savingLevel"); // PauseScreen.SAVING_LEVEL
    public static final Component RESTART_NO = Component.translatable(LANG_PREFIX + "restart.return");
    public static final Component RESTART_NO_TOOLTIP = Component.translatable(LANG_PREFIX + "restart.return.tooltip").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
    public static final Component UNDO = Component.translatable(LANG_PREFIX + "undo");
    public static final Component UNDO_TOOLTIP = Component.translatable(LANG_PREFIX + "undo.tooltip");
    public static final Component RESET = Component.translatable(LANG_PREFIX + "reset");
    public static final Component RESET_TOOLTIP = Component.translatable(LANG_PREFIX + "reset.tooltip");
    public static final int BIG_BUTTON_WIDTH = 310;

    // Ideally this should not be static, but we need it in the construtor's super() call
    protected static final TranslationChecker translationChecker = new TranslationChecker();

    protected final ModContainer mod;
    private final Function4<ConfigurationScreen, ModConfig.Type, ModConfig, Component, Screen> sectionScreen;

    public RestartType needsRestart = RestartType.NONE;
    // If there is only one config type (and it can be edited, we show that instantly on the way "down" and want to close on the way "up".
    // But when returning from the restart/reload confirmation screens, we need to stay open.
    private boolean autoClose = false;

    public ConfigurationScreen(final ModContainer mod, final Screen parent) {
        this(mod, parent, ConfigurationSectionScreen::new);
    }

    public ConfigurationScreen(final ModContainer mod, final Screen parent, ConfigurationSectionScreen.Filter filter) {
        this(mod, parent, (a, b, c, d) -> new ConfigurationSectionScreen(a, b, c, d, filter));
    }

    public ConfigurationScreen(final ModContainer mod, final Screen parent, Function4<ConfigurationScreen, ModConfig.Type, ModConfig, Component, Screen> sectionScreen) {
        super(parent, Minecraft.getInstance().options, Component.translatable(translationChecker.check(mod.getModId() + ".configuration.title", LANG_PREFIX + "title"), mod.getModInfo().getDisplayName()));
        this.mod = mod;
        this.sectionScreen = sectionScreen;
    }

    @Override
    protected void addOptions() {
        Button btn = null;
        int count = 0;
        for (final Type type : ModConfig.Type.values()) {
            boolean headerAdded = false;
            for (final ModConfig modConfig : ModConfigs.getConfigSet(type)) {
                if (modConfig.getModId().equals(mod.getModId())) {
                    if (!headerAdded) {
                        list.addSmall(new StringWidget(BIG_BUTTON_WIDTH, Button.DEFAULT_HEIGHT,
                                Component.translatable(LANG_PREFIX + type.name().toLowerCase(Locale.ENGLISH)).withStyle(ChatFormatting.UNDERLINE), font).alignLeft(), null);
                        headerAdded = true;
                    }
                    btn = Button.builder(Component.translatable(SECTION, translatableConfig(modConfig, "", LANG_PREFIX + "type." + modConfig.getType().name().toLowerCase(Locale.ROOT))),
                            button -> minecraft.setScreen(sectionScreen.apply(this, type, modConfig, translatableConfig(modConfig, ".title", LANG_PREFIX + "title." + type.name().toLowerCase(Locale.ROOT))))).width(BIG_BUTTON_WIDTH).build();
                    MutableComponent tooltip = Component.empty();
                    if (!((ModConfigSpec) modConfig.getSpec()).isLoaded()) {
                        tooltip.append(TOOLTIP_CANNOT_EDIT_NOT_LOADED).append(EMPTY_LINE);
                        btn.active = false;
                        count = 99; // prevent autoClose
                    } else if (type == Type.SERVER && minecraft.getCurrentServer() != null && !minecraft.isSingleplayer()) {
                        tooltip.append(TOOLTIP_CANNOT_EDIT_THIS_WHILE_ONLINE).append(EMPTY_LINE);
                        btn.active = false;
                        count = 99; // prevent autoClose
                    } else if (type == Type.SERVER && minecraft.hasSingleplayerServer() && minecraft.getSingleplayerServer().isPublished()) {
                        tooltip.append(TOOLTIP_CANNOT_EDIT_THIS_WHILE_OPEN_TO_LAN).append(EMPTY_LINE);
                        btn.active = false;
                        count = 99; // prevent autoClose
                    }
                    tooltip.append(Component.translatable(FILENAME_TOOLTIP, modConfig.getFileName()).withStyle(FILENAME_TOOLTIP_STYLE));
                    btn.setTooltip(Tooltip.create(tooltip));
                    list.addSmall(btn, null);
                    count++;
                }
            }
        }
        if (count == 1) {
            autoClose = true;
            btn.onPress();
        }
    }

    public Component translatableConfig(ModConfig modConfig, String suffix, String fallback) {
        return Component.translatable(translationChecker.check(mod.getModId() + ".configuration.section." + modConfig.getFileName().replaceAll("[^a-zA-Z0-9]+", ".").replaceFirst("^\\.", "").replaceFirst("\\.$", "").toLowerCase(Locale.ENGLISH) + suffix, fallback), mod.getModInfo().getDisplayName());
    }

    @Override
    public void added() {
        super.added();
        if (autoClose) {
            autoClose = false;
            onClose();
        }
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void onClose() {
        translationChecker.finish();
        switch (needsRestart) {
            case GAME -> {
                minecraft.setScreen(new TooltipConfirmScreen(b -> {
                    if (b) {
                        minecraft.stop();
                    } else {
                        super.onClose();
                    }
                }, GAME_RESTART_TITLE, GAME_RESTART_MESSAGE, GAME_RESTART_YES, RESTART_NO));
                return;
            }
            case WORLD -> {
                if (minecraft.level != null) {
                    minecraft.setScreen(new TooltipConfirmScreen(b -> {
                        if (b) {
                            // when changing server configs from the client is added, this is where we tell the server to restart and activate the new config.
                            // also needs a different text in MP ("server will restart/exit, yada yada") than in SP
                            onDisconnect();
                        } else {
                            super.onClose();
                        }
                    }, SERVER_RESTART_TITLE, SERVER_RESTART_MESSAGE, minecraft.isLocalServer() ? RETURN_TO_MENU : CommonComponents.GUI_DISCONNECT, RESTART_NO));
                    return;
                }
            }
        }
        super.onClose();
    }

    // direct copy from PauseScreen (which has the best implementation), sadly it's not really accessible
    private void onDisconnect() {
        boolean flag = this.minecraft.isLocalServer();
        ServerData serverdata = this.minecraft.getCurrentServer();
        this.minecraft.level.disconnect();
        if (flag) {
            this.minecraft.disconnect(new GenericMessageScreen(SAVING_LEVEL));
        } else {
            this.minecraft.disconnect();
        }

        TitleScreen titlescreen = new TitleScreen();
        if (flag) {
            this.minecraft.setScreen(titlescreen);
        } else if (serverdata != null && serverdata.isRealm()) {
            this.minecraft.setScreen(new RealmsMainScreen(titlescreen));
        } else {
            this.minecraft.setScreen(new JoinMultiplayerScreen(titlescreen));
        }
    }

    /**
     * A UI screen that presents a single section of configuration values and allows the user to edit them, including an unlimited undo system and reset to default.<p>
     * 
     * This class is automatically used if you use NeoForge's generic configuration UI, see {@link ConfigurationScreen}.<p>
     * 
     * If you have special needs, you can subclass this class to achieve the desired behaviour. For example:<ul>
     * 
     * <li>To filter out values that should not be displayed, override the matching <code>create*Value()</code> method and return null when the key matches.
     * <li>To use another UI element, override the matching <code>create*Value()</code> method and return your new UI element wrapped in a {@link Element}.
     * <li>To change the way lists work, override {@link #createList(String, ListValueSpec, ConfigValue)} and return a subclassed {@link ConfigurationListScreen}.
     * <li>To add additional (synthetic) config values, override {@link #createSyntheticValues()}.
     * <li>To be notified on each changed value instead of getting one {@link ModConfigEvent} at the end, override {@link #onChanged(String)}. Note that {@link #onChanged(String)}
     * will be called on every change (e.g. each typed character for Strings) if the new value is valid and different.
     * <li>To re-arrange your config values, declare them in the appropriate order.
     * <li>To change which values a config value can accept, supply the {@link ModConfigSpec.Builder} with a validator and/or a range.
     * <li>To have your config values display with readable names, add their translations to your language file. When running in a development environment, missing
     * translations are printed out to the log by default. However, you need to visit every configuration screen manually for those to be collected.<br>
     * Tooltips are also taken from the language file, but if there's no translation, the comment will be used instead.
     * </ul>
     * 
     * Note: This class subclasses vanilla's {@link OptionsSubScreen} and inherits some behaviour that is not needed. For example, we need to pass the vanilla
     * <code>options</code> to our superclass' constructor.
     */
    public static class ConfigurationSectionScreen extends OptionsSubScreen {
        protected static final long MAX_SLIDER_SIZE = 256L;

        public record Context(String modId, Screen parent, ModConfig modConfig, ModConfigSpec modSpec,
                Set<? extends Entry> entries, Map<String, Object> valueSpecs, List<String> keylist, Filter filter) {
            @ApiStatus.Internal
            public Context {}

            public static Context top(final String modId, final Screen parent, final ModConfig modConfig, Filter filter) {
                return new Context(modId, parent, modConfig, (ModConfigSpec) modConfig.getSpec(), ((ModConfigSpec) modConfig.getSpec()).getValues().entrySet(),
                        ((ModConfigSpec) modConfig.getSpec()).getSpec().valueMap(), List.of(), filter);
            }

            public static Context section(final Context parentContext, final Screen parent, final Set<? extends Entry> entries, final Map<String, Object> valueSpecs,
                    final String key) {
                return new Context(parentContext.modId, parent, parentContext.modConfig, parentContext.modSpec, entries, valueSpecs,
                        parentContext.makeKeyList(key), parentContext.filter);
            }

            public static Context list(final Context parentContext, final Screen parent) {
                return new Context(parentContext.modId, parent, parentContext.modConfig, parentContext.modSpec,
                        parentContext.entries, parentContext.valueSpecs, parentContext.keylist, null);
            }

            private ArrayList<String> makeKeyList(final String key) {
                final ArrayList<String> result = new ArrayList<>(keylist);
                result.add(key);
                return result;
            }
        }

        public record Element(@Nullable Component name, @Nullable Component tooltip, @Nullable AbstractWidget widget, @Nullable OptionInstance<?> option, boolean undoable) {
            @ApiStatus.Internal
            public Element {}

            public Element(@Nullable final Component name, @Nullable final Component tooltip, final AbstractWidget widget) {
                this(name, tooltip, widget, null, true);
            }

            public Element(@Nullable final Component name, @Nullable final Component tooltip, final AbstractWidget widget, boolean undoable) {
                this(name, tooltip, widget, null, undoable);
            }

            public Element(final Component name, final Component tooltip, final OptionInstance<?> option) {
                this(name, tooltip, null, option, true);
            }

            public Element(final Component name, final Component tooltip, final OptionInstance<?> option, boolean undoable) {
                this(name, tooltip, null, option, undoable);
            }

            public AbstractWidget getWidget(final Options options) {
                return widget != null ? widget : option.createButton(options);
            }

            @Nullable
            public Object any() {
                return widget != null ? widget : option;
            }
        }

        /**
         * A filter callback to suppress certain elements from being shown in the configuration UI.
         * <p>
         * Return null to suppress the element or return a modified Element.
         */
        public interface Filter {
            @Nullable
            Element filterEntry(Context context, String key, Element original);
        }

        protected final Context context;
        protected boolean changed = false;
        protected RestartType needsRestart = RestartType.NONE;
        protected final Map<String, ConfigurationSectionScreen> sectionCache = new HashMap<>();
        @Nullable
        protected Button undoButton, resetButton; // must not be changed after creation unless the reference inside the layout also is replaced
        protected final Button doneButton = Button.builder(CommonComponents.GUI_DONE, button -> onClose()).width(Button.SMALL_WIDTH).build();
        protected final UndoManager undoManager = new UndoManager();

        /**
         * Constructs a new section screen for the top-most section in a {@link ModConfig}.
         * 
         * @param parent    The screen to return to when the user presses escape or the "Done" button.
         *                  If this is a {@link ConfigurationScreen}, additional information is passed before closing.
         * @param type      The {@link Type} this configuration is for. Only used to generate the title of the screen.
         * @param modConfig The actual config to show and edit.
         */
        public ConfigurationSectionScreen(final Screen parent, final ModConfig.Type type, final ModConfig modConfig, Component title) {
            this(parent, type, modConfig, title, (c, k, e) -> e);
        }

        /**
         * Constructs a new section screen for the top-most section in a {@link ModConfig}.
         * 
         * @param parent    The screen to return to when the user presses escape or the "Done" button.
         *                  If this is a {@link ConfigurationScreen}, additional information is passed before closing.
         * @param type      The {@link Type} this configuration is for. Only used to generate the title of the screen.
         * @param filter    The {@link Filter} to use.
         * @param modConfig The actual config to show and edit.
         */
        public ConfigurationSectionScreen(final Screen parent, final ModConfig.Type type, final ModConfig modConfig, Component title, Filter filter) {
            this(Context.top(modConfig.getModId(), parent, modConfig, filter), title);
            needsRestart = type == Type.STARTUP ? RestartType.GAME : RestartType.NONE;
        }

        /**
         * Constructs a new section screen for a sub-section of a config.
         * 
         * @param parentContext The {@link Context} object of the parent.
         * @param parent        The screen to return to when the user presses escape or the "Done" button.
         *                      If this is a {@link ConfigurationSectionScreen}, additional information is passed before closing.
         * @param valueSpecs    The source for the {@link ValueSpec} objects for this section.
         * @param key           The key of the section.
         * @param entrySet      The source for the {@link ConfigValue} objects for this section.
         */
        public ConfigurationSectionScreen(final Context parentContext, final Screen parent, final Map<String, Object> valueSpecs, final String key,
                final Set<? extends Entry> entrySet, Component title) {
            this(Context.section(parentContext, parent, entrySet, valueSpecs, key), Component.translatable(CRUMB, parent.getTitle(), CRUMB_SEPARATOR, title));
        }

        @SuppressWarnings("resource")
        protected ConfigurationSectionScreen(final Context context, final Component title) {
            super(context.parent, Minecraft.getInstance().options, title);
            this.context = context;
        }

        @Nullable
        protected ValueSpec getValueSpec(final String key) {
            final Object object = context.valueSpecs.get(key);
            if (object instanceof final ValueSpec vs) {
                return vs;
            } else {
                return null;
            }
        }

        protected String getTranslationKey(final String key) {
            final ValueSpec valueSpec = getValueSpec(key);
            final String result = valueSpec != null ? valueSpec.getTranslationKey() : context.modSpec.getLevelTranslationKey(context.makeKeyList(key));
            return result != null ? result : context.modId + ".configuration." + key;
        }

        protected MutableComponent getTranslationComponent(final String key) {
            return Component.translatable(translationChecker.check(getTranslationKey(key)));
        }

        protected String getComment(final String key) {
            final ValueSpec valueSpec = getValueSpec(key);
            return valueSpec != null ? getValueSpec(key).getComment() : context.modSpec.getLevelComment(context.makeKeyList(key));
        }

        protected <T> OptionInstance.TooltipSupplier<T> getTooltip(final String key, @Nullable Range<?> range) {
            return OptionInstance.cachedConstantTooltip(getTooltipComponent(key, range));
        }

        protected Component getTooltipComponent(final String key, @Nullable Range<?> range) {
            final String tooltipKey = getTranslationKey(key) + ".tooltip";
            final String comment = getComment(key);
            final boolean hasTranslatedTooltip = translationChecker.existsWithFallback(tooltipKey);
            MutableComponent component = Component.empty().append(getTranslationComponent(key).withStyle(ChatFormatting.BOLD));
            if (hasTranslatedTooltip || !Strings.isBlank(comment)) {
                component = component.append(EMPTY_LINE).append(Component.translatableWithFallback(tooltipKey, comment));
            }
            // The "tooltip.warning" key is to be considered an internal API. It will be removed once Neo has a
            // generic styling mechanism for translation texts. Use at your own risk.
            component = component.append(translationChecker.optional(EMPTY_LINE, tooltipKey + ".warning", ChatFormatting.RED, ChatFormatting.BOLD));
            if (hasTranslatedTooltip && range != null) {
                component = component.append(EMPTY_LINE).append(Component.translatable(RANGE_TOOLTIP, range.toString()).withStyle(RANGE_TOOLTIP_STYLE));
            }
            return component;
        }

        /**
         * This is called whenever a value is changed and the change is submitted to the appropriate {@link ConfigSpec}.
         * 
         * @param key The key of the changed configuration. To get an absolute key, use {@link Context#makeKeyList(String)}.
         */
        protected void onChanged(final String key) {
            changed = true;
            final ValueSpec valueSpec = getValueSpec(key);
            if (valueSpec != null) {
                needsRestart = needsRestart.with(valueSpec.restartType());
            }
        }

        @Override
        protected void addOptions() {
            rebuild();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        protected ConfigurationSectionScreen rebuild() {
            if (list != null) { // this may be called early, skip and wait for init() then
                list.children().clear();
                boolean hasUndoableElements = false;

                final List<@Nullable Element> elements = new ArrayList<>();
                for (final Entry entry : context.entries) {
                    final String key = entry.getKey();
                    final Object rawValue = entry.getRawValue();
                    switch (entry.getRawValue()) {
                        case ConfigValue cv -> {
                            var valueSpec = getValueSpec(key);
                            var element = switch (valueSpec) {
                                case ListValueSpec listValueSpec -> createList(key, listValueSpec, cv);
                                case ValueSpec spec when cv.getClass() == ConfigValue.class && spec.getDefault() instanceof String -> createStringValue(key, valueSpec::test, () -> (String) cv.getRaw(), cv::set);
                                case ValueSpec spec when cv.getClass() == ConfigValue.class && spec.getDefault() instanceof Integer -> createIntegerValue(key, valueSpec, () -> (Integer) cv.getRaw(), cv::set);
                                case ValueSpec spec when cv.getClass() == ConfigValue.class && spec.getDefault() instanceof Long -> createLongValue(key, valueSpec, () -> (Long) cv.getRaw(), cv::set);
                                case ValueSpec spec when cv.getClass() == ConfigValue.class && spec.getDefault() instanceof Double -> createDoubleValue(key, valueSpec, () -> (Double) cv.getRaw(), cv::set);
                                case ValueSpec spec when cv.getClass() == ConfigValue.class && spec.getDefault() instanceof Enum<?> -> createEnumValue(key, valueSpec, (Supplier) cv::getRaw, (Consumer) cv::set);
                                case null -> null;

                                default -> switch (cv) {
                                    case ModConfigSpec.BooleanValue value -> createBooleanValue(key, valueSpec, value::getRaw, value::set);
                                    case ModConfigSpec.IntValue value -> createIntegerValue(key, valueSpec, value::getRaw, value::set);
                                    case ModConfigSpec.LongValue value -> createLongValue(key, valueSpec, value::getRaw, value::set);
                                    case ModConfigSpec.DoubleValue value -> createDoubleValue(key, valueSpec, value::getRaw, value::set);
                                    case ModConfigSpec.EnumValue value -> createEnumValue(key, valueSpec, (Supplier) value::getRaw, (Consumer) value::set);
                                    default -> createOtherValue(key, cv);
                                };
                            };
                            elements.add(context.filter.filterEntry(context, key, element));
                        }
                        case UnmodifiableConfig subsection when context.valueSpecs.get(key) instanceof UnmodifiableConfig subconfig -> elements.add(createSection(key, subconfig, subsection));
                        default -> elements.add(context.filter.filterEntry(context, key, createOtherSection(key, rawValue)));
                    }
                }
                elements.addAll(createSyntheticValues());

                for (final Element element : elements) {
                    if (element != null) {
                        if (element.name() == null) {
                            list.addSmall(new StringWidget(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, Component.empty(), font), element.getWidget(options));
                        } else {
                            final StringWidget label = new StringWidget(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, element.name, font).alignLeft();
                            label.setTooltip(Tooltip.create(element.tooltip));
                            list.addSmall(label, element.getWidget(options));
                        }
                        hasUndoableElements |= element.undoable;
                    }
                }

                if (hasUndoableElements && undoButton == null) {
                    createUndoButton();
                    createResetButton();
                }
            }
            return this;
        }

        /**
         * Override this to add additional configuration elements to the list.
         * 
         * @return A collection of {@link Element}.
         */
        protected Collection<? extends Element> createSyntheticValues() {
            return Collections.emptyList();
        }

        protected boolean isNonDefault(ModConfigSpec.ConfigValue<?> cv) {
            return !Objects.equals(cv.getRaw(), cv.getDefault());
        }

        protected boolean isAnyNondefault() {
            for (final Entry entry : context.entries) {
                if (entry.getRawValue() instanceof final ModConfigSpec.ConfigValue<?> cv) {
                    if (!(getValueSpec(entry.getKey()) instanceof ListValueSpec) && isNonDefault(cv)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Nullable
        protected Element createStringValue(final String key, final Predicate<String> tester, final Supplier<String> source, final Consumer<String> target) {
            if (source.get().length() > 192) {
                // That's just too much for the UI
                final StringWidget label = new StringWidget(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, Component.literal(source.get().substring(0, 128)), font).alignLeft();
                label.setTooltip(Tooltip.create(LONG_STRING));
                return new Element(getTranslationComponent(key), getTooltipComponent(key, null), label, false);
            }
            final EditBox box = new EditBox(font, Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, getTranslationComponent(key));
            box.setEditable(true);
            // no filter or the user wouldn't be able to type
            box.setTooltip(Tooltip.create(getTooltipComponent(key, null)));
            box.setMaxLength(Mth.clamp(source.get().length() + 5, 128, 192));
            box.setValue(source.get());
            box.setResponder(newValue -> {
                if (newValue != null && tester.test(newValue)) {
                    if (!newValue.equals(source.get())) {
                        undoManager.add(v -> {
                            target.accept(v);
                            onChanged(key);
                        }, newValue, v -> {
                            target.accept(v);
                            onChanged(key);
                        }, source.get());
                    }
                    box.setTextColor(EditBox.DEFAULT_TEXT_COLOR);
                    return;
                }
                box.setTextColor(0xFFFF0000);
            });
            return new Element(getTranslationComponent(key), getTooltipComponent(key, null), box);
        }

        /**
         * Called when an entry is encountered that is neither a {@link ModConfigSpec.ConfigValue} nor a section.
         * Override this to produce whatever UI elements are appropriate for this object.<p>
         * 
         * Note that this case is unusual and shouldn't happen unless someone injected something into the config system.
         * 
         * @param key   The key of the entry.
         * @param value The entry itself.
         * @return null if no UI element should be added or an {@link Element} to be added to the UI.
         */
        @Nullable
        protected Element createOtherSection(final String key, final Object value) {
            return null;
        }

        /**
         * Called when a {@link ModConfigSpec.ConfigValue} is found that has an unknown data type.
         * Override this to produce whatever UI elements are appropriate for this object.<p>
         * 
         * @param key   The key of the entry.
         * @param value The entry itself.
         * @return null if no UI element should be added or an {@link Element} to be added to the UI.
         */
        @Nullable
        protected Element createOtherValue(final String key, final ConfigValue<?> value) {
            final StringWidget label = new StringWidget(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, Component.literal(Objects.toString(value.getRaw())), font).alignLeft();
            label.setTooltip(Tooltip.create(UNSUPPORTED_ELEMENT));
            return new Element(getTranslationComponent(key), getTooltipComponent(key, null), label, false);
        }

        /**
         * A custom variant of OptionsInstance.Enum that doesn't show the key on the button, just the value
         */
        public record Custom<T>(List<T> values) implements OptionInstance.ValueSet<T> {

            @Override
            public Function<OptionInstance<T>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<T> tooltip, Options options, int x, int y, int width, Consumer<T> target) {
                return optionsInstance -> CycleButton.builder(optionsInstance.toString)
                        .withValues(CycleButton.ValueListSupplier.create(this.values))
                        .withTooltip(tooltip)
                        .displayOnlyValue()
                        .withInitialValue(optionsInstance.get())
                        .create(x, y, width, 20, optionsInstance.caption, (source, newValue) -> {
                            optionsInstance.set(newValue);
                            options.save();
                            target.accept(newValue);
                        });
            }

            @Override
            public Optional<T> validateValue(T value) {
                return values.contains(value) ? Optional.of(value) : Optional.empty();
            }

            @Override
            public Codec<T> codec() {
                return null;
            }
            public static final Custom<Boolean> BOOLEAN_VALUES_NO_PREFIX = new Custom<>(ImmutableList.of(Boolean.TRUE, Boolean.FALSE));
        }

        @Nullable
        protected Element createBooleanValue(final String key, final ValueSpec spec, final Supplier<Boolean> source, final Consumer<Boolean> target) {
            return new Element(getTranslationComponent(key), getTooltipComponent(key, null),
                    new OptionInstance<>(getTranslationKey(key), getTooltip(key, null), OptionInstance.BOOLEAN_TO_STRING, Custom.BOOLEAN_VALUES_NO_PREFIX, source.get(), newValue -> {
                        // regarding change detection: new value always is different (cycle button)
                        undoManager.add(v -> {
                            target.accept(v);
                            onChanged(key);
                        }, newValue, v -> {
                            target.accept(v);
                            onChanged(key);
                        }, source.get());
                    }));
        }

        @Nullable
        protected <T extends Enum<T>> Element createEnumValue(final String key, final ValueSpec spec, final Supplier<T> source, final Consumer<T> target) {
            @SuppressWarnings("unchecked")
            final Class<T> clazz = (Class<T>) spec.getClazz();
            assert clazz != null;

            final List<T> list = Arrays.stream(clazz.getEnumConstants()).filter(spec::test).toList();

            return new Element(getTranslationComponent(key), getTooltipComponent(key, null),
                    new OptionInstance<>(getTranslationKey(key), getTooltip(key, null), (caption, displayvalue) -> displayvalue instanceof TranslatableEnum tenum ? tenum.getTranslatedName() : Component.literal(displayvalue.name()),
                            new Custom<>(list), source.get(), newValue -> {
                                // regarding change detection: new value always is different (cycle button)
                                undoManager.add(v -> {
                                    target.accept(v);
                                    onChanged(key);
                                }, newValue, v -> {
                                    target.accept(v);
                                    onChanged(key);
                                }, source.get());
                            }));
        }

        @Nullable
        protected Element createIntegerValue(final String key, final ValueSpec spec, final Supplier<Integer> source, final Consumer<Integer> target) {
            final Range<Integer> range = spec.getRange();
            final int min = range != null ? range.getMin() : 0;
            final int max = range != null ? range.getMax() : Integer.MAX_VALUE;

            if ((long) max - (long) min < MAX_SLIDER_SIZE) {
                return createSlider(key, source, target, range);
            } else {
                return createNumberBox(key, spec, source, target, null, Integer::decode, 0);
            }
        }

        @Nullable
        protected Element createSlider(final String key, final Supplier<Integer> source, final Consumer<Integer> target, final @Nullable Range<Integer> range) {
            return new Element(getTranslationComponent(key), getTooltipComponent(key, null),
                    new OptionInstance<>(getTranslationKey(key), getTooltip(key, range),
                            (caption, displayvalue) -> Component.literal("" + displayvalue), new OptionInstance.IntRange(range != null ? range.getMin() : 0, range != null ? range.getMax() : Integer.MAX_VALUE),
                            null, source.get(), newValue -> {
                                if (!newValue.equals(source.get())) {
                                    undoManager.add(v -> {
                                        target.accept(v);
                                        onChanged(key);
                                    }, newValue, v -> {
                                        target.accept(v);
                                        onChanged(key);
                                    }, source.get());
                                }
                            }));
        }

        @Nullable
        protected Element createLongValue(final String key, final ValueSpec spec, final Supplier<Long> source, final Consumer<Long> target) {
            return createNumberBox(key, spec, source, target, null, Long::decode, 0L);
        }

        // if someone knows how to get a proper zero inside...
        @Nullable
        protected <T extends Number & Comparable<? super T>> Element createNumberBox(final String key, final ValueSpec spec, final Supplier<T> source,
                final Consumer<T> target, @Nullable final Predicate<T> tester, final Function<String, T> parser, final T zero) {
            final Range<T> range = spec.getRange();

            final EditBox box = new EditBox(font, Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, getTranslationComponent(key));
            box.setEditable(true);
            box.setFilter(newValueString -> {
                try {
                    parser.apply(newValueString);
                    return true;
                } catch (final NumberFormatException e) {
                    return isPartialNumber(newValueString, (range == null || range.getMin().compareTo(zero) < 0));
                }
            });
            box.setTooltip(Tooltip.create(getTooltipComponent(key, range)));
            box.setValue(source.get() + "");
            box.setResponder(newValueString -> {
                try {
                    final T newValue = parser.apply(newValueString);
                    if (tester != null ? tester.test(newValue) : (newValue != null && (range == null || range.test(newValue)) && spec.test(newValue))) {
                        if (!newValue.equals(source.get())) {
                            undoManager.add(v -> {
                                target.accept(v);
                                onChanged(key);
                            }, newValue, v -> {
                                target.accept(v);
                                onChanged(key);
                            }, source.get());
                        }
                        box.setTextColor(EditBox.DEFAULT_TEXT_COLOR);
                        return;
                    }
                } catch (final NumberFormatException e) {
                    // field probably is just empty/partial, ignore that
                }
                box.setTextColor(0xFFFF0000);
            });
            return new Element(getTranslationComponent(key), getTooltipComponent(key, null), box);
        }

        protected boolean isPartialNumber(String value, boolean allowNegative) {
            return switch (value) {
                case "" -> true;
                case "0" -> true;
                case "0x" -> true;
                case "0X" -> true;
                case "#" -> true; // not valid for doubles, but not worth making a special case
                case "-" -> allowNegative;
                case "-0" -> allowNegative;
                case "-0x" -> allowNegative;
                case "-0X" -> allowNegative;
                // case "-#" -> allowNegative; // Java allows this, but no thanks, that's just cursed.
                // doubles can also do NaN, inf, and 0e0. Again, not worth making a special case for those, I say.
                default -> false;
            };
        }

        @Nullable
        protected Element createDoubleValue(final String key, final ValueSpec spec, final Supplier<Double> source, final Consumer<Double> target) {
            return createNumberBox(key, spec, source, target, null, Double::parseDouble, 0.0);
        }

        @Nullable
        protected Element createSection(final String key, final UnmodifiableConfig subconfig, final UnmodifiableConfig subsection) {
            if (subconfig.isEmpty()) return null;
            return new Element(Component.translatable(SECTION, getTranslationComponent(key)), getTooltipComponent(key, null),
                    Button.builder(Component.translatable(SECTION, Component.translatable(translationChecker.check(getTranslationKey(key) + ".button", SECTION_TEXT))),
                            button -> minecraft.setScreen(sectionCache.computeIfAbsent(key,
                                    k -> new ConfigurationSectionScreen(context, this, subconfig.valueMap(), key, subsection.entrySet(), Component.translatable(getTranslationKey(key))).rebuild())))
                            .tooltip(Tooltip.create(getTooltipComponent(key, null)))
                            .width(Button.DEFAULT_WIDTH)
                            .build(),
                    false);
        }

        @Nullable
        protected <T> Element createList(final String key, final ListValueSpec spec, final ModConfigSpec.ConfigValue<List<T>> list) {
            return new Element(Component.translatable(SECTION, getTranslationComponent(key)), getTooltipComponent(key, null),
                    Button.builder(Component.translatable(SECTION, Component.translatable(translationChecker.check(getTranslationKey(key) + ".button", SECTION_TEXT))),
                            button -> minecraft.setScreen(sectionCache.computeIfAbsent(key,
                                    k -> new ConfigurationListScreen<>(Context.list(context, this), key, Component.translatable(CRUMB, this.getTitle(), CRUMB_SEPARATOR, getTranslationComponent(key)), spec, list)).rebuild()))
                            .tooltip(Tooltip.create(getTooltipComponent(key, null))).build(),
                    false);
        }

        @Override
        public void render(GuiGraphics graphics, int p_281550_, int p_282878_, float p_282465_) {
            setUndoButtonstate(undoManager.canUndo()); // in render()? Really? --- Yes! This is how vanilla does it.
            setResetButtonstate(isAnyNondefault());
            super.render(graphics, p_281550_, p_282878_, p_282465_);
        }

        @Override
        protected void addFooter() {
            if (undoButton != null || resetButton != null) {
                LinearLayout linearlayout = layout.addToFooter(LinearLayout.horizontal().spacing(8));
                if (undoButton != null) {
                    linearlayout.addChild(undoButton);
                }
                if (resetButton != null) {
                    linearlayout.addChild(resetButton);
                }
                linearlayout.addChild(doneButton);
            } else {
                super.addFooter();
            }
        }

        protected void createUndoButton() {
            undoButton = Button.builder(UNDO, button -> {
                undoManager.undo();
                rebuild();
            }).tooltip(Tooltip.create(UNDO_TOOLTIP)).width(Button.SMALL_WIDTH).build();
            undoButton.active = false;
        }

        protected void setUndoButtonstate(boolean state) {
            if (undoButton != null) {
                undoButton.active = state;
            }
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        protected void createResetButton() {
            resetButton = Button.builder(RESET, button -> {
                List<UndoManager.Step<?>> list = new ArrayList<>();
                for (final Entry entry : context.entries) {
                    if (entry.getRawValue() instanceof final ModConfigSpec.ConfigValue cv && !(getValueSpec(entry.getKey()) instanceof ListValueSpec) && isNonDefault(cv)) {
                        final String key = entry.getKey();
                        list.add(undoManager.step(v -> {
                            cv.set(v);
                            onChanged(key);
                        }, getValueSpec(key).correct(null), v -> {
                            cv.set(v);
                            onChanged(key);
                        }, cv.getRaw()));
                    }
                }
                undoManager.add(list);
                rebuild();
            }).tooltip(Tooltip.create(RESET_TOOLTIP)).width(Button.SMALL_WIDTH).build();
        }

        protected void setResetButtonstate(boolean state) {
            if (resetButton != null) {
                resetButton.active = state;
            }
        }

        @Override
        public void onClose() {
            if (changed) {
                if (lastScreen instanceof final ConfigurationSectionScreen parent) {
                    // "bubble up" the marker so the top-most section can change the ModConfig
                    parent.changed = true;
                } else {
                    // we are a top-level per-type config screen, i.e. one specific config file. Save the config and tell the mod to reload.
                    context.modSpec.save();
                }
                // the restart flag only matters when there were actual changes
                if (lastScreen instanceof final ConfigurationSectionScreen parent) {
                    parent.needsRestart = parent.needsRestart.with(needsRestart);
                } else if (lastScreen instanceof final ConfigurationScreen parent) {
                    parent.needsRestart = parent.needsRestart.with(needsRestart);
                }
            }
            super.onClose();
        }
    }

    /**
     * A UI screen that presents a list-type configuration value and allows the user to edit it, including an unlimited undo system and reset to default.<p>
     * 
     * This class is automatically used if you use NeoForge's generic configuration UI, see {@link ConfigurationScreen}.<p>
     * 
     * If you have special needs, you can subclass this class to achieve the desired behaviour. For example:<ul>
     * 
     * <li>To change how the label and buttons for the individual elements look, override {@link #createListLabel(int)}.
     * <li>To use another UI element, override the matching <code>create*Value()</code> method and return your new UI element wrapped in a {@link Element}.
     * <li>To add additional (synthetic) config values, override {@link #rebuild()} and add them to <code>list</code>. ({@link #createSyntheticValues()} is not used for lists).
     * <li>To be notified on each changed value instead of getting one {@link ModConfigEvent} at the end, override {@link #onChanged(String)} on the {@link ConfigurationScreen},
     * not here. The list will only be updated in the {@link ModConfigSpec.ConfigValue} when this screen is closed.
     * <li>To limit the number of elements in a list, pass a {@link ModConfigSpec.Range} to {@link ModConfigSpec.Builder#defineList(List, Supplier, Supplier, Predicate, Range)}.
     * </ul>
     */
    public static class ConfigurationListScreen<T> extends ConfigurationSectionScreen {
        protected final String key;
        protected final ListValueSpec spec;

        // the original data
        protected final ModConfigSpec.ConfigValue<List<T>> valueList;
        // the copy of the data we are working on
        protected List<T> cfgList;

        public ConfigurationListScreen(final Context context, final String key, final Component title, final ListValueSpec spec,
                final ModConfigSpec.ConfigValue<List<T>> valueList) {
            super(context, title);
            this.key = key;
            this.spec = spec;
            this.valueList = valueList; // === (ListValueSpec)getValueSpec(key)
            this.cfgList = new ArrayList<>(valueList.getRaw());
        }

        @Override
        protected ConfigurationSectionScreen rebuild() {
            if (list != null) { // this may be called early, skip and wait for init() then
                list.children().clear();

                for (int idx = 0; idx < cfgList.size(); idx++) {
                    var entry = cfgList.get(idx);
                    var element = switch (entry) {
                        case null -> null;
                        case final Boolean value -> createBooleanListValue(idx, value);
                        case final Integer value -> createIntegerListValue(idx, value);
                        case final Long value -> createLongListValue(idx, value);
                        case final Double value -> createDoubleListValue(idx, value);
                        case final String value -> createStringListValue(idx, value);
                        default -> createOtherValue(idx, entry);
                    };

                    if (element != null) {
                        final AbstractWidget widget = element.getWidget(options);
                        if (widget instanceof EditBox box) {
                            // Force our responder to check content and set text colour.
                            // This is only needed on lists, as section cannot have new UI elements added with bad data.
                            // Here, this can happen when a new element is added to the list.
                            // As the new value is the old value, no undo record will be created.
                            box.setValue(box.getValue());
                        }
                        list.addSmall(createListLabel(idx), widget);
                    }
                }

                createAddElementButton();
                if (undoButton == null) {
                    createUndoButton();
                    createResetButton();
                }
            }
            return this;
        }

        protected boolean isAnyNondefault() {
            return !cfgList.equals(valueList.getDefault());
        }

        /**
         * Creates a button to add a new element to the end of the list and adds it to the UI.<p>
         * 
         * Override this if you want a different button or want to add more elements.
         */
        @SuppressWarnings("unchecked")
        protected void createAddElementButton() {
            final Supplier<?> newElement = spec.getNewElementSupplier();
            final Range<Integer> sizeRange = spec.getSizeRange();

            if (newElement != null && sizeRange.test(cfgList.size() + 1)) {
                list.addSmall(new StringWidget(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, Component.empty(), font), Button.builder(NEW_LIST_ELEMENT, button -> {
                    List<T> newValue = new ArrayList<>(cfgList);
                    newValue.add((T) newElement.get());
                    undoManager.add(v -> {
                        cfgList = v;
                        onChanged(key);
                    }, newValue, v -> {
                        cfgList = v;
                        onChanged(key);
                    }, cfgList);
                    rebuild();
                }).build());
            }
        }

        /**
         * Creates a new widget to label a list value and provide manipulation buttons for it.<p>
         * 
         * Override this if you want different labels/buttons.
         * 
         * @param idx The index into the list.
         * @return An {@link AbstractWidget} to be rendered in the left column of the options screen
         */
        protected AbstractWidget createListLabel(int idx) {
            return new ListLabelWidget(0, 0, Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, Component.translatable(LIST_ELEMENT, idx), idx);
        }

        /**
         * Called when a list element is found that has an unknown or unsupported data type. Override this to produce whatever
         * UI elements are appropriate for this object.<p>
         * 
         * Note that all types of elements that can be read from the config file as part of a list are already supported. You
         * only need this if you manipulate the contents of the list after it has been loaded.<p>
         * 
         * If this returns null, no row will be shown on the screen, but the up/down buttons will still see your element.
         * Which means that the user will see no change when moving another element over the hidden line. Consider returning
         * a {@link StringWidget} as a placeholder instead.<p>
         * 
         * Do <em>not</em> capture {@link #cfgList} here or in another create*Value() method. The undo/reset system will
         * replace the list, so you need to always access the field. You can (and should) capture the index.
         * 
         * @param idx   The index into the list.
         * @param entry The entry itself.
         * @return null if this element should be skipped or an {@link Element} to be added to the UI.
         */
        @Nullable
        protected Element createOtherValue(final int idx, final T entry) {
            final StringWidget label = new StringWidget(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, Component.literal(Objects.toString(entry)), font).alignLeft();
            label.setTooltip(Tooltip.create(UNSUPPORTED_ELEMENT));
            return new Element(getTranslationComponent(key), getTooltipComponent(key, null), label, false);
        }

        @SuppressWarnings("unchecked")
        @Nullable
        protected Element createStringListValue(final int idx, final String value) {
            return createStringValue(key, v -> spec.test(List.of(v)), () -> value, newValue -> cfgList.set(idx, (T) newValue));
        }

        @SuppressWarnings("unchecked")
        @Nullable
        protected Element createDoubleListValue(final int idx, final Double value) {
            return createNumberBox(key, spec, () -> value, newValue -> cfgList.set(idx, (T) newValue), v -> spec.test(List.of(v)), Double::parseDouble, 0.0);
        }

        @SuppressWarnings("unchecked")
        @Nullable
        protected Element createLongListValue(final int idx, final Long value) {
            return createNumberBox(key, spec, () -> value, newValue -> cfgList.set(idx, (T) newValue), v -> spec.test(List.of(v)), Long::decode, 0L);
        }

        @SuppressWarnings("unchecked")
        @Nullable
        protected Element createIntegerListValue(final int idx, final Integer value) {
            return createNumberBox(key, spec, () -> value, newValue -> cfgList.set(idx, (T) newValue), v -> spec.test(List.of(v)), Integer::decode, 0);
        }

        @SuppressWarnings("unchecked")
        @Nullable
        protected Element createBooleanListValue(final int idx, final Boolean value) {
            return createBooleanValue(key, spec, () -> value, newValue -> cfgList.set(idx, (T) newValue));
        }

        /**
         * Swap the given element with the next one. Should be called by the list label widget to manipulate the list.
         */
        protected boolean swap(final int idx, final boolean simulate) {
            final List<T> values = new ArrayList<>(cfgList);
            values.add(idx, values.remove(idx + 1));
            return addUndoListener(simulate, values);
        }

        /**
         * Remove the given element. Should be called by the list label widget to manipulate the list.
         */
        protected boolean del(final int idx, final boolean simulate) {
            final List<T> values = new ArrayList<>(cfgList);
            values.remove(idx);
            return addUndoListener(simulate, values);
        }

        private boolean addUndoListener(boolean simulate, List<T> values) {
            final boolean valid = spec.test(values);
            if (!simulate && valid) {
                undoManager.add(v -> {
                    cfgList = v;
                    onChanged(key);
                }, values, v -> {
                    cfgList = v;
                    onChanged(key);
                }, cfgList);
                rebuild();
            }
            return valid;
        }

        @Override
        public void onClose() {
            if (changed && spec.test(cfgList)) {
                valueList.set(cfgList);
                if (context.parent instanceof ConfigurationSectionScreen parent) {
                    parent.onChanged(key);
                }
            }
            super.onClose();
        }

        @Override
        public void render(GuiGraphics graphics, int p_281550_, int p_282878_, float p_282465_) {
            doneButton.active = spec.test(cfgList);
            super.render(graphics, p_281550_, p_282878_, p_282465_);
        }

        protected void onChanged(final String key) {
            changed = true;
            // parent's onChanged() will be fired when we actually assign the changed list. For now,
            // we've only changed our working copy.
        }

        @SuppressWarnings("unchecked")
        protected void createResetButton() {
            resetButton = Button.builder(RESET, button -> {
                undoManager.add(
                        v -> {
                            cfgList = v;
                            onChanged(key);
                        }, new ArrayList<>((List<T>) getValueSpec(key).correct(null)),
                        v -> {
                            cfgList = v;
                            onChanged(key);
                        }, cfgList);
                rebuild();
            }).tooltip(Tooltip.create(RESET_TOOLTIP)).width(Button.SMALL_WIDTH).build();
        }

        /**
         * A widget to be used as a label in a list of configuration values.<p>
         * 
         * It includes buttons for "move element up", "move element down", and "delete element" as well as a label.
         * 
         */
        public class ListLabelWidget extends AbstractContainerWidget {
            protected final Button upButton = Button.builder(MOVE_LIST_ELEMENT_UP, this::up).build();
            protected final Button downButton = Button.builder(MOVE_LIST_ELEMENT_DOWN, this::down).build();
            protected final Button delButton = Button.builder(REMOVE_LIST_ELEMENT, this::rem).build();
            protected final StringWidget label = new StringWidget(0, 0, 0, 0, Component.empty(), font).alignLeft();
            protected final int idx;
            protected final boolean isFirst;
            protected final boolean isLast;

            public ListLabelWidget(final int x, final int y, final int width, final int height, final Component labelText, final int idx) {
                super(x, y, width, height, labelText);
                this.idx = idx;
                this.isFirst = idx == 0;
                this.isLast = idx + 1 == cfgList.size();
                label.setMessage(labelText);
                checkButtons();
                updateLayout();
            }

            @Override
            public void setX(final int pX) {
                super.setX(pX);
                updateLayout();
            }

            @Override
            public void setY(final int pY) {
                super.setY(pY);
                updateLayout();
            }

            @Override
            public void setHeight(final int pHeight) {
                super.setHeight(pHeight);
                updateLayout();
            }

            @Override
            public void setWidth(int pWidth) {
                super.setWidth(pWidth);
                updateLayout();
            }

            @Override
            public void setSize(int pWidth, int pHeight) {
                super.setSize(pWidth, pHeight);
                updateLayout();
            }

            protected void updateLayout() {
                upButton.setX(getX());
                downButton.setX(getX() + getHeight() + 2);
                delButton.setX(getX() + getWidth() - getHeight());
                label.setX(getX() + 2 * 22);

                upButton.setY(getY());
                downButton.setY(getY());
                delButton.setY(getY());
                label.setY(getY());

                upButton.setHeight(getHeight());
                downButton.setHeight(getHeight());
                delButton.setHeight(getHeight());
                label.setHeight(getHeight());

                upButton.setWidth(getHeight());
                downButton.setWidth(getHeight());
                delButton.setWidth(getHeight());
                label.setWidth(getWidth() - 3 * (getHeight() + 2));
            }

            void up(final Button button) {
                swap(idx - 1, false);
            }

            void down(final Button button) {
                swap(idx, false);
            }

            void rem(final Button button) {
                del(idx, false);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of(upButton, label, downButton, delButton);
            }

            @Override
            protected void renderWidget(final GuiGraphics pGuiGraphics, final int pMouseX, final int pMouseY, final float pPartialTick) {
                checkButtons();
                label.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
                if (!isFirst) {
                    upButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
                }
                if (!isLast) {
                    downButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
                }
                delButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            }

            protected void checkButtons() {
                upButton.visible = !isFirst;
                upButton.active = !isFirst && swap(idx - 1, true);
                downButton.visible = !isLast;
                downButton.active = !isLast && swap(idx, true);
                Range<Integer> sizeRange = spec.getSizeRange();
                delButton.active = !cfgList.isEmpty() && (sizeRange == null || sizeRange.test(cfgList.size() - 1)) && del(idx, true);
            }

            @Override
            protected void updateWidgetNarration(final NarrationElementOutput pNarrationElementOutput) {
                // TODO I have no idea. Help?
            }

            @Override
            protected int contentHeight() {
                return 0; // TODO 1.21.4 no idea
            }

            @Override
            protected double scrollRate() {
                return 4.0; // TODO 1.21.4 no idea
            }
        }
    }

    /**
     * A class representing an undo/redo buffer.<p>
     * 
     * Every undo step is represented as 2 actions, one to initially execute when the step is added and
     * to redo after an undo, and one to execute to undo the step. Both get a captured parameter to make
     * defining them inline or reusing the code portion easier.
     */
    public static final class UndoManager {
        public record Step<T>(Consumer<T> run, T newValue, Consumer<T> undo, T oldValue) {
            private void runUndo() {
                undo.accept(oldValue);
            }

            private void runRedo() {
                run.accept(newValue);
            }
        };

        private final List<Step<?>> undos = new ArrayList<>();
        private final List<Step<?>> redos = new ArrayList<>();

        public void undo() {
            if (canUndo()) {
                Step<?> step = undos.removeLast();
                step.runUndo();
                redos.add(step);
            }
        }

        public void redo() {
            if (canRedo()) {
                Step<?> step = redos.removeLast();
                step.runRedo();
                undos.add(step);
            }
        }

        private void add(Step<?> step, boolean execute) {
            undos.add(step);
            redos.clear();
            if (execute) {
                step.runRedo();
            }
        }

        public <T> Step<T> step(Consumer<T> run, T newValue, Consumer<T> undo, T oldValue) {
            return new Step<>(run, newValue, undo, oldValue);
        }

        public <T> void add(Consumer<T> run, T newValue, Consumer<T> undo, T oldValue) {
            add(step(run, newValue, undo, oldValue), true);
        }

        public <T> void addNoExecute(Consumer<T> run, T newValue, Consumer<T> undo, T oldValue) {
            add(step(run, newValue, undo, oldValue), false);
        }

        public void add(Step<?>... steps) {
            add(ImmutableList.copyOf(steps));
        }

        public void add(final List<Step<?>> steps) {
            add(new Step<>(n -> steps.forEach(Step::runRedo), null, n -> steps.forEach(Step::runUndo), null), true);
        }

        public boolean canUndo() {
            return !undos.isEmpty();
        }

        public boolean canRedo() {
            return !redos.isEmpty();
        }
    }
}
