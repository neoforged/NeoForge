/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.templates.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.InfiniteResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.VoidResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InstancedHandlerTests {
    @Test
    void emptyHandlers() {
        //EMPTY no operation handlers but should throw on index calls
        testEmptyHandler(EmptyResourceHandler.instance(), ItemResource.EMPTY);
        testEmptyHandler(EmptyResourceHandler.instance(), FluidResource.EMPTY);
    }

    @Test
    public void voidHandlers() {
        //VoidResourceHandlers destroys resources but doesn't allow extraction
        testVoidResource(VoidResourceHandler.ITEM, ItemResource.EMPTY);
        testVoidResource(VoidResourceHandler.FLUID, FluidResource.EMPTY);
    }

    @Test
    public void endlessHandlers() {
        //InfiniteResourceHandlers creates infinite of a specified resource, but doesn't allow insertion
        testEndlessResource(FluidResource.of(Fluids.WATER), FluidResource.EMPTY);
        testEndlessResource(ItemResource.of(Blocks.COBBLESTONE), ItemResource.EMPTY);
    }

    private static <T extends IResource> void testVoidResource(VoidResourceHandler<T> handler, T emptyResource) {
        Assertions.assertThat(handler.size()).withFailMessage("Size should be 1").isEqualTo(1);
        Assertions.assertThat(handler.supportsExtraction()).withFailMessage("Extraction should be not be allowed").isFalse().isEqualTo(handler.supportsExtraction(0));
        Assertions.assertThat(handler.supportsInsertion()).withFailMessage("Insertion should be be allowed").isTrue().isEqualTo(handler.supportsInsertion(0));
        Assertions.assertThat(ResourceHandlerUtil.isValid(handler, emptyResource)).withFailMessage("Voids should always be valid for a resource").isTrue();
        Assertions.assertThat(handler.getCapacity(0, emptyResource)).withFailMessage("Voids should hava capacity of max int").isEqualTo(Integer.MAX_VALUE);
        Assertions.assertThat(handler.getResource(0)).withFailMessage("Empty resource should match").isEqualTo(emptyResource);

        try (var transaction = TransactionManager.open(null)) {
            Assertions.assertThat(Integer.MAX_VALUE)
                    .withFailMessage("Void should be able to accept infinite resources")
                    .isEqualTo(handler.insert(0, emptyResource, Integer.MAX_VALUE, transaction))
                    .isEqualTo(handler.insert(emptyResource, Integer.MAX_VALUE, transaction));

            Assertions.assertThat(0)
                    .withFailMessage("Void should be provide no resources")
                    .isEqualTo(handler.extract(0, emptyResource, Integer.MAX_VALUE, transaction))
                    .isEqualTo(handler.extract(emptyResource, Integer.MAX_VALUE, transaction));
        }
    }

    private static <T extends IResource> void testEndlessResource(T resource, T emptyResource) {
        InfiniteResourceHandler<T> handler = new InfiniteResourceHandler<>(resource);
        Assertions.assertThat(handler.size()).withFailMessage("Size should be 1").isEqualTo(1);
        Assertions.assertThat(handler.supportsExtraction()).withFailMessage("Extraction should be allowed").isTrue().isEqualTo(handler.supportsExtraction(0));
        Assertions.assertThat(handler.supportsInsertion()).withFailMessage("Insertion should not be allowed").isFalse().isEqualTo(handler.supportsInsertion(0));
        Assertions.assertThat(ResourceHandlerUtil.isValid(handler, resource)).withFailMessage("Endless resource can not be inserted into, thus isValid should be false").isFalse();
        Assertions.assertThat(Integer.MAX_VALUE).withFailMessage("Capacity should be max int")
                .isEqualTo(handler.getCapacity(0, resource))
                .isEqualTo(handler.getCapacity(0, emptyResource));

        Assertions.assertThat(resource).withFailMessage("Resources should match").isEqualTo(handler.getResource(0));

        try (var transaction = TransactionManager.open(null)) {
            Assertions.assertThat(0)
                    .withFailMessage("InfiniteHandler shouldn't be insertable into")
                    .isEqualTo(handler.insert(0, resource, 1, transaction))
                    .isEqualTo(handler.insert(resource, 1, transaction));

            Assertions.assertThat(1337)
                    .withFailMessage("InfiniteHandler should provide as much as we desire")
                    .isEqualTo(handler.extract(0, resource, 1337, transaction))
                    .isEqualTo(handler.extract(resource, 1337, transaction));
        }
    }

    private static <T extends IResource> void testEmptyHandler(EmptyResourceHandler<T> handler, T emptyResource) {
        Assertions.assertThat(handler.size()).withFailMessage("Size should be 0").isEqualTo(0);
        emptyHandlerThrow(() -> {
            try (var transaction = TransactionManager.open(null)) {
                handler.extract(0, emptyResource, 1, transaction);
            }
        });
        emptyHandlerThrow(() -> {
            try (var transaction = TransactionManager.open(null)) {
                handler.insert(0, emptyResource, 1, transaction);
            }
        });
        emptyHandlerThrow(() -> handler.getAmount(0));
        emptyHandlerThrow(() -> handler.getCapacity(0, emptyResource));
        emptyHandlerThrow(() -> handler.getResource(0));
        emptyHandlerThrow(() -> handler.getAmountAsLong(0));
        emptyHandlerThrow(() -> handler.getCapacityAsLong(0, emptyResource));
    }

    private static void emptyHandlerThrow(ThrowableAssert.ThrowingCallable callable) {
        Assertions.assertThatThrownBy(callable).withFailMessage("Empty handlers should throw when using index methods").isInstanceOf(IllegalArgumentException.class);
    }
}
