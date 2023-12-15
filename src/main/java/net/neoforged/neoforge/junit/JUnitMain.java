package net.neoforged.neoforge.junit;

import com.mojang.logging.LogUtils;
import cpw.mods.modlauncher.Launcher;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.neoforged.api.distmarker.Dist;
import org.slf4j.Logger;

import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

public class JUnitMain {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        // Load mods
        net.neoforged.neoforge.server.loading.ServerModLoader.load();

        Consumer<Dist> extension = Launcher.INSTANCE.environment().findLaunchPlugin("runtimedistcleaner")
                .orElseThrow().getExtension();
        extension.accept(Dist.CLIENT);
    }

}
