package mezz.jei.common.platform;

import java.util.List;

public interface IPlatformModHelper {
	String getModNameForModId(String modId);

	boolean isModLoaded(String modId);

	List<byte[]> getModIconByteCandidates(String modId);

	boolean isInDev();
}
