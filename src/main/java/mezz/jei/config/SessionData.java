package mezz.jei.config;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.client.FMLClientHandler;

public final class SessionData {
	private static boolean jeiOnServer = false;
	private static boolean joinedWorld = false;
	@Nullable
	private static String worldUid = null;
	private static int firstItemIndex = 0;

	private SessionData() {

	}

	public static boolean isJeiOnServer() {
		return jeiOnServer;
	}

	public static void onConnectedToServer(boolean jeiOnServer) {
		SessionData.jeiOnServer = jeiOnServer;
		SessionData.joinedWorld = false;
		SessionData.worldUid = null;
	}

	public static String getWorldUid() {
		if (worldUid == null) {
			FMLClientHandler fmlClientHandler = FMLClientHandler.instance();
			final NetworkManager networkManager = fmlClientHandler.getClientToServerNetworkManager();
			if (networkManager == null) {
				worldUid = "default";
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

	public static boolean hasJoinedWorld() {
		return joinedWorld;
	}

	public static void setJoinedWorld() {
		SessionData.joinedWorld = true;
	}

	public static int getFirstItemIndex() {
		return firstItemIndex;
	}

	public static void setFirstItemIndex(int firstItemIndex) {
		SessionData.firstItemIndex = firstItemIndex;
	}
}
