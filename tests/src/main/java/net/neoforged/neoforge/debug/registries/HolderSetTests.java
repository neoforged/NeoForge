/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.holdersets.AndHolderSet;
import net.neoforged.neoforge.registries.holdersets.AnyHolderSet;
import net.neoforged.neoforge.registries.holdersets.NotHolderSet;
import net.neoforged.neoforge.registries.holdersets.OrHolderSet;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

@ForEachTest(groups = HolderSetTests.GROUP)
public class HolderSetTests {
    public static final String GROUP = "registries.holderset";

    private static List<HolderSet<Item>> createTestHolderSets() {
        Holder<Item> beef = Items.BEEF.builtInRegistryHolder();
        Holder<Item> cod = Items.COD.builtInRegistryHolder();
        HolderLookup.RegistryLookup<Item> lookup = BuiltInRegistries.ITEM.asLookup();
        HolderSet<Item> singleton = HolderSet.direct(beef);
        HolderSet<Item> list = HolderSet.direct(beef, cod);
        HolderSet<Item> tag = BuiltInRegistries.ITEM.getOrCreateTag(ItemTags.FISHES);
        HolderSet<Item> any = new AnyHolderSet<>(lookup);
        HolderSet<Item> and = new AndHolderSet<>(list, tag);
        HolderSet<Item> or = new OrHolderSet<>(singleton, and);
        HolderSet<Item> not = new NotHolderSet<>(lookup, or);
        return List.of(singleton, list, tag, any, and, or, not);
    }

    private static <T> boolean areHolderSetsEqual(HolderSet<T> holderSet0, HolderSet<T> holderSet1) {
        return holderSet0.getClass() == holderSet1.getClass() && holderSet0.unwrap().equals(holderSet1.unwrap());
    }

    private static <T, A> void testHolderSetCodecWithOps(DynamicOps<T> ops, HolderLookup.Provider lookup, Codec<HolderSet<A>> codec, HolderSet<A> holderSet) {
        RegistryOps<T> registryOps = RegistryOps.create(ops, lookup);
        T encoded = codec.encodeStart(registryOps, holderSet).getOrThrow(EncoderException::new);
        HolderSet<A> decoded = codec.parse(registryOps, encoded).getOrThrow(DecoderException::new);
        if (!areHolderSetsEqual(holderSet, decoded)) {
            throw new GameTestAssertException(holderSet + " failed Codec test with "
                    + (ops.compressMaps() ? "compressed " + ops : ops)
                    + ", encoded result: " + encoded
                    + ", decoded result: " + decoded);
        }
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Test if HolderSetCodec can properly encode and decode HolderSet")
    public static void holderSetCodecTest(ExtendedGameTestHelper test) {
        List<HolderSet<Item>> holderSets = createTestHolderSets();
        Codec<HolderSet<Item>> codec = RegistryCodecs.homogeneousList(Registries.ITEM);
        HolderLookup.Provider lookup = test.getLevel().registryAccess();
        for (HolderSet<Item> holderSet : holderSets) {
            testHolderSetCodecWithOps(NbtOps.INSTANCE, lookup, codec, holderSet);
            testHolderSetCodecWithOps(JsonOps.INSTANCE, lookup, codec, holderSet);
            testHolderSetCodecWithOps(JsonOps.COMPRESSED, lookup, codec, holderSet);
        }
        test.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Test if StreamCodec created by ByteBufCodec#holderSet can properly encode and decode HolderSet")
    public static void holderSetStreamCodecTest(ExtendedGameTestHelper test) {
        List<HolderSet<Item>> holderSets = createTestHolderSets();
        StreamCodec<RegistryFriendlyByteBuf, HolderSet<Item>> streamCodec = ByteBufCodecs.holderSet(Registries.ITEM);
        RegistryAccess registryAccess = test.getLevel().registryAccess();
        for (HolderSet<Item> holderSet : holderSets) {
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
            streamCodec.encode(buf, holderSet);
            HolderSet<Item> decoded = streamCodec.decode(buf);
            if (!areHolderSetsEqual(holderSet, decoded)) {
                throw new GameTestAssertException(holderSet + " failed StreamCodec test, decoded result: " + decoded);
            }
        }
        test.succeed();
    }
}
