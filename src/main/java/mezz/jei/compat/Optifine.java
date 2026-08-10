package mezz.jei.compat;

import mezz.jei.util.Log;

public final class Optifine {
	private static final boolean PRESENT = detectPresence();

	public static boolean isPresent() {
		return PRESENT;
	}

	private static boolean detectPresence() {
		try {
			// Optifine 1.12 puts its Config class in the default package.
			// Newer Optifine versions moved this class to net.optifine.Config.
			Class.forName("Config", false, Optifine.class.getClassLoader());
			Log.get().warn("Optifine is detected. JEI's fast item rendering is being disabled to prevent rendering issues.");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	private Optifine() {
	}
}
