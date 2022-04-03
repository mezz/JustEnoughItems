package mezz.jei.common.util;

import mezz.jei.common.platform.IPlatformServerHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.core.util.FileUtil;
import mezz.jei.core.util.ReflectionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public final class ServerConfigPathUtil {
	private static final Path worldDirPath = Path.of("world");
	private static final ReflectionUtil reflectionUtil = new ReflectionUtil();

	private ServerConfigPathUtil() {

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
			IPlatformServerHelper serverHelper = Services.PLATFORM.getServerHelper();
			MinecraftServer minecraftServer = serverHelper.getServer();
			if (minecraftServer != null) {
				return reflectionUtil.getFieldWithClass(minecraftServer, LevelStorageSource.LevelStorageAccess.class)
					.findFirst()
					.map(LevelStorageSource.LevelStorageAccess::getLevelId)
					.map(FileUtil::sanitizePathName)
					.map(name -> worldDirPath.resolve("local").resolve(name))
					.orElse(null);
			}
		} else {
			ServerData serverData = minecraft.getCurrentServer();
			if (serverData != null) {
				int ipHash = serverData.ip.hashCode();
				String ipHashHex = Integer.toHexString(ipHash);
				String name = String.format("%s_%s", serverData.name, ipHashHex);
				name = FileUtil.sanitizePathName(name);
				return worldDirPath.resolve("server").resolve(name);
			}
		}
		return null;
	}
}
