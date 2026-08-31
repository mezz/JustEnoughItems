package mezz.jei.gui.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import mezz.jei.common.input.keys.IJeiKeyMappingWithExtraModifiers;
import org.jspecify.annotations.Nullable;

public final class PinnedTooltipManager {
	private static @Nullable IPinnedTooltipHolder active;

	public static void opened(IPinnedTooltipHolder holder) {
		IPinnedTooltipHolder active = PinnedTooltipManager.active;
		if (active != null && active != holder) {
			active.hide();
		}
		PinnedTooltipManager.active = holder;
	}

	public static void closed(IPinnedTooltipHolder holder) {
		if (PinnedTooltipManager.active == holder) {
			PinnedTooltipManager.active = null;
		}
	}

	public static boolean matchesInput(
		InputConstants.Key inputKey,
		IJeiKeyMappingWithExtraModifiers keyMapping,
		IJeiKeyMappingInternal pinKeyMapping
	) {
		if (keyMapping.isActiveAndMatches(inputKey)) {
			return true;
		}
		return active != null &&
			pinKeyMapping.isDown() &&
			inputKey.getType() != InputConstants.Type.MOUSE &&
			keyMapping.isActiveAndMatchesAllowingExtraModifiers(inputKey);
	}

	private PinnedTooltipManager() {
	}
}
