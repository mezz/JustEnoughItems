package mezz.jei.library.load;

import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

public class PluginCallerTimerRunnable {
	private static final Logger LOGGER = LogManager.getLogger();

	private static final long startReportDurationMs = 10;
	private static final long longReportDurationInterval = Duration.ofSeconds(5).toMillis();

	private final String title;
	private final ResourceLocation pluginUid;
	private final long startTime;

	private long nextLongReportDurationMs = longReportDurationInterval;

	public PluginCallerTimerRunnable(String title, ResourceLocation pluginUid) {
		this.title = title;
		this.pluginUid = pluginUid;
		this.startTime = System.nanoTime();
		LOGGER.debug("{}: {}...", title, pluginUid);
	}

	public void check() {
		Duration elapsed = Duration.ofNanos(System.nanoTime() - this.startTime);
		long elapsedMs = elapsed.toMillis();
		if (elapsedMs > nextLongReportDurationMs) {
			LOGGER.error("{}: {} is running and has taken {} so far", title, pluginUid, toHumanString(elapsed));
			nextLongReportDurationMs += longReportDurationInterval;
		}
	}

	public void stop() {
		Duration elapsed = Duration.ofNanos(System.nanoTime() - this.startTime);
		if (elapsed.toMillis() > startReportDurationMs) {
			LOGGER.info("{}: {} took {}", title, pluginUid, toHumanString(elapsed));
		}
	}

	private static String toHumanString(Duration duration) {
		TimeUnit unit = getSmallestUnit(duration);
		long nanos = duration.toNanos();
		double value = (double) nanos / NANOSECONDS.convert(1, unit);
		return String.format(Locale.ROOT, "%.4g %s", value, unitToString(unit));
	}

	private static TimeUnit getSmallestUnit(Duration duration) {
		if (duration.toDays() > 0) {
			return DAYS;
		}
		if (duration.toHours() > 0) {
			return HOURS;
		}
		if (duration.toMinutes() > 0) {
			return MINUTES;
		}
		if (duration.toSeconds() > 0) {
			return SECONDS;
		}
		if (duration.toMillis() > 0) {
			return MILLISECONDS;
		}
		if (duration.toNanos() > 1000) {
			return MICROSECONDS;
		}
		return NANOSECONDS;
	}

	private static String unitToString(TimeUnit unit) {
		return unit.name().toLowerCase(Locale.ROOT);
	}
}
