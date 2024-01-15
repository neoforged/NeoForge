/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.filters;

import com.google.common.collect.ImmutableMap;
import io.netty.channel.ChannelPipeline;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.network.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class NetworkFilters {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Map<String, Function<Connection, DynamicChannelHandler>> instances = ImmutableMap.of(
            "neoforge:vanilla_filter", manager -> new VanillaConnectionNetworkFilter(),
            "neoforge:forge_fixes", GenericPacketSplitter::new);

    public static void injectIfNecessary(Connection manager) {
        cleanIfNecessary(manager);

        ChannelPipeline pipeline = manager.channel().pipeline();
        if (pipeline.get("packet_handler") == null)
            return; // Realistically this can only ever be null if the connection was prematurely closed due to an error. We return early here to reduce further log spam.

        instances.forEach((key, filterFactory) -> {
            DynamicChannelHandler filter = filterFactory.apply(manager);
            if (filter.isNecessary(manager)) {
                pipeline.addBefore("packet_handler", key, filter);
                LOGGER.debug("Injected {} into {}", filter, manager);
            }
        });
    }

    public static void cleanIfNecessary(Connection manager) {
        ChannelPipeline pipeline = manager.channel().pipeline();
        if (pipeline.get("packet_handler") == null)
            return; // Realistically this can only ever be null if the connection was prematurely closed due to an error. We return early here to reduce further log spam.

        //Grab the pipeline filters to remove in a seperate list to avoid a ConcurrentModificationException
        final List<DynamicChannelHandler> toRemove = pipeline.names()
                .stream()
                .map(pipeline::get)
                .filter(DynamicChannelHandler.class::isInstance)
                .map(DynamicChannelHandler.class::cast)
                .toList();

        toRemove.forEach(pipeline::remove);
    }

    private NetworkFilters() {}

}
