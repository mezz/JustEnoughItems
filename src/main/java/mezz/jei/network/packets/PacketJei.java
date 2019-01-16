package mezz.jei.network.packets;

import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraft.network.PacketBuffer;

import io.netty.buffer.Unpooled;
import mezz.jei.network.IPacketId;
import mezz.jei.network.PacketHandler;

public abstract class PacketJei {
	private final IPacketId id = getPacketId();

	public final FMLProxyPacket getPacket() {
		PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());

		packetBuffer.writeByte(id.ordinal());
		writePacketData(packetBuffer);

		return new FMLProxyPacket(packetBuffer, PacketHandler.CHANNEL_ID);
	}

	public abstract IPacketId getPacketId();

	public abstract void writePacketData(PacketBuffer buf);
}
