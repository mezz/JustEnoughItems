package mezz.jei.core.util;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeUtil {
	public static String toHumanString(Duration duration) {
		TimeUnit unit = getSmallestUnit(duration);
		long nanos = duration.toNanos();
		double value = (double) nanos / TimeUnit.NANOSECONDS.convert(1, unit);
		return String.format(Locale.ROOT, "%.4g %s", value, unitToString(unit));
	}

	private static TimeUnit getSmallestUnit(Duration duration) {
		if (duration.toDays() > 0) {
			return TimeUnit.DAYS;
		}
		if (duration.toHours() > 0) {
			return TimeUnit.HOURS;
		}
		if (duration.toMinutes() > 0) {
			return TimeUnit.MINUTES;
		}
		if (duration.toSeconds() > 0) {
			return TimeUnit.SECONDS;
		}
		if (duration.toMillis() > 0) {
			return TimeUnit.MILLISECONDS;
		}
		if (duration.toNanos() > 1000) {
			return TimeUnit.MICROSECONDS;
		}
		return TimeUnit.NANOSECONDS;
	}

	private static String unitToString(TimeUnit unit) {
		return unit.name().toLowerCase(Locale.ROOT);
	}
}
