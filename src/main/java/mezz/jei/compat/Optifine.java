package mezz.jei.compat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public final class Optifine {
	private static final Logger LOGGER = LogManager.getLogger();

	private static final boolean PRESENT = detectPresence();

	public static boolean isPresent() {
		return PRESENT;
	}

	private static boolean detectPresence() {
		try {
			// Optifine 1.12 puts its Config class in the default package.
			// Newer Optifine versions moved this class to net.optifine.Config.
			Class.forName("Config", false, Optifine.class.getClassLoader());
			LOGGER.warn("Optifine is detected. JEI's fast item rendering is being disabled to prevent rendering issues.");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	private Optifine() {
	}
}
