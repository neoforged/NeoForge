/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.world.item;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.capabilities.CapabilityProvider;
import net.neoforged.neoforge.common.util.TextTable;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStackSimple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(LazyCapabilitiesOnItemsTest.MOD_ID)
public class LazyCapabilitiesOnItemsTest {
    public static final String MOD_ID = "lazy_capabilities_on_items";

    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean ENABLED = true;
    private static final int SAMPLE_SIZE = 100000;

    private static final List<ItemStack> WARMUP_RESULTS = new ArrayList<>(SAMPLE_SIZE * 4);
    private static final List<ItemStack> NO_CAPS_DISABLED_RESULTS = new ArrayList<>(SAMPLE_SIZE);
    private static final List<ItemStack> WITH_CAPS_DISABLED_RESULTS = new ArrayList<>(SAMPLE_SIZE);
    private static final List<ItemStack> NO_CAPS_ENABLED_RESULTS = new ArrayList<>(SAMPLE_SIZE);
    private static final List<ItemStack> WITH_CAPS_ENABLED_RESULTS = new ArrayList<>(SAMPLE_SIZE);

    public LazyCapabilitiesOnItemsTest(final IEventBus modBus) {
        if (!ENABLED)
            return;

        modBus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        try {
            final Field supportsFlagField = CapabilityProvider.class.getDeclaredField("SUPPORTS_LAZY_CAPABILITIES");
            supportsFlagField.setAccessible(true);
            supportsFlagField.set(null, false);

            final Stopwatch timer = Stopwatch.createUnstarted();
            final IEventBus bus = NeoForge.EVENT_BUS;

            final ResourceLocation testCapId = new ResourceLocation(MOD_ID, "test");
            final Consumer<AttachCapabilitiesEvent<ItemStack>> capAttachmentHandler = e -> {
                //Example capability we make everything a bucket :D
                e.addCapability(testCapId, new FluidHandlerItemStackSimple(e.getObject(), SAMPLE_SIZE));
            };

            //Warmup:
            for (int i = 0; i < (SAMPLE_SIZE); i++) {
                WARMUP_RESULTS.add(new ItemStack(Items.WATER_BUCKET));
            }

            ///First test: SAMPLE_SIZE itemstacks which do not have a capability attached.
            timer.start();
            for (int i = 0; i < SAMPLE_SIZE; i++) {
                NO_CAPS_DISABLED_RESULTS.add(new ItemStack(Items.WATER_BUCKET));
            }
            timer.stop();

            final long simpleNoCapsLazyDisabledElapsed = timer.elapsed(TimeUnit.MICROSECONDS);
            timer.reset();

            ///Second test: SAMPLE_SIZE itemstacks with a capability attached.
            bus.addGenericListener(ItemStack.class, capAttachmentHandler);
            //Warmup:
            for (int i = 0; i < (SAMPLE_SIZE); i++) {
                WARMUP_RESULTS.add(new ItemStack(Items.WATER_BUCKET));
            }

            timer.start();
            for (int i = 0; i < SAMPLE_SIZE; i++) {
                WITH_CAPS_DISABLED_RESULTS.add(new ItemStack(Items.WATER_BUCKET));
            }
            timer.stop();

            final long withCapsLazyDisabledElapsed = timer.elapsed(TimeUnit.MICROSECONDS);
            timer.reset();
            bus.unregister(capAttachmentHandler);

            ///Third test: SAMPLE_SIZE itemstacks which do not have a capability attached.
            supportsFlagField.set(null, true);
            //Warmup:
            for (int i = 0; i < (SAMPLE_SIZE); i++) {
                WARMUP_RESULTS.add(new ItemStack(Items.WATER_BUCKET));
            }

            timer.start();
            for (int i = 0; i < SAMPLE_SIZE; i++) {
                NO_CAPS_ENABLED_RESULTS.add(new ItemStack(Items.WATER_BUCKET));
            }
            timer.stop();

            final long simpleNoCapsLazyEnabledElapsed = timer.elapsed(TimeUnit.MICROSECONDS);
            timer.reset();

            ///Fourth test: SAMPLE_SIZE itemstacks with a capability attached.
            bus.addGenericListener(ItemStack.class, capAttachmentHandler);
            //Warmup:
            for (int i = 0; i < (SAMPLE_SIZE); i++) {
                WARMUP_RESULTS.add(new ItemStack(Items.WATER_BUCKET));
            }

            timer.start();
            for (int i = 0; i < SAMPLE_SIZE; i++) {
                WITH_CAPS_ENABLED_RESULTS.add(new ItemStack(Items.WATER_BUCKET));
            }
            timer.stop();

            final long withCapsLazyEnabledElapsed = timer.elapsed(TimeUnit.MICROSECONDS);
            timer.reset();
            bus.unregister(capAttachmentHandler);

            TextTable table = new TextTable(Lists.newArrayList(
                    TextTable.column("Test type", TextTable.Alignment.LEFT),
                    TextTable.column("Total time", TextTable.Alignment.CENTER)));

            table.add("Lazy: Disabled / Caps: None", simpleNoCapsLazyDisabledElapsed + " ms.");
            table.add("Lazy: Disabled / Caps: One", withCapsLazyDisabledElapsed + " ms.");
            table.add("Lazy: Enabled  / Caps: None", simpleNoCapsLazyEnabledElapsed + " ms.");
            table.add("Lazy: Enabled  / Caps: One", withCapsLazyEnabledElapsed + " ms.");

            final String[] resultData = table.build("\n").split("\n");
            for (final String line : resultData) {
                LOGGER.warn(line);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.error("Failed to run capabilities on items test!");
            throw new IllegalStateException(e);
        }
    }
}
