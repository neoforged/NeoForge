/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.I18NParser;
import net.neoforged.fml.IBindingsProvider;
import net.neoforged.neoforge.common.I18nExtension;
import net.neoforged.neoforge.common.NeoForge;

public class NeoForgeBindings implements IBindingsProvider {
    @Override
    public Supplier<IEventBus> getForgeBusSupplier() {
        return () -> NeoForge.EVENT_BUS;
    }

    @Override
    public Supplier<I18NParser> getMessageParser() {
        return () -> new I18NParser() {
            @Override
            public String parseMessage(final String i18nMessage, final Object... args) {
                return I18nExtension.parseMessage(i18nMessage, args);
            }

            @Override
            public String stripControlCodes(final String toStrip) {
                return I18nExtension.stripControlCodes(toStrip);
            }
        };
    }
}
