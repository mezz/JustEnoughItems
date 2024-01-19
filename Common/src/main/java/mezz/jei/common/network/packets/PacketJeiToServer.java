package mezz.jei.common.network.packets;

import mezz.jei.common.network.PacketIdServer;
import mezz.jei.common.network.ServerPacketContext;

public abstract class PacketJeiToServer extends PacketJei<PacketIdServer> {
	public abstract void processOnServerThread(ServerPacketContext context);
}
