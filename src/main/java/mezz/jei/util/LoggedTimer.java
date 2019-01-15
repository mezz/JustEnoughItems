package mezz.jei.util;

import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class LoggedTimer {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Stopwatch stopWatch = Stopwatch.createUnstarted();
	private String message = "";

	public void start(String message) {
		this.message = message;
		LOGGER.info("{}...", message);
		stopWatch.reset();
		stopWatch.start();
	}

	public void stop() {
		stopWatch.stop();
		LOGGER.info("{} took {}", message, stopWatch);
	}
}
