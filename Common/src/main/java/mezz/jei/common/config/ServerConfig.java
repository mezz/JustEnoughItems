package mezz.jei.common.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.config.legacy.LegacyServerConfigMigration;
import net.mezzdev.config.api.Configs;
import net.mezzdev.config.api.schema.IConfigSchema;
import net.mezzdev.config.api.schema.builder.IConfigCategoryBuilder;
import net.mezzdev.config.api.schema.builder.IConfigSchemaBuilder;
import net.mezzdev.config.api.value.IConfigValue;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class ServerConfig implements IServerConfig {
	private static final String FILE_NAME = "jei-server.ini";

	private final IConfigValue<Boolean> enableCheatModeForOp;
	private final IConfigValue<Boolean> enableCheatModeForCreative;
	private final IConfigValue<Boolean> enableCheatModeForGive;
	private final IConfigSchema schema;
	private Optional<LegacyServerConfigMigration> pendingMigration = Optional.empty();

	public static ServerConfig register() {
		IConfigSchemaBuilder builder = Configs.forMod(ModIds.JEI_ID)
			.createServerSchemaBuilder(FILE_NAME, "jei.config.server");
		return new ServerConfig(builder);
	}

	private ServerConfig(IConfigSchemaBuilder builder) {
		IConfigCategoryBuilder permissions = builder.addCategory("permissions");
		enableCheatModeForOp = permissions.addBoolean("enableCheatModeForOp", true).build();
		enableCheatModeForCreative = permissions.addBoolean("enableCheatModeForCreative", true).build();
		enableCheatModeForGive = permissions.addBoolean("enableCheatModeForGive", false).build();
		schema = builder.build();
	}

	/**
	 * Check for legacy settings before MezzConfig creates the world's config file.
	 * MezzConfig's fixed legacy paths cannot follow successive singleplayer worlds.
	 */
	public void prepareLegacyMigration(Path worldConfigDirectory, List<Path> legacyPaths) {
		Path destination = worldConfigDirectory.resolve(ModIds.JEI_ID).resolve(FILE_NAME);
		pendingMigration = LegacyServerConfigMigration.prepare(destination, legacyPaths);
	}

	/** Apply the import after MezzConfig activates the world, before players join. */
	public void migrateLegacyConfig() {
		pendingMigration.ifPresent(migration -> migration.apply(
			schema,
			List.of(enableCheatModeForOp, enableCheatModeForCreative, enableCheatModeForGive)
		));
		pendingMigration = Optional.empty();
	}

	@Override
	public boolean isCheatModeEnabledForOp() {
		return enableCheatModeForOp.get();
	}

	@Override
	public boolean isCheatModeEnabledForCreative() {
		return enableCheatModeForCreative.get();
	}

	@Override
	public boolean isCheatModeEnabledForGive() {
		return enableCheatModeForGive.get();
	}
}
