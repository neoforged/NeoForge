package net.neoforged.neoforge.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

import static net.neoforged.neoforge.common.util.SelfTest.writeSelfTestReport;

@ApiStatus.Internal
public class SelfTestClient {
    public static void initClient() {
        var clientSelfTestDestination = System.getenv("NEOFORGE_CLIENT_SELFTEST");
        if (clientSelfTestDestination != null) {
            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre e) -> {
                if (Minecraft.getInstance().getOverlay() instanceof LoadingOverlay) {
                    return;
                }
                if (Minecraft.getInstance().isRunning()) {
                    writeSelfTestReport(clientSelfTestDestination);
                    Minecraft.getInstance().stop();
                }
            });
        }
    }
}
