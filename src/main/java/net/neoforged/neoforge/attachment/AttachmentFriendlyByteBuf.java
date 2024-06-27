package net.neoforged.neoforge.attachment;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.connection.ConnectionType;

public class AttachmentFriendlyByteBuf<T> extends RegistryFriendlyByteBuf {

	final T attachmentHolder;

	public AttachmentFriendlyByteBuf(ByteBuf buffer, RegistryAccess registryAccess, T attachmentHolder) {
		super(buffer, registryAccess, ConnectionType.NEOFORGE);
		this.attachmentHolder = attachmentHolder;
	}

	public static <TOwner> StreamCodec<AttachmentFriendlyByteBuf<TOwner>, TOwner> streamHolder(Class<TOwner> holder) {
		return new StreamUnitCodec<TOwner>(holder);
	}

	private record StreamUnitCodec<TOwner>(Class<TOwner> holder) implements StreamCodec<AttachmentFriendlyByteBuf<TOwner>, TOwner> {
		public TOwner decode(AttachmentFriendlyByteBuf<TOwner> buf) {
			return buf.attachmentHolder;
		}

		public void encode(AttachmentFriendlyByteBuf<TOwner> buffer, TOwner instance) {}
	}
}
