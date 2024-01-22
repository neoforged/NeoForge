package net.neoforged.neoforge.common.data.internal;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataProvider;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public final class NeoForgeRegistryOrderReportProvider implements DataProvider {
    private final PackOutput output;

    public NeoForgeRegistryOrderReportProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        JsonObject json = new JsonObject();

        JsonArray array = new JsonArray();
        BuiltInRegistries.getVanillaRegistrationOrder().forEach(name -> array.add(name.toString()));
        json.add("order", array);

        Path path = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("registry_order.json");
        return DataProvider.saveStable(output, json, path);
    }

    @Override
    public String getName() {
        return "registry_order_report";
    }
}
