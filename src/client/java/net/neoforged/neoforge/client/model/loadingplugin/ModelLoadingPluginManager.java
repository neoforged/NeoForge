/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.loadingplugin;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;

public final class ModelLoadingPluginManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Identifier, ModelLoadingPlugin> PLUGINS = new Object2ReferenceLinkedOpenHashMap<>();
    private static final Map<Identifier, PreparableModelLoadingPlugin<?>> PREPARABLE_PLUGINS = new Object2ReferenceLinkedOpenHashMap<>();

    private final List<ModifierEntry<ModelModifier.ModifyOnLoad>> onLoadModifiers = new ArrayList<>();
    private final List<ModifierEntry<ModelModifier.ModifyBlockOnLoad>> onLoadBlockModifiers = new ArrayList<>();
    private final List<ModifierEntry<ModelModifier.ModifyBlockBeforeBake>> beforeBakeBlockModifiers = new ArrayList<>();
    private final List<ModifierEntry<ModelModifier.ModifyBlockAfterBake>> afterBakeBlockModifiers = new ArrayList<>();
    private final List<ModifierEntry<ModelModifier.ModifyItemBeforeBake>> beforeBakeItemModifiers = new ArrayList<>();
    private final List<ModifierEntry<ModelModifier.ModifyItemAfterBake>> afterBakeItemModifiers = new ArrayList<>();
    private final ThreadLocal<BlockBeforeBakeContextImpl> beforeBakeBlockContext = ThreadLocal.withInitial(BlockBeforeBakeContextImpl::new);
    private final ThreadLocal<BlockAfterBakeContextImpl> afterBakeBlockContext = ThreadLocal.withInitial(BlockAfterBakeContextImpl::new);
    private final ThreadLocal<ItemBeforeBakeContextImpl> beforeBakeItemContext = ThreadLocal.withInitial(ItemBeforeBakeContextImpl::new);
    private final ThreadLocal<ItemAfterBakeContextImpl> afterBakeItemContext = ThreadLocal.withInitial(ItemAfterBakeContextImpl::new);

    private ModelLoadingPluginManager(List<PluginEntry> plugins) {
        var context = new ModelLoadingPlugin.Context() {
            @UnknownNullability
            private Identifier pluginId;

            @Override
            public void registerModifier(ModelModifier.Phase phase, ModelModifier modifier) {
                switch (modifier) {
                    case ModelModifier.ModifyOnLoad onLoad -> onLoadModifiers.add(new ModifierEntry<>(pluginId, phase, onLoad));
                    case ModelModifier.ModifyBlockOnLoad onLoadBlock -> onLoadBlockModifiers.add(new ModifierEntry<>(pluginId, phase, onLoadBlock));
                    case ModelModifier.ModifyBlockBeforeBake beforeBakeBlock -> beforeBakeBlockModifiers.add(new ModifierEntry<>(pluginId, phase, beforeBakeBlock));
                    case ModelModifier.ModifyBlockAfterBake afterBakeBlock -> afterBakeBlockModifiers.add(new ModifierEntry<>(pluginId, phase, afterBakeBlock));
                    case ModelModifier.ModifyItemBeforeBake beforeBakeItem -> beforeBakeItemModifiers.add(new ModifierEntry<>(pluginId, phase, beforeBakeItem));
                    case ModelModifier.ModifyItemAfterBake afterBakeItem -> afterBakeItemModifiers.add(new ModifierEntry<>(pluginId, phase, afterBakeItem));
                }
            }
        };
        for (PluginEntry plugin : plugins) {
            context.pluginId = plugin.id;
            plugin.plugin.initialize(context);
        }

        Comparator<ModifierEntry<?>> comp = Comparator.comparing(ModifierEntry::phase);
        this.onLoadModifiers.sort(comp);
        this.onLoadBlockModifiers.sort(comp);
        this.beforeBakeBlockModifiers.sort(comp);
        this.afterBakeBlockModifiers.sort(comp);
        this.beforeBakeItemModifiers.sort(comp);
        this.afterBakeItemModifiers.sort(comp);
    }

    @ApiStatus.Internal
    public static void init() {
        ModLoader.postEvent(new ModelEvent.RegisterLoadingPlugins(PLUGINS, PREPARABLE_PLUGINS));
    }

    public static CompletableFuture<ModelLoadingPluginManager> prepare(PreparableReloadListener.SharedState sharedState, Executor executor) {
        List<CompletableFuture<PluginEntry>> pluginFutures = new ArrayList<>();
        for (Map.Entry<Identifier, ModelLoadingPlugin> entry : PLUGINS.entrySet()) {
            pluginFutures.add(CompletableFuture.completedFuture(new PluginEntry(entry.getKey(), entry.getValue())));
        }
        for (Map.Entry<Identifier, PreparableModelLoadingPlugin<?>> entry : PREPARABLE_PLUGINS.entrySet()) {
            pluginFutures.add(preparePlugin(entry.getKey(), entry.getValue(), sharedState, executor));
        }
        return Util.sequence(pluginFutures).thenApplyAsync(ModelLoadingPluginManager::new, executor);
    }

    private static <T> CompletableFuture<PluginEntry> preparePlugin(
            Identifier pluginId,
            PreparableModelLoadingPlugin<T> plugin,
            PreparableReloadListener.SharedState sharedState,
            Executor executor) {
        CompletableFuture<T> dataFuture = plugin.load(sharedState, executor);
        return dataFuture.thenApply(data -> new PluginEntry(pluginId, ctx -> plugin.initialize(data, ctx)));
    }

    public Map<Identifier, UnbakedModel> modifyModelsOnLoad(Map<Identifier, UnbakedModel> models) {
        if (onLoadModifiers.isEmpty()) return models;

        if (!(models instanceof HashMap)) {
            models = new HashMap<>(models);
        }

        var context = new ModelModifier.ModifyOnLoad.Context() {
            @UnknownNullability
            private Identifier id;

            @Override
            public Identifier id() {
                return this.id;
            }
        };

        models.replaceAll((id, model) -> {
            context.id = id;
            return modifyModelOnLoad(model, context);
        });
        return models;
    }

    public UnbakedModel modifyModelOnLoad(UnbakedModel model, ModelModifier.ModifyOnLoad.Context context) {
        for (ModifierEntry<ModelModifier.ModifyOnLoad> entry : onLoadModifiers) {
            try {
                model = entry.modifier.modifyModelOnLoad(model, context);
            } catch (Throwable t) {
                LOGGER.error("On-load modifier {} from plugin {} threw an exception", entry.modifier, entry.owningPlugin, t);
            }
        }
        return model;
    }

    public BlockStateModelLoader.LoadedModels modifyBlockModelsOnLoad(BlockStateModelLoader.LoadedModels models) {
        if (onLoadBlockModifiers.isEmpty()) return models;

        Map<BlockState, BlockStateModel.UnbakedRoot> map = models.models();
        if (!(map instanceof IdentityHashMap)) {
            map = new IdentityHashMap<>(map);
            models = new BlockStateModelLoader.LoadedModels(map);
        }

        var context = new ModelModifier.ModifyBlockOnLoad.Context() {
            @UnknownNullability
            private BlockState state;

            @Override
            public BlockState state() {
                return state;
            }
        };
        map.replaceAll((state, model) -> {
            context.state = state;
            return modifyBlockModelOnLoad(model, context);
        });

        return models;
    }

    public BlockStateModel.UnbakedRoot modifyBlockModelOnLoad(BlockStateModel.UnbakedRoot model, ModelModifier.ModifyBlockOnLoad.Context context) {
        for (ModifierEntry<ModelModifier.ModifyBlockOnLoad> entry : onLoadBlockModifiers) {
            try {
                model = entry.modifier.modifyBlockModelOnLoad(model, context);
            } catch (Throwable t) {
                LOGGER.error("On-load-block modifier {} from plugin {} threw an exception", entry.modifier, entry.owningPlugin, t);
            }
        }
        return model;
    }

    public BlockStateModel.UnbakedRoot modifyBlockModelBeforeBake(BlockState state, BlockStateModel.UnbakedRoot model, ModelBaker baker) {
        if (beforeBakeBlockModifiers.isEmpty()) return model;

        BlockBeforeBakeContextImpl context = beforeBakeBlockContext.get().setup(state, baker);
        for (ModifierEntry<ModelModifier.ModifyBlockBeforeBake> entry : beforeBakeBlockModifiers) {
            try {
                model = entry.modifier.modifyBlockModelBeforeBake(model, context);
            } catch (Throwable t) {
                LOGGER.error("Before-bake-block modifier {} from plugin {} threw an exception", entry.modifier, entry.owningPlugin, t);
            }
        }
        return model;
    }

    public BlockStateModel modifyBlockModelAfterBake(BlockState state, BlockStateModel model, BlockStateModel.UnbakedRoot sourceModel, ModelBaker baker) {
        if (afterBakeBlockModifiers.isEmpty()) return model;

        BlockAfterBakeContextImpl context = afterBakeBlockContext.get().setup(state, sourceModel, baker);
        for (ModifierEntry<ModelModifier.ModifyBlockAfterBake> entry : afterBakeBlockModifiers) {
            try {
                model = entry.modifier.modifyBlockModelAfterBake(model, context);
            } catch (Throwable t) {
                LOGGER.error("After-bake-block modifier {} from plugin {} threw an exception", entry.modifier, entry.owningPlugin, t);
            }
        }
        return model;
    }

    public ItemModel.Unbaked modifyItemModelBeforeBake(Identifier id, ItemModel.Unbaked model, ClientItem clientItem, ItemModel.BakingContext bakingContext) {
        if (beforeBakeItemModifiers.isEmpty()) return model;

        ItemBeforeBakeContextImpl context = beforeBakeItemContext.get().setup(id, clientItem, bakingContext);
        for (ModifierEntry<ModelModifier.ModifyItemBeforeBake> entry : beforeBakeItemModifiers) {
            try {
                model = entry.modifier.modifyItemModelBeforeBake(model, context);
            } catch (Throwable t) {
                LOGGER.error("Before-bake-item modifier {} from plugin {} threw an exception", entry.modifier, entry.owningPlugin, t);
            }
        }
        return model;
    }

    public ItemModel modifyItemModelAfterBake(Identifier id, ItemModel model, ItemModel.Unbaked sourceModel, ClientItem clientItem, ItemModel.BakingContext bakingContext) {
        if (afterBakeItemModifiers.isEmpty()) return model;

        ItemAfterBakeContextImpl context = afterBakeItemContext.get().setup(id, sourceModel, clientItem, bakingContext);
        for (ModifierEntry<ModelModifier.ModifyItemAfterBake> entry : afterBakeItemModifiers) {
            try {
                model = entry.modifier.modifyItemModelAfterBake(model, context);
            } catch (Throwable t) {
                LOGGER.error("After-bake-item modifier {} from plugin {} threw an exception", entry.modifier, entry.owningPlugin, t);
            }
        }
        return model;
    }

    private record PluginEntry(Identifier id, ModelLoadingPlugin plugin) {}

    private record ModifierEntry<T extends ModelModifier>(Identifier owningPlugin, ModelModifier.Phase phase, T modifier) {}

    private static final class BlockBeforeBakeContextImpl implements ModelModifier.ModifyBlockBeforeBake.Context {
        @UnknownNullability
        private BlockState state;
        @UnknownNullability
        private ModelBaker baker;

        @Override
        public BlockState state() {
            return this.state;
        }

        @Override
        public ModelBaker baker() {
            return this.baker;
        }

        public BlockBeforeBakeContextImpl setup(BlockState state, ModelBaker baker) {
            this.state = state;
            this.baker = baker;
            return this;
        }
    }

    private static final class BlockAfterBakeContextImpl implements ModelModifier.ModifyBlockAfterBake.Context {
        @UnknownNullability
        private BlockState state;
        private BlockStateModel.@UnknownNullability UnbakedRoot sourceModel;
        @UnknownNullability
        private ModelBaker baker;

        @Override
        public BlockState state() {
            return this.state;
        }

        @Override
        public BlockStateModel.UnbakedRoot sourceModel() {
            return this.sourceModel;
        }

        @Override
        public ModelBaker baker() {
            return this.baker;
        }

        public BlockAfterBakeContextImpl setup(BlockState state, BlockStateModel.UnbakedRoot sourceModel, ModelBaker baker) {
            this.state = state;
            this.sourceModel = sourceModel;
            this.baker = baker;
            return this;
        }
    }

    private static final class ItemBeforeBakeContextImpl implements ModelModifier.ModifyItemBeforeBake.Context {
        @UnknownNullability
        private Identifier id;
        @UnknownNullability
        private ClientItem clientItem;
        private ItemModel.@UnknownNullability BakingContext bakingContext;

        @Override
        public Identifier id() {
            return this.id;
        }

        @Override
        public ClientItem clientItem() {
            return this.clientItem;
        }

        @Override
        public ItemModel.BakingContext bakingContext() {
            return this.bakingContext;
        }

        public ItemBeforeBakeContextImpl setup(Identifier id, ClientItem clientItem, ItemModel.BakingContext bakingContext) {
            this.id = id;
            this.clientItem = clientItem;
            this.bakingContext = bakingContext;
            return this;
        }
    }

    private static final class ItemAfterBakeContextImpl implements ModelModifier.ModifyItemAfterBake.Context {
        @UnknownNullability
        private Identifier id;
        private ItemModel.@UnknownNullability Unbaked sourceModel;
        @UnknownNullability
        private ClientItem clientItem;
        private ItemModel.@UnknownNullability BakingContext bakingContext;

        @Override
        public Identifier id() {
            return this.id;
        }

        @Override
        public ItemModel.Unbaked sourceModel() {
            return this.sourceModel;
        }

        @Override
        public ClientItem clientItem() {
            return this.clientItem;
        }

        @Override
        public ItemModel.BakingContext bakingContext() {
            return this.bakingContext;
        }

        public ItemAfterBakeContextImpl setup(Identifier id, ItemModel.Unbaked sourceModel, ClientItem clientItem, ItemModel.BakingContext bakingContext) {
            this.id = id;
            this.sourceModel = sourceModel;
            this.clientItem = clientItem;
            this.bakingContext = bakingContext;
            return this;
        }
    }
}
