package mezz.jei.api.recipe.transfer;

import java.util.List;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

/**
 * Gives JEI the information it needs to transfer recipes from a slotted inventory into the crafting area.
 * <p>
 * Most plugins with normal inventories can use the simpler {@link IRecipeTransferRegistry#addRecipeTransferHandler(Class, String, int, int, int, int)}.
 * Containers with slot ranges that contain gaps or other oddities can implement this interface directly.
 * Containers that need full control over the recipe transfer or do not use slots can implement {@link IRecipeTransferHandler}.
 */
public interface IRecipeTransferInfo<C extends Container> {
	/**
	 * Return the container class that this recipe transfer helper supports.
	 */
	Class<C> getContainerClass();

	/**
	 * Return the recipe category that this container can handle.
	 */
	String getRecipeCategoryUid();

	/**
	 * Return true if this recipe transfer info can handle the given container instance.
	 *
	 * @since JEI 4.0.2
	 */
	boolean canHandle(C container);

	/**
	 * Return a list of slots for the recipe area.
	 */
	List<Slot> getRecipeSlots(C container);

	/**
	 * Return a list of slots that the transfer can use to get items for crafting, or place leftover items.
	 */
	List<Slot> getInventorySlots(C container);

	/**
	 * Return false if the recipe transfer should attempt to place as many items as possible for all slots, even if one slot has less.
	 */
	default boolean requireCompleteSets() {
		return true;
	}
}
