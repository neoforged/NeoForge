/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.extensions.IMinecraftServerExtension;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.ApiStatus;

public final class FlagManager {
    private final Object2BooleanMap<Flag> flags = new Object2BooleanOpenHashMap<>();
    private final Collection<Flag> flagsView = Collections.unmodifiableCollection(flags.keySet());

    private final ChangeListener listener;

    @ApiStatus.Internal
    public FlagManager(ChangeListener listener) {
        this.listener = listener;

        flags.defaultReturnValue(false);
    }

    public void loadFromLevel(Level level) {
        level.getExistingData(NeoForgeMod.LEVEL_FLAGS).ifPresent(enabledFlags -> {
            reset();
            enabledFlags.forEach(flag -> flags.put(flag, true));
        });
    }

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

    public void reset() {
        flags.clear();
    }

    public boolean isEnabled(Flag flag) {
        return flags.getBoolean(flag);
    }

    public boolean isEnabled(Flag flag, Flag... flags) {
        if (!isEnabled(flag))
            return false;

        for (var other : flags) {
            if (!isEnabled(other))
                return false;
        }

        return true;
    }

    public boolean isEnabled(Iterable<Flag> flags) {
        for (var flag : flags) {
            if (!isEnabled(flag))
                return false;
        }

        return true;
    }

    public boolean isEnabled(FlagElement element) {
        return isEnabled(element.requiredFlags());
    }

    public boolean set(Flag flag, boolean enabled) {
        if (flags.getBoolean(flag) != enabled) {
            flags.put(flag, enabled);
            listener.accept(flag, enabled);
            return true;
        }

        return false;
    }

    public boolean toggle(Flag flag) {
        return set(flag, !flags.getBoolean(flag));
    }

    public Collection<Flag> getEnabledFlags() {
        return enabledFlags().toList();
    }

    public Stream<Flag> enabledFlags() {
        return flagsView.stream().filter(this::isEnabled);
    }

    public static Optional<FlagManager> lookup() {
        if (FMLEnvironment.dist.isClient())
            return Optional.of(Minecraft.getInstance().getModdedFlagManager());

        return Optional.ofNullable(ServerLifecycleHooks.getCurrentServer()).map(IMinecraftServerExtension::getModdedFlagManager);
    }

    @FunctionalInterface
    public interface ChangeListener {
        void accept(Flag flag, boolean enabled);
    }
}
