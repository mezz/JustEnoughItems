package mezz.jei.common.config;

import mezz.jei.core.config.file.IConfigCategoryBuilder;
import mezz.jei.core.config.file.IConfigSchemaBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class DebugConfig {
	@Nullable
	private static DebugConfig instance;

	public static void create(IConfigSchemaBuilder schema) {
		instance = new DebugConfig(schema);
	}

	private final Supplier<Boolean> debugModeEnabled;

	private DebugConfig(IConfigSchemaBuilder schema) {
		IConfigCategoryBuilder advanced = schema.addCategory("debug");
		debugModeEnabled = advanced.addBoolean(
			"DebugMode",
			false,
			"Debug mode enabled"
		);
	}

	public static boolean isDebugModeEnabled() {
		if (instance == null) {
			return false;
		}
		return instance.debugModeEnabled.get();
	}
}
