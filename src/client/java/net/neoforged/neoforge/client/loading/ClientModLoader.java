/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.loading;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.DataPackConfig;
import net.neoforged.fml.Logging;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.ModWorkManager;
import net.neoforged.fml.VersionChecker;
import net.neoforged.fml.earlydisplay.DisplayWindow;
import net.neoforged.fml.loading.EarlyLoadingScreenController;
import net.neoforged.fml.startup.FatalErrorReporting;
import net.neoforged.neoforge.client.config.NeoForgeClientConfig;
import net.neoforged.neoforge.client.gui.LoadingErrorScreen;
import net.neoforged.neoforge.client.network.registration.ClientNetworkRegistry;
import net.neoforged.neoforge.internal.CommonModLoader;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import net.neoforged.neoforge.server.LanguageHook;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public class ClientModLoader extends CommonModLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    @Nullable
    private static EarlyLoadingScreenController earlyLoadingScreen;

    public static void begin() {
        // force log4j to shutdown logging in a shutdown hook. This is because we disable default shutdown hook so the server properly logs it's shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(LogManager::shutdown));
        earlyLoadingScreen = EarlyLoadingScreenController.current();
        if (earlyLoadingScreen != null) {
            earlyLoadingScreen.updateProgress("Loading mods");
        }
        LanguageHook.loadBuiltinLanguages();

        Runnable periodicTick = earlyLoadingScreen != null ? earlyLoadingScreen::periodicTick : () -> {};
        begin(periodicTick, false);
    }

    public static void setupModResourcePacks(PackRepository repository) {
        ResourcePackLoader.populatePackRepository(repository, PackType.CLIENT_RESOURCES, false);
        DataPackConfig.DEFAULT.addModPacks(ResourcePackLoader.getPackNames(PackType.SERVER_DATA));
    }

    public static void finish() {
        try {
            load(ModWorkManager.syncExecutor(), ModWorkManager.parallelExecutor());
            finish(ModWorkManager.syncExecutor(), ModWorkManager.parallelExecutor());
            ModLoader.runInitTask("Client network registry lock", ModWorkManager.syncExecutor(), () -> {}, ClientNetworkRegistry::setup);
        } catch (ModLoadingException e) {
            FatalErrorReporting.reportFatalError(e);
        }
        if (earlyLoadingScreen instanceof DisplayWindow displayWindow) {
            displayWindow.close();
        }
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
        if (!warnings.isEmpty()) {
            if (NeoForgeClientConfig.INSTANCE.showLoadWarnings.get()) {
                return () -> Minecraft.getInstance().setScreen(new LoadingErrorScreen(warnings, null, initialScreensTask));
            }

            //User disabled warning screen, as least log them
            LOGGER.warn(Logging.LOADING, "Mods loaded with {} warning(s)", warnings.size());
            for (var warning : warnings) {
                LOGGER.warn(Logging.LOADING, "{} [{}]", warning.translationKey(), warning.translationArgs());
            }
        }
        return initialScreensTask;
    }
}
