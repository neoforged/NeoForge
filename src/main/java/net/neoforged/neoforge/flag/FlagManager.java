/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;
import net.neoforged.neoforge.common.extensions.ILevelReaderExtension;
import org.jetbrains.annotations.ApiStatus;

/**
 * The main manager for all {@link Flag} related tasks.
 * <p>
 * An instance of this can be looked up via {@link ILevelReaderExtension#getModdedFlagManager()}.
 */
public sealed interface FlagManager permits DummyFlagManager, NullFlagManager, ServerFlagManager {
    /**
     * Null instance to be used when no other can be found.
     */
    FlagManager NULL = new NullFlagManager();

    /**
     * Returns {@code true} if the given {@link Flag} is enabled, {@code false} otherwise.
     *
     * @param flag The {@link Flag} to be tested.
     * @return {@code true} if the given {@link Flag} is enabled, {@code false} otherwise.
     */
    default boolean isEnabled(Flag flag) {
        return getEnabledFlags().contains(flag);
    }

    /**
     * Returns {@code true} if all given {@link Flag Flags} are enabled, {@code false} otherwise.
     *
     * @param flag  A {@link Flag} to be tested.
     * @param flags Additional {@link Flag Flags} to also be tested.
     * @return {@code true} if all given {@link Flag Flags} are enabled, {@code false} otherwise.
     */
    default boolean isEnabled(Flag flag, Flag... flags) {
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
    default boolean isEnabled(Collection<Flag> flags) {
        for (var flag : flags) {
            if (!isEnabled(flag))
                return false;
        }

        return true;
    }

    /**
     * Sets the state of the given {@link Flag}.
     *
     * @param flag  The {@link Flag} which to have its state changed.
     * @param state The new state for the given {@link Flag}.
     * @return {@code true} if the state of the {@link Flag} changed, {@code false} otherwise.
     */
    boolean set(Flag flag, boolean state);

    /**
     * Toggles the state of the given {@link Flag}.
     *
     * @param flag {@link Flag} which to have its state toggled.
     */
    default void toggle(Flag flag) {
        set(flag, !isEnabled(flag));
    }

    /**
     * Returns an immutable collection containing all currently enabled {@link Flag Flags}.
     *
     * @return Immutable collection containing all currently enabled {@link Flag Flags}.
     */
    Set<Flag> getEnabledFlags();

    /**
     * Returns {@link Stream} representing all currently enabled {@link Flag Flags}.
     *
     * @return {@link Stream} representing all currently enabled {@link Flag Flags}.
     */
    default Stream<Flag> enabledFlags() {
        return getEnabledFlags().stream();
    }

    /**
     * Creates a dummy {@link FlagManager} for the provided {@link Set}.
     *
     * @param enabledFlags {@link Set} of enabled {@link Flag flags} to be wrapped.
     * @return Dummy {@link FlagManager} wrapping the provided {@link Set}.
     */
    @ApiStatus.Internal
    static FlagManager createDummy(Set<Flag> enabledFlags) {
        return new DummyFlagManager(enabledFlags);
    }
}
