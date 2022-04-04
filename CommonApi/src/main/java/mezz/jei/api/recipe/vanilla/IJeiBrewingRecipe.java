package mezz.jei.api.recipe.vanilla;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * The vanilla and modded brewing recipes are not suitable for JEI
 * because they do not expose their inputs and outputs,
 * so JEI creates these recipes to use internally.
 *
 * Create your own with {@link IVanillaRecipeFactory#createBrewingRecipe}
 */
public interface IJeiBrewingRecipe {
	/**
	 * Get the input potion, that is used to create a new one.
	 * Normally this will be one potion, but a list will display several in rotation.
	 * Each of the 3 brewing slots will always display the same potion.
	 *
	 * @since 9.5.0
	 */
	@Unmodifiable
	List<ItemStack> getPotionInputs();

	/**
	 * Get the ingredients added to a potion to create a new one.
	 * Normally this will be one ingredient, but a list will display several in rotation.
	 *
	 * @since 9.5.0
	 */
	@Unmodifiable
	List<ItemStack> getIngredients();

	/**
	 * Get the potion result from this recipe.
	 *
	 * @since 9.5.0
	 */
	ItemStack getPotionOutput();

	/**
	 * @return the number of steps to brew the potion, starting at 0 for the water bottle.
	 * If the number of steps is unknown because there is no path found back to the water bottle,
	 * then this will return {@link Integer#MAX_VALUE}.
	 */
	int getBrewingSteps();
}
