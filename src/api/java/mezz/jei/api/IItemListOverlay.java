package mezz.jei.api;

import javax.annotation.Nullable;
import java.util.Collection;

import net.minecraft.item.ItemStack;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IIngredientType;

/**
 * The IItemListOverlay is JEI's gui that displays all the items next to an open container gui.
 * Use this interface to get information from and interact with it.
 * Get the instance from {@link IJeiRuntime#getItemListOverlay()}.
 *
 * @deprecated since JEI 4.5.0. Use {@link IIngredientListOverlay}
 */
@Deprecated
public interface IItemListOverlay {

	/**
	 * @return true if the text box is focused by the player.
	 * @since JEI 4.2.11
	 * @deprecated since JEI 4.5.0, use {@link IIngredientListOverlay#hasKeyboardFocus()}
	 */
	@Deprecated
	boolean hasKeyboardFocus();

	/**
	 * @return the stack that's currently under the mouse, or null if there is none
	 * @deprecated since JEI 4.5.0, use {@link IIngredientListOverlay#getIngredientUnderMouse()}
	 */
	@Nullable
	@Deprecated
	ItemStack getStackUnderMouse();

	/**
	 * Set the search filter string for the item list.
	 *
	 * @deprecated since JEI 4.5.0. Use {@link IIngredientFilter#setFilterText(String)}
	 */
	@Deprecated
	void setFilterText(String filterText);

	/**
	 * @return the current search filter string for the item list
	 * @deprecated since JEI 4.5.0. Use {@link IIngredientFilter#getFilterText()}
	 */
	@Deprecated
	String getFilterText();

	/**
	 * @return a list containing all stacks that match the current filter.
	 * To get all the ItemStacks known to JEI, see {@link IIngredientRegistry#getAllIngredients(IIngredientType)}.
	 * @deprecated since JEI 4.5.0, use {@link IIngredientFilter#getFilteredIngredients()}
	 */
	@Deprecated
	ImmutableList<ItemStack> getFilteredStacks();

	/**
	 * @return a list containing all currently visible stacks. If JEI is hidden, the list will be empty.
	 * @deprecated since JEI 4.5.0, use {@link IIngredientListOverlay#getVisibleIngredients()}
	 */
	@Deprecated
	ImmutableList<ItemStack> getVisibleStacks();

	/**
	 * Tells JEI which stacks to highlight
	 *
	 * @deprecated Since JEI 4.5.0. This is rarely used and being removed.
	 */
	@Deprecated
	void highlightStacks(Collection<ItemStack> stacks);
}
