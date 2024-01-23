package mezz.jei.common.network.packets;

import mezz.jei.common.network.ClientPacketContext;
import mezz.jei.common.network.PacketIdClient;

public abstract class PacketJeiToClient extends PacketJei<PacketIdClient> {
	public abstract void processOnClientThread(ClientPacketContext context);
}
