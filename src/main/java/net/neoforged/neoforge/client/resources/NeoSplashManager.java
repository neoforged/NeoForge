/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.resources;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import net.minecraft.client.User;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterSplashProvidersEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

/**
 * An implementation of {@link SplashManager} which supports custom splashes via {@link RegisterSplashProvidersEvent}.
 */
public class NeoSplashManager extends SplashManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final ImmutableList<ISplashProvider> providers;

    public NeoSplashManager(final User user) {
        super(user);

        // TODO: we don't actually use namedProviders but we're gathering that information anyway to avoid another breaking change in the future.
        var namedProviders = new HashMap<ResourceLocation, ISplashProvider>();
        var orderedProviders = new ArrayList<ISplashProvider>();
        preRegisterVanillaProviders(namedProviders, orderedProviders);
        var event = new RegisterSplashProvidersEvent(namedProviders, orderedProviders);
        ModLoader.get().postEventWrapContainerInModOrder(event);
        providers = ImmutableList.copyOf(orderedProviders);
    }

    private static void preRegisterVanillaProviders(
        HashMap<ResourceLocation, ISplashProvider> namedProviders,
        List<ISplashProvider> orderedProviders) {
        for (var entry : VanillaSplash.values()) {
            namedProviders.put(entry.id(), entry.provider());
            orderedProviders.add(entry.provider());
        }
    }

    @Nullable
    @Override
    public SplashRenderer getSplash() {
        final var calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        final var context = new SelectionContext(calendar, RANDOM, user, splashes.size());

        // Ask mods if there's a specific splash which can be displayed.
        for (final var provider : providers) {
            final var splash = provider.getSplash(context);

            if (splash != null) {
                return splash;
            }
        }

        // If the random splash pool has nothing in it, there's nothing to render.
        if (splashes.isEmpty()) {
            return null;
        }

        // Get a random splash from the pool
        return new SplashRenderer(splashes.get(RANDOM.nextInt(splashes.size())));
    }

    /**
     * An interface which can be used to provide splashes
     */
    @FunctionalInterface
    public interface ISplashProvider
    {
        /**
         * Gets the splash to render, or null if there is no splash to render.
         *
         * @param context A collection of external properties for splash selection.
         *
         * @return A {@link SplashRenderer splash renderer} used to render splashes.
         */
        @Nullable
        public SplashRenderer getSplash(SelectionContext context);
    }

    /**
     * A record containing external properties used in the selection of a {@link SplashRenderer splash renderer}.
     *
     * @param calendar         The calendar representing the current date, for time-based splashes.
     * @param randomSource     The random source, for random chance splashes.
     * @param user             The user, for user-specific splashes, or {@code null} if no user could be loaded.
     * @param sizeOfRandomPool The size of the random splash pool, or 0 if there are none.
     */
    public record SelectionContext(
        Calendar calendar,
        RandomSource randomSource,
        @Nullable
        User user,
        int sizeOfRandomPool) { }

    // The vanilla splashes, including the logic in which they trigger.
    private enum VanillaSplash {
        CHRISTMAS("christmas", (context) ->
            context.calendar().get(Calendar.MONTH) + 1 == 12 && context.calendar().get(Calendar.DAY_OF_MONTH) == 24
                ? SplashRenderer.CHRISTMAS
                : null),
        NEW_YEAR("new_year", (context) ->
            context.calendar().get(Calendar.MONTH) + 1 == 1 && context.calendar().get(Calendar.DAY_OF_MONTH) == 1
                ? SplashRenderer.NEW_YEAR
                : null),
        HALLOWEEN("halloween", (context) ->
            context.calendar().get(Calendar.MONTH) + 1 == 10 && context.calendar().get(Calendar.DAY_OF_MONTH) == 31
                ? SplashRenderer.HALLOWEEN
                : null),
        USERNAME_IS_YOU("username_is_you", (context) ->
            context.user() != null && context.randomSource().nextInt(context.sizeOfRandomPool()) == 42
                ? new SplashRenderer(context.user().getName().toUpperCase(Locale.ROOT) + " IS YOU")
                : null);

        private final ResourceLocation id;
        private final ISplashProvider provider;

        VanillaSplash(String id, ISplashProvider provider) {
            this.id = new ResourceLocation("minecraft", id);
            this.provider = provider;
        }

        public ResourceLocation id() {
            return id;
        }

        public ISplashProvider provider() {
            return provider;
        }
    }
}
