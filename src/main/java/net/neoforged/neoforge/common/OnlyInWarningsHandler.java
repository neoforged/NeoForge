package net.neoforged.neoforge.common;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Type;
import org.slf4j.Logger;

@ApiStatus.Internal
@SuppressWarnings("unused")
@Mod(NeoForgeVersion.MOD_ID)
public class OnlyInWarningsHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public OnlyInWarningsHandler(ModContainer container) {
        if (!FMLEnvironment.production) {
            ModList.get().forEachModFile(file -> {
                if (file.getModInfos().stream().anyMatch(info -> info.getModId().equals("minecraft") || info.getModId().equals(NeoForgeVersion.MOD_ID))) {
                    return;
                }
                Type anType = Type.getType(OnlyIn.class);
                if (file.getScanResult().getAnnotations().stream().anyMatch(ad -> ad.annotationType().equals(anType))) {
                    LOGGER.warn("The mod {} uses the @OnlyIn annotation; the runtime member-stripping behaviour of this annotation is no longer present, which may lead to issues if that behaviour was relied upon", file.getModInfos().getFirst().getModId());
                    ModLoader.addLoadingIssue(ModLoadingIssue.warning("loadwarning.neoforge.onlyin", file.getModInfos().getFirst().getModId()).withAffectedModFile(file));
                }
            });
        }
    }
}
