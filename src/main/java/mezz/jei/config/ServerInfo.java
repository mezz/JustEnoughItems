package mezz.jei.config;

import javax.annotation.Nullable;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.nio.file.Path;

public final class ServerInfo {
	private static boolean jeiOnServer = false;
	private static final Path worldDirPath = Path.of("world");
	private static final String unsafeFileChars = "[^\\w-]";

	private ServerInfo() {

	}

	public static boolean isJeiOnServer() {
		return jeiOnServer;
	}

	public static void onConnectedToServer(boolean jeiOnServer) {
		ServerInfo.jeiOnServer = jeiOnServer;
	}

	@Nullable
	public static Path getWorldPath(Path basePath) {
		Path worldPath = getWorldPath();
		if (worldPath == null) {
			return null;
		}
		return basePath.resolve(worldPath);
	}

	@Nullable
	private static Path getWorldPath() {
		Minecraft minecraft = Minecraft.getInstance();
		ClientPacketListener clientPacketListener = minecraft.getConnection();
		if (clientPacketListener == null) {
			return null;
		}
		Connection connection = clientPacketListener.getConnection();
		if (connection.isMemoryConnection()) {
			MinecraftServer minecraftServer = ServerLifecycleHooks.getCurrentServer();
			if (minecraftServer != null) {
				String name = minecraftServer.storageSource.getLevelId();
				name = sanitizePathName(name);
				return worldDirPath.resolve("local").resolve(name);
			}
		} else {
			ServerData serverData = minecraft.getCurrentServer();
			if (serverData != null) {
				int ipHash = serverData.ip.hashCode();
				String ipHashHex = Integer.toHexString(ipHash);
				String name = String.format("%s_%s", serverData.name, ipHashHex);
				name = sanitizePathName(name);
				return worldDirPath.resolve("server").resolve(name);
			}
		}
		return null;
	}

	public static String sanitizePathName(String filename) {
		return String.join("_", filename.split(unsafeFileChars));
	}
}
