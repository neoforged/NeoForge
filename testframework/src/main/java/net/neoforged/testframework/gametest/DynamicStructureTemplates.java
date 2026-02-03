/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.gametest;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.loader.TemplateSource;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.testframework.impl.ReflectionUtils;

public class DynamicStructureTemplates {
    // StructureTemplateManager#sources
    private static final String SOURCES_FIELD = "sources";

    private final Map<Identifier, Supplier<StructureTemplate>> templates = new ConcurrentHashMap<>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setup(StructureTemplateManager manager, DataFixer fixerUpper, HolderGetter<Block> blockLookup) {
        final List<TemplateSource> sources = new ArrayList(ReflectionUtils.<List>getInstanceField(manager, SOURCES_FIELD));
        sources.add(new DynamicTemplateSource(fixerUpper, blockLookup));
        ObfuscationReflectionHelper.setPrivateValue(StructureTemplateManager.class, manager, sources, SOURCES_FIELD);

        LogUtils.getLogger().debug("Injected dynamic template source in manager {}", manager);
    }

    private Optional<StructureTemplate> load(Identifier location) {
        final Supplier<StructureTemplate> sup = templates.get(location);
        if (sup == null) return Optional.empty();
        return Optional.of(sup.get());
    }

    private Stream<Identifier> list() {
        return templates.keySet().stream();
    }

    public boolean contains(Identifier id) {
        return templates.containsKey(id);
    }

    public void register(Identifier id, Supplier<StructureTemplate> template) {
        templates.put(id, template);
    }

    public void register(Identifier id, StructureTemplate template) {
        register(id, () -> template);
    }

    private final class DynamicTemplateSource extends TemplateSource {
        DynamicTemplateSource(DataFixer fixerUpper, HolderGetter<Block> blockLookup) {
            super(fixerUpper, blockLookup);
        }

        @Override
        public Optional<StructureTemplate> load(Identifier id) {
            return DynamicStructureTemplates.this.load(id);
        }

        @Override
        public Stream<Identifier> list() {
            return DynamicStructureTemplates.this.list();
        }
    }
}
