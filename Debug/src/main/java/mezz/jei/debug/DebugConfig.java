package mezz.jei.debug;

public final class DebugConfig {
	private static final String PROPERTY_PREFIX = "mezz.jei.debug.";

	private DebugConfig() {

	}

	public static boolean isCrashingTestIngredientsEnabled() {
		return Boolean.getBoolean(PROPERTY_PREFIX + "crashingTestIngredientsEnabled");
	}

	public static boolean isCrashingTestRecipesEnabled() {
		return Boolean.getBoolean(PROPERTY_PREFIX + "crashingTestRecipesEnabled");
	}

	public static boolean isDebugGuisEnabled() {
		return Boolean.getBoolean(PROPERTY_PREFIX + "debugGuisEnabled");
	}
}
