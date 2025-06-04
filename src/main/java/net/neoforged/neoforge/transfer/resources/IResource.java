/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

/**
 * Most general form of a resource that can be quantified and moved around.
 *
 * <p>Instances must all be immutable, comparable with {@link Object#equals(Object)}
 * and they must implement a suitable {@link Object#hashCode()}.
 * <p>
 * Note, the amount is not encoded in the resource, but rather in something like a {@link ResourceStack} or {@link MutableResourceStack} based on what you need.
 * <p>
 * It is also possible to make a resource like the following to represent elements as a resource
 * 
 * <pre>
 * {
 *     &#64;code
 *     public enum ElementResource implements IResource, StringRepresentable {
 *         NONE("none"),
 *         FIRE("fire"),
 *         WATER("water"),
 *         EARTH("earth"),
 *         AIR("air");
 *
 *         private final String elementName;
 *
 *         ElementResource(String elementName) {
 *             this.elementName = elementName;
 *         }
 *
 *         &#64;Override
 *         public boolean isEmpty() {
 *             return this == NONE;
 *         }
 *
 *         @Override
 *         public String getSerializedName() {
 *             return elementName;
 *         }
 *
 *         public static final StringRepresentableCodec<ElementResource> CODEC = StringRepresentable.fromEnum(ElementResource::values);
 *         public static final StreamCodec<FriendlyByteBuf, ElementResource> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(ElementResource.class);
 *     }
 * }
 * </pre>
 *
 */
public interface IResource {
    /**
     * Returns {@code true} if this represents an empty resource.
     *
     * <p>Examples include item resource with air as an item, or fluid resource with empty fluid.
     */
    boolean isEmpty();
}
