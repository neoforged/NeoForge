package net.neoforged.neoforge.debug.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.advancements.critereon.ICustomItemPredicate;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod("custom_predicate_test")
public class CustomPredicateTest {
    public static final String MOD_ID = "custom_predicate_test";

    private static final DeferredRegister<Codec<? extends ICustomItemPredicate>> ITEM_PREDICATE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.ITEM_PREDICATE_SERIALIZERS, MOD_ID);
    private static final Supplier<? extends Codec<CustomNamePredicate>> CUSTOM_NAME_PREDICATE = ITEM_PREDICATE_SERIALIZERS.register("custom_name",
            () -> {
                Codec<CustomNamePredicate> codec = RecordCodecBuilder.create(g -> g.group(
                        Codec.INT.fieldOf("data1").forGetter(CustomNamePredicate::data1),
                        Codec.INT.fieldOf("data2").forGetter(CustomNamePredicate::data2)
                ).apply(g, CustomNamePredicate::new));
                return codec.xmap(Function.identity(), Function.identity());
            });

    public CustomPredicateTest() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEM_PREDICATE_SERIALIZERS.register(modBus);
        modBus.addListener(CustomPredicateTest::generateData);
    }

    public static void generateData(GatherDataEvent event) {
        var gen = event.getGenerator();
        var advancementProvider = new AdvancementProvider(
                gen.getPackOutput(),
                event.getLookupProvider(),
                event.getExistingFileHelper(),
                List.of(new AdvancementGenerator()));
        // Rename to avoid conflict with another testmod.
        // Would be good to have a better solution.
        var renamedProvider = new DataProvider() {
            @Override
            public CompletableFuture<?> run(CachedOutput cachedOutput) {
                return advancementProvider.run(cachedOutput);
            }

            @Override
            public String getName() {
                return MOD_ID + "/" + advancementProvider.getName();
            }
        };
        gen.addProvider(event.includeServer(), renamedProvider);
    }

    public record CustomNamePredicate(int data1, int data2) implements ICustomItemPredicate {
        @Override
        public Codec<? extends ICustomItemPredicate> codec() {
            return CUSTOM_NAME_PREDICATE.get();
        }

        @Override
        public boolean test(ItemStack itemStack) {
            return itemStack.hasCustomHoverName();
        }
    }

    public record AdvancementGenerator() implements AdvancementProvider.AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
            Advancement.Builder.advancement()
                    .parent(new ResourceLocation("story/root"))
                    .display(Items.ANVIL, Component.literal("Named!"), Component.literal("Get a named item"), null, FrameType.TASK, true, true, false)
                    .addCriterion("has_named_item", InventoryChangeTrigger.TriggerInstance.hasItems(new CustomNamePredicate(1, 2).toVanilla()))
                    .save(saver, new ResourceLocation(MOD_ID, "named_item"), existingFileHelper);
        }
    }
}
