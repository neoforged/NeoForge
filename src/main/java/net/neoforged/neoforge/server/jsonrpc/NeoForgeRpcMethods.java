package net.neoforged.neoforge.server.jsonrpc;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.jsonrpc.IncomingRpcMethod;
import net.minecraft.server.jsonrpc.api.ParamInfo;
import net.minecraft.server.jsonrpc.api.ResultInfo;
import net.minecraft.server.jsonrpc.api.Schema;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.neoforged.neoforge.event.server.RegisterMinecraftApiServiceEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.server.jsonrpc.impl.NeoForgeServiceImpl;
import net.neoforged.neoforgespi.language.IModInfo;

public class NeoForgeRpcMethods {
    public static void register(RegisterEvent event) {
        event.register(Registries.INCOMING_RPC_METHOD, NeoForgeRpcMethods::registerIncoming);
    }

    public static void registerServices(RegisterMinecraftApiServiceEvent event) {
        event.register(NeoForgeService.SERVICE_KEY, new NeoForgeServiceImpl(event.getServer(), event.getJsonRpcLogger()));
    }

    private static void registerIncoming(RegisterEvent.RegisterHelper<IncomingRpcMethod> helper) {
        helper.register(rl("modlist"), IncomingRpcMethod
                .method(NeoForgeRpcMethods::getModList, ModRecord.CODEC.listOf())
                .response(new ResultInfo("modlist", NeoForgeSchemas.MOD_SCHEMA.asArray()))
                .description("Get a list of all mods installed on the server")
                .build());
        helper.register(rl("registries"), IncomingRpcMethod
                .method(NeoForgeRpcMethods::listRegistries, Codec.STRING.listOf())
                .response(new ResultInfo("registries", Schema.STRING_SCHEMA.asArray()))
                .description("List all registries on the server")
                .build());
        helper.register(rl("registry"), IncomingRpcMethod
                .method(NeoForgeRpcMethods::listRegistryContents, ResourceLocation.CODEC, ResourceLocation.CODEC.listOf())
                .response(new ResultInfo("registry", Schema.STRING_SCHEMA.asArray()))
                .param(new ParamInfo("registryId", Schema.STRING_SCHEMA, true))
                .description("List all keys in the given registry")
                .build());
    }

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, path);
    }

    private static List<ModRecord> getModList(MinecraftApi api) {
        List<IModInfo> modList = api.getService(NeoForgeService.SERVICE_KEY).getModList();
        return modList.stream().map(info -> new ModRecord(info.getModId(), info.getVersion().toString(), info.getDisplayName(), info.getDescription())).toList();
    }

    private static List<String> listRegistries(MinecraftApi api) {
        return api.getService(NeoForgeService.SERVICE_KEY).listRegistries().map(reg -> reg.location().toString()).toList();
    }

    private static List<ResourceLocation> listRegistryContents(MinecraftApi api, ResourceLocation registryId, ClientInfo clientInfo) {
        return api.getService(NeoForgeService.SERVICE_KEY).listRegistryKeys(ResourceKey.createRegistryKey(registryId)).map(ResourceKey::location).toList();
    }
}
