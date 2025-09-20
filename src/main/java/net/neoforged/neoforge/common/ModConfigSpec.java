/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec.CorrectionAction;
import com.electronwill.nightconfig.core.ConfigSpec.CorrectionListener;
import com.electronwill.nightconfig.core.EnumGetMethod;
import com.electronwill.nightconfig.core.InMemoryFormat;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.neoforged.fml.Logging;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/*
 * Like {@link com.electronwill.nightconfig.core.ConfigSpec} except in builder format, and extended to accept comments, language keys,
 * and other things mod configs would find useful.
 */
public class ModConfigSpec implements IConfigSpec {
    /*
     * Each config field (for example `category.subcategory.something`) has:
     * - an associated ValueSpec which contains metadata about the field,
     * - a ConfigValue which caches the value for the field.
     * These are stored in `spec` and `values` respectively,
     * (ab)using the tree structure of a Config.
     *
     * Intermediate levels cannot be represented inside a Config like that,
     * so their metadata is stored in `levelComments` and `levelTranslationKeys`.
     */
    /**
     * Stores the comments for intermediate levels.
     */
    private final Map<List<String>, String> levelComments;
    /**
     * Stores the translation keys for intermediate levels.
     */
    private final Map<List<String>, String> levelTranslationKeys;

    /**
     * Stores the {@link ValueSpec}s, (ab)using the hierarchical structure of {@link Config}.
     */
    private final UnmodifiableConfig spec;
    /**
     * Stores the {@link ConfigValue}s, (ab)using the hierarchical structure of {@link Config}.
     */
    private final UnmodifiableConfig values;
    /**
     * The currently loaded config values.
     */
    @Nullable
    private ILoadedConfig loadedConfig;

    private static final Logger LOGGER = LogManager.getLogger();

    private ModConfigSpec(UnmodifiableConfig spec, UnmodifiableConfig values, Map<List<String>, String> levelComments, Map<List<String>, String> levelTranslationKeys) {
        this.spec = spec;
        this.values = values;
        this.levelComments = levelComments;
        this.levelTranslationKeys = levelTranslationKeys;
    }

    @Override
    public boolean isEmpty() {
        return this.spec.isEmpty();
    }

    public String getLevelComment(List<String> path) {
        return levelComments.get(path);
    }

    public String getLevelTranslationKey(List<String> path) {
        return levelTranslationKeys.get(path);
    }

    @Override
    public void acceptConfig(@Nullable ILoadedConfig config) {
        this.loadedConfig = config;
        if (config != null && !isCorrect(config.config())) {
            // Correct in case the config did not get corrected before this function was called.
            // This should not happen under normal circumstances, hence the warning.
            LOGGER.warn(Logging.CORE, "Configuration {} is not correct. Correcting", config);
            correct(config.config(),
                    (action, path, incorrectValue, correctedValue) -> LOGGER.warn(Logging.CORE, "Incorrect key {} was corrected from {} to its default, {}. {}", DOT_JOINER.join(path), incorrectValue, correctedValue, incorrectValue == correctedValue ? "This seems to be an error." : ""),
                    (action, path, incorrectValue, correctedValue) -> LOGGER.debug(Logging.CORE, "The comment on key {} does not match the spec. This may create a backup.", DOT_JOINER.join(path)));

            config.save();
        }
        this.afterReload();
    }

    @Override
    public void validateSpec(ModConfig config) {
        forEachValue(getValues().valueMap().values(), configValue -> {
            if (!configValue.getSpec().restartType().isValid(config.getType())) {
                throw new IllegalArgumentException("Configuration value " + String.join(".", configValue.getPath())
                        + " defined in config " + config.getFileName() + " has restart of type " + configValue.getSpec().restartType() + " which cannot be used for configs of type " + config.getType());
            }
        });
    }

    public boolean isLoaded() {
        return loadedConfig != null;
    }

    public UnmodifiableConfig getSpec() {
        return this.spec;
    }

    public UnmodifiableConfig getValues() {
        return this.values;
    }

    private void forEachValue(Iterable<Object> configValues, Consumer<ConfigValue<?>> consumer) {
        configValues.forEach(value -> {
            if (value instanceof ConfigValue<?> configValue) {
                consumer.accept(configValue);
            } else if (value instanceof Config innerConfig) {
                forEachValue(innerConfig.valueMap().values(), consumer);
            }
        });
    }

    public void afterReload() {
        // Only clear the caches of configs that don't need a restart
        this.resetCaches(RestartType.NONE);
    }

    @ApiStatus.Internal
    public void resetCaches(RestartType restartType) {
        forEachValue(getValues().valueMap().values(), configValue -> {
            if (configValue.getSpec().restartType == restartType) {
                configValue.clearCache();
            }
        });
    }

    /**
     * Saves the current config values to the config file, and fires the config reloading event.
     */
    public void save() {
        Preconditions.checkNotNull(loadedConfig, "Cannot save config value without assigned Config object present");
        loadedConfig.save();
    }

    @Override
    public boolean isCorrect(UnmodifiableCommentedConfig config) {
        LinkedList<String> parentPath = new LinkedList<>();
        return correct(this.spec, config, parentPath, Collections.unmodifiableList(parentPath), (a, b, c, d) -> {}, null, true) == 0;
    }

    public void correct(CommentedConfig config) {
        correct(config, (action, path, incorrectValue, correctedValue) -> {}, null);
    }

    public int correct(CommentedConfig config, CorrectionListener listener) {
        return correct(config, listener, null);
    }

    public int correct(CommentedConfig config, CorrectionListener listener, @Nullable CorrectionListener commentListener) {
        LinkedList<String> parentPath = new LinkedList<>(); //Linked list for fast add/removes
        return correct(this.spec, config, parentPath, Collections.unmodifiableList(parentPath), listener, commentListener, false);
    }

