package mezz.jei.fabric;

import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.ServerPacketRouter;
import mezz.jei.core.config.IServerConfig;
import mezz.jei.fabric.config.ServerConfig;
import mezz.jei.fabric.network.ConnectionToClient;
import mezz.jei.fabric.network.ServerNetworkHandler;
import net.fabricmc.api.ModInitializer;

public class JustEnoughItems implements ModInitializer {
	@Override
	public void onInitialize() {
		IServerConfig serverConfig = ServerConfig.getInstance();
		IConnectionToClient connection = new ConnectionToClient();
		ServerPacketRouter packetRouter = new ServerPacketRouter(connection, serverConfig);
		ServerNetworkHandler.registerServerPacketHandler(packetRouter);
	}
}
