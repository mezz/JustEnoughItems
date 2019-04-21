package mezz.jei.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class LimitedLogger {
	private final Map<String, Long> logTimes = new HashMap<>();
	private final Logger logger;
	private final long timeBetweenLoggingMs;

	public LimitedLogger(Logger logger, Duration timeBetweenLogging) {
		this.logger = logger;
		this.timeBetweenLoggingMs = timeBetweenLogging.toMillis();
	}

	public void log(Level level, String key, String message, Object... params) {
		if (this.logger.isEnabled(level)) {
			long now = System.currentTimeMillis();
			Long lastTime = logTimes.get(key);
			if (lastTime == null || (now - lastTime > timeBetweenLoggingMs)) {
				this.logTimes.put(key, now);
				this.logger.log(level, message, params);
			}
		}
	}
}
