/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import io.netty.util.internal.UnstableApi;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.extensions.ILevelReaderExtension;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.ApiStatus;

/**
 * The main manager for all {@link Flag} related tasks.
 * <p>
 * An instance of this can be looked up via {@link #loadFromLevel(Level)} but it is preferred to use the provided extension {@link ILevelReaderExtension#getModdedFlagManager()}
 * <p>
 * This class is mostly used for internal usages but modders are welcome to make use of {@link #isEnabled(Flag)} and its variants where needed.
 */
public final class FlagManager {
    private final Reference2BooleanMap<Flag> flags = new Reference2BooleanOpenHashMap<>();
    private final Collection<Flag> flagsView = Collections.unmodifiableCollection(flags.keySet());

    private final ChangeListener listener;

    @ApiStatus.Internal
    public FlagManager(ChangeListener listener) {
        this.listener = listener;

        flags.defaultReturnValue(false);
    }

    /**
     * Returns {@code true} if the given {@link Flag} is enabled, {@code false} otherwise.
     *
     * @param flag The {@link Flag} to be tested.
     * @return {@code true} if the given {@link Flag} is enabled, {@code false} otherwise.
     */
    public boolean isEnabled(Flag flag) {
        return flags.getBoolean(flag);
    }

    /**
     * Returns {@code true} if all given {@link Flag Flags} are enabled, {@code false} otherwise.
     *
     * @param flag  A {@link Flag} to be tested.
     * @param flags Additional {@link Flag Flags} to also be tested.
     * @return {@code true} if all given {@link Flag Flags} are enabled, {@code false} otherwise.
     */
    public boolean isEnabled(Flag flag, Flag... flags) {
        if (!isEnabled(flag))
            return false;

        for (var other : flags) {
            if (!isEnabled(other))
                return false;
        }

        return true;
    }

    /**
     * Returns {@code true} if all given {@link Flag Flags} are enabled, {@code false} otherwise.
     *
     * @param flags {@link Flag Flags} to be tested.
     * @return {@code true} if all given {@link Flag Flags} are enabled, {@code false} otherwise.
     */
    public boolean isEnabled(Iterable<Flag> flags) {
        for (var flag : flags) {
            if (!isEnabled(flag))
                return false;
        }

        return true;
    }

    /**
     * Returns {@code true} if the given {@link FlagElement} should be enabled, {@code false} otherwise.
     *
     * @param element {@link FlagElement Element} to be tested.
     * @return {@code true} if the given {@link FlagElement} should be enabled, {@code false} otherwise.
     * @apiNote Users should prefer to use {@link FlagElement#isEnabled()} or {@link FeatureElement#isEnabled(FeatureFlagSet)} where possible. Preferring {@link FeatureElement#isEnabled(FeatureFlagSet)} over {@link FlagElement#isEnabled()}.
     */
    public boolean isEnabled(FlagElement element) {
        return isEnabled(element.requiredFlags());
    }

    /**
     * Sets the state of the given {@link Flag}.
     *
     * @param flag    The {@link Flag} which to have its state changed.
     * @param enabled The new state for the given {@link Flag}.
     * @return {@code true} if the state of the {@link Flag} changed, {@code false} otherwise.
     */
    public boolean set(Flag flag, boolean enabled) {
        if (flags.getBoolean(flag) != enabled) {
            flags.put(flag, enabled);
            listener.accept(flag, enabled);
            return true;
        }

        return false;
    }

    /**
     * Attempts to toggle the state of the given {@link Flag}.
     *
     * @param flag {@link Flag} which to have its state toggled.
     * @return {@code true} if the state of the {@link Flag} changed, {@code false} otherwise.
     */
    public boolean toggle(Flag flag) {
        return set(flag, !flags.getBoolean(flag));
    }

    /**
     * Returns an immutable collection containing all currently enabled {@link Flag Flags}.
     *
     * @return Immutable collection containing all currently enabled {@link Flag Flags}.
     */
    public Collection<Flag> getEnabledFlags() {
        return enabledFlags().toList();
    }

    /**
     * Returns {@link Stream} representing all currently enabled {@link Flag Flags}.
     *
     * @return {@link Stream} representing all currently enabled {@link Flag Flags}.
     */
    public Stream<Flag> enabledFlags() {
        return flagsView.stream().filter(this::isEnabled);
    }

    /**
     * Loads all relevant data from the given level via the {@link NeoForgeMod#LEVEL_FLAGS flags} data attachment.
     *
     * @param level The level in which to load data from.
     */
    @ApiStatus.Internal
    public void loadFromLevel(Level level) {
        level.getExistingData(NeoForgeMod.LEVEL_FLAGS).ifPresent(enabledFlags -> {
            reset();
            enabledFlags.forEach(flag -> flags.put(flag, true));
        });
    }

    /**
     * Saves all relevant data to the given level via the {@link NeoForgeMod#LEVEL_FLAGS flags} data attachment.
     *
     * @param level The level in which to save data to.
     */
    @ApiStatus.Internal
    public void saveToLevel(Level level) {
        level.setData(NeoForgeMod.LEVEL_FLAGS, enabledFlags().collect(Collectors.toSet()));
    }

    void syncFromRemote(ClientboundSyncFlag payload) {
        flags.put(payload.flag, payload.enabled);
    }

    void syncFromRemote(ClientboundSyncFlags payload) {
        reset();
        payload.enabledFlags.forEach(flag -> flags.put(flag, true));
    }

    /**
     * Clears all loaded flag states for this manager.
     *
     * @apiNote This will only clear for the current {@link Dist phsyical side}.
     */
    @ApiStatus.Internal
    public void reset() {
        flags.clear();
    }

    /**
     * Prefer to lookup instance via {@linkplain ILevelReaderExtension#getModdedFlagManager()}
     */
    @UnstableApi
    public static Optional<FlagManager> lookup() {
        return lookupClient().or(() -> {
            var server = ServerLifecycleHooks.getCurrentServer();
            return server == null ? Optional.empty() : Optional.of(server.getModdedFlagManager());
        });
    }

    private static Optional<FlagManager> lookupClient() {
        if (DatagenModLoader.isRunningDataGen())
            return Optional.empty();
        if (FMLEnvironment.dist.isClient())
            return Optional.ofNullable(Minecraft.getInstance()).map(Minecraft::getModdedFlagManager);
        return Optional.empty();
    }

    /**
     * Returns {@code true} if {@link Flag flags} should be enabled by default, {@code false} otherwise.
     * <p>
     * This is mostly used to ensure flags are enabled during data generation.
     * Flags should be assumed to be disabled by default outside of data generation.
     *
     * @return {@code true} if {@link Flag flags} should be enabled by default, {@code false} otherwise.
     */
    public static boolean shouldBeEnabledDefault() {
        return DatagenModLoader.isRunningDataGen();
    }

    @FunctionalInterface
    public interface ChangeListener {
        void accept(Flag flag, boolean enabled);
    }
}
