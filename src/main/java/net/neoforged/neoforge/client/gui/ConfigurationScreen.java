/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import static net.neoforged.neoforge.common.NeoForgeConfig.CLIENT;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig.Entry;
import com.google.common.collect.ImmutableList;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.IConfigEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.ListValueSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Range;
import net.neoforged.neoforge.common.ModConfigSpec.ValueSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigurationScreen extends OptionsSubScreen {
    public static class TranslationChecker {
        private static final Logger LOGGER = LogManager.getLogger();
        private final Set<String> untranslatables = new HashSet<String>();

        public String check(final String translationKey) {
            if (!I18n.exists(translationKey)) {
                untranslatables.add(translationKey);
            }
            return translationKey;
        }

        public void finish() {
            if (CLIENT.logUntranslatedConfigurationWarnings.get() && !FMLLoader.isProduction() && !untranslatables.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("""
                        \n	Dev warning - Untranslated configuration keys detected. Please translate your configuration keys so users can properly configure your mod.
                        """);
                stringBuilder.append("\nUntranslated keys:");
                for (String key : untranslatables) {
                    stringBuilder.append("\n     ").append(key);
                }

                LOGGER.warn(stringBuilder);
            }
            untranslatables.clear();
        }
    }

    public enum RestartType {
        NONE, SERVER, GAME;

        RestartType with(RestartType other) {
            return other == NONE ? this : (other == GAME || this == GAME) ? GAME : SERVER;
        }
    }

    public static boolean compactList = false;

    private static final String LANG_PREFIX = "neoforge.configuration.uitext.";
    public static final Component TOOLTIP_CANNOT_EDIT_THIS_WHILE_ONLINE = Component.translatable(LANG_PREFIX + "notonline");
    public static final Component TOOLTIP_CANNOT_EDIT_THIS_WHILE_OPEN_TO_LAN = Component.translatable(LANG_PREFIX + "notlan");
    public static final Component TOOLTIP_CANNOT_EDIT_NOT_LOADED = Component.translatable(LANG_PREFIX + "notloaded");
    private static final String SECTION = LANG_PREFIX + "section";
    private static final String LIST_ELEMENT = LANG_PREFIX + "listelement";
    public static final Component NEW_LIST_ELEMENT = Component.translatable(LANG_PREFIX + "newlistelement");
    public static final Component MOVE_LIST_ELEMENT_UP = Component.translatable(LANG_PREFIX + "listelementup");
    public static final Component MOVE_LIST_ELEMENT_DOWN = Component.translatable(LANG_PREFIX + "listelementdown");
    public static final Component REMOVE_LIST_ELEMENT = Component.translatable(LANG_PREFIX + "listelementremove");
    public static final Component UNSUPPORTED_ELEMENT = Component.translatable(LANG_PREFIX + "unsupportedelement");
    public static final Component GAME_RESTART_TITLE = Component.translatable(LANG_PREFIX + "restart.game.title");
    public static final Component GAME_RESTART_MESSAGE = Component.translatable(LANG_PREFIX + "restart.game.text");
    public static final Component GAME_RESTART_YES = Component.translatable("menu.quit");
    public static final Component SERVER_RESTART_TITLE = Component.translatable(LANG_PREFIX + "restart.server.title");
    public static final Component SERVER_RESTART_MESSAGE = Component.translatable(LANG_PREFIX + "restart.server.text");
    public static final Component RETURN_TO_MENU = Component.translatable("menu.returnToMenu");
    public static final Component SAVING_LEVEL = Component.translatable("menu.savingLevel");
    public static final Component RESTART_NO = Component.translatable(LANG_PREFIX + "restart.return");
    public static final Component UNDO = Component.translatable(LANG_PREFIX + "undo");
    public static final Component UNDO_TOOLTIP = Component.translatable(LANG_PREFIX + "undo.tooltip");
    public static final Component RESET = Component.translatable(LANG_PREFIX + "reset");
    public static final Component RESET_TOOLTIP = Component.translatable(LANG_PREFIX + "reset.tooltip");
    static final TranslationChecker translationChecker = new TranslationChecker();

    private final ModContainer mod;
    private RestartType needsRestart = RestartType.NONE;

    public ConfigurationScreen(final ModContainer mod, final Minecraft mc, final Screen parent) {
        super(parent, mc.options, Component.translatable(translationChecker.check(mod.getModId() + ".configuration.title")));
        this.mod = mod;
    }

    @Override
    protected void addOptions() {
        for (final Type type : ModConfig.Type.values()) {
            for (final ModConfig modConfig : ConfigTracker.INSTANCE.configSets().get(type)) {
                if (modConfig.getModId().equals(mod.getModId())) {
                    final Button btn = Button.builder(Component.translatable(SECTION, Component.translatable(LANG_PREFIX + type.name().toLowerCase())),
                            button -> minecraft.setScreen(new ConfigurationSectionScreen(mod, minecraft, this, type, modConfig).rebuild())).build();
                    if (!((ModConfigSpec) modConfig.getSpec()).isLoaded()) {
                        btn.setTooltip(Tooltip.create(TOOLTIP_CANNOT_EDIT_NOT_LOADED));
                        btn.active = false;
                    } else if (type == Type.SERVER && minecraft.getCurrentServer() != null && !minecraft.isSingleplayer()) {
                        btn.setTooltip(Tooltip.create(TOOLTIP_CANNOT_EDIT_THIS_WHILE_ONLINE));
                        btn.active = false;
                    } else if (type == Type.SERVER && minecraft.hasSingleplayerServer() && minecraft.getSingleplayerServer().isPublished()) {
                        btn.setTooltip(Tooltip.create(TOOLTIP_CANNOT_EDIT_THIS_WHILE_OPEN_TO_LAN));
                        btn.active = false;
                    }
                    list.addSmall(btn, null);
                }
            }
        }
    }

    @Override
    public void onClose() {
        translationChecker.finish();
        switch (needsRestart) {
            case GAME:
                minecraft.setScreen(new ConfirmScreen(b -> {
                    if (b) {
                        minecraft.stop();
                    } else {
                        minecraft.setScreen(this);
                    }
                }, GAME_RESTART_TITLE, GAME_RESTART_MESSAGE, GAME_RESTART_YES, RESTART_NO));
                return;
            case SERVER:
                if (minecraft.level != null) {
                    minecraft.setScreen(new ConfirmScreen(b -> {
                        if (b) {
                            // when changing server configs from the client is added, this is where we tell the server to restart and the new config.
                            // also needs a different text in MP ("server will restart/exit, yada yada") than in SP
                            onDisconnect();
                        } else {
                            minecraft.setScreen(this);
                        }
                    }, SERVER_RESTART_TITLE, SERVER_RESTART_MESSAGE, minecraft.isLocalServer() ? RETURN_TO_MENU : CommonComponents.GUI_DISCONNECT, RESTART_NO));
                    return;
                }
                // else fallthrough. If no server is running, we don't need to stop one.
            case NONE:
                super.onClose();
        }
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

    public static class ConfigurationSectionScreen extends OptionsSubScreen {
        public static record Context(ModContainer mod, Minecraft mc, Screen parent, Type type, ModConfig modConfig, ModConfigSpec modSpec,
                Set<? extends Entry> entries, Map<String, Object> valueSpecs, List<String> keylist) {
            public static Context top(final ModContainer mod, final Minecraft mc, final Screen parent, final Type type, final ModConfig modConfig) {
                return new Context(mod, mc, parent, type, modConfig, (ModConfigSpec) modConfig.getSpec(), ((ModConfigSpec) modConfig.getSpec()).getValues().entrySet(),
                        modConfig.getSpec().valueMap(), List.of());
            }

            public static Context section(final Context parentContext, final Screen parent, final Set<? extends Entry> entries, final Map<String, Object> valueSpecs,
                    final String key) {
                return new Context(parentContext.mod, parentContext.mc, parent, parentContext.type, parentContext.modConfig, parentContext.modSpec, entries, valueSpecs,
                        parentContext.makeKeyList(key));
            }

            public static Context list(final Context parentContext, final Screen parent) {
                return new Context(parentContext.mod, parentContext.mc, parent, parentContext.type, parentContext.modConfig, parentContext.modSpec,
                        parentContext.entries, parentContext.valueSpecs, parentContext.keylist);
            }

            private ArrayList<String> makeKeyList(final String key) {
                final ArrayList<String> result = new ArrayList<>(keylist);
                result.add(key);
                return result;
            }
        }

        public static record Element(Component name, Component tooltip, AbstractWidget widget, OptionInstance<?> option) {
            public Element(final Component name, final Component tooltip, final AbstractWidget widget) {
                this(name, tooltip, widget, null);
            }

            public Element(final Component name, final Component tooltip, final OptionInstance<?> option) {
                this(name, tooltip, null, option);
            }

            public AbstractWidget getWidget(final Options options) {
                return widget != null ? widget : option.createButton(options);
            }

            public Object any() {
                return widget != null ? widget : option;
            }
        }

        protected final Context context;
        protected boolean changed = false;
        protected RestartType needsRestart = RestartType.NONE;
        protected final Map<String, Object> undoMap = new HashMap<>();
        protected final Map<String, ConfigurationSectionScreen> sectionCache = new HashMap<>();
        protected Button undoButton, resetButton; // must not be changed after creation unless the reference inside the layout also is replaced

        public ConfigurationSectionScreen(final ModContainer mod, final Minecraft mc, final Screen parent, final ModConfig.Type type, final ModConfig modConfig) {
            this(Context.top(mod, mc, parent, type, modConfig), Component.translatable(translationChecker.check(mod.getModId() + ".configuration." + type.name().toLowerCase() + ".title")));
            needsRestart = type == Type.STARTUP ? RestartType.GAME : RestartType.NONE;
        }

        public ConfigurationSectionScreen(final Context parentContext, final Screen parent, final Map<String, Object> valueSpecs, final String key,
                final Set<? extends Entry> entrySet) {
            this(Context.section(parentContext, parent, entrySet, valueSpecs, key),
                    Component.translatable(translationChecker.check(parentContext.mod.getModId() + ".configuration." + key + ".title")));
        }

        public ConfigurationSectionScreen(final Context context, final Component title) {
            super(context.parent, context.mc.options, title);
            this.context = context;
        }

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
            return result != null ? result : context.mod.getModId() + ".configuration." + key;
        }

        protected MutableComponent getTranslationComponent(final String key) {
            return Component.translatable(translationChecker.check(getTranslationKey(key)));
        }

        protected String getTooltipString(final String key) {
            final ValueSpec valueSpec = getValueSpec(key);
            return valueSpec != null ? getValueSpec(key).getComment() : context.modSpec.getLevelComment(context.makeKeyList(key));
        }

        protected <T> OptionInstance.TooltipSupplier<T> getTooltip(final String key) {
            return OptionInstance.cachedConstantTooltip(getTooltipComponent(key));
        }

        protected Component getTooltipComponent(final String key) {
            return Component.empty().append(getTranslationComponent(key).withStyle(ChatFormatting.BOLD))
                    .append(Component.literal("\n\n")).append(
                            Component.translatableWithFallback(translationChecker.check(context.mod.getModId() + ".configuration." + key + ".tooltip"), getTooltipString(key)));
        }

        protected void onChanged(final String key) {
            changed = true;
            final ValueSpec valueSpec = getValueSpec(key);
            if (valueSpec != null && valueSpec.needsWorldRestart()) {
                needsRestart = needsRestart.with(RestartType.SERVER);
            }
        }

        @Override
        protected void addOptions() {
            rebuild();
        }

        @SuppressWarnings("unchecked")
        protected ConfigurationSectionScreen rebuild() {
            if (list != null) { // this may be called early, skip and wait for init() then
                list.children().clear();
                boolean hasUndoableElements = false;
                boolean hasNonDefaultValues = false;
                final List<Element> elements = new ArrayList<>();
                for (final Entry entry : context.entries) {
                    final String key = entry.getKey();
                    final Object rawValue = entry.getRawValue();
                    if (rawValue instanceof final ModConfigSpec.ConfigValue cv) {
                        final ValueSpec valueSpec = getValueSpec(key);
                        if (valueSpec != null) {
                            if (cv instanceof final ModConfigSpec.BooleanValue value) {
                                elements.add(createBooleanValue(key, valueSpec, value, value::set));
                            } else if (cv instanceof final ModConfigSpec.IntValue value) {
                                elements.add(createIntegerValue(key, valueSpec, value, value::set));
                            } else if (cv instanceof final ModConfigSpec.LongValue value) {
                                elements.add(createLongValue(key, valueSpec, value, value::set));
                            } else if (cv instanceof final ModConfigSpec.DoubleValue value) {
                                elements.add(createDoubleValue(key, valueSpec, value, value::set));
                            } else if (cv instanceof final ModConfigSpec.EnumValue value) {
                                elements.add(createEnumValue(key, valueSpec, value, value::set));
                            } else if (String.class.isInstance(valueSpec.getDefault())) {
                                elements.add(createStringValue(key, valueSpec::test, cv, cv::set));
                            } else if (valueSpec instanceof ListValueSpec listSpec) {
                                elements.add(createList(key, listSpec, cv));
                            } else {
                                elements.add(createOtherValue(key, cv));
                            }
                            hasUndoableElements |= elements.getLast() != null;
                            hasNonDefaultValues |= isNonDefault(cv);
                        }
                    } else if (rawValue instanceof final UnmodifiableConfig subsection) {
                        final Object object = context.valueSpecs.get(key);
                        if (object instanceof final UnmodifiableConfig subconfig) {
                            elements.add(createSection(key, subconfig, subsection));
                        }
                    } else {
                        elements.add(createOtherSection(key, rawValue));
                    }
                }

                if (compactList) {
                    list.addSmall(elements.stream().filter(Objects::nonNull).map(Element::widget).toList());
                } else {
                    for (final Element element : elements) {
                        if (element != null) {
                            final StringWidget label = new StringWidget(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, element.name, font).alignLeft();
                            label.setTooltip(Tooltip.create(element.tooltip));
                            list.addSmall(label, element.getWidget(options));
                        }
                    }
                }

                if (hasUndoableElements && undoButton == null) {
                    createUndoButton();
                    createResetButton();
                    setResetButtonstate(hasNonDefaultValues);
                }
            }
            return this;
        }

        protected boolean isNonDefault(ModConfigSpec.ConfigValue<?> cv) {
            return ((cv.get() == null) != (cv.getDefault() == null)) || (cv.get() != null && !cv.get().equals(cv.getDefault()));
        }

        protected Element createStringValue(final String key, final Predicate<String> tester, final Supplier<String> source, final Consumer<String> target) {
            final EditBox box = new EditBox(font, Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, getTranslationComponent(key));
            box.setEditable(true);
            // no filter or the user wouldn't be able to type
            box.setTooltip(Tooltip.create(getTooltipComponent(key)));
            box.setValue(source.get() + "");
            box.setResponder(newValue -> {
                if (newValue != null && tester.test(newValue)) {
                    if (!newValue.equals(source.get())) {
                        undoMap.putIfAbsent(key, source.get());
                        target.accept(newValue);
                        onChanged(key);
                    }
                    box.setTextColor(EditBox.DEFAULT_TEXT_COLOR);
                    return;
                }
                box.setTextColor(0xFFFF0000);
            });
            return new Element(getTranslationComponent(key), getTooltipComponent(key), box);
        }

        /**
         * Called when an entry is encountered that is neither a {@link ModConfigSpec.ConfigValue} nor a section. Override this to produce whatever UI elements are appropriate for this object.<p>
         * 
         * Note that this case is unusual and shouldn't happen unless someone injected something into the config system.
         * 
         * @param key   The key of the entry.
         * @param value The entry itself.
         * @return null if no UI element should be added or an {@link Element} to be added to the UI.
         */
        protected Element createOtherSection(final String key, final Object value) {
            return null;
        }

        /**
         * Called when a {@link ModConfigSpec.ConfigValue} is found that has an unknown data type. Override this to produce whatever UI elements are appropriate for this object.<p>
         * 
         * @param key   The key of the entry.
         * @param value The entry itself.
         * @return null if no UI element should be added or an {@link Element} to be added to the UI.
         */
        protected Element createOtherValue(final String key, final ConfigValue<?> value) {
            final StringWidget label = new StringWidget(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, Component.literal(Objects.toString(value.get())), font).alignLeft();
            label.setTooltip(Tooltip.create(UNSUPPORTED_ELEMENT));
            return new Element(getTranslationComponent(key), getTooltipComponent(key), label);
        }

        /**
         * A custom variant of OptionsInstance.Enum that doesn't show the key on the button, just the value
         */
        public static record Custom<T>(List<T> values) implements OptionInstance.ValueSet<T> {
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
        }

        public static final Custom<Boolean> BOOLEAN_VALUES_NO_PREFIX = new Custom<>(ImmutableList.of(Boolean.TRUE, Boolean.FALSE));

        protected Element createBooleanValue(final String key, final ValueSpec spec, final Supplier<Boolean> source, final Consumer<Boolean> target) {
            return new Element(getTranslationComponent(key), getTooltipComponent(key),
                    new OptionInstance<>(getTranslationKey(key), getTooltip(key), OptionInstance.BOOLEAN_TO_STRING, BOOLEAN_VALUES_NO_PREFIX, source.get(), newVal -> {
                        // regarding change detection: new value always is different (cycle button)
                        undoMap.putIfAbsent(key, source.get());
                        target.accept(newVal);
                        onChanged(key);
                    }));
        }

        protected <T extends Enum<T>> Element createEnumValue(final String key, final ValueSpec spec, final Supplier<T> source, final Consumer<T> target) {
            @SuppressWarnings("unchecked")
            final Class<T> clazz = (Class<T>) spec.getClazz();
            final List<T> list = Arrays.stream(clazz.getEnumConstants()).filter(spec::test).map(e -> e).toList();

            return new Element(getTranslationComponent(key), getTooltipComponent(key),
                    new OptionInstance<>(getTranslationKey(key), getTooltip(key), (caption, displayvalue) -> Component.literal(displayvalue.name()),
                            new Custom<>(list), source.get(), newValue -> {
                                // regarding change detection: new value always is different (cycle button)
                                undoMap.putIfAbsent(key, source.get());
                                target.accept(newValue);
                                onChanged(key);
                            }));
        }

        protected Element createIntegerValue(final String key, final ValueSpec spec, final Supplier<Integer> source, final Consumer<Integer> target) {
            final Range<Integer> range = spec.getRange();
            final Integer min = range != null ? range.getMin() : 0;
            final Integer max = range != null ? range.getMax() : Integer.MAX_VALUE;

            if (max - min < 256) {
                return new Element(getTranslationComponent(key), getTooltipComponent(key),
                        new OptionInstance<>(getTranslationKey(key), getTooltip(key),
                                (caption, displayvalue) -> Options.genericValueLabel(caption, Component.literal("" + displayvalue)), new OptionInstance.IntRange(min, max),
                                null /* codec */, source.get(), newValue -> {
                                    if (!newValue.equals(source.get())) {
                                        undoMap.putIfAbsent(key, source.get());
                                        target.accept(newValue);
                                        onChanged(key);
                                    }
                                }));
            } else {
                return createNumberBox(key, spec, source, target, null, Integer::decode, 0);
            }
        }

        protected Element createLongValue(final String key, final ValueSpec spec, final Supplier<Long> source, final Consumer<Long> target) {
            return createNumberBox(key, spec, source, target, null, Long::decode, 0L);
        }

        // if someone knows how to get a proper zero inside...
        protected <T extends Number & Comparable<? super T>> Element createNumberBox(final String key, final ValueSpec spec, final Supplier<T> source,
                final Consumer<T> target, final Predicate<T> tester, final Function<String, T> parser, final T zero) {
            final Range<T> range = spec.getRange();

            final EditBox box = new EditBox(font, Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, getTranslationComponent(key));
            box.setEditable(true);
            box.setFilter(newValue -> {
                try {
                    parser.apply(newValue);
                    return true;
                } catch (final NumberFormatException e) {
                    return newValue.isEmpty() || ((range == null || range.getMin().compareTo(zero) < 0) && newValue.equals("-"));
                }
            });
            box.setTooltip(Tooltip.create(getTooltipComponent(key)));
            box.setValue(source.get() + "");
            box.setResponder(newValue -> {
                try {
                    final T val = parser.apply(newValue);
                    if (tester != null ? tester.test(val) : (val != null && (range == null || range.test(val)) && spec.test(val))) {
                        if (!val.equals(source.get())) {
                            undoMap.putIfAbsent(key, source.get());
                            target.accept(val);
                            onChanged(key);
                        }
                        box.setTextColor(EditBox.DEFAULT_TEXT_COLOR);
                        return;
                    }
                } catch (final NumberFormatException e) {
                    // field probably is just empty, ignore that
                }
                box.setTextColor(0xFFFF0000);
            });
            return new Element(getTranslationComponent(key), getTooltipComponent(key), box);
        }

        protected Element createDoubleValue(final String key, final ValueSpec spec, final Supplier<Double> source, final Consumer<Double> target) {
            return createNumberBox(key, spec, source, target, null, Double::parseDouble, 0.0);
        }

        protected Element createSection(final String key, final UnmodifiableConfig subconfig, final UnmodifiableConfig subsection) {
            return new Element(Component.empty(), Component.empty(),
                    Button
                            .builder(Component.translatable(SECTION, getTranslationComponent(key)),
                                    button -> minecraft.setScreen(sectionCache.computeIfAbsent(key,
                                            k -> new ConfigurationSectionScreen(context, this, subconfig.valueMap(), key, subsection.entrySet()).rebuild())))
                            .tooltip(Tooltip.create(getTooltipComponent(key))).build());
        }

        protected <T> Element createList(final String key, final ListValueSpec spec, final ModConfigSpec.ConfigValue<List<T>> list) {
            return new Element(getTranslationComponent(key), getTooltipComponent(key),
                    Button
                            .builder(Component.translatable(SECTION, getTranslationComponent(key)),
                                    button -> minecraft.setScreen(sectionCache.computeIfAbsent(key,
                                            k -> new ConfigurationListScreen<>(Context.list(context, this), key, getTranslationComponent(key), spec, list)).rebuild()))
                            .tooltip(Tooltip.create(getTooltipComponent(key))).build());
        }

        @Override
        public void render(GuiGraphics p_281549_, int p_281550_, int p_282878_, float p_282465_) {
            super.render(p_281549_, p_281550_, p_282878_, p_282465_);
            // need to check both as a screen may opt to store its initial value in the map on creation. The list screen does this.
            setUndoButtonstate(changed && !undoMap.isEmpty()); // in render()? Really? --- Yes! This is how vanilla does it.
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
                linearlayout.addChild(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).build());
            } else {
                super.addFooter();
            }
        }

        protected void createUndoButton() {
            undoButton = Button.builder(UNDO, button -> {
                for (final Entry entry : context.entries) {
                    if (entry.getRawValue() instanceof final ModConfigSpec.ConfigValue cv) {
                        undo(entry.getKey(), cv);
                    }
                }
                setResetButtonstate(true);
                rebuild();
            }).tooltip(Tooltip.create(UNDO_TOOLTIP)).build();
            undoButton.active = false;
        }

        protected void setUndoButtonstate(boolean state) {
            if (undoButton != null) {
                undoButton.active = state;
            }
        }

        protected void createResetButton() {
            resetButton = Button.builder(RESET, button -> {
                for (final Entry entry : context.entries) {
                    if (entry.getRawValue() instanceof final ModConfigSpec.ConfigValue cv) {
                        reset(entry.getKey(), cv);
                    }
                }
                setResetButtonstate(false);
                rebuild();
            }).tooltip(Tooltip.create(RESET_TOOLTIP)).build();
        }

        protected void setResetButtonstate(boolean state) {
            if (resetButton != null) {
                resetButton.active = state;
            }
        }

        @SuppressWarnings("unchecked")
        protected void undo(String key, @SuppressWarnings("rawtypes") ModConfigSpec.ConfigValue configValue) {
            if (undoMap.containsKey(key)) {
                configValue.set(undoMap.remove(key));
                onChanged(key);
            }
        }

        @SuppressWarnings("unchecked")
        protected void reset(String key, @SuppressWarnings("rawtypes") ModConfigSpec.ConfigValue configValue) {
            if (isNonDefault(configValue)) {
                undoMap.put(key, configValue.get());
                configValue.set(getValueSpec(key).correct(null));
                onChanged(key);
            }
        }

        @Override
        public void onClose() {
            if (changed) {
                if (lastScreen instanceof final ConfigurationSectionScreen parent) {
                    parent.changed = true;
                } else {
                    // we are a top-level per-type config screen, i.e. one specific config file. Save the config and tell the mod to reload.
                    // Yes, this means this will fire multiple times if the user changes values in multiple configs, but that doesn't really matter.
                    context.modConfig.save();
                    IConfigEvent.reloading(context.modConfig).post();
                }
                // restart only matters when there were actual changes
                if (lastScreen instanceof final ConfigurationSectionScreen parent) {
                    parent.needsRestart = parent.needsRestart.with(needsRestart);
                } else if (lastScreen instanceof final ConfigurationScreen parent) {
                    parent.needsRestart = parent.needsRestart.with(needsRestart);
                }
            }
            super.onClose();
        }
    }

    public static class ConfigurationListScreen<T> extends ConfigurationSectionScreen {
        protected final String key;
        protected final ListValueSpec spec;

        // the original data
        protected final ModConfigSpec.ConfigValue<List<T>> valueList;
        // the copy of the real data we are working on
        protected List<T> cfgList;

        public ConfigurationListScreen(final Context context, final String key, final Component title, final ListValueSpec spec,
                final ModConfigSpec.ConfigValue<List<T>> valueList) {
            super(context, title);
            this.key = key;
            this.valueList = valueList; // === (ListValueSpec)getValueSpec(key)
            this.cfgList = new ArrayList<>(valueList.get());
            this.spec = spec;
        }

        @Override
        protected ConfigurationSectionScreen rebuild() {
            if (list != null) { // this may be called early, skip and wait for init() then
                list.children().clear();
                final List<Element> elements = new ArrayList<>();
                int idx = 0;
                for (final T entry : cfgList) {
                    if (entry instanceof final Boolean value) {
                        elements.add(createBooleanListValue(idx, value));
                    } else if (entry instanceof final Integer value) {
                        elements.add(createIntegerListValue(idx, value));
                    } else if (entry instanceof final Long value) {
                        elements.add(createLongListValue(idx, value));
                    } else if (entry instanceof final Double value) {
                        elements.add(createDoubleListValue(idx, value));
                    } else if (entry instanceof final String value) {
                        elements.add(createStringListValue(idx, value));
                    } else {
                        elements.add(createOtherValue(idx, entry));
                    }
                    idx++;
                }

                idx = 0;
                for (final Element element : elements) {
                    if (element != null) {
                        list.addSmall(createListLabel(idx), element.getWidget(options));
                    }
                    idx++;
                }

                createAddElementButton();
                if (undoButton == null) {
                    createUndoButton();
                    createResetButton();
                    setResetButtonstate(isNonDefault(valueList));
                }
            }
            return this;
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

            if (newElement != null && (sizeRange == null || sizeRange.test(cfgList.size() + 1))) {
                list.addSmall(Button.builder(NEW_LIST_ELEMENT, button -> {
                    cfgList.add((T) newElement.get());
                    rebuild();
                    onChanged(key);
                    undoMap.putIfAbsent(key, 0);
                }).build(), null);
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
         * Called when a list element is found that has an unknown or unsupported data type. Override this to produce whatever UI elements are appropriate for this object.<p>
         * 
         * Note that all types of elements that can be read from the config file as part of a list are already supported. You only need this if you manupulate the contents of the list after it has been loaded.
         * 
         * @param idx   The index into the list.
         * @param entry The entry itself.
         * @return null if this element should be skipped or an {@link Element} to be added to the UI.
         */
        protected Element createOtherValue(final int idx, final T entry) {
            final StringWidget label = new StringWidget(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, Component.literal(Objects.toString(entry)), font).alignLeft();
            label.setTooltip(Tooltip.create(UNSUPPORTED_ELEMENT));
            return new Element(getTranslationComponent(key), getTooltipComponent(key), label);
        }

        @SuppressWarnings("unchecked")
        protected Element createStringListValue(final int idx, final String value) {
            return createStringValue(key, v -> spec.test(List.of(v)), () -> value, newValue -> cfgList.set(idx, (T) newValue));
        }

        @SuppressWarnings("unchecked")
        protected Element createDoubleListValue(final int idx, final Double value) {
            return createNumberBox(key, spec, () -> value, newValue -> cfgList.set(idx, (T) newValue), v -> spec.test(List.of(v)), Double::parseDouble, 0.0);
        }

        @SuppressWarnings("unchecked")
        protected Element createLongListValue(final int idx, final Long value) {
            return createNumberBox(key, spec, () -> value, newValue -> cfgList.set(idx, (T) newValue), v -> spec.test(List.of(v)), Long::decode, 0L);
        }

        @SuppressWarnings("unchecked")
        protected Element createIntegerListValue(final int idx, final Integer value) {
            return createNumberBox(key, spec, () -> value, newValue -> cfgList.set(idx, (T) newValue), v -> spec.test(List.of(v)), Integer::decode, 0);
        }

        @SuppressWarnings("unchecked")
        protected Element createBooleanListValue(final int idx, final Boolean value) {
            return createBooleanValue(key, spec, () -> value, newValue -> cfgList.set(idx, (T) newValue));
        }

        /**
         * Swap the given element with the next one.
         */
        protected boolean swap(final int idx, final boolean simulate) {
            final List<T> values = new ArrayList<>(cfgList);
            values.add(idx, values.remove(idx + 1));
            final boolean valid = spec.test(values);
            if (!simulate && valid) {
                cfgList = values;
                rebuild();
                onChanged(key);
                undoMap.putIfAbsent(key, 0);
            }
            return valid;
        }

        protected boolean del(final int idx, final boolean simulate) {
            final List<T> values = new ArrayList<>(cfgList);
            values.remove(idx);
            final boolean valid = spec.test(values);
            if (!simulate && valid) {
                cfgList = values;
                rebuild();
                onChanged(key);
                undoMap.putIfAbsent(key, 0);
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

        protected void onChanged(final String key) {
            changed = true;
            // parent's onChanged() will be fired when we actually assign the changed list
        }

        @SuppressWarnings("unchecked")
        protected void undo(String key, @SuppressWarnings("rawtypes") ModConfigSpec.ConfigValue configValue) {
            if (key.equals(this.key)) {
                // we can't trust this value, it is most likely the undo value for one of the edit fields.
                // But if we get a List, it must have been set by reset(), as there is no edit field that
                // can produce Lists. Then we can (and must) use that instead of falling back to the original
                // value, because in that case we're undoing a reset.
                final Object undoValue = undoMap.remove(key);
                cfgList = new ArrayList<>(undoValue instanceof List ? (List<T>) undoValue : valueList.get());
            }
            changed = false;
        }

        @SuppressWarnings("unchecked")
        protected void reset(String key, @SuppressWarnings("rawtypes") ConfigValue configValue) {
            if (key.equals(this.key)) {
                undoMap.put(key, cfgList);
                configValue.set(getValueSpec(key).correct(null));
                onChanged(key);
                // actually changes the value here, so notify the parent
                if (context.parent instanceof ConfigurationSectionScreen parent) {
                    parent.onChanged(key);
                }
                cfgList = new ArrayList<>(valueList.get());
            }
        };

        public class ListLabelWidget extends AbstractContainerWidget {
            protected final Button upButton = Button.builder(MOVE_LIST_ELEMENT_UP, this::up).build();
            protected final Button downButton = Button.builder(MOVE_LIST_ELEMENT_DOWN, this::down).build();
            protected final Button delButton = Button.builder(REMOVE_LIST_ELEMENT, this::rem).build();
            protected final StringWidget label = new StringWidget(0, 0, 0, 0, Component.empty(), font).alignLeft();
            protected final int idx;
            protected final boolean isFirst;
            protected final boolean isLast;
            protected int recheck = 20;

            public ListLabelWidget(final int x, final int y, final int width, final int height, final Component message, final int idx) {
                super(x, y, width, height, message);
                this.idx = idx;
                this.isFirst = idx == 0;
                this.isLast = idx + 1 == cfgList.size();
                label.setMessage(message);
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
                if (recheck-- < 0) {
                    // the check is relatively expensive and it is unlikely that it changes as the list is rebuilt on change. Still, a mod may have a list
                    // where the deletability of an element depends on the value of another. So periodically re-run the check.
                    checkButtons();
                }
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
                delButton.active = cfgList.size() > 1 && (sizeRange == null || sizeRange.test(cfgList.size() - 1)) && del(idx, true);
                recheck = 20;
            }

            @Override
            protected void updateWidgetNarration(final NarrationElementOutput pNarrationElementOutput) {
                // TODO I have no idea. Help?
            }
        }
    }
}
