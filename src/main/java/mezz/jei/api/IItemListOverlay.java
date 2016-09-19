package mezz.jei.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;

import java.util.Collection;

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
	void setFilterText(@Nonnull String filterText);

	/**
	 * @return the current search filter string for the item list
	 */
	@Nonnull
	String getFilterText();

	/**
	 * @return a list containing all stacks that match the current filter.
	 * For the list of all ItemStacks known to JEI, see {@link IItemRegistry#getItemList()}.
	 */
	@Nonnull
	ImmutableList<ItemStack> getFilteredStacks();

	/**
	 * @return a list containing all currently visible stacks. If JEI is hidden, the list will be empty.
	 */
	@Nonnull
	ImmutableList<ItemStack> getVisibleStacks();

	/**
	 * Tells JEI which stacks to highlight
	 */
	void highlightStacks(@Nonnull Collection<ItemStack> stacks);
}
