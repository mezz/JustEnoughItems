package mezz.jei.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.client.FMLClientHandler;

public class SessionData {
	private static boolean jeiOnServer = false;
	private static boolean jeiStarted = false;
	private static String worldUid = null;

	private SessionData() {

	}

	public static boolean isJeiOnServer() {
		return jeiOnServer;
	}

	public static void onConnectedToServer(boolean jeiOnServer) {
		SessionData.jeiOnServer = jeiOnServer;
		SessionData.jeiStarted = false;
		SessionData.worldUid = null;
	}

	public static String getWorldUid() {
		if (worldUid == null) {
			FMLClientHandler fmlClientHandler = FMLClientHandler.instance();
			final NetworkManager networkManager = fmlClientHandler.getClientToServerNetworkManager();
			if (networkManager == null) { // when opened in main window before a game is started
				return "none";	
			} else if (networkManager.isLocalChannel()) {
				final MinecraftServer minecraftServer = fmlClientHandler.getServer();
				if (minecraftServer != null) {
					worldUid = minecraftServer.getFolderName();
				}
			} else {
				final ServerData serverData = Minecraft.getMinecraft().getCurrentServerData();
				if (serverData != null) {
					worldUid = serverData.serverIP + ' ' + serverData.serverName;
				}
			}

			if (worldUid == null) {
				worldUid = "default";
			}
			worldUid = "world" + Integer.toString(worldUid.hashCode());
		}
		return worldUid;
	}

	public static boolean isJeiStarted() {
		return jeiStarted;
	}

	public static void setJeiStarted() {
		SessionData.jeiStarted = true;
	}
}
