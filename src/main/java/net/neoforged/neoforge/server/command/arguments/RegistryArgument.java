package net.neoforged.neoforge.server.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RegistryArgument implements ArgumentType<ResourceLocation> {
    private static final DynamicCommandExceptionType UNKNOWN_REGISTRY = new DynamicCommandExceptionType(key -> Component.translatable("commands.neoforge.arguments.registry.error.unknown_registry", key));

    public static RegistryArgument registryArgument() {
        return new RegistryArgument();
    }

    public static <T> Registry<T> getRegistry(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
        var location = ctx.getArgument(name, ResourceLocation.class);
        Optional<Registry<T>> optional = ctx.getSource().registryAccess().registry(ResourceKey.createRegistryKey(location));
        return optional.orElseThrow(() -> UNKNOWN_REGISTRY.create(location));
    }

    @Override
    public ResourceLocation parse(StringReader reader) throws CommandSyntaxException {
        return ResourceLocation.read(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (context.getSource() instanceof SharedSuggestionProvider provider) {
            return SharedSuggestionProvider.suggestResource(provider.registryAccess().listRegistries().map(ResourceKey::location), builder);
        }

        return builder.buildFuture();
    }
}
