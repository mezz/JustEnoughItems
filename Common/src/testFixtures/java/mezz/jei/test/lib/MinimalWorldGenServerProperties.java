package mezz.jei.test.lib;

import java.util.Properties;

/**
 * Creates server.properties values that keep disposable client-test servers from spending time on normal world generation.
 */
public final class MinimalWorldGenServerProperties {
	private MinimalWorldGenServerProperties() {

	}

	public static Properties create() {
		Properties properties = new Properties();
		properties.setProperty("allow-flight", "true");
		properties.setProperty("difficulty", "peaceful");
		properties.setProperty("force-gamemode", "true");
		properties.setProperty("gamemode", "creative");
		properties.setProperty("generate-structures", "false");
		properties.setProperty("level-name", "jei-test-world");
		properties.setProperty("level-seed", "jei-client-test");
		properties.setProperty("level-type", "minecraft:flat");
		properties.setProperty("max-players", "1");
		properties.setProperty("online-mode", "false");
		properties.setProperty("simulation-distance", "2");
		properties.setProperty("spawn-protection", "0");
		properties.setProperty("sync-chunk-writes", "false");
		properties.setProperty("view-distance", "2");
		return properties;
	}

	public static Properties createForLocalhost(int port) {
		Properties properties = create();
		properties.setProperty("server-ip", "127.0.0.1");
		properties.setProperty("server-port", Integer.toString(port));
		return properties;
	}
}
