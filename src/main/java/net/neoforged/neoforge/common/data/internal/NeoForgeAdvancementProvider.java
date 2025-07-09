/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DamagePredicate;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.DataComponentMatchers;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.PlayerHurtEntityTrigger;
import net.minecraft.advancements.critereon.PlayerInteractTrigger;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.advancements.packs.VanillaAdvancementProvider;
import net.minecraft.data.advancements.packs.VanillaHusbandryAdvancements;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.advancements.critereon.ItemAbilityPredicate;
import net.neoforged.neoforge.common.advancements.critereon.PiglinCurrencyItemPredicate;
import net.neoforged.neoforge.common.advancements.critereon.PiglinNeutralArmorEntityPredicate;
import net.neoforged.neoforge.common.advancements.critereon.SnowBootsEntityPredicate;
import net.neoforged.neoforge.common.advancements.critereon.TridentEntityPredicate;
import org.jetbrains.annotations.Nullable;

public class NeoForgeAdvancementProvider extends AdvancementProvider {
    public NeoForgeAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, getVanillaAdvancementProviders(output, registries));
    }

    private static List<AdvancementSubProvider> getVanillaAdvancementProviders(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        List<BiFunction<Criterion<?>, HolderLookup.Provider, Criterion<?>>> criteriaReplacers = new ArrayList<>();
        criteriaReplacers.add(replaceMatchToolCriteria(ItemAbilities.AXE_WAX_OFF, getPrivateValue(VanillaHusbandryAdvancements.class, null, "WAX_SCRAPING_TOOLS")));
        criteriaReplacers.add(replaceInteractCriteria(ItemPredicate.Builder.item().withComponents(DataComponentMatchers.Builder.components().partial(ItemAbilityPredicate.TYPE, new ItemAbilityPredicate(ItemAbilities.SHEARS_REMOVE_ARMOR)).build()).build(), Items.SHEARS));
        criteriaReplacers.add(replaceInteractCriteria(ItemPredicate.Builder.item().withComponents(DataComponentMatchers.Builder.components().partial(PiglinCurrencyItemPredicate.TYPE, PiglinCurrencyItemPredicate.INSTANCE).build()).build(), PiglinAi.BARTERING_ITEM));
        criteriaReplacers.add(replaceLootEntityPredicate(helper -> {
            if (helper.clearEquipmentIfMatches(predicate -> {
                if (predicate.head().filter(item -> predicateMatches(item, ItemTags.PIGLIN_SAFE_ARMOR)).isPresent()) {
                    return true;
                } else if (predicate.chest().filter(item -> predicateMatches(item, ItemTags.PIGLIN_SAFE_ARMOR)).isPresent()) {
                    return true;
                } else if (predicate.legs().filter(item -> predicateMatches(item, ItemTags.PIGLIN_SAFE_ARMOR)).isPresent()) {
                    return true;
                }
                return predicate.feet().filter(item -> predicateMatches(item, ItemTags.PIGLIN_SAFE_ARMOR)).isPresent();
            })) {
                helper.replaceSubPredicate(PiglinNeutralArmorEntityPredicate.INSTANCE);
                return true;
            }
            return false;
        }));
        criteriaReplacers.add(replacePlayerHurtEntityCriteria(helper -> {
            if (helper.clearTypeIfMatches(EntityType.TRIDENT)) {
                helper.replaceSubPredicate(TridentEntityPredicate.INSTANCE);
                return true;
            }
            return false;
        }));
        //Walk on powdered snow
        criteriaReplacers.add(replaceLootEntityPredicate(helper -> {
            if (helper.clearEquipmentIfMatches(predicate -> predicate.feet().filter(item -> predicateMatches(item, Items.LEATHER_BOOTS)).isPresent())) {
                helper.replaceSubPredicate(SnowBootsEntityPredicate.INSTANCE);
                return true;
            }
            return false;
        }));

        List<AdvancementSubProvider> subProviders = getPrivateValue(AdvancementProvider.class, VanillaAdvancementProvider.create(output, registries), "subProviders");
        return subProviders.stream()
                .<AdvancementSubProvider>map(vanillaProvider -> new NeoForgeAdvancementGenerator(vanillaProvider, criteriaReplacers))
                .toList();
    }

    private static BiFunction<Criterion<?>, HolderLookup.Provider, Criterion<?>> replaceMatchToolCriteria(ItemAbility itemAbility, ItemLike... targetItem) {
        UnaryOperator<LootItemCondition> replacer = condition -> {
            if (condition instanceof MatchTool toolMatch && toolMatch.predicate().filter(predicate -> predicateMatches(predicate, targetItem)).isPresent()) {
                return new MatchTool(Optional.of(ItemPredicate.Builder.item().withComponents(DataComponentMatchers.Builder.components().partial(ItemAbilityPredicate.TYPE, new ItemAbilityPredicate(itemAbility)).build()).build()));
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

    private static BiFunction<Criterion<?>, HolderLookup.Provider, Criterion<?>> replacePlayerHurtEntityCriteria(Predicate<EntityPredicateReplacementHelper> predicateHelper) {
        return (criterion, registries) -> {
            if (criterion.trigger() instanceof PlayerHurtEntityTrigger trigger && criterion.triggerInstance() instanceof PlayerHurtEntityTrigger.TriggerInstance instance) {
                if (instance.damage().isPresent()) {
                    DamagePredicate damagePredicate = instance.damage().get();
                    if (damagePredicate.type().isPresent()) {
                        DamageSourcePredicate sourcePredicate = damagePredicate.type().get();
                        if (sourcePredicate.directEntity().isPresent()) {
                            EntityPredicateReplacementHelper helper = new EntityPredicateReplacementHelper(sourcePredicate.directEntity().get());
                            if (predicateHelper.test(helper)) {
                                DamageSourcePredicate replacementSourcePredicate = new DamageSourcePredicate(sourcePredicate.tags(),
                                        Optional.of(helper.create()), sourcePredicate.sourceEntity(), sourcePredicate.isDirect());
                                DamagePredicate replacement = new DamagePredicate(damagePredicate.dealtDamage(), damagePredicate.takenDamage(), damagePredicate.sourceEntity(),
                                        damagePredicate.blocked(), Optional.of(replacementSourcePredicate));
                                return new Criterion<>(trigger, new PlayerHurtEntityTrigger.TriggerInstance(instance.player(), Optional.of(replacement), instance.entity()));
                            }
                        }
                    }
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

    private static boolean predicateMatches(ItemPredicate predicate, TagKey<Item> tagKey) {
        return predicate.items().orElse(HolderSet.empty())
                .unwrapKey()
                .map(k -> k == tagKey)
                .orElse(false);
    }

    private static BiFunction<Criterion<?>, HolderLookup.Provider, Criterion<?>> replaceLootEntityPredicate(Predicate<EntityPredicateReplacementHelper> predicateHelper) {
        return replacePlayerPredicate(condition -> {
            boolean invert = false;
            if (condition instanceof InvertedLootItemCondition inverted) {
                condition = inverted.term();
                invert = true;
            }
            if (condition instanceof LootItemEntityPropertyCondition entityPropertyCondition) {
                Optional<EntityPredicate> predicate = entityPropertyCondition.predicate();
                if (predicate.isPresent()) {
                    EntityPredicateReplacementHelper helper = new EntityPredicateReplacementHelper(predicate.get());
                    if (predicateHelper.test(helper)) {
                        LootItemCondition.Builder conditionBuilder = LootItemEntityPropertyCondition.hasProperties(entityPropertyCondition.entityTarget(), helper.create());
                        if (invert) {
                            return conditionBuilder.invert().build();
                        }
                        return conditionBuilder.build();
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
        return codec.encodeStart(registryops, old.triggerInstance())
                .flatMap(element -> {
                    if (element instanceof JsonObject object && object.has("player")) {
                        object.add("player", ContextAwarePredicate.CODEC.encodeStart(registryops, newPlayer).getOrThrow(error -> new IllegalStateException("Unable to serialize new player predicate")));
                        return codec.parse(registryops, object);
                    }
                    return DataResult.error(() -> "Serialized instance does not contain a 'player' element");
                })
                .map(old.trigger()::createCriterion)
                .getOrThrow(error -> new IllegalStateException("Unable to convert criterion serialization and replacement: " + error));
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

    private record NeoForgeAdvancementGenerator(AdvancementSubProvider vanillaProvider, List<BiFunction<Criterion<?>, HolderLookup.Provider, Criterion<?>>> criteriaReplacers) implements AdvancementSubProvider {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver) {
            // Warning: ugly code here.
            // Wrap the registries to allow using any tag. This is used to make decoding using the codec work in `replacePlayerPredicate`.
            var registriesWithAnyTag = new HolderLookup.Provider() {
                @Override
                public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
                    return registries.listRegistryKeys();
                }

                @Override
                public <T> Optional<? extends HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> p_256285_) {
                    return registries.lookup(p_256285_);
                }

                @Override
                public <V> RegistryOps<V> createSerializationContext(DynamicOps<V> p_326817_) {
                    return RegistryOps.create(p_326817_, new RegistryOps.RegistryInfoLookup() {
                        @Override
                        public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> registry) {
                            var builtInRegistry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(registry.location());
                            return registries.lookup(registry)
                                    // Need to pass the builtin registry as the holder owner to make deserialization work!
                                    .map(lookup -> new RegistryOps.RegistryInfo<>(builtInRegistry, new HolderLookup.RegistryLookup.Delegate<>() {
                                        @Override
                                        public RegistryLookup<T> parent() {
                                            return lookup;
                                        }

                                        @Override
                                        public boolean canSerializeIn(HolderOwner<T> p_255875_) {
                                            return parent().canSerializeIn(p_255875_);
                                        }

                                        @Override
                                        public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
                                            var ret = Delegate.super.get(tagKey);
                                            if (ret.isEmpty()) {
                                                ret = Optional.of(HolderSet.emptyNamed(lookup, tagKey));
                                            }
                                            return ret;
                                        }
                                    }, lookup.registryLifecycle()));
                        }
                    });
                }
            };
            vanillaProvider.generate(registriesWithAnyTag, advancementHolder -> {
                Advancement.Builder newBuilder = findAndReplaceInHolder(advancementHolder, registriesWithAnyTag);
                if (newBuilder != null) {
                    newBuilder.save(saver, advancementHolder.id());
                }
            });
        }

        @Nullable
        @SuppressWarnings("removal")
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

    private static class EntityPredicateReplacementHelper {
        private final EntityPredicate source;
        private Optional<EntityTypePredicate> entityType;
        private Optional<EntityEquipmentPredicate> equipment;
        private Optional<EntitySubPredicate> subPredicate;

        public EntityPredicateReplacementHelper(EntityPredicate source) {
            this.source = source;
            this.entityType = this.source.entityType();
            this.equipment = this.source.equipment();
            this.subPredicate = this.source.subPredicate();
        }

        public boolean clearTypeIfMatches(EntityType<?> type) {
            if (entityType.isPresent() && entityType.get().matches(type)) {
                entityType = Optional.empty();
                return true;
            }
            return false;
        }

        public boolean clearEquipmentIfMatches(Predicate<EntityEquipmentPredicate> shouldReplace) {
            if (equipment.isPresent() && shouldReplace.test(equipment.get())) {
                equipment = Optional.empty();
                return true;
            }
            return false;
        }

        public void replaceSubPredicate(EntitySubPredicate predicate) {
            if (subPredicate.isPresent()) {
                throw new IllegalStateException("Attempting to replace an entity predicate that already has a sub predicate");
            }
            subPredicate = Optional.of(predicate);
        }

        public EntityPredicate create() {
            return new EntityPredicate(
                    entityType,
                    source.distanceToPlayer(),
                    source.movement(),
                    source.location(),
                    source.effects(),
                    source.nbt(),
                    source.flags(),
                    equipment,
                    subPredicate,
                    source.periodicTick(),
                    source.vehicle(),
                    source.passenger(),
                    source.targetedEntity(),
                    source.team(),
                    source.slots(),
                    source.components());
        }
    }
}