    /**
     * {@code config} will be downcast to {@link CommentedConfig} if {@code dryRun} is {@code false} and a modification needs to be made.
     */
    private int correct(UnmodifiableConfig spec, UnmodifiableCommentedConfig config, LinkedList<String> parentPath, List<String> parentPathUnmodifiable, CorrectionListener listener, @Nullable CorrectionListener commentListener, boolean dryRun) {
        int count = 0;

        Map<String, Object> specMap = spec.valueMap();
        Map<String, Object> configMap = config.valueMap();

        for (Map.Entry<String, Object> specEntry : specMap.entrySet()) {
            final String key = specEntry.getKey();
            final Object specValue = specEntry.getValue();
            final Object configValue = configMap.get(key);
            final CorrectionAction action = configValue == null ? CorrectionAction.ADD : CorrectionAction.REPLACE;

            parentPath.addLast(key);

            if (specValue instanceof Config) {
                if (configValue instanceof CommentedConfig) {
                    count += correct((Config) specValue, (CommentedConfig) configValue, parentPath, parentPathUnmodifiable, listener, commentListener, dryRun);
                    if (count > 0 && dryRun)
                        return count;
                } else if (dryRun) {
                    return 1;
                } else {
                    CommentedConfig newValue = ((CommentedConfig) config).createSubConfig();
                    configMap.put(key, newValue);
                    listener.onCorrect(action, parentPathUnmodifiable, configValue, newValue);
                    count++;
                    count += correct((Config) specValue, newValue, parentPath, parentPathUnmodifiable, listener, commentListener, dryRun);
                }

                String newComment = levelComments.get(parentPath);
                String oldComment = config.getComment(key);
                if (!stringsMatchNormalizingNewLines(oldComment, newComment)) {
                    if (commentListener != null)
                        commentListener.onCorrect(action, parentPathUnmodifiable, oldComment, newComment);

                    if (dryRun)
                        return 1;

                    ((CommentedConfig) config).setComment(key, newComment);
                }
            } else {
                ValueSpec valueSpec = (ValueSpec) specValue;
                if (!valueSpec.test(configValue)) {
                    if (dryRun)
                        return 1;

                    Object newValue = valueSpec.correct(configValue);
                    configMap.put(key, newValue);
                    listener.onCorrect(action, parentPathUnmodifiable, configValue, newValue);
                    count++;
                }
                String oldComment = config.getComment(key);
                if (!stringsMatchNormalizingNewLines(oldComment, valueSpec.getComment())) {
                    if (commentListener != null)
                        commentListener.onCorrect(action, parentPathUnmodifiable, oldComment, valueSpec.getComment());

                    if (dryRun)
                        return 1;

                    ((CommentedConfig) config).setComment(key, valueSpec.getComment());
                }
            }

            parentPath.removeLast();
        }

        // Second step: removes the unspecified values
        for (Iterator<Map.Entry<String, Object>> ittr = configMap.entrySet().iterator(); ittr.hasNext();) {
            Map.Entry<String, Object> entry = ittr.next();
            if (!specMap.containsKey(entry.getKey())) {
                if (dryRun)
                    return 1;

                ittr.remove();
                parentPath.addLast(entry.getKey());
                listener.onCorrect(CorrectionAction.REMOVE, parentPathUnmodifiable, entry.getValue(), null);
                parentPath.removeLast();
                count++;
            }
        }
        return count;
    }

    private boolean stringsMatchNormalizingNewLines(@Nullable String string1, @Nullable String string2) {
        boolean blank1 = string1 == null || string1.isBlank();
        boolean blank2 = string2 == null || string2.isBlank();
        if (blank1 != blank2) {
            return false;
        } else if (blank1 && blank2) {
            return true;
        } else {
            return string1.replaceAll("\r\n", "\n")
                    .equals(string2.replaceAll("\r\n", "\n"));
        }
    }

    public static class Builder {
        private final Config spec = Config.of(LinkedHashMap::new, InMemoryFormat.withUniversalSupport()); // Use LinkedHashMap for consistent ordering
        private BuilderContext context = new BuilderContext();
        private final Map<List<String>, String> levelComments = new HashMap<>();
        private final Map<List<String>, String> levelTranslationKeys = new HashMap<>();
        private final List<String> currentPath = new ArrayList<>();
        private final List<ConfigValue<?>> values = new ArrayList<>();

        //Object
        public <T> ConfigValue<T> define(String path, T defaultValue) {
            return define(split(path), defaultValue);
        }

        public <T> ConfigValue<T> define(List<String> path, T defaultValue) {
            return define(path, defaultValue, o -> o != null && defaultValue.getClass().isAssignableFrom(o.getClass()));
        }

        public <T> ConfigValue<T> define(String path, T defaultValue, Predicate<Object> validator) {
            return define(split(path), defaultValue, validator);
        }

        public <T> ConfigValue<T> define(List<String> path, T defaultValue, Predicate<Object> validator) {
            Objects.requireNonNull(defaultValue, "Default value can not be null");
            return define(path, () -> defaultValue, validator);
        }

        public <T> ConfigValue<T> define(String path, Supplier<T> defaultSupplier, Predicate<Object> validator) {
            return define(split(path), defaultSupplier, validator);
        }

        public <T> ConfigValue<T> define(List<String> path, Supplier<T> defaultSupplier, Predicate<Object> validator) {
            return define(path, defaultSupplier, validator, Object.class);
        }

        public <T> ConfigValue<T> define(List<String> path, Supplier<T> defaultSupplier, Predicate<Object> validator, Class<?> clazz) {
            context.setClazz(clazz);
            return define(path, new ValueSpec(defaultSupplier, validator, context, path), defaultSupplier);
        }

        public <T> ConfigValue<T> define(List<String> path, ValueSpec value, Supplier<T> defaultSupplier) { // This is the root where everything at the end of the day ends up.
            if (!currentPath.isEmpty()) {
                List<String> tmp = new ArrayList<>(currentPath.size() + path.size());
                tmp.addAll(currentPath);
                tmp.addAll(path);
                path = tmp;
            }
            spec.set(path, value);
            context = new BuilderContext();
            return new ConfigValue<>(this, path, defaultSupplier);
        }

        public <V extends Comparable<? super V>> ConfigValue<V> defineInRange(String path, V defaultValue, V min, V max, Class<V> clazz) {
            return defineInRange(split(path), defaultValue, min, max, clazz);
        }

        public <V extends Comparable<? super V>> ConfigValue<V> defineInRange(List<String> path, V defaultValue, V min, V max, Class<V> clazz) {
            return defineInRange(path, (Supplier<V>) () -> defaultValue, min, max, clazz);
        }

        public <V extends Comparable<? super V>> ConfigValue<V> defineInRange(String path, Supplier<V> defaultSupplier, V min, V max, Class<V> clazz) {
            return defineInRange(split(path), defaultSupplier, min, max, clazz);
        }

