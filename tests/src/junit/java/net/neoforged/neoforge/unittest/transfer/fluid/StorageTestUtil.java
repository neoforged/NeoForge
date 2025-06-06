package net.neoforged.neoforge.unittest.transfer.fluid;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.transfer.item.ItemVariant;
import net.neoforged.neoforge.transfer.storage.Storage;

import static org.assertj.core.api.Assertions.assertThat;

public final class StorageTestUtil {
    private StorageTestUtil() {
    }

    public static void assertInventory(Storage<ItemVariant> storage, Object... expectedContent) {
        assertThat(storage.size())
                .as("expected minimum storage size")
                .isGreaterThanOrEqualTo(expectedContent.length);

        for (int i = 0; i < expectedContent.length; i++) {
            var contained = storage.getResource(i);
            ItemStack expected;
            var expectedObj = expectedContent[i];
            if (expectedObj instanceof ItemStack stack) {
                expected = stack;
            } else if (expectedObj instanceof ItemLike item) {
                expected = new ItemStack(item.asItem());
            } else {
                throw new IllegalArgumentException("Unsupported expected inventory content: " + expectedObj);
            }
            assertThat(contained.getItem())
                    .as("item in slot %d", i)
                    .isSameAs(expected.getItem());
            assertThat(contained.getComponentsPatch())
                    .as("data components of item in slot %d", i)
                    .isEqualTo(expected.getComponentsPatch());
            assertThat(storage.getAmount(i))
                    .as("number of items in slot %d", i)
                    .isEqualTo(expected.getCount());
        }
        for (int i = expectedContent.length; i < storage.size(); i++) {
            assertThat(storage.getResource(i))
                    .as("slot %d should be empty", i)
                    .isEqualTo(ItemVariant.EMPTY);
        }
    }
}
