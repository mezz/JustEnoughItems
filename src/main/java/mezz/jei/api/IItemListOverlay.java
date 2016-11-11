package mezz.jei.api;

import javax.annotation.Nullable;
import java.util.Collection;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.ingredients.IIngredientRegistry;
import net.minecraft.item.ItemStack;

/**
 * The IItemListOverlay is JEI's gui that displays all the items next to an open container gui.
 * Use this interface to get information from and interact with it.
 * Get the instance from {@link IJeiRuntime#getItemListOverlay()}.
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
	String getFilterText();

	/**
	 * @return a list containing all stacks that match the current filter.
	 * For the list of all ItemStacks known to JEI, see {@link IIngredientRegistry#getIngredients(Class)}.
	 */
	ImmutableList<ItemStack> getFilteredStacks();

	/**
	 * @return a list containing all currently visible stacks. If JEI is hidden, the list will be empty.
	 */
	ImmutableList<ItemStack> getVisibleStacks();

	/**
	 * Tells JEI which stacks to highlight
	 */
	void highlightStacks(Collection<ItemStack> stacks);
}
