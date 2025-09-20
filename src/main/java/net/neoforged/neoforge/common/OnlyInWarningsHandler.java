/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

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
    private static final boolean HIDE_WARNING_SCREEN = Boolean.getBoolean("neoforge.warnings.onlyin.hide");

    public OnlyInWarningsHandler(ModContainer container) {
        if (!FMLEnvironment.isProduction()) {
            ModList.get().forEachModFile(file -> {
                if (file.getModInfos().stream().anyMatch(info -> info.getModId().equals("minecraft"))) {
                    return;
                }
                Type anType = Type.getType(OnlyIn.class);
                var onlyInUsages = file.getScanResult().getAnnotations().stream().filter(ad -> ad.annotationType().equals(anType)).toList();
                if (!onlyInUsages.isEmpty()) {
                    if (!HIDE_WARNING_SCREEN) {
                        ModLoader.addLoadingIssue(ModLoadingIssue.warning("loadwarning.neoforge.onlyin", file.getModInfos().getFirst().getModId()).withAffectedModFile(file));
                    }
                    LOGGER.error("The mod {} uses the @OnlyIn annotation; the runtime member-stripping behaviour of this annotation is no longer present, which may lead to issues if that behaviour was relied upon", file.getModInfos().getFirst().getModId());
                    for (var annData : onlyInUsages) {
                        switch (annData.targetType()) {
                            case TYPE -> LOGGER.error("@OnlyIn used on class {}", annData.clazz().getClassName());
                            case FIELD -> LOGGER.error("@OnlyIn used on field {}.{}", annData.clazz().getClassName(), annData.memberName());
                            case METHOD -> LOGGER.error("@OnlyIn used on method {}.{}", annData.clazz().getClassName(), annData.memberName());
                            case CONSTRUCTOR -> LOGGER.error("@OnlyIn used on constructor for {}", annData.clazz().getClassName());
                            case ANNOTATION_TYPE -> LOGGER.error("@OnlyIn used on annotation {}", annData.clazz().getClassName());
                            case PACKAGE -> LOGGER.error("@OnlyIn used on package {}", annData.clazz().getClassName());
                        }
                    }
                }
            });
        }
    }
}
