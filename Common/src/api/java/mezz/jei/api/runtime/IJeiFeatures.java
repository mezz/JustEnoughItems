package mezz.jei.api.runtime;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import org.jetbrains.annotations.ApiStatus;

/**
 * Provides access for mod plugins to disable various JEI features.
 * This may be needed by mods that substantially change hard-coded vanilla behaviors.
 *
 * Get an instance from {@link IModPlugin#configureJei(IJeiFeatures)}.
 *
 * @since 17.3.0
 */
@ApiStatus.NonExtendable
public interface IJeiFeatures {
	/**
	 * Disable JEI's built-in GUI.
	 *
	 * <p>
	 * This prevents JEI from registering its own GUI handlers, overlays, recipe GUI,
	 * input handlers, render handlers, and GUI resource reload handlers. JEI's API,
	 * ingredient and recipe registrations, recipe transfer handlers, and runtime remain available.
	 * </p>
	 *
	 * <p>
	 * This should be called from {@link IModPlugin#configureJei(IJeiFeatures)}
	 * so that JEI can disable the GUI before its own GUI handlers are registered.
	 * </p>
	 *
	 * @since 19.42.0
	 */
	void disableJeiGui();

	/**
	 * Returns true if JEI's built-in GUI is enabled.
	 *
	 * @since 19.42.0
	 */
	boolean isJeiGuiEnabled();

	/**
	 * Disable JEI's Inventory Effect Renderer {@link IGuiContainerHandler}.
	 * This is used by JEI in order to move out of the way of potion effects shown next to the inventory.
	 * It can be disabled by mods that remove this behavior or substitute their own.
	 *
	 * @since 17.3.0
	 */
	void disableInventoryEffectRendererGuiHandler();
}
