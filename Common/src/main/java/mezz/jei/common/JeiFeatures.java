package mezz.jei.common;

import mezz.jei.api.runtime.IJeiFeatures;

public class JeiFeatures implements IJeiFeatures {
	private boolean inventoryEffectRendererGuiHandlerEnabled = true;

	@Override
	public void disableInventoryEffectRendererGuiHandler() {
		inventoryEffectRendererGuiHandlerEnabled = false;
	}

	public boolean getInventoryEffectRendererGuiHandlerEnabled() {
		return inventoryEffectRendererGuiHandlerEnabled;
	}
}
