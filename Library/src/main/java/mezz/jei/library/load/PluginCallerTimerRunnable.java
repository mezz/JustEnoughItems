package mezz.jei.library.load;

import mezz.jei.core.util.TimeUtil;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

public class PluginCallerTimerRunnable {
	private static final Logger LOGGER = LogManager.getLogger();

	private static final long startReportDurationMs = 10;
	private static final long longReportDurationInterval = Duration.ofSeconds(5).toMillis();

	private final String title;
	private final Identifier pluginUid;
	private final long startTime;

	private long nextLongReportDurationMs = longReportDurationInterval;

	public PluginCallerTimerRunnable(String title, Identifier pluginUid) {
		this.title = title;
		this.pluginUid = pluginUid;
		this.startTime = System.nanoTime();
		LOGGER.debug("{}: {}...", title, pluginUid);
	}

	public void check() {
		Duration elapsed = Duration.ofNanos(System.nanoTime() - this.startTime);
		long elapsedMs = elapsed.toMillis();
		if (elapsedMs > nextLongReportDurationMs) {
			LOGGER.error("{}: {} is running and has taken {} so far", title, pluginUid, TimeUtil.toHumanString(elapsed));
			nextLongReportDurationMs += longReportDurationInterval;
		}
	}

	public void stop() {
		Duration elapsed = Duration.ofNanos(System.nanoTime() - this.startTime);
		if (elapsed.toMillis() > startReportDurationMs) {
			LOGGER.info("{}: {} took {}", title, pluginUid, TimeUtil.toHumanString(elapsed));
		}
	}

}
