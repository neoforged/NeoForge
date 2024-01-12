package net.neoforged.neoforge.network.configuration;

import java.util.Collection;
import java.util.function.Consumer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

public record CheckBrewingRecipeCompatability(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
    private static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "check_brewing_recipe_compatability");
    public static final Type TYPE = new Type(ID);

    public static Collection<RecipeHolder<?>> filterCompatible(ServerGamePacketListenerImpl serverGamePacketListener, Collection<RecipeHolder<?>> holders) {
        if (!serverGamePacketListener.isVanillaConnection()) return holders;
        return holders.stream().filter(holder -> !(holder.value() instanceof IBrewingRecipe)).toList();
    }

    @Override
    public Type type() {
        return TYPE;
    }

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        if (listener().isVanillaConnection() && vanillaIncompatible()) {
            listener().disconnect(Component.translatableWithFallback("neoforge.network.negotiation.failure.vanilla.client.not_supported", Language.getInstance().getOrDefault("neoforge.network.negotiation.failure.vanilla.client.not_supported"), NeoForgeVersion.getVersion()));
        } else {
            listener().finishCurrentTask(type());
        }
    }

    private boolean vanillaIncompatible() {
        if (!(listener().getMainThreadEventLoop() instanceof MinecraftServer server)) return true;
        for (RecipeHolder<IBrewingRecipe> recipe : server.getRecipeManager().getAllRecipesFor(NeoForgeMod.BREWING_RECIPE_TYPE.get())) {
            if (!recipe.id().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) return true;
        }
        return false;
    }
}
