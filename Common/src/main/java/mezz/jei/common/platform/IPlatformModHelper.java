package mezz.jei.common.platform;

import java.util.Optional;

public interface IPlatformModHelper {
	String getModNameForModId(String modId);

	Optional<byte[]> getModIconBytes(String modId);

	boolean isInDev();
}
