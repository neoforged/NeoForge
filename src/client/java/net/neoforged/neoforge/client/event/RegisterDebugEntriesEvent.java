/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.client.gui.components.debug.DebugEntrySystemSpecs;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Event fired when debug entries are registered.
 * <p>
 * This event is fired during the {@link DebugScreenEntries} initialization to allow registration of custom entries.
 * <p>
 * Existing entries cannot be modified or amended directly. However new lines can be appended to existing groups,
 * such as the {@link DebugEntrySystemSpecs#GROUP "System Specs"}, by using {@link DebugScreenDisplayer#addToGroup(Identifier, java.lang.String)}.
 * <p>
 * This event is fired on the mod event bus.
 */
public final class RegisterDebugEntriesEvent extends Event implements IModBusEvent {
    private final Map<Identifier, DebugScreenEntry> entries;
    private final Map<Identifier, DebugScreenEntryStatus> defaultProfile;
    private final Map<Identifier, DebugScreenEntryStatus> performanceProfile;

    @ApiStatus.Internal
    public RegisterDebugEntriesEvent(Map<Identifier, DebugScreenEntry> entries, Map<Identifier, DebugScreenEntryStatus> defaultProfile, Map<Identifier, DebugScreenEntryStatus> performanceProfile) {
        this.entries = entries;
        // These are immutable in vanilla, we make them mutable to allow custom entry inclusion
        this.defaultProfile = new HashMap<>(defaultProfile);
        this.performanceProfile = new HashMap<>(performanceProfile);
    }

    /**
     * Registers a new {@link DebugScreenEntry} to allow it to be used for debug rendering and state toggling.
     *
     * @param id    Registration ID for this entry.
     * @param entry Screen entry to be registered.
     */
    public void register(Identifier id, DebugScreenEntry entry) {
        if (entries.putIfAbsent(id, entry) != null)
            throw new IllegalStateException("Duplicate DebugScreenEntry registration: " + id);
    }

    /**
     * {@return true if the given entry id has been registered}
     */
    public boolean isRegistered(Identifier id) {
        return entries.containsKey(id);
    }

    /**
     * Includes the entry into the given profile.
     *
     * @param id            Registration ID for this entry.
     * @param profile       Debug profile this entry will be included with.
     * @param profileStatus Status this entry will be set to when the profile is enabled.
     */
    public void includeInProfile(Identifier id, DebugScreenProfile profile, DebugScreenEntryStatus profileStatus) {
        if (getProfileMap(profile).putIfAbsent(id, profileStatus) != null)
            throw new IllegalStateException("Duplicate DebugScreenEntry " + profile.getSerializedName() + "-profile inclusion: " + id);
    }

    /**
     * {@return true if the entry is included into the given profile}
     */
    public boolean isIncludedInProfile(Identifier id, DebugScreenProfile profile) {
        return getProfileMap(profile).containsKey(id);
    }

    private Map<Identifier, DebugScreenEntryStatus> getProfileMap(DebugScreenProfile profile) {
        return switch (profile) {
            case DEFAULT -> defaultProfile;
            case PERFORMANCE -> performanceProfile;
        };
    }

    @ApiStatus.Internal
    public Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> validateProfiles() {
        // we delegate validation to its own method to allow people to call 'includeInProfile' before 'register'
        var defaultError = validateProfile(DebugScreenProfile.DEFAULT);
        var performanceError = validateProfile(DebugScreenProfile.PERFORMANCE);

        // we delay throwing the error to allow both profiles to be validated in the same run
        // we should still validate 'performance' if 'default' is invalid
        if (defaultError != null && performanceError != null) {
            var error = new IllegalStateException("Fatal errors occurred while validating DebugScreenProfiles");
            error.addSuppressed(defaultError);
            error.addSuppressed(performanceError);
            throw error;
        } else if (defaultError != null) {
            throw defaultError;
        } else if (performanceError != null) {
            throw performanceError;
        }

        return Map.of(
                // use 'Map.copyOf' to make our now mutable maps immutable again
                DebugScreenProfile.DEFAULT, Map.copyOf(defaultProfile),
                DebugScreenProfile.PERFORMANCE, Map.copyOf(performanceProfile));
    }

    @Nullable
    private IllegalStateException validateProfile(DebugScreenProfile profile) {
        var profileMap = getProfileMap(profile);
        var invalidIds = Sets.difference(profileMap.keySet(), entries.keySet());

        if (!invalidIds.isEmpty()) {
            var logger = LogUtils.getLogger();

            logger.error("Found {} unregistered debug entries in profile: {}", invalidIds.size(), profile.getSerializedName());
            logger.error("Unregistered debug entries: {}", invalidIds.stream().map(Identifier::toString).collect(Collectors.joining(",", "[", "]")));

            // throw in dev to ensure people are correctly registering their entries when including them in profiles
            if (!FMLEnvironment.isProduction())
                return new IllegalStateException("Fatal error occurred while validating DebugScreenProfile: " + profile.getSerializedName());
        }

        return null;
    }
}
