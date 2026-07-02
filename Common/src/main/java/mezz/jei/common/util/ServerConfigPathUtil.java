package mezz.jei.common.util;

import mezz.jei.common.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;

import java.nio.file.Path;
import java.util.Optional;

public final class ServerConfigPathUtil {
	private static final Path worldDirPath = Path.of("world");

	private ServerConfigPathUtil() {

	}

	public static Optional<Path> getWorldPath(Path basePath) {
		return getWorldPath()
			.map(basePath::resolve);
	}

	private static Optional<Path> getWorldPath() {
		Minecraft minecraft = Minecraft.getInstance();
		return Optional.ofNullable(minecraft.getConnection())
			.flatMap(clientPacketListener -> {
				Connection connection = clientPacketListener.getConnection();
				if (connection.isMemoryConnection()) {
					return Optional.ofNullable(minecraft.getSingleplayerServer())
						.flatMap(minecraftServer ->
							Services.PLATFORM.getWorldHelper().getLevelId(minecraftServer)
								.map(PathUtil::sanitizePathName)
								.map(name -> worldDirPath.resolve("local").resolve(name))
						);
				}
				return Optional.ofNullable(minecraft.getCurrentServer())
					.map(serverData -> {
						int ipHash = serverData.ip.hashCode();
						String ipHashHex = Integer.toHexString(ipHash);
						String name = String.format("%s_%s", serverData.name, ipHashHex);
						name = PathUtil.sanitizePathName(name);
						return worldDirPath.resolve("server").resolve(name);
					});
			});
	}
}
