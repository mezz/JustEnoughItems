package mezz.jei.common.input.keys;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.runtime.IJeiKeyMapping;

public interface IJeiKeyMappingWithExtraModifiers extends IJeiKeyMapping {
	boolean isActiveAndMatchesAllowingExtraModifiers(InputConstants.Key key);
}
