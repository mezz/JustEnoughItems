package mezz.jei.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

/**
 * The IItemListOverlay is JEI's gui that displays all the items next to an open container gui.
 * Use this interface to get information from and interact with it.
 */
public interface IItemListOverlay {

	/**
	 * @return the stack that's currently under the mouse, or null if there is none
	 */
	@Nullable
	ItemStack getStackUnderMouse();

	/**
	 * Set the search filter string for the item list.
	 */
	void setFilterText(String filterText);

	/**
	 * @return the current search filter string for the item list
	 */
	@Nonnull
	String getFilterText();
}
