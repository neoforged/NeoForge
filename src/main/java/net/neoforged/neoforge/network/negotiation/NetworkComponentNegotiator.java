/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.negotiation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * Negotiates the network components between the server and client.
 */
@ApiStatus.Internal
public class NetworkComponentNegotiator {

    /**
     * Negotiates the network components between the server and client.
     * <p>
     * The following rules are followed:
     * <ul>
     * <li>Any component that is optional on the client but is not present on the server is removed from the client's list.</li>
     * <li>Any component that is optional on the server but is not present on the client is removed from the server's list.</li>
     * <li>If the client has none optional components that are not present on the server, then negotiation fails</li>
     * <li>If the server has none optional components that are not present on the client, then negotiation fails</li>
     * <li>For each of the matching channels the following is executed:</li>
     * <ul>
     * <li>Check if packet flow directions are set, and if at least one is set match it to the other, by missing or wrong value fail the negotiation.</li>
     * <li>Check if both sides have the same version, or none set.</li>
     * </ul>
     * <li>At this point the channels are considered compatible, pick the servers version. It does not matter what side is picked since either both have the same version, or no version at all.</li>
     * </ul>
     * </p>
     * <p>
     * If negotiation succeeds then a list of agreed upon channels and their versions is returned.
     * </p>
     * <p>
     * If negotiation fails then a {@link Component} is returned with the reason for failure.
     * </p>
     *
     * @param server The list of server components that the server wishes to use for communication.
     * @param client The list of client components that the client wishes to use for communication.
     * @return A {@link NegotiationResult} that contains the agreed upon channels and their versions if negotiation succeeded, or a {@link Component} with the reason for failure if negotiation failed.
     */
    public static NegotiationResult negotiate(List<NegotiableNetworkComponent> server, List<NegotiableNetworkComponent> client) {
        //Ensure the inputs are modifiable
        server = new ArrayList<>(server);
        client = new ArrayList<>(client);

        List<NegotiableNetworkComponent> finalServer = server;
        final List<NegotiableNetworkComponent> disabledOptionalOnClient = client.stream()
                .filter(NegotiableNetworkComponent::optional)
                .filter(c -> finalServer.stream().noneMatch(c2 -> c2.id().equals(c.id())))
                .toList();

        client.removeAll(disabledOptionalOnClient);

        List<NegotiableNetworkComponent> finalClient = client;
        final List<NegotiableNetworkComponent> disabledOptionalOnServer = server.stream()
                .filter(NegotiableNetworkComponent::optional)
                .filter(c -> finalClient.stream().noneMatch(c2 -> c2.id().equals(c.id())))
                .toList();

        server.removeAll(disabledOptionalOnServer);

        Table<ResourceLocation, NegotiableNetworkComponent, NegotiableNetworkComponent> matches = HashBasedTable.create();
        server.forEach(s -> finalClient.forEach(c -> {
            if (s.id().equals(c.id())) {
                matches.put(s.id(), s, c);
            }
        }));

        client.removeIf(c -> matches.containsRow(c.id()));
        server.removeIf(c -> matches.containsRow(c.id()));

        if (!client.isEmpty()) {
            final Map<ResourceLocation, Component> failureReasons = Maps.newHashMap();
            client.forEach(c -> failureReasons.put(c.id(), Component.translatable("neoforge.network.negotiation.failure.missing.client.server")));
            return new NegotiationResult(List.of(), false, failureReasons);
        }

        if (!server.isEmpty()) {
            final Map<ResourceLocation, Component> failureReasons = Maps.newHashMap();
            server.forEach(c -> failureReasons.put(c.id(), Component.translatable("neoforge.network.negotiation.failure.missing.server.client")));
            return new NegotiationResult(List.of(), false, failureReasons);
        }

        final List<NegotiatedNetworkComponent> result = Lists.newArrayList();
        final Map<ResourceLocation, Component> failureReasons = Maps.newHashMap();
        for (Table.Cell<ResourceLocation, NegotiableNetworkComponent, NegotiableNetworkComponent> resourceLocationNegotiableNetworkComponentNegotiableNetworkComponentCell : matches.cellSet()) {
            final NegotiableNetworkComponent serverComponent = resourceLocationNegotiableNetworkComponentNegotiableNetworkComponentCell.getColumnKey();
            final NegotiableNetworkComponent clientComponent = resourceLocationNegotiableNetworkComponentNegotiableNetworkComponentCell.getValue();

            Optional<ComponentNegotiationResult> serverToClientComparison = validateComponent(serverComponent, clientComponent, "client");
            if (serverToClientComparison.isPresent() && !serverToClientComparison.get().success()) {
                failureReasons.put(serverComponent.id(), serverToClientComparison.get().failureReason());
                continue;
            }

            Optional<ComponentNegotiationResult> clientToServerComparison = validateComponent(clientComponent, serverComponent, "server");
            if (clientToServerComparison.isPresent() && !clientToServerComparison.get().success()) {
                failureReasons.put(serverComponent.id(), clientToServerComparison.get().failureReason());
                continue;
            }

            //We can take the servers version here. Either both sides have the same version, or both sides have no version.
            result.add(new NegotiatedNetworkComponent(serverComponent.id(), serverComponent.version()));
        }

        if (!failureReasons.isEmpty()) {
            result.clear();
        }
        return new NegotiationResult(result, failureReasons.isEmpty(), failureReasons);
    }

