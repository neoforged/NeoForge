package net.neoforged.neoforge.debug.capabilities.handlers;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.transfer.resources.IResource;

public enum TestElementResource implements IResource, StringRepresentable {
    NONE("none"),
    FIRE("fire"),
    WATER("water"),
    EARTH("earth"),
    AIR("air");

    private final String elementName;
    TestElementResource(String elementName) {
        this.elementName = elementName;
    }
    @Override
    public boolean isEmpty() {
        return this == NONE;
    }

    @Override
    public String getSerializedName() {
        return elementName;
    }

    //Unsure if these are correct, but for the purpose of testing, this was enough
    public static final StringRepresentableCodec<TestElementResource> CODEC = StringRepresentable.fromEnum(TestElementResource::values);
    public static final StreamCodec<FriendlyByteBuf, TestElementResource> STREAM_CODEC = StreamCodec.of(
            FriendlyByteBuf::writeEnum, buf -> buf.readEnum(TestElementResource.class)
    );

}
