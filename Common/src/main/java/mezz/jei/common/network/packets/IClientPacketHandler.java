package mezz.jei.common.network.packets;

import mezz.jei.common.network.ClientPacketData;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface IClientPacketHandler {
	CompletableFuture<Void> readPacketData(ClientPacketData data);
}
