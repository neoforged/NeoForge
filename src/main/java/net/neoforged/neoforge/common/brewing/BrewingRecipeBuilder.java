package net.neoforged.neoforge.common.brewing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

public abstract class BrewingRecipeBuilder {
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    private final Set<ICondition> conditions = new LinkedHashSet<>();

    public BrewingRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    public BrewingRecipeBuilder when(ICondition condition) {
        this.conditions.add(condition);
        return this;
    }

    public void save(RecipeOutput output) {
        this.save(output, getDefaultRecipeId());
    }

    public void save(RecipeOutput output, String name) {
        ResourceLocation defaultId = getDefaultRecipeId();
        ResourceLocation id = new ResourceLocation(name);
        if (id.equals(defaultId)) {
            throw new IllegalStateException("Recipe " + name + " should remove its 'save' argument as it is equal to default one");
        } else {
            this.save(output, id);
        }
    }

    public void save(RecipeOutput output, ResourceLocation id) {
        Advancement.Builder advancementBuilder = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancementBuilder::addCriterion);
        output.accept(id, build(), advancementBuilder.build(id.withPrefix("recipes/brewing/")), conditions.toArray(ICondition[]::new));
    }

    public abstract IBrewingRecipe build();

    public abstract ResourceLocation getDefaultRecipeId();

    public static Mixing mixing(Potion in, ItemLike catalyst, Potion out) {
        return mixing().withPotionIn(in).withCatalyst(catalyst).withPotionOut(out);
    }

    public static Mixing mixing(Potion in, TagKey<Item> catalyst, Potion out) {
        return mixing().withPotionIn(in).withCatalyst(catalyst).withPotionOut(out);
    }

    public static Mixing mixing(Potion in, Ingredient catalyst, Potion out) {
        return mixing().withPotionIn(in).withCatalyst(catalyst).withPotionOut(out);
    }

    public static Mixing mixing() {
        return new Mixing();
    }

    public static Container container(ItemLike input, ItemLike catalyst, ItemStack output) {
        return container().withInput(input).withCatalyst(catalyst).withOutput(output);
    }

    public static Container container(ItemLike input, ItemLike catalyst, ItemLike output) {
        return container().withInput(input).withCatalyst(catalyst).withOutput(output);
    }

    public static Container container(TagKey<Item> input, TagKey<Item> catalyst, ItemStack output) {
        return container().withInput(input).withCatalyst(catalyst).withOutput(output);
    }

    public static Container container(TagKey<Item> input, TagKey<Item> catalyst, ItemLike output) {
        return container().withInput(input).withCatalyst(catalyst).withOutput(output);
    }

    public static Container container(ItemLike input, TagKey<Item> catalyst, ItemLike output) {
        return container().withInput(input).withCatalyst(catalyst).withOutput(output);
    }

    public static Container container(Ingredient input, Ingredient catalyst, ItemStack output) {
        return container().withInput(input).withCatalyst(catalyst).withOutput(output);
    }

    public static Container container(Ingredient input, Ingredient catalyst, ItemLike output) {
        return container().withInput(input).withCatalyst(catalyst).withOutput(output);
    }

    public static Container container(ItemLike input, Ingredient catalyst, ItemLike output) {
        return container().withInput(input).withCatalyst(catalyst).withOutput(output);
    }

    public static Container container() {
        return new Container();
    }
    public static Simple simple() {
        return new Simple();
    }

    @Nullable
    protected static ResourceLocation getKeyForIngredient(Ingredient ingredient) {
        Ingredient.Value[] values = ingredient.getValues();
        if (values.length == 1) {
            if (values[0] instanceof Ingredient.ItemValue item) {
                return BuiltInRegistries.ITEM.getKey(item.item().getItem());
            } else if (values[0] instanceof Ingredient.TagValue tag) {
                return tag.tag().location();
            }
        }
        return null;
    }

    protected static ResourceLocation composeKey(ResourceLocation[] in, ResourceLocation[] out) {
        String namespace = Stream.concat(Arrays.stream(in), Arrays.stream(out))
                .filter(Objects::nonNull)
                .map(ResourceLocation::getNamespace)
                .filter(Predicate.not(Predicate.isEqual(ResourceLocation.DEFAULT_NAMESPACE)))
                .findFirst()
                .orElse(ResourceLocation.DEFAULT_NAMESPACE);
        String inPath = Arrays.stream(in)
                .filter(Objects::nonNull)
                .map(ResourceLocation::getPath)
                .collect(Collectors.joining("_"));
        String outPath = Arrays.stream(out)
                .filter(Objects::nonNull)
                .map(ResourceLocation::getPath)
                .collect(Collectors.joining("_"));
        return new ResourceLocation(namespace, inPath.isEmpty() ? outPath : inPath + "_to_" + outPath);
    }

    public static class Mixing extends BrewingRecipeBuilder {
        private final List<MobEffectInstance> effectsIn = new ArrayList<>();
        private final List<MobEffectInstance> effectsOut = new ArrayList<>();
        private Potion potionIn = Potions.EMPTY;
        private Potion potionOut = Potions.EMPTY;
        private Ingredient catalyst = null;

        @Override
        public Mixing unlockedBy(String name, Criterion<?> criterion) {
            super.unlockedBy(name, criterion);
            return this;
        }

        @Override
        public Mixing when(ICondition condition) {
            super.when(condition);
            return this;
        }

        public Mixing withPotionIn(Potion in) {
            this.potionIn = in;
            return this;
        }

        public Mixing withEffectIn(MobEffectInstance in) {
            this.effectsIn.add(in);
            return this;
        }

        public Mixing withEffectsIn(List<MobEffectInstance> in) {
            this.effectsIn.clear();
            this.effectsIn.addAll(in);
            return this;
        }

        public Mixing withEffectsIn(MobEffectInstance... in) {
            return this.withEffectsIn(Arrays.asList(in));
        }

        public Mixing withCatalyst(Ingredient catalyst) {
            this.catalyst = catalyst;
            return this;
        }

        public Mixing withCatalyst(ItemLike catalyst) {
            return this.withCatalyst(Ingredient.of(catalyst));
        }

        public Mixing withCatalyst(TagKey<Item> catalyst) {
            return this.withCatalyst(Ingredient.of(catalyst));
        }

        public Mixing withPotionOut(Potion out) {
            this.potionOut = out;
            return this;
        }

        public Mixing withEffectOut(MobEffectInstance out) {
            this.effectsOut.add(out);
            return this;
        }

        public Mixing withEffectsOut(List<MobEffectInstance> out) {
            this.effectsOut.clear();
            this.effectsOut.addAll(out);
            return this;
        }

        public Mixing withEffectsOut(MobEffectInstance... out) {
            return this.withEffectsOut(Arrays.asList(out));
        }

        @Override
        public MixingBrewingRecipe build() {
            if (this.catalyst == null) {
                throw new IllegalArgumentException("catalyst must not be null");
            }
            return new MixingBrewingRecipe(this.potionIn, this.effectsIn, this.catalyst, this.potionOut, this.effectsOut);
        }

        @Override
        public ResourceLocation getDefaultRecipeId() {
            ResourceLocation inKey = BuiltInRegistries.POTION.getKey(this.potionIn);
            ResourceLocation catalystKey = getKeyForIngredient(this.catalyst);
            ResourceLocation outKey = BuiltInRegistries.POTION.getKey(this.potionOut);
            return composeKey(new ResourceLocation[]{inKey, catalystKey}, new ResourceLocation[]{outKey});
        }
    }
    
    public static class Container extends BrewingRecipeBuilder {
        private Ingredient input;
        private Ingredient catalyst;
        private ItemStack output;

        @Override
        public Container unlockedBy(String name, Criterion<?> criterion) {
            super.unlockedBy(name, criterion);
            return this;
        }

        @Override
        public Container when(ICondition condition) {
            super.when(condition);
            return this;
        }

        public Container withInput(Ingredient input) {
            this.input = input;
            return this;
        }

        public Container withInput(ItemLike input) {
            return this.withInput(Ingredient.of(input));
        }

        public Container withInput(TagKey<Item> input) {
            return this.withInput(Ingredient.of(input));
        }

        public Container withCatalyst(Ingredient catalyst) {
            this.catalyst = catalyst;
            return this;
        }

        public Container withCatalyst(ItemLike catalyst) {
            return this.withCatalyst(Ingredient.of(catalyst));
        }

        public Container withCatalyst(TagKey<Item> catalyst) {
            return this.withCatalyst(Ingredient.of(catalyst));
        }

        public Container withOutput(ItemStack output) {
            this.output = output;
            return this;
        }

        public Container withOutput(ItemLike output) {
            this.output = new ItemStack(output);
            return this;
        }

        @Override
        public ContainerBrewingRecipe build() {
            if (this.input == null) {
                throw new IllegalArgumentException("input must not be null");
            }
            if (this.catalyst == null) {
                throw new IllegalArgumentException("catalyst must not be null");
            }
            if (this.output == null) {
                throw new IllegalArgumentException("output must not be null");
            }
            return new ContainerBrewingRecipe(this.input, this.catalyst, this.output);
        }

        @Override
        public ResourceLocation getDefaultRecipeId() {
            ResourceLocation inputKey = getKeyForIngredient(this.input);
            ResourceLocation catalystKey = getKeyForIngredient(this.catalyst);
            ResourceLocation outputKey = BuiltInRegistries.ITEM.getKey(this.output.getItem());
            return composeKey(new ResourceLocation[]{inputKey, catalystKey}, new ResourceLocation[]{outputKey});
        }
    }

    public static class Simple extends BrewingRecipeBuilder {
        private Ingredient input;
        private Ingredient catalyst;
        private ItemStack output;

        @Override
        public Simple unlockedBy(String name, Criterion<?> criterion) {
            super.unlockedBy(name, criterion);
            return this;
        }

        @Override
        public Simple when(ICondition condition) {
            super.when(condition);
            return this;
        }

        public Simple withInput(Ingredient input) {
            this.input = input;
            return this;
        }

        public Simple withInput(ItemLike input) {
            return this.withInput(Ingredient.of(input));
        }

        public Simple withInput(TagKey<Item> input) {
            return this.withInput(Ingredient.of(input));
        }

        public Simple withCatalyst(Ingredient catalyst) {
            this.catalyst = catalyst;
            return this;
        }

        public Simple withCatalyst(ItemLike catalyst) {
            return this.withCatalyst(Ingredient.of(catalyst));
        }

        public Simple withCatalyst(TagKey<Item> catalyst) {
            return this.withCatalyst(Ingredient.of(catalyst));
        }

        public Simple withOutput(ItemStack output) {
            this.output = output;
            return this;
        }

        public Simple withOutput(ItemLike output) {
            return this.withOutput(new ItemStack(output));
        }

        @Override
        public SimpleBrewingRecipe build() {
            if (this.input == null) {
                throw new IllegalArgumentException("input must not be null");
            }
            if (this.catalyst == null) {
                throw new IllegalArgumentException("catalyst must not be null");
            }
            if (this.output == null) {
                throw new IllegalArgumentException("output must not be null");
            }
            return new SimpleBrewingRecipe(this.input, this.catalyst, this.output);
        }

        @Override
        public ResourceLocation getDefaultRecipeId() {
            ResourceLocation inputKey = getKeyForIngredient(this.input);
            ResourceLocation catalystKey = getKeyForIngredient(this.catalyst);
            ResourceLocation outputKey = BuiltInRegistries.ITEM.getKey(this.output.getItem());
            return composeKey(new ResourceLocation[]{inputKey, catalystKey}, new ResourceLocation[]{outputKey});
        }
    }
}
