package mezz.jei.network.packets;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import io.netty.buffer.Unpooled;
import mezz.jei.network.IPacketId;
import mezz.jei.network.PacketHandler;
import mezz.jei.util.Log;

public abstract class PacketJEI {
	private final IPacketId id = getPacketId();

	public final FMLProxyPacket getPacket() {
		PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());

		packetBuffer.writeByte(id.ordinal());
		try {
			writePacketData(packetBuffer);
		} catch (IOException e) {
			Log.error("Error creating packet", e);
		}

		return new FMLProxyPacket(packetBuffer, PacketHandler.CHANNEL_ID);
	}

	public abstract IPacketId getPacketId();

	public abstract void readPacketData(PacketBuffer buf, EntityPlayer player) throws IOException;

	public abstract void writePacketData(PacketBuffer buf) throws IOException;
}
