package net.neoforged.neoforge.network.negotiation;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Represents the result of a negotiation of network components.
 *
 * @param components The successfully negotiated components. Is empty when {@link #success()} is false.
 * @param success Whether the negotiation was successful.
 * @param failureReasons The reasons for the failure of the negotiation. Is empty when {@link #success()} is true.
 */
public record NegotiationResult(List<NegotiatedNetworkComponent> components, boolean success, Map<ResourceLocation, Component> failureReasons) {
}
