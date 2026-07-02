package mezz.jei.test;

import mezz.jei.common.util.PathUtil;
import mezz.jei.common.util.ServerConfigPathUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServerConfigPathUtilTest {
	@TempDir
	Path tempDir;

	@Test
	public void testGetServerPathUsesHostname() {
		// Setup: the server address is a hostname with mixed casing.
		String serverName = "Test Server: 1";
		String serverAddress = "Play.Example.com";

		// Operation:
		Path path = ServerConfigPathUtil.getServerPath(serverName, serverAddress);

		// Assertions: the server name and normalized hostname are used for the path.
		Path expectedPath = getServerPath("Test Server_ 1 (play_example_com)");
		Assertions.assertEquals(expectedPath, path);
	}

	@Test
	public void testGetServerPathUsesHostnameWithNonDefaultPort() {
		// Setup: the server address is a hostname with a non-default port.
		String serverName = "Test Server";
		String serverAddress = "play.example.com:25566";

		// Operation:
		Path path = ServerConfigPathUtil.getServerPath(serverName, serverAddress);

		// Assertions: the server name, hostname, and port are included in the path.
		Path expectedPath = getServerPath("Test Server (play_example_com 25566)");
		Assertions.assertEquals(expectedPath, path);
	}

	@Test
	public void testGetServerPathOmitsDefaultPort() {
		// Setup: the server address is a hostname with the default port.
		String serverName = "Test Server";
		String serverAddress = "play.example.com:25565";

		// Operation:
		Path path = ServerConfigPathUtil.getServerPath(serverName, serverAddress);

		// Assertions: the default port is omitted from the path.
		Path expectedPath = getServerPath("Test Server (play_example_com)");
		Assertions.assertEquals(expectedPath, path);
	}

	@Test
	public void testGetServerPathUsesLiteralIpv4Address() {
		// Setup: the server address is a literal IPv4 address.
		String serverName = "Test Server";
		String serverAddress = "192.0.2.1";

		// Operation:
		Path path = ServerConfigPathUtil.getServerPath(serverName, serverAddress);

		// Assertions: the server name and literal address are used for the path.
		Path expectedPath = getServerPath("Test Server (192_0_2_1)");
		Assertions.assertEquals(expectedPath, path);
	}

	@Test
	public void testGetServerPathUsesLiteralIpv4AddressWithNonDefaultPort() {
		// Setup: the server address is a literal IPv4 address with a non-default port.
		String serverName = "Test Server";
		String serverAddress = "192.0.2.1:25566";

		// Operation:
		Path path = ServerConfigPathUtil.getServerPath(serverName, serverAddress);

		// Assertions: the server name, literal address, and port are used for the path.
		Path expectedPath = getServerPath("Test Server (192_0_2_1 25566)");
		Assertions.assertEquals(expectedPath, path);
	}

	@Test
	public void testGetServerPathUsesLiteralIpv6Address() {
		// Setup: the server address is a literal IPv6 address.
		String serverName = "Test Server";
		String serverAddress = "[2001:db8::1]";

		// Operation:
		Path path = ServerConfigPathUtil.getServerPath(serverName, serverAddress);

		// Assertions: the server name and literal address are used for the path.
		Path expectedPath = getServerPath("Test Server (2001_db8__1)");
		Assertions.assertEquals(expectedPath, path);
	}

	@Test
	public void testGetServerPathUsesLocalhostAddress() {
		// Setup: the server address is localhost with a port.
		String serverName = "Test Server";
		String serverAddress = "localhost:25565";

		// Operation:
		Path path = ServerConfigPathUtil.getServerPath(serverName, serverAddress);

		// Assertions: the server name and localhost address are used for the path.
		Path expectedPath = getServerPath("Test Server (localhost)");
		Assertions.assertEquals(expectedPath, path);
	}

	@Test
	public void testGetServerPathUsesNameForLanServer() {
		// Setup: the server is a LAN-discovered server with a literal address and dynamic port.
		String serverName = "LAN Server";
		String serverAddress = "192.0.2.1:54321";

		// Operation:
		Path path = ServerConfigPathUtil.getServerPath(serverName, serverAddress, true);

		// Assertions: LAN servers use the stable advertised server name and connection type.
		Path expectedPath = getServerPath("LAN Server (LAN connection)");
		Assertions.assertEquals(expectedPath, path);
	}

	@Test
	public void testGetServerPathPrefersExistingHostnamePath() throws IOException {
		// Setup: the new hostname-based path already exists.
		String serverName = "Test Server";
		String serverAddress = "play.example.com";
		Path expectedPath = getServerPath("Test Server (play_example_com)");
		Files.createDirectories(tempDir.resolve(expectedPath));

		// Operation:
		Path path = ServerConfigPathUtil.getServerPath(tempDir, serverName, serverAddress);

		// Assertions: the existing hostname-based path is used.
		Assertions.assertEquals(expectedPath, path);
	}

	@Test
	public void testGetServerPathUsesLegacyPathForExistingServerAddress() throws IOException {
		// Setup: only the exact old JEI hashed path exists.
		String serverName = "Test Server";
		String serverAddress = "play.example.com";
		Path legacyPath = getLegacyServerPath(serverName, serverAddress);
		Files.createDirectories(tempDir.resolve(legacyPath));

		// Operation:
		Path path = ServerConfigPathUtil.getServerPath(tempDir, serverName, serverAddress);

		// Assertions: the old hashed path is preserved.
		Assertions.assertEquals(legacyPath, path);
	}

	@Test
	public void testGetServerPathUsesLegacyPathForExistingLanServerAddress() throws IOException {
		// Setup: the exact old JEI hashed path exists for this LAN server address.
		String serverName = "LAN Server";
		String serverAddress = "192.0.2.1:54321";
		Path legacyPath = getLegacyServerPath(serverName, serverAddress);
		Files.createDirectories(tempDir.resolve(legacyPath));

		// Operation:
		Path path = ServerConfigPathUtil.getServerPath(tempDir, serverName, serverAddress, true);

		// Assertions: the old hashed path is preserved.
		Assertions.assertEquals(legacyPath, path);
	}

	@Test
	public void testGetServerPathPrefersLegacyPathWhenLegacyAndNewPathsExist() throws IOException {
		// Setup: both the old hashed path and the new hostname-based path exist.
		String serverName = "Test Server";
		String serverAddress = "play.example.com";
		Path legacyPath = getLegacyServerPath(serverName, serverAddress);
		Path newPath = getServerPath("Test Server (play_example_com)");
		Files.createDirectories(tempDir.resolve(legacyPath));
		Files.createDirectories(tempDir.resolve(newPath));

		// Operation:
		Path path = ServerConfigPathUtil.getServerPath(tempDir, serverName, serverAddress);

		// Assertions: the old hashed path wins for backwards compatibility.
		Assertions.assertEquals(legacyPath, path);
	}

	private static Path getLegacyServerPath(String serverName, String serverAddress) {
		String ipHashHex = Integer.toHexString(serverAddress.hashCode());
		String name = "%s_%s".formatted(serverName, ipHashHex);
		name = PathUtil.sanitizePathNameLegacy(name);
		return getServerPath(name);
	}

	private static Path getServerPath(String pathName) {
		return ServerConfigPathUtil.getServerDirPath().resolve(pathName);
	}
}
