/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.jsonrpc;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.jsonrpc.IncomingRpcMethod;
import net.minecraft.server.jsonrpc.api.Schema;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class NeoForgeRpcMethods {
    private NeoForgeRpcMethods() {}

    public static void register(RegisterEvent event) {
        event.register(Registries.INCOMING_RPC_METHOD, NeoForgeRpcMethods::registerIncoming);
    }

    private static void registerIncoming(RegisterEvent.RegisterHelper<IncomingRpcMethod<?, ?>> helper) {
        helper.register(id("mods"), IncomingRpcMethod
                .method(NeoForgeRpcMethods::getModList)
                .response("mods", NeoForgeSchemas.MOD_SCHEMA.asArray())
                .description("Get a list of all mods installed on the server")
                .build());
        helper.register(id("registries"), IncomingRpcMethod
                .method(NeoForgeRpcMethods::listRegistries)
                .response("registries", Schema.STRING_SCHEMA.asArray())
                .description("List all registries on the server")
                .build());
        helper.register(id("registry"), IncomingRpcMethod
                .method(NeoForgeRpcMethods::listRegistryContents)
                .response("registry", NeoForgeSchemas.REGISTRY_SCHEMA.asRef())
                .param("registryId", Schema.ofType("string", Identifier.CODEC))
                .description("Get the information for the given registry")
                .build());
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, path);
    }

    private static List<ModRecord> getModList(MinecraftApi api) {
        return ModList.get().getMods()
                .stream()
                .map(ModRecord::new)
                .toList();
    }

    private static List<String> listRegistries(MinecraftApi api) {
        return api.registryAccess()
                .listRegistries()
                .map(r -> r.key().identifier().toString())
                .toList();
    }

    private static RegistryInfo listRegistryContents(MinecraftApi api, Identifier registryId, ClientInfo clientInfo) {
        return RegistryInfo.withEntries(api.registryAccess().lookupOrThrow(ResourceKey.createRegistryKey(registryId)));
    }
}
