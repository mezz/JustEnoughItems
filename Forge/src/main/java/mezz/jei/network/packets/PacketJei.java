package mezz.jei.network.packets;

import net.minecraft.network.FriendlyByteBuf;

import io.netty.buffer.Unpooled;
import mezz.jei.network.IPacketId;
import org.apache.commons.lang3.tuple.Pair;

public abstract class PacketJei {
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