    /**
     * Checks if two components are compatible.
     * <p>
     * The following rules are followed:
     * <ul>
     * <li>Check if packet flow directions are set, and if at least one is set match it to the other, by missing or wrong value fail the negotiation.</li>
     * <li>Check if both sides have the same version, or none set.</li>
     * </ul>
     * </p>
     * <p>
     * If negotiation succeeds then an empty {@link Optional} is returned.
     * </p>
     * <p>
     * If negotiation fails then a {@link NegotiationResult} is returned with the reason for failure.
     * </p>
     *
     * @param left           The verification component to compare.
     * @param right          The requesting component to compare.
     * @param requestingSide The side of the requesting component.
     * @return An empty {@link Optional} if negotiation succeeded, or a {@link NegotiationResult} with the reason for failure if negotiation failed.
     */
    @VisibleForTesting
    public static Optional<ComponentNegotiationResult> validateComponent(NegotiableNetworkComponent left, NegotiableNetworkComponent right, String requestingSide) {
        if (left.flow().isPresent()) {
            if (right.flow().isEmpty()) {
                return Optional.of(new ComponentNegotiationResult(false, Component.translatable("neoforge.network.negotiation.failure.flow.%s.missing".formatted(requestingSide), left.id())));
            } else if (left.flow().get() != right.flow().get()) {
                return Optional.of(new ComponentNegotiationResult(false, Component.translatable("neoforge.network.negotiation.failure.flow.%s.mismatch".formatted(requestingSide), left.id(), left.flow().get(), right.flow().get())));
            }
        }

        //If either side has no version set, fail
        if (left.version().isEmpty() && right.version().isPresent()) {
            return Optional.of(new ComponentNegotiationResult(false, Component.translatable("neoforge.network.negotiation.failure.version.%s.required".formatted(requestingSide), left.id(), right.version().get())));
        }

        //Check if both sides have the same version, or none set.
        if (left.version().isPresent() && right.version().isPresent()) {
            if (!left.version().get().equals(right.version().get())) {
                return Optional.of(new ComponentNegotiationResult(false, Component.translatable("neoforge.network.negotiation.failure.version.mismatch", left.id(), left.version().get(), right.version().get())));
            }
        }

        //This happens when both the ranges are empty.
        //In other words, no channel has a range, and no channel has a preferred version.
        return Optional.empty();
    }
    
    /**
     * The result of a negotiation.
     *
     * @param success If negotiation succeeded.
     * @param failureReason The reason for failure if negotiation failed.
     */
    public record ComponentNegotiationResult(boolean success, @Nullable Component failureReason) {}
}
