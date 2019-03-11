package mezz.jei.api.registration;

import java.util.Collection;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.runtime.IIngredientManager;

/**
 * Allows registration of new types of ingredients, beyond the basic ItemStack and FluidStack.
 * After every mod has registered its ingredients, the {@link IIngredientManager} is created from this information.
 *
 * This is given to your {@link IModPlugin#registerIngredients(IModIngredientRegistration)}.
 */
public interface IModIngredientRegistration {
	ISubtypeManager getSubtypeManager();

	/**
	 * Register a new type of ingredient.
	 *
	 * @param ingredientType     The type of the ingredient.
	 * @param allIngredients     A collection of every to be displayed in the ingredient list.
	 * @param ingredientHelper   The ingredient helper to allows JEI to get information about ingredients for searching and other purposes.
	 * @param ingredientRenderer The ingredient render to allow JEI to render these ingredients in the ingredient list.
	 *                           This ingredient renderer must be configured to draw in a 16 by 16 pixel space.
	 */
	<V> void register(
		IIngredientType<V> ingredientType,
		Collection<V> allIngredients,
		IIngredientHelper<V> ingredientHelper,
		IIngredientRenderer<V> ingredientRenderer
	);
}
