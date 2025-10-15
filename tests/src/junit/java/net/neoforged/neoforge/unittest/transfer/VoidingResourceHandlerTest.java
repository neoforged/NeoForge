/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.VoidingResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.junit.jupiter.api.Test;

public class VoidingResourceHandlerTest {
    ResourceHandler<TestResource> handler = new VoidingResourceHandler<>(TestResource.EMPTY);

    @Test
    public void testSizeIsOne() {
        assertEquals(1, handler.size());
    }

    @Test
    public void testIsValid() {
        assertTrue(ResourceHandlerUtil.isValid(handler, TestResource.SOME));
    }

    @Test
    public void testGetCapacityAsLong() {
        assertEquals(Long.MAX_VALUE, handler.getCapacityAsLong(0, TestResource.EMPTY));
    }

    @Test
    public void testGetAmountAsLong() {
        assertEquals(0, handler.getAmountAsLong(0));
    }

    @Test
    public void testGetResource() {
        assertEquals(TestResource.EMPTY, handler.getResource(0));
    }

    @Test
    public void testInsertOperation() {
        try (var transaction = Transaction.openRoot()) {
            assertEquals(Integer.MAX_VALUE, handler.insert(0, TestResource.SOME, Integer.MAX_VALUE, transaction));
        }
    }

    @Test
    public void testInsertWithoutIndexOperation() {
        try (var transaction = Transaction.openRoot()) {
            assertEquals(Integer.MAX_VALUE, handler.insert(TestResource.SOME, Integer.MAX_VALUE, transaction));
        }
    }

    @Test
    public void testExtractOperation() {
        try (var transaction = Transaction.openRoot()) {
            assertEquals(0, handler.extract(0, TestResource.SOME, Integer.MAX_VALUE, transaction));
            assertEquals(0, handler.extract(TestResource.SOME, Integer.MAX_VALUE, transaction));
        }
    }

    @Test
    public void testExtractWithoutIndexOperation() {
        try (var transaction = Transaction.openRoot()) {
            assertEquals(0, handler.extract(TestResource.SOME, Integer.MAX_VALUE, transaction));
        }
    }
}
