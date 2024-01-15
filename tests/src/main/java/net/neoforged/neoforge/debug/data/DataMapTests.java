package net.neoforged.neoforge.debug.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover.Default;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

import java.util.Objects;

@ForEachTest(groups = "data.data_map")
public class DataMapTests {

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if registry data maps work")
    static void testDataMap(final DynamicTest test, final RegistrationHelper reg) {
        final DataMapType<SomeObject, Item, Default<SomeObject, Item>> someData = DataMapType.builder(
                    new ResourceLocation(reg.modId(), "some_data"),
                    Registries.ITEM, SomeObject.CODEC
                )
                .synced(SomeObject.CODEC, true)
                .build();

        test.framework().modEventBus().addListener((final RegisterDataMapTypesEvent event) -> event.register(someData));

        reg.addProvider(event -> new DataMapProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected void gather() {
                builder(someData)
                        // Add to carrot and logs
                        .add(Items.CARROT.builtInRegistryHolder(), new SomeObject(14, "some_string"), false)
                        .add(ItemTags.LOGS, new SomeObject(156, "some other string"), false)

                        // But explicitly remove birch logs
                        .remove(Items.BIRCH_LOG.builtInRegistryHolder());
            }
        });

        // This is to make sure that sync work
        test.eventListeners().forge().addListener((final UseItemOnBlockEvent event) -> {
            if (event.getLevel().isClientSide() && event.getHand() == InteractionHand.MAIN_HAND) {
                event.getEntity().displayClientMessage(Component.literal("Attachment value: " + event.getItemStack().getItemHolder()
                        .getData(someData)), true);
            }
        });

        test.onGameTest(helper -> {
            final Registry<Item> registry = helper.getLevel().registryAccess()
                            .registryOrThrow(Registries.ITEM);
            helper.assertTrue(Objects.equals(registry.wrapAsHolder(Items.CARROT).getData(someData), new SomeObject(14, "some_string")), "Data wasn't attached to carrot!");

            // All logs but birch should have the value
            registry.getTagOrEmpty(ItemTags.LOGS)
                    .forEach(item -> {
                        if (item.value() != Items.BIRCH_LOG) {
                            helper.assertTrue(Objects.equals(item.getData(someData), new SomeObject(156, "some other string")), "Data wasn't attached to logs!");
                        }
                    });

            helper.assertTrue(registry.wrapAsHolder(Items.BIRCH_LOG).getData(someData) == null, "Data was attached to birch!");

            helper.succeed();
        });
    }

    public record SomeObject(
            int intValue,
            String stringValue
    ) {
        public static final Codec<SomeObject> CODEC = RecordCodecBuilder.create(in -> in.group(
                Codec.INT.fieldOf("intValue").forGetter(SomeObject::intValue),
                Codec.STRING.fieldOf("stringValue").forGetter(SomeObject::stringValue)
        ).apply(in, SomeObject::new));
    }
}
