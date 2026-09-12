package mezz.jei.common.config;

import net.mezzdev.config.api.schema.builder.IConfigCategoryBuilder;
import net.mezzdev.config.api.schema.builder.IConfigSchemaBuilder;
import net.mezzdev.config.api.value.IConfigValue;
import org.jetbrains.annotations.Nullable;

public final class DebugConfig {
	@Nullable
	private static DebugConfig instance;

	public static void create(IConfigSchemaBuilder schema) {
		instance = new DebugConfig(schema);
	}

	private final IConfigValue<Boolean> debugIngredientsEnabled;
	private final IConfigValue<Boolean> debugGuisEnabled;
	private final IConfigValue<Boolean> debugInputsEnabled;
	private final IConfigValue<Boolean> debugInfoTooltipsEnabled;
	private final IConfigValue<Boolean> logSuffixTreeStats;

	private DebugConfig(IConfigSchemaBuilder schema) {
		IConfigCategoryBuilder advanced = schema.addCategory("debug");
		debugIngredientsEnabled = advanced.addBoolean("debugIngredientsEnabled", false)
			.addLegacyName("debugMode")
			.build();
		debugGuisEnabled = advanced.addBoolean("debugGuis", false)
			.build();
		debugInputsEnabled = advanced.addBoolean("debugInputs", false)
			.build();
		debugInfoTooltipsEnabled = advanced.addBoolean("debugInfoTooltipsEnabled", false)
			.build();
		logSuffixTreeStats = advanced.addBoolean("logSuffixTreeStats", false)
			.build();
	}

	public static boolean isDebugIngredientsEnabled() {
		if (instance == null) {
			return false;
		}
		return instance.debugIngredientsEnabled.get();
	}

	public static boolean isDebugGuisEnabled() {
		if (instance == null) {
			return false;
		}
		return instance.debugGuisEnabled.get();
	}

	public static boolean isDebugInputsEnabled() {
		if (instance == null) {
			return false;
		}
		return instance.debugInputsEnabled.get();
	}

	public static boolean isDebugInfoTooltipsEnabled() {
		if (instance == null) {
			return false;
		}
		return instance.debugInfoTooltipsEnabled.get();
	}

	public static boolean isLogSuffixTreeStatsEnabled() {
		if (instance == null) {
			return false;
		}
		return instance.logSuffixTreeStats.get();
	}
}