        public <V extends Comparable<? super V>> ConfigValue<V> defineInRange(List<String> path, Supplier<V> defaultSupplier, V min, V max, Class<V> clazz) {
            Range<V> range = new Range<>(clazz, min, max);
            context.setRange(range);
            comment(" Default: " + defaultSupplier.get());
            comment(" Range: " + range);
            return define(path, defaultSupplier, range);
        }

        public <T> ConfigValue<T> defineInList(String path, T defaultValue, Collection<? extends T> acceptableValues) {
            return defineInList(split(path), defaultValue, acceptableValues);
        }

        public <T> ConfigValue<T> defineInList(String path, Supplier<T> defaultSupplier, Collection<? extends T> acceptableValues) {
            return defineInList(split(path), defaultSupplier, acceptableValues);
        }

        public <T> ConfigValue<T> defineInList(List<String> path, T defaultValue, Collection<? extends T> acceptableValues) {
            return defineInList(path, () -> defaultValue, acceptableValues);
        }

        public <T> ConfigValue<T> defineInList(List<String> path, Supplier<T> defaultSupplier, Collection<? extends T> acceptableValues) {
            return define(path, defaultSupplier, acceptableValues::contains);
        }

        /**
         * See {@link #defineList(List, Supplier, Supplier, Predicate)} for details.<p>
         * 
         * This variant takes its key as a string and splits it on ".".<br>
         * This variant takes its default value directly and wraps it in a supplier.<br>
         * This variant has no supplier for new elements, so no new elements can be added in the config UI.
         * 
         * @deprecated Use {@link #defineList(String, List, Supplier, Predicate)}
         */
        @Deprecated
        public <T> ConfigValue<List<? extends T>> defineList(String path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
            return defineList(split(path), defaultValue, elementValidator);
        }

        /**
         * See {@link #defineList(List, Supplier, Supplier, Predicate)} for details.<p>
         * 
         * This variant takes its key as a string and splits it on ".".<br>
         * This variant takes its default value directly and wraps it in a supplier.
         * 
         */
        public <T> ConfigValue<List<? extends T>> defineList(String path, List<? extends T> defaultValue, Supplier<T> newElementSupplier, Predicate<Object> elementValidator) {
            return defineList(split(path), defaultValue, newElementSupplier, elementValidator);
        }

        /**
         * See {@link #defineList(List, Supplier, Supplier, Predicate)} for details.<p>
         * 
         * This variant takes its key as a string and splits it on ".".<br>
         * This variant has no supplier for new elements, so no new elements can be added in the config UI.
         * 
         * @deprecated Use {@link #defineList(String, Supplier, Supplier, Predicate)}
         */
        @Deprecated
        public <T> ConfigValue<List<? extends T>> defineList(String path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            return defineList(split(path), defaultSupplier, elementValidator);
        }

        /**
         * See {@link #defineList(List, Supplier, Supplier, Predicate)} for details.<p>
         * 
         * This variant takes its key as a string and splits it on ".".
         * 
         */
        public <T> ConfigValue<List<? extends T>> defineList(String path, Supplier<List<? extends T>> defaultSupplier, Supplier<T> newElementSupplier, Predicate<Object> elementValidator) {
            return defineList(split(path), defaultSupplier, newElementSupplier, elementValidator);
        }

        /**
         * See {@link #defineList(List, Supplier, Supplier, Predicate)} for details.<p>
         * 
         * This variant takes its default value directly and wraps it in a supplier.<br>
         * This variant has no supplier for new elements, so no new elements can be added in the config UI.
         * 
         * @deprecated Use {@link #defineList(List, List, Supplier, Predicate)}
         */
        @Deprecated
        public <T> ConfigValue<List<? extends T>> defineList(List<String> path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
            return defineList(path, () -> defaultValue, elementValidator);
        }

        /**
         * See {@link #defineList(List, Supplier, Supplier, Predicate)} for details.<p>
         * 
         * This variant takes its default value directly and wraps it in a supplier.
         * 
         */
        public <T> ConfigValue<List<? extends T>> defineList(List<String> path, List<? extends T> defaultValue, Supplier<T> newElementSupplier, Predicate<Object> elementValidator) {
            return defineList(path, () -> defaultValue, newElementSupplier, elementValidator);
        }

        /**
         * See {@link #defineList(List, Supplier, Supplier, Predicate)} for details.<p>
         * 
         * This variant has no supplier for new elements, so no new elements can be added in the config UI.
         * 
         * @deprecated Use {@link #defineList(List, Supplier, Supplier, Predicate)}
         */
        @Deprecated
        public <T> ConfigValue<List<? extends T>> defineList(List<String> path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            return defineList(path, defaultSupplier, null, elementValidator);
        }

        /**
         * Build a new config value that holds a {@link List}.<p>
         * 
         * This list cannot be empty. See also {@link #defineList(List, Supplier, Supplier, Predicate, Range)} for more control over the list size.
         * 
         * @param <T>                The class of element of the list. Directly supported are {@link String}, {@link Boolean}, {@link Integer}, {@link Long} and {@link Double}.
         *                           Other classes will be saved using their string representation and will be read back from the config file as strings.
         * @param path               The key for the config value in list form, i.e. pre-split into section and key.
         * @param defaultSupplier    A {@link Supplier} for the default value of the list. This will be used if the config file doesn't exist or if it reads as invalid.
         * @param newElementSupplier A {@link Supplier} for new elements to be added to the list. This is only used in the UI when the user presses the "add" button.
         *                           The supplied value doesn't have to validate as correct, but it should provide a good starting point for the user to make it correct.
         *                           If this parameter is null, there will be no "add" button in the UI (if the default UI is used).
         * @param elementValidator   A {@link Predicate} to verify if a list element is valid. Elements that are read from the config file are removed from the list if the
         *                           validator rejects them.
         * @return A {@link ConfigValue} object that can be used to access the config value and that will live-update if the value changed, i.e. because the config file
         *         was updated or the config UI was used.
         */
        public <T> ConfigValue<List<? extends T>> defineList(List<String> path, Supplier<List<? extends T>> defaultSupplier, Supplier<T> newElementSupplier, Predicate<Object> elementValidator) {
            return defineList(path, defaultSupplier, newElementSupplier, elementValidator, ListValueSpec.NON_EMPTY);
        }

