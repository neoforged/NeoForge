/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.filters;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.commands.suggestions.CustomSuggestion;
import net.minecraft.commands.suggestions.SuggestionContext;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.PermissionProviderCheck;
import net.minecraft.server.permissions.PermissionSetSupplier;
import org.jspecify.annotations.Nullable;

// TODO 1.21.6: Investigate whether the NodeBuilder/NodeInspector are actually correctly implemented and whether the filtering can be done directly in the NodeBuilder
class CommandTreeCleaner {
    /**
     * We use this as a node requirement marker to restore the original bitmask entry for restricted commands.
     */
    private static final Predicate<SuggestionContext> RESTRICTED = _ -> true;

    /**
     * Reconstructs the command nodes from information contained in the {@link ClientboundCommandsPacket}.
     * The Vanilla version of this lives in the client source set and uses the client-side suggestion provider.
     */
    public static final ClientboundCommandsPacket.NodeBuilder<SuggestionContext> COMMAND_NODE_BUILDER = new ClientboundCommandsPacket.NodeBuilder<>() {
        @Override
        public ArgumentBuilder<SuggestionContext, ?> createLiteral(String literal) {
            return LiteralArgumentBuilder.literal(literal);
        }

        @Override
        public ArgumentBuilder<SuggestionContext, ?> createArgument(String id, ArgumentType<?> argumentType, @Nullable CustomSuggestion customSuggestion) {
            RequiredArgumentBuilder<SuggestionContext, ?> builder = RequiredArgumentBuilder.argument(id, argumentType);
            if (customSuggestion != null) {
                builder.suggests(customSuggestion.toBrigadier());
            }

            return builder;
        }

        @Override
        public ArgumentBuilder<SuggestionContext, ?> configure(ArgumentBuilder<SuggestionContext, ?> builder, boolean executes, boolean restricted) {
            if (executes) {
                builder.executes(_ -> 0);
            }

            if (restricted) {
                builder.requires(RESTRICTED);
            }

            return builder;
        }
    };
    public static final ClientboundCommandsPacket.NodeInspector<PermissionSetSupplier> COMMAND_NODE_INSPECTOR = new ClientboundCommandsPacket.NodeInspector<>() {
        @Override
        public boolean isExecutable(CommandNode<? extends PermissionSetSupplier> node) {
            return node.getCommand() != null;
        }

        @Override
        public boolean isRestricted(CommandNode<? extends PermissionSetSupplier> node) {
            return node.getRequirement() instanceof PermissionProviderCheck<?>(PermissionCheck test) && test instanceof PermissionCheck.Require;
        }
    };

    /**
     * Cleans the command tree starting at the given root node from any argument types that do not match the given predicate.
     * Any {@code ArgumentCommandNode}s that have an unmatched argument type will be stripped from the tree.
     * 
     * @return A new command tree, stripped of any unmatched argument types
     */
    public static <S> RootCommandNode<S> cleanArgumentTypes(RootCommandNode<S> root, Predicate<ArgumentType<?>> argumentTypeFilter) {
        Predicate<CommandNode<?>> nodeFilter = node -> !(node instanceof ArgumentCommandNode<?, ?> argNode) || argumentTypeFilter.test(argNode.getType());
        return (RootCommandNode<S>) processCommandNode(root, nodeFilter, new HashMap<>());
    }

    private static <S> CommandNode<S> processCommandNode(CommandNode<S> node, Predicate<CommandNode<?>> nodeFilter, Map<CommandNode<S>, CommandNode<S>> newNodes) {
        CommandNode<S> existingNode = newNodes.get(node);
        if (existingNode == null) {
            CommandNode<S> newNode = cloneNode(node, nodeFilter, newNodes);
            newNodes.put(node, newNode);
            node.getChildren().stream()
                    .filter(nodeFilter)
                    .map(child -> processCommandNode(child, nodeFilter, newNodes))
                    .forEach(newNode::addChild);
            return newNode;
        } else {
            return existingNode;
        }
    }

    private static <S> CommandNode<S> cloneNode(CommandNode<S> node, Predicate<CommandNode<?>> nodeFilter, Map<CommandNode<S>, CommandNode<S>> newNodes) {
        if (node instanceof RootCommandNode<?>) {
            return new RootCommandNode<>();
        } else {
            ArgumentBuilder<S, ?> builder = node.createBuilder();
            if (node.getRedirect() != null) {
                if (nodeFilter.test(node.getRedirect())) {
                    builder.forward(processCommandNode(node.getRedirect(), nodeFilter, newNodes), node.getRedirectModifier(), node.isFork());
                } else {
                    builder.redirect(null);
                }
            }
            return builder.build();
        }
    }
}
