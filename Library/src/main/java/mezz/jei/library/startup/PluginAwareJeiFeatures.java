package mezz.jei.library.startup;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.runtime.IJeiFeatures;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PluginAwareJeiFeatures implements IJeiFeatures {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IJeiFeatures jeiFeatures;
	private final IModPlugin modPlugin;

	public PluginAwareJeiFeatures(IJeiFeatures jeiFeatures, IModPlugin modPlugin) {
		this.jeiFeatures = jeiFeatures;
		this.modPlugin = modPlugin;
	}

	@Override
	public void disableJeiGui() {
		LOGGER.info("JEI GUI is being disabled by {}", modPlugin.getPluginUid());
		jeiFeatures.disableJeiGui();
	}

	@Override
	public boolean isJeiGuiEnabled() {
		return jeiFeatures.isJeiGuiEnabled();
	}

	@Override
	public void disableInventoryEffectRendererGuiHandler() {
		LOGGER.info("JEI inventory effect renderer GUI handler is being disabled by {}", modPlugin.getPluginUid());
		jeiFeatures.disableInventoryEffectRendererGuiHandler();
	}
}
