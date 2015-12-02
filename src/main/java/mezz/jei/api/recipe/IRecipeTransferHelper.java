package mezz.jei.api.recipe;

import java.util.List;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

/**
 * Gives JEI the information it needs to transfer recipes from the player's inventory into the crafting area.
 * Most plugins should create new IRecipeTransferHelper instances with IGuiHelper.createRecipeTransferHelper.
 * Complicated containers can implement this interface directly if necessary.
 */
public interface IRecipeTransferHelper {
	/**
	 * Return the container class that this recipe transfer helper supports
	 */
	Class<? extends Container> getContainerClass();

	/**
	 * Return the recipe category that this container can handle.
	 */
	String getRecipeCategoryUid();

	/**
	 * Return a list of slots for the recipe area.
	 */
	List<Slot> getRecipeSlots(Container container);

	/**
	 * Return a list of slots that the transfer can use to get items for crafting, or place leftover items.
	 */
	List<Slot> getInventorySlots(Container container);
}
