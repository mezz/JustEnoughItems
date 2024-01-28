package mezz.jei.api.runtime;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IAdvancedRegistration;

/**
 * Provides access for mod plugins to disable various JEI features.
 * This may be needed by mods that substantially change hard-coded vanilla behaviors.
 *
 * Get an instance from {@link IAdvancedRegistration#getJeiFeatures()}
 *
 * @since 17.3.0
 */
public interface IJeiFeatures {
    /**
     * Disable JEI's Inventory Effect Renderer {@link IGuiContainerHandler}.
     * This is used by JEI in order to move out of the way of potion effects shown next to the inventory.
     * It can be disabled by mods that remove this behavior or substitute their own.
     *
     * @since 17.3.0
     */
    void disableInventoryEffectRendererGuiHandler();
}
