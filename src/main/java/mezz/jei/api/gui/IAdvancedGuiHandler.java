package mezz.jei.api.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;

/**
 * Allows mods to change how JEI is displayed next to their gui.
 */
public interface IAdvancedGuiHandler<T extends GuiContainer> {
	/**
	 * @return the class that this IAdvancedGuiHandler handles.
	 */
	@Nonnull
	Class<T> getGuiContainerClass();

	/**
	 * Give JEI information about extra space that the GuiContainer takes up.
	 * Used for moving JEI out of the way of extra things like gui tabs.
	 *
	 * @return the space that the gui takes up besides the normal rectangle defined by GuiContainer.
	 */
	@Nullable
	List<Rectangle> getGuiExtraAreas(T guiContainer);
}
