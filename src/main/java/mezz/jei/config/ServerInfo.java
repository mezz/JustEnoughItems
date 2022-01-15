package mezz.jei.config;

import mezz.jei.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.FMLConnectionData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ServerInfo {
	private static final Path worldDirPath = Paths.get("world");
	private static final String unsafeFileChars = "[^\\w-]";
	@Nullable
	private static NetworkManager jeiOnServerCacheConnection;
	private static boolean jeiOnServerCacheValue;

	private ServerInfo() {

	}

	public static boolean isJeiOnServer() {
		ClientPlayNetHandler clientPlayNetHandler = Minecraft.getInstance().getConnection();
		if (clientPlayNetHandler == null) {
			return false;
		}
		NetworkManager connection = clientPlayNetHandler.getConnection();
		if (connection != jeiOnServerCacheConnection) {
			jeiOnServerCacheConnection = connection;
			FMLConnectionData connectionData = NetworkHooks.getConnectionData(connection);
			jeiOnServerCacheValue = connectionData != null && connectionData.getChannels().containsKey(PacketHandler.CHANNEL_ID);
		}
		return jeiOnServerCacheValue;
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
		ClientPlayNetHandler clientPlayNetHandler = minecraft.getConnection();
		if (clientPlayNetHandler == null) {
			return null;
		}
		NetworkManager connection = clientPlayNetHandler.getConnection();
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
