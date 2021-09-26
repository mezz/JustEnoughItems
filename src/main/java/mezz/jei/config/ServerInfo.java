package mezz.jei.config;

import javax.annotation.Nullable;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

public final class ServerInfo {
	private static boolean jeiOnServer = false;
	private static final Path worldDirPath = Path.of("world");
	@Nullable
	private static Path worldPath = null;

	private ServerInfo() {

	}

	public static boolean isJeiOnServer() {
		return jeiOnServer;
	}

	public static void onConnectedToServer(boolean jeiOnServer) {
		ServerInfo.jeiOnServer = jeiOnServer;
		ServerInfo.worldPath = null;
	}

	@Nullable
	public static Path getWorldPath(Path basePath) {
		if (worldPath == null) {
			worldPath = getWorldPath();
			if (worldPath == null) {
				return null;
			}
		}
		return basePath.resolve(worldPath);
	}

	@Nullable
	private static Path getWorldPath() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null) {
			return null;
		}
		ClientPacketListener clientPacketListener = minecraft.getConnection();
		if (clientPacketListener == null) {
			return null;
		}
		Connection connection = clientPacketListener.getConnection();
		if (connection == null) {
			return null;
		}
		if (connection.isMemoryConnection()) {
			MinecraftServer minecraftServer = ServerLifecycleHooks.getCurrentServer();
			if (minecraftServer != null) {
				String name = minecraftServer.storageSource.getLevelId();
				return worldDirPath.resolve("local").resolve(name);
			}
		} else {
			ServerData serverData = minecraft.getCurrentServer();
			if (serverData != null) {
				String name = String.format("%s (%s)", serverData.name, serverData.ip);
				return worldDirPath.resolve("server").resolve(name);
			}
		}
		return null;
	}
}
