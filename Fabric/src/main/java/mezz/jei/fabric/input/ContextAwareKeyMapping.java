package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;

/** JEI matching and reset behavior shared by Fabric's input backends. */
public interface ContextAwareKeyMapping {
	InputConstants.Key getBoundKey();

	boolean isActiveAndMatches(InputConstants.Key key, boolean allowExtraModifiers);

	void resetModifiersToDefault();
}
