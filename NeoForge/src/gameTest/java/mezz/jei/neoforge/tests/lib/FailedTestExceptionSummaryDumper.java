package mezz.jei.neoforge.tests.lib;

import mezz.jei.common.platform.Services;
import net.minecraft.gametest.framework.UnknownGameTestException;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.summary.SummaryDumper;
import net.neoforged.testframework.summary.TestSummary;
import org.slf4j.Logger;

public class FailedTestExceptionSummaryDumper implements SummaryDumper {
	@Override
	public void dump(TestSummary summary, Logger logger) {
		for (TestSummary.TestInfo testInfo : summary.testInfos()) {
			Test.Status status = testInfo.status();
			if (status.result() != Test.Result.FAILED) {
				continue;
			}

			Exception exception = status.exception();
			if (exception == null) {
				logger.error("Test '{}' failed: {}", testInfo.testId(), status.message());
			} else {
				logger.error("Test '{}' failed: {}", testInfo.testId(), status.message(), exception);
				logUnknownGameTestReason(testInfo, exception, logger);
			}
		}
	}

	private static void logUnknownGameTestReason(TestSummary.TestInfo testInfo, Exception exception, Logger logger) {
		if (exception instanceof UnknownGameTestException unknownGameTestException) {
			Throwable reason = Services.PLATFORM.getTestHelper()
				.getReason(unknownGameTestException);
			logger.error("Unknown game test failure reason for test '{}'", testInfo.testId(), reason);
		}
	}
}
