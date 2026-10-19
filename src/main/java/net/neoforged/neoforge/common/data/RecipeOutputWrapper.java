/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.util.NoExceptionAutoCloseable;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// An internal wrapper around [RecipeOutput] to handle scoping conditions and changing the
/// default mod id used in [RecipeBuilder]s.
@ApiStatus.Internal
public final class RecipeOutputWrapper implements RecipeOutput, NoExceptionAutoCloseable {
    private static final ICondition[] EMPTY = new ICondition[0];

    private final RecipeOutput output;
    private final Stack<Context> context;

    /// @param output        The original [RecipeOutput].
    /// @param defaultModId      The default mod id to apply to [RecipeBuilder#save(RecipeOutput)] and [RecipeBuilder#save(RecipeOutput, String)].
    /// @param defaultConditions The base conditions to apply to all recipes.
    public RecipeOutputWrapper(RecipeOutput output, @Nullable String defaultModId, ICondition[] defaultConditions) {
        this.output = output;
        this.context = new ObjectArrayList<>();
        this.context.push(new Context(defaultModId, defaultConditions));
    }

    private RecipeOutputWrapper(RecipeOutput output, Context context) {
        this(output, context.modId(), context.conditions());
    }

    /// Sets the context of the provided [RecipeOutput] using the [ContextBuilder]. If the provided
    /// output is already a [RecipeOutputWrapper], then the context is appended to the existing wrapper.
    /// Otherwise, a new [RecipeOutputWrapper] is created. This should be used in a `try`-with-resources
    /// block.
    ///
    /// @param output  The original recipe output.
    /// @param builder A unary operator used to construct the context.
    /// @return A wrapped recipe output.
    public static RecipeOutputWrapper withContext(RecipeOutput output, UnaryOperator<ContextBuilder> builder) {
        // If the output is a wrapper
        if (output instanceof RecipeOutputWrapper wrapper) {
            // Push the context onto the wrapper.
            wrapper.context.push(builder.apply(new ContextBuilder(wrapper.modId())).build(wrapper.conditions()));
            return wrapper;
        } else {
            // Otherwise, return a new wrapper.
            return new RecipeOutputWrapper(output, builder.apply(new ContextBuilder(null)).build(EMPTY));
        }
    }

    /// Sets the context of the provided [RecipeOutput] using the [ContextBuilder]. If the provided
    /// output is already a [RecipeOutputWrapper], then the context is appended to the existing wrapper.
    /// Otherwise, a new [RecipeOutputWrapper] is created.
    ///
    /// This should be used only if the context is provided for a few recipes. Otherwise, use
    /// [#withContext(RecipeOutput, UnaryOperator)].
    ///
    /// @param output  The original recipe output.
    /// @param builder A unary operator used to construct the context.
    /// @return A single use recipe output.
    public static RecipeOutput singleUse(RecipeOutput output, UnaryOperator<ContextBuilder> builder) {
        // If the output is a wrapper.
        if (output instanceof RecipeOutputWrapper wrapper) {
            // Construct using the previous wrapper's context.
            return new RecipeOutputWrapper(wrapper.output, builder.apply(new ContextBuilder(wrapper.modId())).build(wrapper.conditions()));
        } else {
            // Otherwise, return a new wrapper.
            return new RecipeOutputWrapper(output, builder.apply(new ContextBuilder(null)).build(EMPTY));
        }
    }

    @Override
    public Advancement.Builder advancement() {
        return this.output.advancement();
    }

    @Override
    public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
        return this.output.lookup(key);
    }

    @Override
    public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
        return this.output.listContextElements(key);
    }

    @Override
    public void accept(ResourceKey<Recipe<?>> key, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
        this.output.accept(key, recipe, advancement, mergeConditions(this.conditions(), conditions));
    }

    @Override
    public @Nullable String overrideModId() {
        return this.modId();
    }

    @Override
    public void close() {
        // Only pop the stack if not-empty.
        if (!this.context.isEmpty()) {
            this.context.pop();
        }
    }

    /// {@return the mod id at the top the stack, or `null`}
    private @Nullable String modId() {
        return this.context.isEmpty() ? null : this.context.top().modId();
    }

    /// {@return the conditions at the top the stack, or an empty array}
    private ICondition[] conditions() {
        return this.context.isEmpty() ? EMPTY : this.context.top().conditions();
    }

    /// A helper to merge two condition arrays into a new array.
    ///
    /// @param originalConditions The original condition array to merge.
    /// @param newConditions      The array of new conditions to merge.
    /// @return A new array containing all conditions.
    private static ICondition[] mergeConditions(ICondition[] originalConditions, ICondition... newConditions) {
        if (newConditions.length == 0) {
            return originalConditions;
        } else if (originalConditions.length == 0) {
            return newConditions;
        } else {
            return ArrayUtils.addAll(originalConditions, newConditions);
        }
    }

    private record Context(@Nullable String modId, ICondition[] conditions) {}

    /// A builder for constructing the new context the recipe output should use.
    public static final class ContextBuilder {
        private @Nullable String modId;
        private ICondition[] conditions = EMPTY;

        private ContextBuilder(@Nullable String parentModId) {
            this.modId = parentModId;
        }

        /// Sets the mod id that should be used as the namespace for generating recipes via
        /// [RecipeBuilder#save(RecipeOutput)] or [RecipeBuilder#save(RecipeOutput, String)].
        /// This is provided as a helper for existing [RecipeProvider] methods that do not
        /// provide some way to set the recipe namespace.
        ///
        /// @param modId The mod id to use.
        /// @return This builder.
        public ContextBuilder forMod(@Nullable String modId) {
            this.modId = modId;
            return this;
        }

        /// Sets the conditions to apply to all received recipes.
        ///
        /// @param conditions The conditions to apply to the recipe output.
        /// @return This builder.
        public ContextBuilder withConditions(ICondition... conditions) {
            this.conditions = conditions;
            return this;
        }

        private Context build(ICondition[] parentConditions) {
            return new Context(this.modId, mergeConditions(parentConditions, this.conditions));
        }
    }
}
