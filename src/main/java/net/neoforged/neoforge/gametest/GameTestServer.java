/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.gametest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.startup.Entrypoint;
import net.neoforged.fml.startup.FatalErrorReporting;
import net.neoforged.fml.startup.FatalStartupException;
import net.neoforged.fml.startup.StartupArgs;
import org.jetbrains.annotations.Nullable;

public class GameTestServer extends Entrypoint {
    private GameTestServer() {}

    private static boolean running;

    public static boolean isRunning() {
        return running;
    }

    public static void main(String[] args) {
        running = true;

        try (var loader = startup()) {
            var main = createMainMethodCallable(loader, "net.minecraft.gametest.Main");
            main.invokeExact(loader.programArgs().getArguments());

            var serverThread = findServerThread();
            if (serverThread == null) {
                throw new FatalStartupException("Couldn't find Minecraft server thread. Startup likely failed.");
            }

            serverThread.join();
        } catch (Throwable t) {
            FatalErrorReporting.reportFatalErrorOnConsole(t);
            System.exit(1);
        } finally {
            running = false;
        }
    }

    private static FMLLoader startup() {
        var gameDir = Path.of("").toAbsolutePath();

        var startupArgs = new StartupArgs(
                gameDir,
                true /* headless */,
                Dist.DEDICATED_SERVER,
                false /* disable cleaning dist */,
                new String[0],
                new HashSet<>(),
                List.of(),
                Thread.currentThread().getContextClassLoader());

        try {
            return FMLLoader.create(startupArgs);
        } catch (Exception e) {
            var sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            throw new RuntimeException("Failed to start FML: " + e);
        }
    }

    private static @Nullable Thread findServerThread() {
        Thread serverThread = null;
        for (var thread : Thread.getAllStackTraces().keySet()) {
            if ("Server thread".equals(thread.getName())) {
                // While there's no guarantee for thread ids to be monotonically increasing
                // if there's ever a conflict between threads named "Server thread" because a mod spawned one
                // with that name, we'll pick the lower.
                if (serverThread == null || thread.threadId() <= serverThread.threadId()) {
                    serverThread = thread;
                }
            }
        }
        return serverThread;
    }
}
