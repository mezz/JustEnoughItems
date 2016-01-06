package mezz.jei.api.gui;

import java.util.List;

public interface ITooltipCallback<T> {
	/** Change the tooltip for an ingredient. */
	void onTooltip(int slotIndex, boolean input, T ingredient, List<String> tooltip);
}
