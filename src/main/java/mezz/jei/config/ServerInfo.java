package mezz.jei.config;

import javax.annotation.Nullable;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;

public final class ServerInfo {
	private static boolean jeiOnServer = false;
	@Nullable
	private static String worldUid = null;

	private ServerInfo() {

	}

	public static boolean isJeiOnServer() {
		return jeiOnServer;
	}

	public static void onConnectedToServer(boolean jeiOnServer) {
		ServerInfo.jeiOnServer = jeiOnServer;
		ServerInfo.worldUid = null;
	}

	public static String getWorldUid(@Nullable NetworkManager networkManager) {
		if (worldUid == null) {
			if (networkManager == null) {
				worldUid = "default"; // we get here when opening the in-game config before loading a world
			} else if (networkManager.isLocalChannel()) {
				FMLClientHandler fmlClientHandler = FMLClientHandler.instance();
				MinecraftServer minecraftServer = fmlClientHandler.getServer();
				if (minecraftServer != null) {
					worldUid = minecraftServer.getFolderName();
				}
			} else {
				ServerData serverData = Minecraft.getMinecraft().getCurrentServerData();
				if (serverData != null) {
					worldUid = serverData.serverIP + ' ' + serverData.serverName;
				}
			}

			if (worldUid == null) {
				worldUid = "default";
			}
			worldUid = "world" + worldUid.hashCode();
		}
		return worldUid;
	}
}
