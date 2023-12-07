/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.client;

import java.util.List;
import java.util.function.BooleanSupplier;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.RegisterGuiOverlaysEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.testframework.conf.ClientConfiguration;
import net.neoforged.testframework.impl.FrameworkClient;
import net.neoforged.testframework.impl.MutableTestFramework;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FrameworkClientImpl implements FrameworkClient {
    private final MutableTestFramework impl;
    private final ClientConfiguration configuration;

    public FrameworkClientImpl(MutableTestFramework impl, ClientConfiguration clientConfiguration) {
        this.impl = impl;
        this.configuration = clientConfiguration;
    }

    @Override
    public void init(IEventBus modBus, ModContainer container) {
        final String keyCategory = "key.categories." + impl.id().getNamespace() + "." + impl.id().getPath();

        final BooleanSupplier overlayEnabled;
        if (configuration.toggleOverlayKey() != 0) {
            final ToggleKeyMapping overlayKey = new ToggleKeyMapping("key.testframework.toggleoverlay", configuration.toggleOverlayKey(), keyCategory, () -> true);
            modBus.addListener((final RegisterKeyMappingsEvent event) -> event.register(overlayKey));
            overlayEnabled = () -> !overlayKey.isDown();
        } else {
            overlayEnabled = () -> true;
        }

        modBus.addListener((final RegisterGuiOverlaysEvent event) -> event.registerAboveAll(impl.id().getPath(), new TestsOverlay(impl, overlayEnabled)));

        if (configuration.openManagerKey() != 0) {
            final KeyMapping openManagerKey = new KeyMapping("key.testframework.openmanager", configuration.openManagerKey(), keyCategory) {
                @Override
                public void setDown(boolean pValue) {
                    if (pValue) {
                        Minecraft.getInstance().setScreen(new TestScreen(
                                Component.literal("All tests"), impl, List.copyOf(impl.tests().allGroups())));
                    }
                    super.setDown(pValue);
                }
            };
            modBus.addListener((final RegisterKeyMappingsEvent event) -> event.register(openManagerKey));
        }
    }

    public static final class Factory implements FrameworkClient.Factory {

        @Override
        public FrameworkClient create(MutableTestFramework impl, ClientConfiguration clientConfiguration) {
            return new FrameworkClientImpl(impl, clientConfiguration);
        }
    }
}
