/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.google.common.collect.Sets;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.client.gui.components.debug.DebugEntrySystemSpecs;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Event fired when debug entries are registered.
 * <p>
 * This event is fired during the {@link DebugScreenEntries} initialization to allow registration of custom entries.
 * <p>
 * Existing entries cannot be modified or amended directly. However new lines can be appended to existing groups,
 * such as the {@link DebugEntrySystemSpecs#GROUP "System Specs"}, by using {@link DebugScreenDisplayer#addToGroup(ResourceLocation, java.lang.String)}.
 * <p>
 * This event is fired on the mod event bus.
 */
public final class RegisterDebugEntriesEvent extends Event implements IModBusEvent {
    private final Map<ResourceLocation, DebugScreenEntry> entries;
    private final Map<ResourceLocation, DebugScreenEntryStatus> defaultProfile;
    private final Map<ResourceLocation, DebugScreenEntryStatus> performanceProfile;

    @ApiStatus.Internal
    public RegisterDebugEntriesEvent(Map<ResourceLocation, DebugScreenEntry> entries, Map<ResourceLocation, DebugScreenEntryStatus> defaultProfile, Map<ResourceLocation, DebugScreenEntryStatus> performanceProfile) {
        this.entries = entries;
        this.defaultProfile = defaultProfile;
        this.performanceProfile = performanceProfile;
    }

    /**
     * Registers a new {@link DebugScreenEntry} to allow it to be used for debug rendering and state toggling.
     *
     * @param id    Registration ID for this entry.
     * @param entry Screen entry to be registered.
     */
    public void register(ResourceLocation id, DebugScreenEntry entry) {
        if (entries.putIfAbsent(id, entry) != null)
            throw new IllegalStateException("Duplicate DebugScreenEntry registration: " + id);
    }

    /**
     * {@return true if the given entry id has been registered}
     */
    public boolean isRegistered(ResourceLocation id) {
        return entries.containsKey(id);
    }

    /**
     * Includes the entry into the given profile.
     *
     * @param id            Registration ID for this entry.
     * @param profile       Debug profile this entry will be included with.
     * @param profileStatus Status this entry will be set to when the profile is enabled.
     */
    public void includeInProfile(ResourceLocation id, DebugScreenProfile profile, DebugScreenEntryStatus profileStatus) {
        if (getProfileMap(profile).putIfAbsent(id, profileStatus) != null)
            throw new IllegalStateException("Duplicate DebugScreenEntry " + profile.getSerializedName() + "-profile inclusion: " + id);
    }

    /**
     * {@return true if the entry is included into the given profile}
     */
    public boolean isIncludedInProfile(ResourceLocation id, DebugScreenProfile profile) {
        return getProfileMap(profile).containsKey(id);
    }

    private Map<ResourceLocation, DebugScreenEntryStatus> getProfileMap(DebugScreenProfile profile) {
        return switch (profile) {
            case DEFAULT -> defaultProfile;
            case PERFORMANCE -> performanceProfile;
        };
    }

    @ApiStatus.Internal
    public static void validateProfiles(RegisterDebugEntriesEvent event) {
        // we delegate validation to its own method to allow people to call 'includeInProfile' before 'register'
        var defaultError = validateProfile(event, DebugScreenProfile.DEFAULT);
        var performanceError = validateProfile(event, DebugScreenProfile.PERFORMANCE);

        // we delay throwing the error to allow both profiles to be validated in the same run
        // we should still validate 'performance' if 'default' is invalid
        if (defaultError != null && performanceError != null) {
            var error = new IllegalStateException("Fatal errors occurred while validating DebugScreenProfiles");
            error.addSuppressed(defaultError);
            error.addSuppressed(performanceError);
            throw error;
        } else if (defaultError != null)
            throw defaultError;
        else if (performanceError != null)
            throw performanceError;
    }

    @Nullable
    private static IllegalStateException validateProfile(RegisterDebugEntriesEvent event, DebugScreenProfile profile) {
        var profileMap = event.getProfileMap(profile);
        var invalidIds = Sets.difference(profileMap.keySet(), event.entries.keySet());

        if (!invalidIds.isEmpty()) {
            var logger = LogManager.getLogger();
            var msg = logger.getMessageFactory().newMessage("Found {} unregistered debug entries in profile: {}", invalidIds.size(), profile.getSerializedName());

            logger.fatal(msg.getFormattedMessage());
            logger.fatal("Unregistered debug entries: {}", invalidIds.stream().map(ResourceLocation::toString).collect(Collectors.joining(",", "[", "]")));

            // throw in dev to ensure people are correctly registering their entries when including them in profiles
            if (!FMLEnvironment.isProduction())
                return new IllegalStateException(msg.getFormattedMessage());
        }

        return null;
    }
}
