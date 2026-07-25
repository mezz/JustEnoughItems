package mezz.jei.common;

import mezz.jei.api.runtime.IJeiFeatures;

public class JeiFeatures implements IJeiFeatures {
	private boolean jeiGuiEnabled = true;
	private boolean inventoryEffectRendererGuiHandlerEnabled = true;

	@Override
	public void disableJeiGui() {
		jeiGuiEnabled = false;
	}

	@Override
	public boolean isJeiGuiEnabled() {
		return jeiGuiEnabled;
	}

	@Override
	public void disableInventoryEffectRendererGuiHandler() {
		inventoryEffectRendererGuiHandlerEnabled = false;
	}

	public boolean getInventoryEffectRendererGuiHandlerEnabled() {
		return inventoryEffectRendererGuiHandlerEnabled;
	}
}
