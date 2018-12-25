package mezz.jei.network.packets;

import net.minecraft.network.PacketBuffer;

import io.netty.buffer.Unpooled;
import mezz.jei.network.IPacketId;
import org.apache.commons.lang3.tuple.Pair;

public abstract class PacketJei {
	public final Pair<PacketBuffer, Integer> getPacketData() {
		IPacketId id = getPacketId();
		PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());
		writePacketData(packetBuffer);
		return Pair.of(packetBuffer, id.ordinal());
	}

	protected abstract IPacketId getPacketId();

	protected abstract void writePacketData(PacketBuffer buf);
}
