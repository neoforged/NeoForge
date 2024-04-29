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
import net.minecraft.network.HandlerNames;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class NetworkFilters {
    private static final Logger LOGGER = LogManager.getLogger();

    // TODO: Expose custom packet filters to mods
    private static final Map<String, Function<ConnectionType, DynamicChannelHandler>> instances = ImmutableMap.of(
            "neoforge:vanilla_filter", VanillaConnectionNetworkFilter::new,
            GenericPacketSplitter.CHANNEL_HANDLER_NAME, connectionType -> new GenericPacketSplitter());

    public static void injectIfNecessary(Connection manager) {
        cleanIfNecessary(manager);

        // Inject the filters right "after" the encoder (but "before" the unbundler if it exists).
        // Because Netty processes the pipeline last-to-first when encoding,
        // this means that they will be processed before the encoder.

        ChannelPipeline pipeline = manager.channel().pipeline();
        if (pipeline.get(HandlerNames.ENCODER) == null)
            return; // Realistically this can only ever be null if the connection was prematurely closed due to an error. We return early here to reduce further log spam.

        var connectionType = NetworkRegistry.getConnectionType(manager);
        instances.forEach((key, filterFactory) -> {
            DynamicChannelHandler filter = filterFactory.apply(connectionType);
            if (filter.isNecessary(manager)) {
                pipeline.addAfter(HandlerNames.ENCODER, key, filter);
                LOGGER.debug("Injected {} into {}", filter, manager);
            }
        });
    }

    public static void cleanIfNecessary(Connection manager) {
        ChannelPipeline pipeline = manager.channel().pipeline();

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
