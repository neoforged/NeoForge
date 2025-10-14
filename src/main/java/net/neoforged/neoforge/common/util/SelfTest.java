/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * We use a "self-test" to launch a client and dedicated server from within our CI and exit.
 * This allows us to do an "end-to-end" test that actually uses the installer we produce to
 * install a client&server and test it.
 * The self-test writes a file so that the build script can detect that the game actually
 * loaded up enough to start ticking the game loop.
 */
@ApiStatus.Internal
public final class SelfTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelfTest.class);

    private SelfTest() {}

    public static void initCommon() {
        var serverSelfTestDestination = System.getenv("NEOFORGE_DEDICATED_SERVER_SELFTEST");
        if (serverSelfTestDestination != null) {
            if (FMLEnvironment.getDist() != Dist.DEDICATED_SERVER) {
                LOGGER.error("The server self-test ran with a dist of {} instead of dedicated server!", FMLEnvironment.getDist());
                System.exit(1);
            }
            NeoForge.EVENT_BUS.addListener((ServerTickEvent.Pre e) -> {
                if (e.getServer().isRunning()) {
                    writeSelfTestReport(serverSelfTestDestination);
                    e.getServer().halt(false);
                }
            });
        }
    }

    /**
     * This is used by our GitHub Actions pipeline to run an E2E test for PRs.
     * It writes a small self-test report to the file indicated by the system property and exits.
     */
    public static void writeSelfTestReport(String path) {
        try {
            Files.createFile(Paths.get(path));
            LOGGER.info("Wrote self-test report to '{}'", path);
        } catch (IOException e) {
            LOGGER.error("Failed to write self-test to '{}'", path, e);
            System.exit(1);
        }
    }
}
