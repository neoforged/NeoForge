package net.neoforged.neoforge.junittest;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.junit.utils.EphemeralTestServerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

public class TestMePls {

    @Test
    void test() {
        Assertions.assertEquals(
                Items.ACACIA_LEAVES,
                BuiltInRegistries.ITEM.get(new ResourceLocation("acacia_leaves"))
        );
        assert new ItemStack(Items.PUMPKIN_SEEDS).is(Items.PUMPKIN_SEEDS);
    }

    @Test
    @ExtendWith(EphemeralTestServerProvider.class)
    void testWServer(final MinecraftServer server) {
        org.assertj.core.api.Assertions.assertThat(server.getTickCount())
                .isGreaterThan(12);
    }
}
