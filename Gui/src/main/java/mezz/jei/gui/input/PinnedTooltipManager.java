package mezz.jei.gui.input;

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

	private PinnedTooltipManager() {
	}
}
