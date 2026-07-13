package mezz.jei.common.util;

import com.google.common.net.HostAndPort;
import com.google.common.net.InetAddresses;
import mezz.jei.common.platform.Services;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;

import java.net.IDN;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

public final class ServerConfigPathUtil {
	private static final Path worldDirPath = Path.of("world");
	private static final Path serverDirPath = worldDirPath.resolve("server");

	private ServerConfigPathUtil() {

	}

	public static Optional<Path> getWorldPath(Path basePath) {
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
					.map(serverData -> getServerPath(basePath, serverData.name, serverData.ip, serverData.isLan()));
			})
			.map(basePath::resolve);
	}

	public static Path getServerPath(String serverName, String serverAddress) {
		return getServerPath(serverName, serverAddress, false);
	}

	public static Path getServerPath(String serverName, String serverAddress, boolean isLan) {
		if (isLan) {
			return getNamedServerPath("%s (LAN connection)".formatted(serverName));
		}

		return parseServerAddress(serverAddress)
			.map(serverAddressHostAndPort -> {
				String addressName = getAddressName(serverAddressHostAndPort);
				String name = "%s (%s)".formatted(serverName, addressName);
				return getNamedServerPath(name);
			})
			.orElseGet(() -> getLegacyServerPath(serverName, serverAddress));
	}

	public static Path getServerDirPath() {
		return serverDirPath;
	}

	public static Path getServerPath(Path basePath, String serverName, String serverAddress) {
		return getServerPath(basePath, serverName, serverAddress, false);
	}

	public static Path getServerPath(Path basePath, String serverName, String serverAddress, boolean isLan) {
		Path legacyServerPath = getLegacyServerPath(serverName, serverAddress);
		if (Files.exists(basePath.resolve(legacyServerPath))) {
			return legacyServerPath;
		}

		return getServerPath(serverName, serverAddress, isLan);
	}

	private static String getAddressName(HostAndPort hostAndPort) {
		String host = hostAndPort.getHost();
		host = PathUtil.sanitizePathName(host.toLowerCase(Locale.ROOT));
		int port = hostAndPort.getPort();
		if (port != SharedConstants.DEFAULT_MINECRAFT_PORT) {
			return "%s %d".formatted(host, port);
		}
		return host;
	}

	private static Optional<HostAndPort> parseServerAddress(String serverAddressString) {
		try {
			HostAndPort hostAndPort = HostAndPort.fromString(serverAddressString)
				.withDefaultPort(SharedConstants.DEFAULT_MINECRAFT_PORT);
			String host = hostAndPort.getHost();
			if (!InetAddresses.isInetAddress(host)) {
				host = IDN.toASCII(host);
			}
			if (!host.isEmpty()) {
				return Optional.of(HostAndPort.fromParts(host, hostAndPort.getPort()));
			}
		} catch (IllegalArgumentException ignored) {

		}
		return Optional.empty();
	}

	private static Path getLegacyServerPath(String serverName, String serverAddress) {
		String ipHashHex = Integer.toHexString(serverAddress.hashCode());
		String name = "%s_%s".formatted(serverName, ipHashHex);
		name = PathUtil.sanitizePathNameLegacy(name);
		return serverDirPath.resolve(name);
	}

	private static Path getNamedServerPath(String name) {
		name = PathUtil.sanitizePathName(name);
		return serverDirPath.resolve(name);
	}
}
