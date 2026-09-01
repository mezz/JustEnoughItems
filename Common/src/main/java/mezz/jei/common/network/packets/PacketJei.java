package mezz.jei.common.network.packets;

import mezz.jei.common.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import io.netty.buffer.Unpooled;
import mezz.jei.common.network.IPacketId;
import org.apache.commons.lang3.tuple.Pair;

public abstract class PacketJei {
	public ResourceLocation getChannelId() {
		return Constants.NETWORK_CHANNEL_ID;
	}

	public final Pair<FriendlyByteBuf, Integer> getPacketData() {
		IPacketId packetId = getPacketId();
		int packetIdOrdinal = packetId.ordinal();
		FriendlyByteBuf packetBuffer = new FriendlyByteBuf(Unpooled.buffer());
		packetBuffer.writeByte(packetIdOrdinal);
		writePacketData(packetBuffer);
		return Pair.of(packetBuffer, packetIdOrdinal);
	}

	protected abstract IPacketId getPacketId();

	protected abstract void writePacketData(FriendlyByteBuf buf);
}
