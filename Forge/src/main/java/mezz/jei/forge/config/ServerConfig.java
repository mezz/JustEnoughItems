package mezz.jei.forge.config;

import mezz.jei.common.config.IServerConfig;
import mezz.jei.forge.events.PermanentEventSubscriptions;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;

import java.nio.file.Path;
import java.util.List;

public final class ServerConfig {
	private ServerConfig() {}

	public static IServerConfig register(PermanentEventSubscriptions subscriptions) {
		var config = mezz.jei.common.config.ServerConfig.register();
		subscriptions.register(ServerAboutToStartEvent.class, event -> {
			Path worldConfigDirectory = event.getServer().getWorldPath(LevelResource.ROOT).resolve("serverconfig");
			config.prepareLegacyMigration(worldConfigDirectory, List.of(
				worldConfigDirectory.resolve("jei-server.toml"),
				FMLPaths.GAMEDIR.get().resolve("defaultconfigs").resolve("jei-server.toml")
			));
		});
		subscriptions.register(EventPriority.LOWEST, ServerStartedEvent.class, event -> config.migrateLegacyConfig());
		return config;
	}
}
