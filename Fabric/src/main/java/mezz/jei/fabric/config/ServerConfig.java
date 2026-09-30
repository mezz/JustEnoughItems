package mezz.jei.fabric.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.config.IServerConfig;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.List;

public final class ServerConfig {
	private ServerConfig() {}

	public static IServerConfig register() {
		var config = mezz.jei.common.config.ServerConfig.register();
		Path legacyConfig = FabricLoader.getInstance().getConfigDir().resolve("jei-server.properties");
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			Path worldConfigDirectory = server.getWorldPath(LevelResource.ROOT).resolve("serverconfig");
			config.prepareLegacyMigration(worldConfigDirectory, List.of(legacyConfig));
		});
		Identifier migrationPhase = Identifier.fromNamespaceAndPath(ModIds.JEI_ID, "migrate_server_config");
		ServerLifecycleEvents.SERVER_STARTED.addPhaseOrdering(Event.DEFAULT_PHASE, migrationPhase);
		ServerLifecycleEvents.SERVER_STARTED.register(migrationPhase, server -> config.migrateLegacyConfig());
		return config;
	}
}
