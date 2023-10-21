/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.data.loot.packs.VanillaLootTableProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.EntryGroup;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.entries.SequentialEntry;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.neoforged.neoforge.common.ToolActions;
import net.neoforged.neoforge.common.loot.CanToolPerformAction;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import org.jetbrains.annotations.Nullable;

/**
 * Currently used only for replacing shears item to shears_dig tool action
 */
public final class ForgeLootTableProvider extends LootTableProvider {
    private final List<Function<LootItemCondition, LootItemCondition.Builder>> conditionReplacers = new ArrayList<>();

    public ForgeLootTableProvider(PackOutput packOutput) {
        super(packOutput, Set.of(), VanillaLootTableProvider.create(packOutput).getTables());
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationcontext) {
        // Do not validate against all registered loot tables
    }

    @Override
    public List<LootTableProvider.SubProviderEntry> getTables() {
        replaceLootItemCondition(condition -> {
            if (condition instanceof MatchTool matchTool && checkMatchTool(matchTool, Items.SHEARS)) {
                return CanToolPerformAction.canToolPerformAction(ToolActions.SHEARS_DIG);
            }
            return null;
        });
        return super.getTables().stream().map(entry -> {
            // Provides new sub provider with filtering only changed loot tables and replacing condition item to condition tag
            return new LootTableProvider.SubProviderEntry(() -> replaceAndFilterChangesOnly(entry.provider().get()), entry.paramSet());
        }).collect(Collectors.toList());
    }

    private LootTableSubProvider replaceAndFilterChangesOnly(LootTableSubProvider subProvider) {
        return newConsumer -> subProvider.generate((resourceLocation, builder) -> {
            LootTable.Builder newBuilder = findAndReplaceInLootTableBuilder(builder);
            if (newBuilder != null) {
                newConsumer.accept(resourceLocation, newBuilder);
            }
        });
    }
    
    private void replaceLootItemCondition(Function<LootItemCondition, LootItemCondition.Builder> replacer) {
        conditionReplacers.add(replacer);
    }

    @Nullable
    private LootTable.Builder findAndReplaceInLootTableBuilder(LootTable.Builder builder) {
        LootTable lootTable = builder.build();

        Optional<ResourceLocation> randomSequence = getPrivateValue(LootTable.class, lootTable, "randomSequence");
        List<LootPool> lootPools = getPrivateValue(LootTable.class, lootTable, "pools");
        List<LootItemFunction> lootItemFunctions = getPrivateValue(LootTable.class, lootTable, "functions");

        boolean found = false;
        LootTable.Builder newBuilder = new LootTable.Builder();
        newBuilder.setParamSet(lootTable.getParamSet());
        randomSequence.ifPresent(newBuilder::setRandomSequence);
        for (LootItemFunction lootItemFunction : lootItemFunctions) {
            newBuilder.apply(() -> lootItemFunction);
        }

        for (LootPool lootPool : lootPools) {
            found |= findAndReplaceInLootPool(lootPool, newBuilder);
        }

        return found ? newBuilder : null;
    }

    private boolean findAndReplaceInLootPool(LootPool lootPool, LootTable.Builder newBuilder) {
        List<LootPoolEntryContainer> lootEntries = getPrivateValue(LootPool.class, lootPool, "entries");
        List<LootItemCondition> lootConditions = getPrivateValue(LootPool.class, lootPool, "conditions");
        List<LootItemFunction> lootFunctions = getPrivateValue(LootPool.class, lootPool, "functions");

        LootPool.Builder poolBuilder = new LootPool.Builder();
        poolBuilder.setRolls(lootPool.getRolls());
        poolBuilder.setBonusRolls(lootPool.getBonusRolls());
        poolBuilder.name(lootPool.getName());
        boolean found = false;

        for (LootPoolEntryContainer lootEntry : lootEntries) {
            found |= findAndReplaceInLootEntry(lootEntry, poolBuilder::add);
        }

        for (LootItemCondition lootCondition : lootConditions) {
            if (lootCondition instanceof InvertedLootItemCondition invertedLootItemCondition) {
                LootItemCondition invLootCondition = invertedLootItemCondition.term();

                Consumer<LootItemCondition.Builder> consumer = cond -> poolBuilder.when(InvertedLootItemCondition.invert(cond));
                if (invLootCondition instanceof CompositeLootItemCondition compositeLootItemCondition && findAndReplaceInComposite(compositeLootItemCondition, consumer)) {
                    found = true;
                } else {
                    found |= replaceCondition(invLootCondition, consumer);
                }
            } else {
                found |= replaceCondition(lootCondition, poolBuilder::when);
            }
        }

        for (LootItemFunction lootFunction : lootFunctions) {
            poolBuilder.apply(() -> lootFunction);
        }

        newBuilder.withPool(poolBuilder);

        return found;
    }

    private boolean findAndReplaceInParentedLootEntry(CompositeEntryBase entry, Consumer<LootPoolEntryContainer.Builder<?>> newBuilder) {
        List<LootPoolEntryContainer> lootEntries = getPrivateValue(CompositeEntryBase.class, entry, "children");

        boolean found = false;
        for (LootPoolEntryContainer lootEntry : lootEntries) {
            found |= findAndReplaceInLootEntry(lootEntry, newBuilder);
        }

        return found;
    }

