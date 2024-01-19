package mezz.jei.common.network.packets;

import net.minecraft.network.FriendlyByteBuf;

import io.netty.buffer.Unpooled;
import mezz.jei.common.network.IPacketId;
import org.apache.commons.lang3.tuple.Pair;

public abstract class PacketJei<IDType extends IPacketId> {
	public final Pair<FriendlyByteBuf, Integer> getPacketData() {
		IPacketId packetId = getPacketId();
		int packetIdOrdinal = packetId.ordinal();
		FriendlyByteBuf packetBuffer = new FriendlyByteBuf(Unpooled.buffer());
		packetBuffer.writeByte(packetIdOrdinal);
		writePacketData(packetBuffer);
		return Pair.of(packetBuffer, packetIdOrdinal);
	}

	public abstract IDType getPacketId();

	public abstract void writePacketData(FriendlyByteBuf buf);
}
