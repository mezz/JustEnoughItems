package mezz.jei.common.config;

import mezz.jei.common.config.file.ConfigSchemaBuilder;
import mezz.jei.common.config.file.IConfigSchema;
import mezz.jei.common.config.file.IConfigSchemaBuilder;

import java.nio.file.Path;

public class JeiDebugConfigs {
	private final DebugConfig debugConfig;

	private final IConfigSchema schema;

	public JeiDebugConfigs(Path configFile) {
		IConfigSchemaBuilder builder = new ConfigSchemaBuilder(configFile);

		debugConfig = new DebugConfig(builder);

		schema = builder.build();
	}

	public void register() {
		schema.register();
	}

	public DebugConfig getDebugConfig() {
		return debugConfig;
	}
}
