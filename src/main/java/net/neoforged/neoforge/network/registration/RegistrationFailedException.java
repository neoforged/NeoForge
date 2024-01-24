/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * Defines an exception that can be thrown at runtime, if a modder has registered a payload incorrectly.
 * In practice there are three reasons this exception can be thrown:
 * <ul>
 * <li>The payload id is already registered.</li>
 * <li>The payload id is registered in the wrong namespace.</li>
 * <li>Some other unknown reason.</li>
 * </ul>
 * The reason for the exception can be determined by the {@link Reason} enum.
 * <p>
 * Note: this exception is a runtime exception, meaning that it does not need to be caught.
 * It is not recommended that this exception is caught, since it is a sign of a programming error.
 * </p>
 */
public class RegistrationFailedException extends RuntimeException {
    private final ResourceLocation id;
    private final String namespace;
    private final Reason reason;

    /**
     * Creates a new exception with the given parameters.
     * The reason can not be unknown.
     *
     * @param id        The id of the payload that was being registered.
     * @param namespace The namespace the payload was being registered in.
     * @param reason    The reason the registration failed.
     */
    @ApiStatus.Internal
    public RegistrationFailedException(ResourceLocation id, String namespace, Reason reason) {
        super(reason.format(id, namespace));
        this.id = id;
        this.namespace = namespace;
        this.reason = reason;

        if (reason == Reason.UNKNOWN) {
            throw new IllegalArgumentException("Reason can not be unknown. Supply a throwing reason for the exception.");
        }
    }

    /**
     * Creates a new exception with the given parameters.
     * Automatically sets the reason to unknown, and passes the given throwable to the super constructor as reason for the exception.
     *
     * @param id        The id of the payload that was being registered.
     * @param namespace The namespace the payload was being registered in.
     * @param throwable The throwable that caused the registration to fail.
     */
    @ApiStatus.Internal
    public RegistrationFailedException(ResourceLocation id, String namespace, Throwable throwable) {
        super(Reason.UNKNOWN.format(id, namespace), throwable);
        this.id = id;
        this.namespace = namespace;
        reason = Reason.UNKNOWN;
    }

    /**
     * The id of the payload that was being registered.
     *
     * @return The id of the payload that was being registered.
     */
    public ResourceLocation getId() {
        return id;
    }

    /**
     * The namespace the payload should be registered in.
     *
     * @return The namespace the payload should be registered in.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * The reason the registration failed.
     *
     * @return The reason the registration failed.
     */
    public Reason getReason() {
        return reason;
    }

    /**
     * Defines possible reasons for a payload registration to fail.
     */
    public enum Reason implements ReasonFormatter {
        /**
         * The payload id is already registered.
         */
        DUPLICATE_ID((id, namespace) -> "Duplicate payload id " + id + " for payload in namespace " + namespace + "."),
        /**
         * The payload id is registered in the wrong namespace.
         */
        INVALID_NAMESPACE((id, namespace) -> "Try registering payload in namespace " + namespace + " for payload with id " + id + "."),
        /**
         * The registrar is invalid.
         */
        INVALID_REGISTRAR((id, namespace) -> "Invalid registrar. It can not be used to register payloads."),
        /**
         * Some other unknown reason, an exception was thrown downstream.
         */
        UNKNOWN((id, namespace) -> "General payload registration failure for payload with id " + id + " in namespace " + namespace + ".");

        /**
         * The internal formatter used to format the reason.
         */
        private final ReasonFormatter formatter;

        Reason(ReasonFormatter formatter) {
            this.formatter = formatter;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String format(ResourceLocation id, String namespace) {
            return this.formatter.format(id, namespace);
        }
    }

    /**
     * Internal interface used to format the error message for a given reason.
     */
    @FunctionalInterface
    private interface ReasonFormatter {
        /**
         * Creates a nice error message for the given parameters.
         *
         * @param id        The id of the payload that was being registered.
         * @param namespace The namespace the payload was being registered in.
         * @return A nice error message for the given parameters.
         */
        String format(ResourceLocation id, String namespace);
    }
}
