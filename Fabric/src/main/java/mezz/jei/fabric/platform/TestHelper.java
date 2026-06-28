package mezz.jei.fabric.platform;

import mezz.jei.common.platform.ITestHelper;
import net.minecraft.gametest.framework.UnknownGameTestException;

public class TestHelper implements ITestHelper {
	@Override
	public Throwable getReason(UnknownGameTestException exception) {
		return exception.reason;
	}
}
