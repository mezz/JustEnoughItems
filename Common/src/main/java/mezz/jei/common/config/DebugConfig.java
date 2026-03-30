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
	private final Supplier<Boolean> enableAsyncLoading;
	private final Supplier<Boolean> enableTooltipCache;
	private final Supplier<Boolean> enableParallelSearch;

	private DebugConfig(IConfigSchemaBuilder schema) {
		IConfigCategoryBuilder advanced = schema.addCategory("debug");
		debugModeEnabled = advanced.addBoolean(
			"DebugMode",
			false,
			"Debug mode enabled."
		);
		debugGuisEnabled = advanced.addBoolean(
			"DebugGuis",
			false,
			"Debug GUIs enabled."
		);
		debugInputsEnabled = advanced.addBoolean(
			"DebugInputs",
			false,
			"Debug inputs enabled."
		);
		debugInfoTooltipsEnabled = advanced.addBoolean(
			"debugInfoTooltipsEnabled",
			false,
			"Add debug information to ingredient tooltips when advanced tooltips are enabled."
		);
		crashingTestIngredientsEnabled = advanced.addBoolean(
			"CrashingTestItemsEnabled",
			false,
			"Adds ingredients to JEI that intentionally crash, to help debug JEI."
		);
		logSuffixTreeStats = advanced.addBoolean(
			"logSuffixTreeStats",
			false,
			"Log information about the suffix trees used for searching, to help debug JEI."
		);
		enableAsyncLoading = advanced.addBoolean(
			"enableAsyncLoading",
			true,
			"Enable asynchronous loading features for improved performance. Set to false ONLY if you experience compatibility issues with specific mods."
		);
		enableTooltipCache = advanced.addBoolean(
			"enableTooltipCache",
			true,
			"Enable tooltip caching for improved performance. Set to false ONLY if you experience tooltip-related crashes or issues."
		);
		enableParallelSearch = advanced.addBoolean(
			"enableParallelSearch",
			true,
			"Enable parallel search processing for improved performance. Set to false ONLY if you experience search-related crashes or issues."
		);
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

	public static boolean isAsyncLoadingEnabled() {
		if (instance == null) {
			return true; // Default to enabled
		}
		return instance.enableAsyncLoading.get();
	}

	public static boolean isTooltipCacheEnabled() {
		if (instance == null) {
			return true; // Default to enabled
		}
		return instance.enableTooltipCache.get();
	}

	public static boolean isParallelSearchEnabled() {
		if (instance == null) {
			return true; // Default to enabled
		}
		return instance.enableParallelSearch.get();
	}
}
