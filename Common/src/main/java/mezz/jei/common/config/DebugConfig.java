package mezz.jei.common.config;

import mezz.jei.common.config.file.IConfigCategoryBuilder;
import mezz.jei.common.config.file.IConfigSchemaBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class DebugConfig {
	@Nullable
	private static DebugConfig instance;

	public static void create(IConfigSchemaBuilder schema) {
		instance = new DebugConfig(schema);
	}

	private final Supplier<Boolean> debugModeEnabled;
	private final Supplier<Boolean> debugGuisEnabled;
	private final Supplier<Boolean> debugInputsEnabled;
	private final Supplier<Boolean> debugInfoTooltipsEnabled;
	private final Supplier<Boolean> crashingTestIngredientsEnabled;
	private final Supplier<Boolean> logSuffixTreeStats;

	private DebugConfig(IConfigSchemaBuilder schema) {
		IConfigCategoryBuilder advanced = schema.addCategory("debug");
		debugModeEnabled = advanced.addBoolean("debugMode", false);
		debugGuisEnabled = advanced.addBoolean("debugGuis", false);
		debugInputsEnabled = advanced.addBoolean("debugInputs", false);
		debugInfoTooltipsEnabled = advanced.addBoolean("debugInfoTooltipsEnabled", false);
		crashingTestIngredientsEnabled = advanced.addBoolean("crashingTestItemsEnabled", false);
		logSuffixTreeStats = advanced.addBoolean("logSuffixTreeStats", false);
	}

	public static boolean isDebugModeEnabled() {
		if (instance == null) {
			return false;
		}
		return instance.debugModeEnabled.get();
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

	public static boolean isCrashingTestIngredientsEnabled() {
		if (instance == null) {
			return false;
		}
		return instance.crashingTestIngredientsEnabled.get();
	}

	public static boolean isLogSuffixTreeStatsEnabled() {
		if (instance == null) {
			return false;
		}
		return instance.logSuffixTreeStats.get();
	}
}