        /**
         * See {@link #defineListAllowEmpty(List, Supplier, Supplier, Predicate)} for details.<p>
         * 
         * This variant takes its key as a string and splits it on ".".<br>
         * This variant takes its default value directly and wraps it in a supplier.<br>
         * This variant has no supplier for new elements, so no new elements can be added in the config UI.
         * 
         * @deprecated Use {@link #defineListAllowEmpty(String, List, Supplier, Predicate)}
         */
        @Deprecated
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
            return defineListAllowEmpty(split(path), defaultValue, elementValidator);
        }

        /**
         * See {@link #defineListAllowEmpty(List, Supplier, Supplier, Predicate)} for details.<p>
         * 
         * This variant takes its key as a string and splits it on ".".<br>
         * This variant takes its default value directly and wraps it in a supplier.
         * 
         */
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String path, List<? extends T> defaultValue, Supplier<T> newElementSupplier, Predicate<Object> elementValidator) {
            return defineListAllowEmpty(split(path), defaultValue, newElementSupplier, elementValidator);
        }

        /**
         * See {@link #defineListAllowEmpty(List, Supplier, Supplier, Predicate)} for details.<p>
         * 
         * This variant takes its key as a string and splits it on ".".<br>
         * This variant has no supplier for new elements, so no new elements can be added in the config UI.
         * 
         * @deprecated Use {@link #defineListAllowEmpty(String, Supplier, Supplier, Predicate)}
         */
        @Deprecated
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            return defineListAllowEmpty(split(path), defaultSupplier, elementValidator);
        }

        /**
         * See {@link #defineListAllowEmpty(List, Supplier, Supplier, Predicate)} for details.<p>
         * 
         * This variant takes its key as a string and splits it on ".".
         * 
         */
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String path, Supplier<List<? extends T>> defaultSupplier, Supplier<T> newElementSupplier, Predicate<Object> elementValidator) {
            return defineListAllowEmpty(split(path), defaultSupplier, newElementSupplier, elementValidator);
        }

        /**
         * See {@link #defineListAllowEmpty(List, Supplier, Supplier, Predicate)} for details.<p>
         * 
         * This variant takes its default value directly and wraps it in a supplier.<br>
         * This variant has no supplier for new elements, so no new elements can be added in the config UI.
         * 
         * @deprecated Use {@link #defineListAllowEmpty(List, List, Supplier, Predicate)}
         */
        @Deprecated
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(List<String> path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
            return defineListAllowEmpty(path, () -> defaultValue, elementValidator);
        }

        /**
         * See {@link #defineListAllowEmpty(List, Supplier, Supplier, Predicate)} for details.<p>
         * 
         * This variant takes its default value directly and wraps it in a supplier.
         * 
         */
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(List<String> path, List<? extends T> defaultValue, Supplier<T> newElementSupplier, Predicate<Object> elementValidator) {
            return defineListAllowEmpty(path, () -> defaultValue, newElementSupplier, elementValidator);
        }

        /**
         * See {@link #defineListAllowEmpty(List, Supplier, Supplier, Predicate)} for details.<p>
         * 
         * This variant has no supplier for new elements, so no new elements can be added in the config UI.
         * 
         * @deprecated Use {@link #defineListAllowEmpty(List, Supplier, Supplier, Predicate)}
         */
        @Deprecated
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(List<String> path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            return defineListAllowEmpty(path, defaultSupplier, null, elementValidator);
        }

        /**
         * Build a new config value that holds a {@link List}.<p>
         * 
         * This list can be empty. See also {@link #defineList(List, Supplier, Supplier, Predicate, Range)} for more control over the list size.
         * 
         * @param <T>                The class of element of the list. Directly supported are {@link String}, {@link Boolean}, {@link Integer}, {@link Long} and {@link Double}.
         *                           Other classes will be saved using their string representation and will be read back from the config file as strings.
         * @param path               The key for the config value in list form, i.e. pre-split into section and key.
         * @param defaultSupplier    A {@link Supplier} for the default value of the list. This will be used if the config file doesn't exist or if it reads as invalid.
         * @param newElementSupplier A {@link Supplier} for new elements to be added to the list. This is only used in the UI when the user presses the "add" button.
         *                           The supplied value doesn't have to validate as correct, but it should provide a good starting point for the user to make it correct.
         *                           If this parameter is null, there will be no "add" button in the UI (if the default UI is used).
         * @param elementValidator   A {@link Predicate} to verify if a list element is valid. Elements that are read from the config file are removed from the list if the
         *                           validator rejects them.
         * @return A {@link ConfigValue} object that can be used to access the config value and that will live-update if the value changed, i.e. because the config file
         *         was updated or the config UI was used.
         */
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(List<String> path, Supplier<List<? extends T>> defaultSupplier, Supplier<T> newElementSupplier, Predicate<Object> elementValidator) {
            return defineList(path, defaultSupplier, newElementSupplier, elementValidator, null);
        }

        /**
         * Build a new config value that holds a {@link List}.<p>
         * 
         * @param <T>                The class of element of the list. Directly supported are {@link String}, {@link Boolean}, {@link Integer}, {@link Long} and {@link Double}.
         *                           Other classes will be saved using their string representation and will be read back from the config file as strings.
         * @param path               The key for the config value in list form, i.e. pre-split into section and key.
         * @param defaultSupplier    A {@link Supplier} for the default value of the list. This will be used if the config file doesn't exist or if it reads as invalid.
         * @param newElementSupplier A {@link Supplier} for new elements to be added to the list. This is only used in the UI when the user presses the "add" button.
         *                           The supplied value doesn't have to validate as correct, but it should provide a good starting point for the user to make it correct.
         *                           If this parameter is null, there will be no "add" button in the UI (if the default UI is used).
         * @param elementValidator   A {@link Predicate} to verify if a list element is valid. Elements that are read from the config file are removed from the list if the
         *                           validator rejects them.
         * @param sizeRange          A {@link Range} defining the valid length of the list. Lists read from the config file that don't validate with this Range will be replaced
         *                           with the default. When <code>null</code>, the list size is unbounded.
         * @return A {@link ConfigValue} object that can be used to access the config value and that will live-update if the value changed, i.e. because the config file
         *         was updated or the config UI was used.
         */
        public <T> ConfigValue<List<? extends T>> defineList(List<String> path, Supplier<List<? extends T>> defaultSupplier, @Nullable Supplier<T> newElementSupplier, Predicate<Object> elementValidator, @Nullable Range<Integer> sizeRange) {
            context.setClazz(List.class);
            return define(path, new ListValueSpec(defaultSupplier, newElementSupplier, x -> x instanceof List && ((List<?>) x).stream().allMatch(elementValidator), elementValidator, context, path, sizeRange) {
                @Override
                public Object correct(Object value) {
                    if (!(value instanceof List) || (getSizeRange() != null && !getSizeRange().test(((List<?>) value).size()))) {
                        LOGGER.debug(Logging.CORE, "List on key {} is deemed to need correction, as it is null, not a list, or the wrong size.", path.getLast());
                        return getDefault();
                    }
                    List<?> list = Lists.newArrayList((List<?>) value);
                    list.removeIf(elementValidator.negate());
                    if (list.isEmpty()) {
                        LOGGER.debug(Logging.CORE, "List on key {} is deemed to need correction. It failed validation.", path.getLast());
                        return getDefault();
                    }
                    return list;
                }
            }, defaultSupplier);
        }

        //Enum
        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue) {
            return defineEnum(split(path), defaultValue);
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter) {
            return defineEnum(split(path), defaultValue, converter);
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue) {
            return defineEnum(path, defaultValue, defaultValue.getDeclaringClass().getEnumConstants());
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter) {
            return defineEnum(path, defaultValue, converter, defaultValue.getDeclaringClass().getEnumConstants());
        }

        @SuppressWarnings("unchecked")
        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, V... acceptableValues) {
            return defineEnum(split(path), defaultValue, acceptableValues);
        }

        @SuppressWarnings("unchecked")
        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter, V... acceptableValues) {
            return defineEnum(split(path), defaultValue, converter, acceptableValues);
        }

        @SuppressWarnings("unchecked")
        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, V... acceptableValues) {
            return defineEnum(path, defaultValue, (Collection<V>) Arrays.asList(acceptableValues));
        }

        @SuppressWarnings("unchecked")
        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter, V... acceptableValues) {
            return defineEnum(path, defaultValue, converter, Arrays.asList(acceptableValues));
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, Collection<V> acceptableValues) {
            return defineEnum(split(path), defaultValue, acceptableValues);
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter, Collection<V> acceptableValues) {
            return defineEnum(split(path), defaultValue, converter, acceptableValues);
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, Collection<V> acceptableValues) {
            return defineEnum(path, defaultValue, EnumGetMethod.NAME_IGNORECASE, acceptableValues);
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter, Collection<V> acceptableValues) {
            return defineEnum(path, defaultValue, converter, obj -> {
                if (obj instanceof Enum) {
                    return acceptableValues.contains(obj);
                }
                if (obj == null) {
                    return false;
                }
                try {
                    return acceptableValues.contains(converter.get(obj, defaultValue.getDeclaringClass()));
                } catch (IllegalArgumentException | ClassCastException e) {
                    return false;
                }
            });
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, Predicate<Object> validator) {
            return defineEnum(split(path), defaultValue, validator);
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter, Predicate<Object> validator) {
            return defineEnum(split(path), defaultValue, converter, validator);
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, Predicate<Object> validator) {
            return defineEnum(path, () -> defaultValue, validator, defaultValue.getDeclaringClass());
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter, Predicate<Object> validator) {
            return defineEnum(path, () -> defaultValue, converter, validator, defaultValue.getDeclaringClass());
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, Supplier<V> defaultSupplier, Predicate<Object> validator, Class<V> clazz) {
            return defineEnum(split(path), defaultSupplier, validator, clazz);
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, Supplier<V> defaultSupplier, EnumGetMethod converter, Predicate<Object> validator, Class<V> clazz) {
            return defineEnum(split(path), defaultSupplier, converter, validator, clazz);
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, Supplier<V> defaultSupplier, Predicate<Object> validator, Class<V> clazz) {
            return defineEnum(path, defaultSupplier, EnumGetMethod.NAME_IGNORECASE, validator, clazz);
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, Supplier<V> defaultSupplier, EnumGetMethod converter, Predicate<Object> validator, Class<V> clazz) {
            context.setClazz(clazz);
            V[] allowedValues = clazz.getEnumConstants();
            comment("Allowed Values: " + Arrays.stream(allowedValues).filter(validator).map(Enum::name).collect(Collectors.joining(", ")));
            return new EnumValue<V>(this, define(path, new ValueSpec(defaultSupplier, validator, context, path), defaultSupplier).getPath(), defaultSupplier, converter, clazz);
        }

        //boolean
        public BooleanValue define(String path, boolean defaultValue) {
            return define(split(path), defaultValue);
        }

        public BooleanValue define(List<String> path, boolean defaultValue) {
            return define(path, (Supplier<Boolean>) () -> defaultValue);
        }

        public BooleanValue define(String path, Supplier<Boolean> defaultSupplier) {
            return define(split(path), defaultSupplier);
        }

        public BooleanValue define(List<String> path, Supplier<Boolean> defaultSupplier) {
            return new BooleanValue(this, define(path, defaultSupplier, o -> {
                if (o instanceof String) return ((String) o).equalsIgnoreCase("true") || ((String) o).equalsIgnoreCase("false");
                return o instanceof Boolean;
            }, Boolean.class).getPath(), defaultSupplier);
        }

        //Double
        public DoubleValue defineInRange(String path, double defaultValue, double min, double max) {
            return defineInRange(split(path), defaultValue, min, max);
        }

        public DoubleValue defineInRange(List<String> path, double defaultValue, double min, double max) {
            return defineInRange(path, (Supplier<Double>) () -> defaultValue, min, max);
        }

        public DoubleValue defineInRange(String path, Supplier<Double> defaultSupplier, double min, double max) {
            return defineInRange(split(path), defaultSupplier, min, max);
        }

        public DoubleValue defineInRange(List<String> path, Supplier<Double> defaultSupplier, double min, double max) {
            return new DoubleValue(this, defineInRange(path, defaultSupplier, min, max, Double.class).getPath(), defaultSupplier);
        }

        //Ints
        public IntValue defineInRange(String path, int defaultValue, int min, int max) {
            return defineInRange(split(path), defaultValue, min, max);
        }

        public IntValue defineInRange(List<String> path, int defaultValue, int min, int max) {
            return defineInRange(path, (Supplier<Integer>) () -> defaultValue, min, max);
        }

        public IntValue defineInRange(String path, Supplier<Integer> defaultSupplier, int min, int max) {
            return defineInRange(split(path), defaultSupplier, min, max);
        }

        public IntValue defineInRange(List<String> path, Supplier<Integer> defaultSupplier, int min, int max) {
            return new IntValue(this, defineInRange(path, defaultSupplier, min, max, Integer.class).getPath(), defaultSupplier);
        }

        //Longs
        public LongValue defineInRange(String path, long defaultValue, long min, long max) {
            return defineInRange(split(path), defaultValue, min, max);
        }

        public LongValue defineInRange(List<String> path, long defaultValue, long min, long max) {
            return defineInRange(path, (Supplier<Long>) () -> defaultValue, min, max);
        }

        public LongValue defineInRange(String path, Supplier<Long> defaultSupplier, long min, long max) {
            return defineInRange(split(path), defaultSupplier, min, max);
        }

        public LongValue defineInRange(List<String> path, Supplier<Long> defaultSupplier, long min, long max) {
            return new LongValue(this, defineInRange(path, defaultSupplier, min, max, Long.class).getPath(), defaultSupplier);
        }

        public Builder comment(String comment) {
            context.addComment(comment);
            return this;
        }

        public Builder comment(String... comment) {
            // Iterate list first, to throw meaningful errors
            // Don't add any comments until we make sure there is no nulls
            for (int i = 0; i < comment.length; i++)
                Preconditions.checkNotNull(comment[i], "Comment string at " + i + " is null.");

            for (String s : comment)
                context.addComment(s);

            return this;
        }

        public Builder translation(String translationKey) {
            context.setTranslationKey(translationKey);
            return this;
        }

        /**
         * Config values marked as needing a world restart will not reset their {@linkplain ConfigValue#get() cached value} until they are unloaded
         * (i.e. when a world is closed).
         */
        public Builder worldRestart() {
            context.worldRestart();
            return this;
        }

        /**
         * Config values marked as needing a game restart will never reset their {@linkplain ConfigValue#get() cached value}.
         */
        public Builder gameRestart() {
            context.gameRestart();
            return this;
        }

        public Builder push(String path) {
            return push(split(path));
        }

        public Builder push(List<String> path) {
            currentPath.addAll(path);
            if (context.hasComment()) {
                levelComments.put(new ArrayList<>(currentPath), context.buildComment(path));
                context.clearComment(); // Set to empty
            }
            if (context.getTranslationKey() != null) {
                levelTranslationKeys.put(new ArrayList<String>(currentPath), context.getTranslationKey());
                context.setTranslationKey(null);
            }
            context.ensureEmpty();
            return this;
        }

        public Builder pop() {
            return pop(1);
        }

        public Builder pop(int count) {
            if (count > currentPath.size())
                throw new IllegalArgumentException("Attempted to pop " + count + " elements when we only had: " + currentPath);
            for (int x = 0; x < count; x++)
                currentPath.remove(currentPath.size() - 1);
            return this;
        }

        public <T> Pair<T, ModConfigSpec> configure(Function<Builder, T> consumer) {
            T o = consumer.apply(this);
            return Pair.of(o, this.build());
        }

        public ModConfigSpec build() {
            context.ensureEmpty();
            Config valueCfg = Config.of(Config.getDefaultMapCreator(true, true), InMemoryFormat.withSupport(ConfigValue.class::isAssignableFrom));
            values.forEach(v -> valueCfg.set(v.getPath(), v));

            ModConfigSpec ret = new ModConfigSpec(spec.unmodifiable(), valueCfg.unmodifiable(), Collections.unmodifiableMap(levelComments), Collections.unmodifiableMap(levelTranslationKeys));
            values.forEach(v -> v.spec = ret);
            return ret;
        }
    }

    private static class BuilderContext {
        private final List<String> comment = new LinkedList<>();
        @Nullable
        private String langKey;
        @Nullable
        private Range<?> range;
        private RestartType restartType = RestartType.NONE;
        @Nullable
        private Class<?> clazz;

        public void addComment(String value) {
            // Don't use `validate` because it throws IllegalStateException, not NullPointerException
            Preconditions.checkNotNull(value, "Passed in null value for comment");

            comment.add(value);
        }

        public void clearComment() {
            comment.clear();
        }

        public boolean hasComment() {
            return this.comment.size() > 0;
        }

        public String buildComment() {
            return buildComment(List.of("unknown", "unknown"));
        }

        public String buildComment(final List<String> path) {
            if (comment.stream().allMatch(String::isBlank)) {
                if (FMLEnvironment.isProduction())
                    LOGGER.warn(Logging.CORE, "Detected a comment that is all whitespace for config option {}, which causes obscure bugs in NeoForge's config system and will cause a crash in the future. Please report this to the mod author.",
                            DOT_JOINER.join(path));
                else
                    throw new IllegalStateException("Can not build comment for config option " + DOT_JOINER.join(path) + " as it comprises entirely of blank lines/whitespace. This is not allowed as it causes a \"constantly correcting config\" bug with NightConfig in NeoForge's config system.");

                return "A developer of this mod has defined this config option with a blank comment, which causes obscure bugs in NeoForge's config system and will cause a crash in the future. Please report this to the mod author.";
            }

            return LINE_JOINER.join(comment);
        }

        public void setTranslationKey(@Nullable String value) {
            this.langKey = value;
        }

        @Nullable
        public String getTranslationKey() {
            return this.langKey;
        }

        public <V extends Comparable<? super V>> void setRange(Range<V> value) {
            this.range = value;
            this.setClazz(value.getClazz());
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public <V extends Comparable<? super V>> Range<V> getRange() {
            return (Range<V>) this.range;
        }

        public void worldRestart() {
            this.restartType = RestartType.WORLD;
        }

        public void gameRestart() {
            this.restartType = RestartType.GAME;
        }

        public RestartType restartType() {
            return restartType;
        }

        public void setClazz(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Nullable
        public Class<?> getClazz() {
            return this.clazz;
        }

        public void ensureEmpty() {
            validate(hasComment(), "Non-empty comment when empty expected");
            validate(langKey, "Non-null translation key when null expected");
            validate(range, "Non-null range when null expected");
            validate(restartType != RestartType.NONE, "Dangling restart value set to " + restartType);
        }

        private void validate(@Nullable Object value, String message) {
            if (value != null)
                throw new IllegalStateException(message);
        }

        private void validate(boolean value, String message) {
            if (value)
                throw new IllegalStateException(message);
        }
    }

    @SuppressWarnings("unused")
    public static class Range<V extends Comparable<? super V>> implements Predicate<Object> {
        private final Class<? extends V> clazz;
        private final V min;
        private final V max;

        private Range(Class<V> clazz, V min, V max) {
            this.clazz = clazz;
            this.min = min;
            this.max = max;
            if (min.compareTo(max) > 0) {
                throw new IllegalArgumentException("Range min must be less then max.");
            }
        }

        public static Range<Integer> of(int min, int max) {
            return new Range<>(Integer.class, min, max);
        }

        public Class<? extends V> getClazz() {
            return clazz;
        }

        public V getMin() {
            return min;
        }

        public V getMax() {
            return max;
        }

        private boolean isNumber(@Nullable Object other) {
            return Number.class.isAssignableFrom(clazz) && other instanceof Number;
        }

        @Override
        public boolean test(Object t) {
            if (isNumber(t)) {
                Number n = (Number) t;
                boolean result = ((Number) min).doubleValue() <= n.doubleValue() && n.doubleValue() <= ((Number) max).doubleValue();
                if (!result) {
                    LOGGER.debug(Logging.CORE, "Range value {} is not within its bounds {}-{}", n.doubleValue(), ((Number) min).doubleValue(), ((Number) max).doubleValue());
                }
                return result;
            }
            if (!clazz.isInstance(t)) return false;
            V c = clazz.cast(t);

            boolean result = c.compareTo(min) >= 0 && c.compareTo(max) <= 0;
            if (!result) {
                LOGGER.debug(Logging.CORE, "Range value {} is not within its bounds {}-{}", c, min, max);
            }
            return result;
        }

        public Object correct(@Nullable Object value, Object def) {
            if (isNumber(value)) {
                Number n = (Number) value;
                return n.doubleValue() < ((Number) min).doubleValue() ? min : n.doubleValue() > ((Number) max).doubleValue() ? max : value;
            }
            if (!clazz.isInstance(value)) return def;
            V c = clazz.cast(value);
            return c.compareTo(min) < 0 ? min : c.compareTo(max) > 0 ? max : value;
        }

        @Override
        public String toString() {
            if (clazz == Integer.class) {
                if (max.equals(Integer.MAX_VALUE)) {
                    return "> " + min;
                } else if (min.equals(Integer.MIN_VALUE)) {
                    return "< " + max;
                }
            } // TODO add more special cases?
            return min + " ~ " + max;
        }
    }

    public static class ValueSpec {
        @Nullable
        private final String comment;
        @Nullable
        private final String langKey;
        @Nullable
        private final Range<?> range;
        @Nullable
        private final Class<?> clazz;
        private final Supplier<?> supplier;
        private final Predicate<Object> validator;
        private final RestartType restartType;

        private ValueSpec(Supplier<?> supplier, Predicate<Object> validator, BuilderContext context, List<String> path) {
            Objects.requireNonNull(supplier, "Default supplier can not be null");
            Objects.requireNonNull(validator, "Validator can not be null");

            this.comment = context.hasComment() ? context.buildComment(path) : null;
            this.langKey = context.getTranslationKey();
            this.range = context.getRange();
            this.restartType = context.restartType();
            this.clazz = context.getClazz();
            this.supplier = supplier;
            this.validator = validator;
        }

        @Nullable
        public String getComment() {
            return comment;
        }

        @Nullable
        public String getTranslationKey() {
            return langKey;
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public <V extends Comparable<? super V>> Range<V> getRange() {
            return (Range<V>) this.range;
        }

        public RestartType restartType() {
            return restartType;
        }

        @Nullable
        public Class<?> getClazz() {
            return this.clazz;
        }

        public boolean test(@Nullable Object value) {
            return validator.test(value);
        }

        public Object correct(@Nullable Object value) {
            return range == null ? getDefault() : range.correct(value, getDefault());
        }

        public Object getDefault() {
            return supplier.get();
        }
    }

    public static class ListValueSpec extends ValueSpec {
        private static final Range<Integer> MAX_ELEMENTS = Range.of(0, Integer.MAX_VALUE);
        private static final Range<Integer> NON_EMPTY = Range.of(1, Integer.MAX_VALUE);

        @Nullable
        private final Supplier<?> newElementSupplier;
        @Nullable
        private final Range<Integer> sizeRange;
        private final Predicate<Object> elementValidator;

        private ListValueSpec(Supplier<?> supplier, @Nullable Supplier<?> newElementSupplier, Predicate<Object> listValidator, Predicate<Object> elementValidator, BuilderContext context, List<String> path, @Nullable Range<Integer> sizeRange) {
            super(supplier, listValidator, context, path);
            Objects.requireNonNull(elementValidator, "ElementValidator can not be null");

            this.newElementSupplier = newElementSupplier;
            this.elementValidator = elementValidator;
            this.sizeRange = Objects.requireNonNullElse(sizeRange, MAX_ELEMENTS);
        }

        /**
         * Creates a new empty element that can be added to the end of the list or null if the list doesn't support adding elements.<p>
         * 
         * The element does not need to validate with either {@link #test(Object)} or {@link #testElement(Object)}, but it should give the user a good starting point for their edit.<p>
         * 
         * Only used by the UI!
         */
        @Nullable
        public Supplier<?> getNewElementSupplier() {
            return newElementSupplier;
        }

        /**
         * Determines if a given object can be part of the list.<p>
         * 
         * Note that the list-level validator overrules this.<p>
         * 
         * Only used by the UI!
         */
        public boolean testElement(Object value) {
            return elementValidator.test(value);
        }

        /**
         * The allowable range of the size of the list.
         * <p>
         * Note that the validator overrules this.
         * <p>
         * Only used by the UI!
         */
        public Range<Integer> getSizeRange() {
            return sizeRange;
        }
    }

    public static class ConfigValue<T> implements Supplier<T> {
        private final Builder parent;
        private final List<String> path;
        private final Supplier<T> defaultSupplier;

        @Nullable
        private T cachedValue = null;

        @Nullable
        private ModConfigSpec spec;

        ConfigValue(Builder parent, List<String> path, Supplier<T> defaultSupplier) {
            this.parent = parent;
            this.path = path;
            this.defaultSupplier = defaultSupplier;
            this.parent.values.add(this);
        }

        public List<String> getPath() {
            return Lists.newArrayList(path);
        }

        /**
         * Returns the configured value for the configuration setting, throwing if the config has not yet been loaded.
         * <p>
         * This getter is cached, and will respect the {@link Builder#worldRestart() world restart} and {@link Builder#gameRestart() game restart}
         * options by not clearing its cache if one of those options are set.
         *
         * @return the configured value for the setting
         * @throws NullPointerException  if the {@link ModConfigSpec config spec} object that will contain this has
         *                               not yet been built
         * @throws IllegalStateException if the associated config has not yet been loaded
         */
        @Override
        public T get() {
            if (cachedValue == null) {
                cachedValue = getRaw();
            }
            return cachedValue;
        }

        /**
         * Returns the uncached value for the configuration setting, throwing if the config has not yet been loaded.
         * <p>
         * <em>Do not call this for any other purpose than editing the value. Use {@link #get()} instead.</em>
         */
        public T getRaw() {
            Preconditions.checkNotNull(spec, "Cannot get config value before spec is built");
            var loadedConfig = spec.loadedConfig;
            Preconditions.checkState(loadedConfig != null, "Cannot get config value before config is loaded.");
            return getRaw(loadedConfig.config(), path, defaultSupplier);
        }

        public T getRaw(Config config, List<String> path, Supplier<T> defaultSupplier) {
            return config.getOrElse(path, defaultSupplier);
        }

        /**
         * {@return the default value for the configuration setting}
         */
        public T getDefault() {
            return defaultSupplier.get();
        }

        public Builder next() {
            return parent;
        }

        public void save() {
            Preconditions.checkNotNull(spec, "Cannot save config value before spec is built");
            Preconditions.checkNotNull(spec.loadedConfig, "Cannot save config value without assigned Config object present");
            spec.save();
        }

        /**
         * Directly sets the value, without firing events or writing the config to disk.
         * Make sure to call {@link ModConfigSpec#save()} eventually.
         */
        public void set(T value) {
            Preconditions.checkNotNull(spec, "Cannot set config value before spec is built");
            var loadedConfig = spec.loadedConfig;
            Preconditions.checkNotNull(loadedConfig, "Cannot set config value without assigned Config object present");
            loadedConfig.config().set(path, value);

            if (getSpec().restartType == RestartType.NONE) {
                this.cachedValue = value;
            }
        }

        public ValueSpec getSpec() {
            return parent.spec.get(path);
        }

        public void clearCache() {
            this.cachedValue = null;
        }
    }

    public static class BooleanValue extends ConfigValue<Boolean> implements BooleanSupplier {
        BooleanValue(Builder parent, List<String> path, Supplier<Boolean> defaultSupplier) {
            super(parent, path, defaultSupplier);
        }

        @Override
        public boolean getAsBoolean() {
            return get();
        }

        public boolean isTrue() {
            return getAsBoolean();
        }

        public boolean isFalse() {
            return !getAsBoolean();
        }
    }

    public static class IntValue extends ConfigValue<Integer> implements IntSupplier {
        IntValue(Builder parent, List<String> path, Supplier<Integer> defaultSupplier) {
            super(parent, path, defaultSupplier);
        }

        @Override
        public Integer getRaw(Config config, List<String> path, Supplier<Integer> defaultSupplier) {
            return config.getIntOrElse(path, () -> defaultSupplier.get());
        }

        @Override
        public int getAsInt() {
            return get();
        }
    }

    public static class LongValue extends ConfigValue<Long> implements LongSupplier {
        LongValue(Builder parent, List<String> path, Supplier<Long> defaultSupplier) {
            super(parent, path, defaultSupplier);
        }

        @Override
        public Long getRaw(Config config, List<String> path, Supplier<Long> defaultSupplier) {
            return config.getLongOrElse(path, () -> defaultSupplier.get());
        }

        @Override
        public long getAsLong() {
            return get();
        }
    }

    public static class DoubleValue extends ConfigValue<Double> implements DoubleSupplier {
        DoubleValue(Builder parent, List<String> path, Supplier<Double> defaultSupplier) {
            super(parent, path, defaultSupplier);
        }

        @Override
        public Double getRaw(Config config, List<String> path, Supplier<Double> defaultSupplier) {
            Number n = config.<Number>get(path);
            return n == null ? defaultSupplier.get() : n.doubleValue();
        }

        @Override
        public double getAsDouble() {
            return get();
        }
    }

    public static class EnumValue<T extends Enum<T>> extends ConfigValue<T> {
        private final EnumGetMethod converter;
        private final Class<T> clazz;

        EnumValue(Builder parent, List<String> path, Supplier<T> defaultSupplier, EnumGetMethod converter, Class<T> clazz) {
            super(parent, path, defaultSupplier);
            this.converter = converter;
            this.clazz = clazz;
        }

        @Override
        public T getRaw(Config config, List<String> path, Supplier<T> defaultSupplier) {
            return config.getEnumOrElse(path, clazz, converter, defaultSupplier);
        }
    }

    private static final Joiner LINE_JOINER = Joiner.on("\n");
    private static final Joiner DOT_JOINER = Joiner.on(".");
    private static final Splitter DOT_SPLITTER = Splitter.on(".");

    private static List<String> split(String path) {
        return Lists.newArrayList(DOT_SPLITTER.split(path));
    }

    /**
     * Used to prevent cached config values from being updated unless the game or the world is restarted.
     */
    public enum RestartType {
        /**
         * Do not require a restart to update the cached config value.
         */
        NONE,
        /**
         * Require a world restart.
         */
        WORLD,
        /**
         * Require a game restart.
         * <p>
         * Cannot be used for {@linkplain ModConfig.Type#SERVER server configs}.
         */
        GAME(ModConfig.Type.SERVER);

        private final Set<ModConfig.Type> invalidTypes;

        RestartType(ModConfig.Type... invalidTypes) {
            this.invalidTypes = EnumSet.noneOf(ModConfig.Type.class);
            this.invalidTypes.addAll(Arrays.asList(invalidTypes));
        }

        private boolean isValid(ModConfig.Type type) {
            return !invalidTypes.contains(type);
        }

        public RestartType with(RestartType other) {
            return other == NONE ? this : (other == GAME || this == GAME) ? GAME : WORLD;
        }
    }
}
