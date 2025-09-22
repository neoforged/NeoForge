/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.neoforged.neoforge.common.CommonHooks;
import org.junit.jupiter.api.Test;

public final class EntityDataAccessorTests {
    private static abstract class NoopSyncedDataHolder implements SyncedDataHolder {
        @Override
        public final void onSyncedDataUpdated(final EntityDataAccessor<?> p_326288_) {}

        @Override
        public final void onSyncedDataUpdated(final List<SynchedEntityData.DataValue<?>> p_326334_) {}
    }

    public static final class NoIssuesHolder extends NoopSyncedDataHolder {
        static {
            CommonHooks.verifyEntityDataAccessorRegistration(NoIssuesHolder.class, NoIssuesHolder.class);
        }

        static void init() {}
    }

    public static final class CorrectMixinTarget extends NoopSyncedDataHolder {
        static {
            CommonHooks.verifyEntityDataAccessorRegistration(CorrectMixinTarget.class, CorrectMixinTarget.class);
        }

        static void init() {}
    }

    public static final class WrongMixinTarget extends NoopSyncedDataHolder {
        static {
            CommonHooks.verifyEntityDataAccessorRegistration(EntityDataAccessorTests.WrongMixinTarget.class, EntityDataAccessorTests.WrongMixinTarget.class);
        }

        static void init() {}
    }

    @Test
    void testThatNoErrorsAreThrownWhenSafeClassIsInitialized() {
        assertDoesNotThrow(NoIssuesHolder::init);
    }

    @Test
    void testExternalSyncedDataAccessorsAreCorrectlyDetected() {
        assertThrows(IllegalStateException.class, () -> CommonHooks.verifyEntityDataAccessorRegistration(EntityDataAccessor.class, NoIssuesHolder.class));
    }

    @Test
    void testMixinSyncedDataAccessorsAreCorrectlyDetected() {
        final var throwable = assertThrows(ExceptionInInitializerError.class, WrongMixinTarget::init);
        final var cause = throwable.getCause();
        assertNotNull(cause);
        assertInstanceOf(IllegalStateException.class, cause);
        assertTrue(() -> cause.getMessage().contains("EntityDataAccessorTestsWrongMixin"));
    }

    @Test
    void testOtherInjectedMixinFieldsAreIgnored() {
        assertDoesNotThrow(CorrectMixinTarget::init);
    }
}
