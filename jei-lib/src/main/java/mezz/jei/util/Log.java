package mezz.jei.util;

import mezz.jei.api.ModIds;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @deprecated use a new private static Logger at the top of each class instead.
 */
@Deprecated
public final class Log {
	private static final Logger LOGGER = LogManager.getLogger(ModIds.JEI_ID);

	@Deprecated
	public static Logger get() {
		return LOGGER;
	}

	private Log() {
	}
}
