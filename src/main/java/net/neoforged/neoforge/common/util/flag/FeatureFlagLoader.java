/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util.flag;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.flag.FeatureFlagRegistry;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class FeatureFlagLoader {
    private static final Gson GSON = new Gson();

    private FeatureFlagLoader() {}

    public static void loadModdedFlags(FeatureFlagRegistry.Builder builder) {
        Map<IModInfo, Path> pathPerMod = new HashMap<>();
        LoadingModList.get()
                .getModFiles()
                .stream()
                .map(ModFileInfo::getMods)
                .flatMap(List::stream)
                .forEach(mod -> mod.getConfig().<String>getConfigElement("featureFlags").ifPresent(file -> {
                    Path path = mod.getOwningFile().getFile().findResource(file);
                    if (!Files.isRegularFile(path)) {
                        ModLoader.addLoadingIssue(ModLoadingIssue.error("fml.modloadingissue.feature_flags.file_not_found", path).withAffectedMod(mod));
                        return;
                    }
                    pathPerMod.put(mod, path);
                }));

        pathPerMod.forEach((mod, path) -> {
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                JsonObject obj = GSON.fromJson(reader, JsonObject.class);
                JsonArray flagArray = GsonHelper.getAsJsonArray(obj, "flags");
                for (JsonElement elem : flagArray) {
                    String flagName = GsonHelper.convertToString(elem, "flag");
                    ResourceLocation flagLocation = ResourceLocation.parse(flagName);
                    Preconditions.checkArgument(
                            flagLocation.getNamespace().equals(mod.getModId()),
                            "Cannot add new flags to foreign namespaces: %s",
                            flagLocation);
                    builder.create(flagLocation, true);
                }
            } catch (Throwable e) {
                ModLoader.addLoadingIssue(ModLoadingIssue.error("fml.modloadingissue.feature_flags.loading_error", path)
                        .withAffectedMod(mod)
                        .withCause(e));
            }
        });
    }
}
