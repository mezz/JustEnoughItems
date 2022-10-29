package mezz.jei.common.network.packets;

import mezz.jei.common.network.ServerPacketData;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface IServerPacketHandler {
	CompletableFuture<Void> readPacketData(ServerPacketData data);
}
