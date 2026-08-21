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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.predicates.DamagePredicate;
import net.minecraft.advancements.predicates.DamageSourcePredicate;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.entity.EntityEquipmentPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.predicates.entity.EntitySubPredicate;
import net.minecraft.advancements.predicates.entity.EntityTypePredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.triggers.PlayerHurtEntityTrigger;
import net.minecraft.advancements.triggers.PlayerInteractTrigger;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.advancements.packs.VanillaAdvancementProvider;
import net.minecraft.data.advancements.packs.VanillaHusbandryAdvancements;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.BootstrapContextAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;
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
import org.jspecify.annotations.Nullable;

public class NeoForgeAdvancementProvider extends AdvancementProvider {
    public NeoForgeAdvancementProvider() {
        super(getVanillaAdvancementProviders());
    }

    private static List<AdvancementSubProvider.Factory> getVanillaAdvancementProviders() {
        List<BiFunction<Criterion<?>, BootstrapContextAccess, @Nullable Criterion<?>>> criteriaReplacers = new ArrayList<>();
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
                helper.addSubPredicate(PiglinNeutralArmorEntityPredicate.CODEC, PiglinNeutralArmorEntityPredicate.INSTANCE);
                return true;
            }
            return false;
        }));
        criteriaReplacers.add(replacePlayerHurtEntityCriteria(helper -> {
            if (helper.clearTypeIfMatches(EntityTypes.TRIDENT)) {
                helper.addSubPredicate(TridentEntityPredicate.CODEC, TridentEntityPredicate.INSTANCE);
                return true;
            }
            return false;
        }));
        //Walk on powdered snow
        criteriaReplacers.add(replaceLootEntityPredicate(helper -> {
            if (helper.clearEquipmentIfMatches(predicate -> predicate.feet().filter(item -> predicateMatches(item, Items.LEATHER_BOOTS)).isPresent())) {
                helper.addSubPredicate(SnowBootsEntityPredicate.CODEC, SnowBootsEntityPredicate.INSTANCE);
                return true;
            }
            return false;
        }));

        List<AdvancementSubProvider.Factory> subProviders = getPrivateValue(AdvancementProvider.class, (AdvancementProvider) VanillaAdvancementProvider.create(), "subProviders");
        return subProviders.stream()
                .<AdvancementSubProvider.Factory>map(vanillaProvider -> ctx -> vanillaProvider.create(new AdvancementRewriteContext(ctx, criteriaReplacers)))
                .toList();
    }

    private static BiFunction<Criterion<?>, BootstrapContextAccess, @Nullable Criterion<?>> replaceMatchToolCriteria(ItemAbility itemAbility, ItemLike... targetItem) {
        Function<LootItemCondition, @Nullable LootItemCondition> replacer = condition -> {
            if (condition instanceof MatchTool(Optional<ItemPredicate> optPredicate) && optPredicate.filter(predicate -> predicateMatches(predicate, targetItem)).isPresent()) {
                return new MatchTool(Optional.of(ItemPredicate.Builder.item().withComponents(DataComponentMatchers.Builder.components().partial(ItemAbilityPredicate.TYPE, new ItemAbilityPredicate(itemAbility)).build()).build()));
            }
            return null;
        };
        return (criterion, _) -> {
            if (criterion.trigger() instanceof ItemUsedOnLocationTrigger trigger && criterion.triggerInstance() instanceof ItemUsedOnLocationTrigger.TriggerInstance(Optional<Holder<LootItemCondition>> player, Optional<Holder<LootItemCondition>> location)) {
                Holder<LootItemCondition> newLocation = replaceConditions(location.orElse(null), replacer, _ -> false);
                if (newLocation != null) {
                    return new Criterion<>(trigger, new ItemUsedOnLocationTrigger.TriggerInstance(player, Optional.of(newLocation)));
                }
            }
            return null;
        };
    }

    private static BiFunction<Criterion<?>, BootstrapContextAccess, @Nullable Criterion<?>> replaceInteractCriteria(ItemPredicate replacement, ItemLike... targetItem) {
        return (criterion, _) -> {
            if (criterion.trigger() instanceof PlayerInteractTrigger trigger && criterion.triggerInstance() instanceof PlayerInteractTrigger.TriggerInstance(Optional<Holder<LootItemCondition>> player, Optional<ItemPredicate> item, Optional<Holder<LootItemCondition>> entity)) {
                if (item.filter(predicate -> predicateMatches(predicate, targetItem)).isPresent()) {
                    return new Criterion<>(trigger, new PlayerInteractTrigger.TriggerInstance(player, Optional.of(replacement), entity));
                }
            }
            return null;
        };
    }

    private static BiFunction<Criterion<?>, BootstrapContextAccess, @Nullable Criterion<?>> replacePlayerHurtEntityCriteria(Predicate<EntityPredicateReplacementHelper> predicateHelper) {
        return (criterion, _) -> {
            if (criterion.trigger() instanceof PlayerHurtEntityTrigger trigger && criterion.triggerInstance() instanceof PlayerHurtEntityTrigger.TriggerInstance(Optional<Holder<LootItemCondition>> player, Optional<DamagePredicate> damage, Optional<Holder<LootItemCondition>> entity)) {
                if (damage.isPresent()) {
                    DamagePredicate damagePredicate = damage.get();
                    if (damagePredicate.type().isPresent()) {
                        DamageSourcePredicate sourcePredicate = damagePredicate.type().get();
                        if (sourcePredicate.directEntity().isPresent()) {
                            EntityPredicateReplacementHelper helper = new EntityPredicateReplacementHelper(sourcePredicate.directEntity().get());
                            if (predicateHelper.test(helper)) {
                                DamageSourcePredicate replacementSourcePredicate = new DamageSourcePredicate(sourcePredicate.tags(),
                                        Optional.of(helper.create()), sourcePredicate.sourceEntity(), sourcePredicate.isDirect());
                                DamagePredicate replacement = new DamagePredicate(damagePredicate.dealtDamage(), damagePredicate.takenDamage(), damagePredicate.sourceEntity(),
                                        damagePredicate.blocked(), Optional.of(replacementSourcePredicate));
                                return new Criterion<>(trigger, new PlayerHurtEntityTrigger.TriggerInstance(player, Optional.of(replacement), entity));
                            }
                        }
                    }
                }
            }
            return null;
        };
    }

    @SuppressWarnings("deprecation")
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

    private static BiFunction<Criterion<?>, BootstrapContextAccess, @Nullable Criterion<?>> replaceLootEntityPredicate(Predicate<EntityPredicateReplacementHelper> predicateHelper) {
        return replacePlayerPredicate(condition -> {
            boolean invert = false;
            if (condition instanceof InvertedLootItemCondition(Holder<LootItemCondition> term)) {
                condition = term.value();
                invert = true;
            }
            if (condition instanceof LootItemEntityPropertyCondition(Optional<EntityPredicate> predicate, LootContext.EntityTarget entityTarget)) {
                if (predicate.isPresent()) {
                    EntityPredicateReplacementHelper helper = new EntityPredicateReplacementHelper(predicate.get());
                    if (predicateHelper.test(helper)) {
                        LootItemCondition.Builder conditionBuilder = LootItemEntityPropertyCondition.hasProperties(entityTarget, helper.create());
                        if (invert) {
                            return conditionBuilder.invert().build();
                        }
                        return conditionBuilder.build();
                    }
                }
            }
            return null;
        }, _ -> true);//Skip any additional replacements as we know they would be duplicates
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static BiFunction<Criterion<?>, BootstrapContextAccess, @Nullable Criterion<?>> replacePlayerPredicate(Function<LootItemCondition, @Nullable LootItemCondition> replacer, Predicate<LootItemCondition> shouldSkipReplacement) {
        return (criterion, registries) -> {
            if (criterion.triggerInstance() instanceof SimpleCriterionTrigger.SimpleInstance simpleInstance) {
                Holder<LootItemCondition> newPlayer = replaceConditions(simpleInstance.player().orElse(null), replacer, shouldSkipReplacement);
                if (newPlayer != null) {
                    return replacePlayerPredicate((Criterion) criterion, newPlayer, registries);
                }
            }
            return null;
        };
    }

    private static <T extends SimpleCriterionTrigger.SimpleInstance> Criterion<T> replacePlayerPredicate(Criterion<T> old, Holder<LootItemCondition> newPlayer, BootstrapContextAccess registries) {
        Codec<T> codec = old.trigger().codec();
        RegistryOps<JsonElement> registryops = RegistryOps.create(JsonOps.INSTANCE, new RegistryOps.RegistryInfoLookup() {
            @Override
            public <E> Optional<HolderGetter<E>> lookup(ResourceKey<? extends Registry<? extends E>> registry) {
                return Optional.of(registries.lookup(registry));
            }
        });
        return codec.encodeStart(registryops, old.triggerInstance())
                .flatMap(element -> {
                    if (element instanceof JsonObject object && object.has("player")) {
                        object.add("player", LootItemCondition.CODEC.encodeStart(registryops, newPlayer).getOrThrow(_ -> new IllegalStateException("Unable to serialize new player predicate")));
                        return codec.parse(registryops, object);
                    }
                    return DataResult.error(() -> "Serialized instance does not contain a 'player' element");
                })
                .map(old.trigger()::createCriterion)
                .getOrThrow(error -> new IllegalStateException("Unable to convert criterion serialization and replacement: " + error));
    }

    @Nullable
    private static Holder<LootItemCondition> replaceConditions(@Nullable Holder<LootItemCondition> basePredicate, Function<LootItemCondition, @Nullable LootItemCondition> replacer, Predicate<LootItemCondition> shouldSkipReplacement) {
        if (basePredicate == null) {
            return null;
        }
        boolean invert = false;
        if (basePredicate.value() instanceof InvertedLootItemCondition(Holder<LootItemCondition> term)) {
            basePredicate = term;
            invert = true;
        }
        List<LootItemCondition> conditions = (switch (basePredicate.value()) {
            case AnyOfCondition any -> NeoForgeAdvancementProvider.<HolderSet<LootItemCondition>, CompositeLootItemCondition>getPrivateValue(CompositeLootItemCondition.class, any, "terms").unwrap().right().orElseThrow();
            case AllOfCondition all -> NeoForgeAdvancementProvider.<HolderSet<LootItemCondition>, CompositeLootItemCondition>getPrivateValue(CompositeLootItemCondition.class, all, "terms").unwrap().right().orElseThrow();
            default -> List.of(basePredicate);
        }).stream().map(Holder::value).toList();
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
                LootItemCondition finalCondition = switch (basePredicate.value()) {
                    case AnyOfCondition _ -> {
                        AnyOfCondition.Builder builder = AnyOfCondition.anyOf();
                        for (LootItemCondition condition : clonedConditions) {
                            builder.or(Holder.direct(condition));
                        }
                        yield builder.build();
                    }
                    case AllOfCondition _ -> AllOfCondition.allOf(HolderSet.direct(clonedConditions.stream().map(Holder::direct).toList()));
                    default -> clonedConditions.getFirst();
                };
                if (invert) {
                    finalCondition = new InvertedLootItemCondition(Holder.direct(finalCondition));
                }
                return Holder.direct(finalCondition);
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

    private record AdvancementRewriteContext(BootstrapContext<Advancement> baseContext, List<BiFunction<Criterion<?>, BootstrapContextAccess, @Nullable Criterion<?>>> criteriaReplacers) implements BootstrapContext<Advancement> {
        @Override
        public Holder.Reference<Advancement> register(ResourceKey<Advancement> key, Advancement value) {
            Advancement.Builder replaced = findAndReplace(value, baseContext);
            if (replaced != null) {
                return baseContext.register(key, replaced.build(key.identifier()).value());
            } else {
                return Holder.Reference.createStandAlone(lookup(Registries.ADVANCEMENT), key);
            }
        }

        @Override
        public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
            return baseContext.lookup(key);
        }

        @Override
        @Deprecated
        public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
            return baseContext.listContextElements(key);
        }

        @SuppressWarnings("removal")
        private Advancement.@Nullable Builder findAndReplace(Advancement advancement, BootstrapContextAccess registries) {
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
        private final Map<Codec<? extends EntitySubPredicate>, EntitySubPredicate> extra = new HashMap<>();
        private Optional<EntityTypePredicate> entityType;
        private Optional<EntityEquipmentPredicate> equipment;

        public EntityPredicateReplacementHelper(EntityPredicate source) {
            this.source = source;
            this.entityType = Optional.ofNullable(this.source.getPartIfExists(EntityTypePredicate.CODEC));
            this.equipment = Optional.ofNullable(this.source.getPartIfExists(EntityEquipmentPredicate.CODEC));
        }

        public boolean clearTypeIfMatches(EntityType<?> type) {
            if (entityType.isPresent() && entityType.get().matches(type.builtInRegistryHolder())) {
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

        public <T extends EntitySubPredicate> void addSubPredicate(Codec<T> key, T predicate) {
            extra.put(key, predicate);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        public EntityPredicate create() {
            var builder = EntityPredicate.Builder.from(source, c -> c != EntityEquipmentPredicate.CODEC && c != EntityTypePredicate.CODEC);
            if (entityType.isPresent()) {
                builder.entityType(entityType.orElseThrow());
            }
            if (equipment.isPresent()) {
                builder.equipment(equipment.orElseThrow());
            }
            extra.forEach((k, v) -> builder.put((Codec) k, v));
            return builder.build();
        }
    }
}
