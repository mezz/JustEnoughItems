package mezz.jei.api.recipe.transfer;

import java.util.List;
import java.util.Optional;

import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

import mezz.jei.api.registration.IRecipeTransferRegistration;

import javax.annotation.Nullable;

/**
 * Gives JEI the information it needs to transfer recipes from a slotted inventory into the crafting area.
 *
 * Most plugins with normal inventories can use the simpler
 * {@link IRecipeTransferRegistration#addRecipeTransferHandler(Class, MenuType, RecipeType, int, int, int, int)}.
 *
 * Containers with slot ranges that contain gaps or other oddities can implement this interface directly.
 * Containers that need full control over the recipe transfer or do not use slots can implement {@link IRecipeTransferHandler}.
 */
public interface IRecipeTransferInfo<C extends AbstractContainerMenu, R> {
	/**
	 * Return the container class that this recipe transfer helper supports.
	 */
	Class<? extends C> getContainerClass();

	/**
	 * Return the optional menu type that this recipe transfer helper supports.
	 * This is used to optionally narrow down the type of container handled by this recipe transfer info.
	 */
	Optional<MenuType<C>> getMenuType();

	/**
	 * Return the recipe type that this container can handle.
	 *
	 * @since 9.5.0
	 */
	RecipeType<R> getRecipeType();

	/**
	 * Return true if this recipe transfer info can handle the given container instance and recipe.
	 */
	boolean canHandle(C container, R recipe);

	/**
	 * Return an optional descriptive error if this recipe transfer info cannot handle
	 * the given container instance and recipe.
	 *
	 * @implNote this is only called if {@link #canHandle} returns `false`.
	 *
	 * @since 9.5.4
	 */
	@Nullable
	default IRecipeTransferError getHandlingError(C container, R recipe) {
		return null;
	}

	/**
	 * Return a list of slots for the recipe area.
	 */
	List<Slot> getRecipeSlots(C container, R recipe);

	/**
	 * Return a list of slots that the transfer can use to get items for crafting, or place leftover items.
	 */
	List<Slot> getInventorySlots(C container, R recipe);

	/**
	 * Return false if the recipe transfer should attempt to place as many items as possible for all slots, even if one slot has less.
	 */
	default boolean requireCompleteSets(C container, R recipe) {
		return true;
	}
}