    private boolean findAndReplaceInLootEntry(LootPoolEntryContainer entry, Consumer<LootPoolEntryContainer.Builder<?>> newBuilder) {
        List<LootItemCondition> lootConditions = getPrivateValue(LootPoolEntryContainer.class, entry, "conditions");

        boolean found = false;

        LootPoolEntryContainer.Builder<?> builder;
        if (entry instanceof CompositeEntryBase compositeEntryBase) {
            Consumer<LootPoolEntryContainer.Builder<?>> consumer;
            if (compositeEntryBase instanceof AlternativesEntry) {
                builder = new AlternativesEntry.Builder();
                consumer = builder::otherwise;
            } else if (compositeEntryBase instanceof SequentialEntry) {
                builder = new SequentialEntry.Builder();
                consumer = builder::then;
            } else if (compositeEntryBase instanceof EntryGroup) {
                builder = new EntryGroup.Builder();
                consumer = builder::append;
            } else {
                throw new IllegalStateException("Unknown CompositeEntryBase type: " + compositeEntryBase.getClass().getName());
            }
            found |= findAndReplaceInParentedLootEntry(compositeEntryBase, consumer);
        } else if (entry instanceof LootPoolSingletonContainer singleton) {
            if (singleton instanceof DynamicLoot dynamicLoot) {
                ResourceLocation name = getPrivateValue(DynamicLoot.class, dynamicLoot, "name");
                builder = DynamicLoot.dynamicEntry(name);
            } else if (singleton instanceof EmptyLootItem) {
                builder = EmptyLootItem.emptyItem();
            } else if (singleton instanceof LootItem lootItem) {
                Holder<Item> item = getPrivateValue(LootItem.class, lootItem, "item");
                builder = LootItem.lootTableItem(item.get());
            } else if (singleton instanceof TagEntry tagEntry) {
                TagKey<Item> tag = getPrivateValue(TagEntry.class, tagEntry, "tag");
                boolean expand = getPrivateValue(TagEntry.class, tagEntry, "expand");
                builder = expand ? TagEntry.expandTag(tag) : TagEntry.tagContents(tag);
            } else if (singleton instanceof LootTableReference reference) {
                ResourceLocation name = getPrivateValue(LootTableReference.class, reference, "name");
                builder = LootTableReference.lootTableReference(name);
            } else {
                throw new IllegalStateException("Unknown LootPoolSingletonContainer type: " + singleton.getClass().getName());
            }
            int weight = getPrivateValue(LootPoolSingletonContainer.class, singleton, "weight");
            int quality = getPrivateValue(LootPoolSingletonContainer.class, singleton, "quality");
            List<LootItemFunction> functions = getPrivateValue(LootPoolSingletonContainer.class, singleton, "functions");
            ((LootPoolSingletonContainer.Builder<?>)builder).setWeight(weight);
            ((LootPoolSingletonContainer.Builder<?>)builder).setQuality(quality);
            for (LootItemFunction function : functions) {
                ((LootPoolSingletonContainer.Builder<?>)builder).apply(() -> function);
            }
        } else {
            throw new IllegalStateException("Unknown LootPoolEntryContainer type: " + entry.getClass().getName());
        }

        for (LootItemCondition lootCondition : lootConditions) {
            if (lootCondition instanceof CompositeLootItemCondition composite && findAndReplaceInComposite(composite, builder::when)) {
                found = true;
            } else {
                found |= replaceCondition(lootCondition, builder::when);
            }
        }

        newBuilder.accept(builder);

        return found;
    }

    private boolean findAndReplaceInComposite(CompositeLootItemCondition alternative, Consumer<LootItemCondition.Builder> poolBuilder) {
        List<LootItemCondition> lootConditions = getPrivateValue(CompositeLootItemCondition.class, alternative, "terms");
        CompositeLootItemCondition.Builder builder = alternative instanceof AllOfCondition ? new AllOfCondition.Builder() : alternative instanceof AnyOfCondition ? new AnyOfCondition.Builder() : null;
        boolean found = false;

        for (LootItemCondition lootCondition : lootConditions) {
            found |= replaceCondition(lootCondition, builder::addTerm);
        }

        poolBuilder.accept(builder);

        return found;
    }

    private boolean checkMatchTool(MatchTool lootCondition, Item expected) {
        return lootCondition
                .predicate()
                .flatMap(ItemPredicate::items)
                .filter(holders -> holders.contains(expected.builtInRegistryHolder()))
                .isPresent();
    }

    private boolean replaceCondition(LootItemCondition lootCondition, Consumer<LootItemCondition.Builder> poolBuilder) {
        for (Function<LootItemCondition, LootItemCondition.Builder> conditionReplacer : conditionReplacers) {
            LootItemCondition.Builder newCondition = conditionReplacer.apply(lootCondition);
            if (newCondition != null) {
                poolBuilder.accept(newCondition);
                return true;
            }
        }
        poolBuilder.accept(() -> lootCondition);
        return false;
    }

    private <T, C> T getPrivateValue(Class<C> clazz, C inst, String name) {
        T value = ObfuscationReflectionHelper.getPrivateValue(clazz, inst, name);
        if (value == null) {
            throw new IllegalStateException(clazz.getName() + " is missing field " + name);
        }
        return value;
    }
}
