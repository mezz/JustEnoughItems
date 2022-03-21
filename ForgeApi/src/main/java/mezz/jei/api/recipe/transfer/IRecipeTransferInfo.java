package mezz.jei.api.recipe.transfer;

import java.util.List;

import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.registration.IRecipeTransferRegistration;

/**
 * Gives JEI the information it needs to transfer recipes from a slotted inventory into the crafting area.
 *
 * Most plugins with normal inventories can use the simpler
 * {@link IRecipeTransferRegistration#addRecipeTransferHandler(Class, RecipeType, int, int, int, int)}.
 *
 * Containers with slot ranges that contain gaps or other oddities can implement this interface directly.
 * Containers that need full control over the recipe transfer or do not use slots can implement {@link IRecipeTransferHandler}.
 */
public interface IRecipeTransferInfo<C extends AbstractContainerMenu, R> {
	/**
	 * Return the container class that this recipe transfer helper supports.
	 */
	Class<C> getContainerClass();

	/**
	 * Return the recipe type that this container can handle.
	 *
	 * @since 9.5.0
	 */
	default RecipeType<R> getRecipeType() {
		return new RecipeType<>(getRecipeCategoryUid(), getRecipeClass());
	}

	/**
	 * Return true if this recipe transfer info can handle the given container instance and recipe.
	 */
	boolean canHandle(C container, R recipe);

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

	/**
	 * Return the recipe class that this recipe transfer helper supports.
	 *
	 * @deprecated use {@link #getRecipeType()} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	Class<R> getRecipeClass();

	/**
	 * Return the recipe category that this container can handle.
	 *
	 * @deprecated use {@link #getRecipeType()} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	ResourceLocation getRecipeCategoryUid();
}
