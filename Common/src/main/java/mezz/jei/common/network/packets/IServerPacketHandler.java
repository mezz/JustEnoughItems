package mezz.jei.common.network.packets;

import mezz.jei.common.network.ServerPacketData;

public interface IServerPacketHandler {
	void readPacketData(ServerPacketData data);
}
