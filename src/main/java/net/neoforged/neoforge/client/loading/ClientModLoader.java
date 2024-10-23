/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.loading;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.DataPackConfig;
import net.neoforged.fml.Logging;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.ModWorkManager;
import net.neoforged.fml.VersionChecker;
import net.neoforged.fml.loading.ImmediateWindowHandler;
import net.neoforged.neoforge.client.gui.LoadingErrorScreen;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeConfig;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.internal.BrandingControl;
import net.neoforged.neoforge.internal.CommonModLoader;
import net.neoforged.neoforge.logging.CrashReportExtender;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import net.neoforged.neoforge.server.LanguageHook;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class ClientModLoader extends CommonModLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean loading;
    private static Minecraft mc;
    private static boolean loadingComplete;
    @Nullable
    private static ModLoadingException error;

    public static void begin(final Minecraft minecraft, final PackRepository defaultResourcePacks, final ReloadableResourceManager mcResourceManager) {
        // force log4j to shutdown logging in a shutdown hook. This is because we disable default shutdown hook so the server properly logs it's shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(LogManager::shutdown));
        ImmediateWindowHandler.updateProgress("Loading mods");
        loading = true;
        ClientModLoader.mc = minecraft;
        LogicalSidedProvider.setClient(() -> minecraft);
        LanguageHook.loadBuiltinLanguages();
        try {
            begin(ImmediateWindowHandler::renderTick, false);
        } catch (ModLoadingException e) {
            error = e;
        }
        if (error == null) {
            ResourcePackLoader.populatePackRepository(defaultResourcePacks, PackType.CLIENT_RESOURCES, false);
            DataPackConfig.DEFAULT.addModPacks(ResourcePackLoader.getPackNames(PackType.SERVER_DATA));
            mcResourceManager.registerReloadListener(ClientModLoader::onResourceReload);
            mcResourceManager.registerReloadListener(BrandingControl.resourceManagerReloadListener());
        }
    }

    private static CompletableFuture<Void> onResourceReload(final PreparableReloadListener.PreparationBarrier stage, final ResourceManager resourceManager, final Executor asyncExecutor, final Executor syncExecutor) {
        return CompletableFuture.runAsync(() -> startModLoading(syncExecutor, asyncExecutor), ModWorkManager.parallelExecutor())
                .thenCompose(stage::wait)
                .thenRunAsync(() -> finishModLoading(syncExecutor, asyncExecutor), ModWorkManager.parallelExecutor());
    }

    private static void catchLoadingException(Runnable r) {
        // Don't load again on subsequent reloads
        if (loadingComplete) return;
        // If the mod loading state is invalid, skip further mod initialization
        if (ModLoader.hasErrors()) return;

        try {
            r.run();
        } catch (ModLoadingException e) {
            if (error == null) error = e;
        }
    }

    private static void startModLoading(Executor syncExecutor, Executor parallelExecutor) {
        catchLoadingException(() -> load(syncExecutor, parallelExecutor));
    }

    private static void finishModLoading(Executor syncExecutor, Executor parallelExecutor) {
        catchLoadingException(() -> finish(syncExecutor, parallelExecutor));
        loading = false;
        loadingComplete = true;
        // reload game settings on main thread
        syncExecutor.execute(() -> mc.options.load(true));
    }

    public static VersionChecker.Status checkForUpdates() {
        boolean anyOutdated = ModList.get().getMods().stream()
                .map(VersionChecker::getResult)
                .map(result -> result.status())
                .anyMatch(status -> status == VersionChecker.Status.OUTDATED || status == VersionChecker.Status.BETA_OUTDATED);
        return anyOutdated ? VersionChecker.Status.OUTDATED : null;
    }

    public static Runnable completeModLoading(Runnable initialScreensTask) {
        List<ModLoadingIssue> warnings = ModLoader.getLoadingIssues();
        boolean showWarnings = true;
        try {
            showWarnings = NeoForgeConfig.CLIENT.showLoadWarnings.get();
        } catch (NullPointerException | IllegalStateException e) {
            // We're in an early error state, config is not available. Assume true.
        }

        if (error != null) {
            // Double check we have the langs loaded for forge
            LanguageHook.loadBuiltinLanguages();
            File dumpedLocation = CrashReportExtender.dumpModLoadingCrashReport(LOGGER, error.getIssues(), mc.gameDirectory);
            // Ignore incoming initial screens task, the subsequent screens are unreachable in an error state
            return () -> mc.setScreen(new LoadingErrorScreen(error.getIssues(), dumpedLocation, () -> {}));
        }

        // We can finally start the game eventbus up
        NeoForge.EVENT_BUS.start();

        if (!warnings.isEmpty()) {
            if (showWarnings) {
                return () -> mc.setScreen(new LoadingErrorScreen(warnings, null, initialScreensTask));
            }

            //User disabled warning screen, as least log them
            LOGGER.warn(Logging.LOADING, "Mods loaded with {} warning(s)", warnings.size());
            for (var warning : warnings) {
                LOGGER.warn(Logging.LOADING, "{} [{}]", warning.translationKey(), warning.translationArgs());
            }
        }
        return initialScreensTask;
    }

    public static boolean isLoading() {
        return loading;
    }
}
