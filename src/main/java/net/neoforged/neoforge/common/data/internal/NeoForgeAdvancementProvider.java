/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.PlayerInteractTrigger;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.advancements.packs.VanillaAdvancementProvider;
import net.minecraft.data.advancements.packs.VanillaHusbandryAdvancements;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.ToolAction;
import net.neoforged.neoforge.common.ToolActions;
import net.neoforged.neoforge.common.advancements.critereon.PiglinCurrencyItemPredicate;
import net.neoforged.neoforge.common.advancements.critereon.PiglinNeutralArmorEntityPredicate;
import net.neoforged.neoforge.common.advancements.critereon.ToolActionItemPredicate;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class NeoForgeAdvancementProvider extends AdvancementProvider {
    public NeoForgeAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, getVanillaAdvancementProviders(output, registries));
    }

    private static List<AdvancementGenerator> getVanillaAdvancementProviders(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        List<BiFunction<Criterion<?>, HolderLookup.Provider, Criterion<?>>> criteriaReplacers = new ArrayList<>();
        criteriaReplacers.add(replaceMatchToolCriteria(ToolActions.AXE_WAX_OFF, getPrivateValue(VanillaHusbandryAdvancements.class, null, "WAX_SCRAPING_TOOLS")));
        criteriaReplacers.add(replaceInteractCriteria(ItemPredicate.Builder.item().withSubPredicate(PiglinCurrencyItemPredicate.TYPE, PiglinCurrencyItemPredicate.INSTANCE).build(), PiglinAi.BARTERING_ITEM));
        criteriaReplacers.add(replaceWearingPredicate(PiglinNeutralArmorEntityPredicate.INSTANCE.toVanilla(), predicate -> {
            if (predicate.head().filter(item -> predicateMatches(item, Items.GOLDEN_HELMET)).isPresent()) {
                return true;
            } else if (predicate.chest().filter(item -> predicateMatches(item, Items.GOLDEN_CHESTPLATE)).isPresent()) {
                return true;
            } else if (predicate.legs().filter(item -> predicateMatches(item, Items.GOLDEN_LEGGINGS)).isPresent()) {
                return true;
            }
            return predicate.feet().filter(item -> predicateMatches(item, Items.GOLDEN_BOOTS)).isPresent();
        }));

        List<AdvancementSubProvider> subProviders = getPrivateValue(net.minecraft.data.advancements.AdvancementProvider.class, VanillaAdvancementProvider.create(output, registries), "subProviders");
        return subProviders.stream()
                .<AdvancementGenerator>map(vanillaProvider -> new NeoForgeAdvancementGenerator(vanillaProvider, criteriaReplacers))
                .toList();
    }

    private static BiFunction<Criterion<?>, HolderLookup.Provider, Criterion<?>> replaceMatchToolCriteria(ToolAction toolAction, ItemLike... targetItem) {
        UnaryOperator<LootItemCondition> replacer = condition -> {
            if (condition instanceof MatchTool toolMatch && toolMatch.predicate().filter(predicate -> predicateMatches(predicate, targetItem)).isPresent()) {
                return new MatchTool(Optional.of(ItemPredicate.Builder.item().withSubPredicate(ToolActionItemPredicate.TYPE, new ToolActionItemPredicate(toolAction)).build()));
            }
            return null;
        };
        return (criterion, registries) -> {
            if (criterion.trigger() instanceof ItemUsedOnLocationTrigger trigger && criterion.triggerInstance() instanceof ItemUsedOnLocationTrigger.TriggerInstance instance) {
                ContextAwarePredicate newLocation = replaceConditions(instance.location().orElse(null), replacer, condition -> false);
                if (newLocation != null) {
                    return new Criterion<>(trigger, new ItemUsedOnLocationTrigger.TriggerInstance(instance.player(), Optional.of(newLocation)));
                }
            }
            return null;
        };
    }

    private static BiFunction<Criterion<?>, HolderLookup.Provider, Criterion<?>> replaceInteractCriteria(ItemPredicate replacement, ItemLike... targetItem) {
        return (criterion, registries) -> {
            if (criterion.trigger() instanceof PlayerInteractTrigger trigger && criterion.triggerInstance() instanceof PlayerInteractTrigger.TriggerInstance instance) {
                if (instance.item().filter(predicate -> predicateMatches(predicate, targetItem)).isPresent()) {
                    return new Criterion<>(trigger, new PlayerInteractTrigger.TriggerInstance(instance.player(), Optional.of(replacement), instance.entity()));
                }
            }
            return null;
        };
    }

    private static boolean predicateMatches(ItemPredicate predicate, ItemLike... targets) {
        Optional<HolderSet<Item>> items = predicate.items();
        if (items.isEmpty()) {
            return false;
        }
        HolderSet<Item> holders = items.get();
        for (ItemLike target : targets) {
            if (!holders.contains(target.asItem().builtInRegistryHolder())) {
                return false;
            }
        }
        return true;
    }

    private static BiFunction<Criterion<?>, HolderLookup.Provider, Criterion<?>> replaceWearingPredicate(EntityPredicate replacement, Predicate<EntityEquipmentPredicate> shouldReplace) {
        return replacePlayerPredicate(condition -> {
            if (condition instanceof InvertedLootItemCondition inverted) {
                if (inverted.term() instanceof LootItemEntityPropertyCondition entityPropertyCondition) {
                    Optional<EntityPredicate> predicate = entityPropertyCondition.predicate();
                    if (predicate.isPresent()) {
                        EntityPredicate entityPredicate = predicate.get();
                        if (entityPredicate.equipment().filter(shouldReplace).isPresent()) {
                            //Note: We as currently there are no issues, we just skip any cases where other fields in the entity predicate are present
                            return LootItemEntityPropertyCondition.hasProperties(entityPropertyCondition.entityTarget(), replacement).invert().build();
                        }
                    }
                }
            }
            return null;
        }, condition -> true);//Skip any additional replacements as we know they would be duplicates
    }

    private static BiFunction<Criterion<?>, HolderLookup.Provider, Criterion<?>> replacePlayerPredicate(UnaryOperator<LootItemCondition> replacer, Predicate<LootItemCondition> shouldSkipReplacement) {
        return (criterion, registries) -> {
            if (criterion.triggerInstance() instanceof SimpleCriterionTrigger.SimpleInstance simpleInstance) {
                ContextAwarePredicate newPlayer = replaceConditions(simpleInstance.player().orElse(null), replacer, shouldSkipReplacement);
                if (newPlayer != null) {
                    return replacePlayerPredicate((Criterion) criterion, newPlayer, registries);
                }
            }
            return null;
        };
    }

    private static <T extends SimpleCriterionTrigger.SimpleInstance> Criterion<T> replacePlayerPredicate(Criterion<T> old, ContextAwarePredicate newPlayer, HolderLookup.Provider registries) {
        Codec<T> codec = old.trigger().codec();
        RegistryOps<JsonElement> registryops = registries.createSerializationContext(JsonOps.INSTANCE);
        return Util.getOrThrow(codec.encodeStart(registryops, old.triggerInstance())
                .flatMap(element -> {
                    if (element instanceof JsonObject object && object.has("player")) {
                        object.add("player", Util.getOrThrow(ContextAwarePredicate.CODEC.encodeStart(registryops, newPlayer), error -> new IllegalStateException("Unable to serialize new player predicate")));
                        return codec.parse(registryops, object);
                    }
                    return DataResult.error(() -> "Serialized instance does not contain a 'player' element");
                })
                .map(old.trigger()::createCriterion),
                error -> new IllegalStateException("Unable to convert criterion serialization and replacement"));
    }

    @Nullable
    private static ContextAwarePredicate replaceConditions(@Nullable ContextAwarePredicate basePredicate, UnaryOperator<LootItemCondition> replacer, Predicate<LootItemCondition> shouldSkipReplacement) {
        if (basePredicate == null) {
            return null;
        }
        List<LootItemCondition> conditions = getPrivateValue(ContextAwarePredicate.class, basePredicate, "conditions");
        if (!conditions.isEmpty()) {
            boolean shouldReplace = false;
            List<LootItemCondition> clonedConditions = new ArrayList<>(conditions.size());
            for (LootItemCondition condition : conditions) {
                LootItemCondition replacement = replacer.apply(condition);
                if (replacement != null) {
                    if (shouldReplace && shouldSkipReplacement.test(replacement)) {
                        continue;
                    }
                    shouldReplace = true;
                    condition = replacement;
                }
                clonedConditions.add(condition);
            }
            if (shouldReplace) {
                return ContextAwarePredicate.create(clonedConditions.toArray(LootItemCondition[]::new));
            }
        }
        return null;
    }

    private static <T, C> T getPrivateValue(Class<C> clazz, @Nullable C inst, String name) {
        T value = ObfuscationReflectionHelper.getPrivateValue(clazz, inst, name);
        if (value == null) {
            throw new IllegalStateException(clazz.getName() + " is missing field " + name);
        }
        return value;
    }

    private record NeoForgeAdvancementGenerator(AdvancementSubProvider vanillaProvider, List<BiFunction<Criterion<?>, HolderLookup.Provider, Criterion<?>>> criteriaReplacers) implements AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
            vanillaProvider.generate(registries, advancementHolder -> {
                Advancement.Builder newBuilder = findAndReplaceInHolder(advancementHolder, registries);
                if (newBuilder != null) {
                    newBuilder.save(saver, advancementHolder.id(), existingFileHelper);
                }
            });
        }

        @Nullable
        private Advancement.Builder findAndReplaceInHolder(AdvancementHolder advancementHolder, HolderLookup.Provider registries) {
            Advancement advancement = advancementHolder.value();
            Advancement.Builder builder = Advancement.Builder.advancement();
            boolean hasReplaced = false;
            for (var entry : advancement.criteria().entrySet()) {
                Criterion<?> criterion = entry.getValue();
                for (var criteriaReplacer : criteriaReplacers) {
                    Criterion<?> replacedCriterion = criteriaReplacer.apply(criterion, registries);
                    if (replacedCriterion != null) {
                        hasReplaced = true;
                        criterion = replacedCriterion;
                        //Don't break out, but instead continue going allowing applying replacers to our already replaced criteria
                        //This allows for different replacers to replace different parts of the criteria
                    }
                }
                builder.addCriterion(entry.getKey(), criterion);
            }
            if (!hasReplaced) {
                return null;
            }
            advancement.parent().ifPresent(builder::parent);
            advancement.display().ifPresent(builder::display);
            builder.rewards(advancement.rewards());
            builder.requirements(advancement.requirements());
            if (advancement.sendsTelemetryEvent()) {
                builder.sendsTelemetryEvent();
            }
            return builder;
        }
    }
}
