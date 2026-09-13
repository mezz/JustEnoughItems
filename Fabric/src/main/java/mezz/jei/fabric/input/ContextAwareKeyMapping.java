package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;

public interface ContextAwareKeyMapping {
	boolean isContextActive();

	boolean isActiveAndMatches(InputConstants.Key key);
}
