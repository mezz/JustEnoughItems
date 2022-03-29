package mezz.jei.common.network.packets;

import mezz.jei.common.network.ClientPacketData;

public interface IClientPacketHandler {
	void readPacketData(ClientPacketData data);
}
