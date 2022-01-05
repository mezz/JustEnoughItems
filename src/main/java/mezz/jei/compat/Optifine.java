package mezz.jei.compat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Optifine {
	private static final Logger LOGGER = LogManager.getLogger();
	private static Boolean present = null;

	public static boolean isPresent() {
		if (present == null) {
			try {
				Class.forName("net.optifine.Config");
				LOGGER.warn("Optifine is detected. JEI's fast item rendering is being disabled to prevent rendering issues.");
				present = true;
			} catch (ClassNotFoundException e) {
				present = false;
			}
		}
		return present;
	}
}
