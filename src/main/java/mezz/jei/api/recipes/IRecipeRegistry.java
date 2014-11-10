package mezz.jei.api.recipes;

import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * IRecipeManager is used to register new IRecipeHelpers and recipes.
 * Once registered, the IRecipeManager offers several functions for retrieving and handling recipes.
 * The IRecipeManager instance is provided in JEIManager.
 */
public interface IRecipeRegistry {

	/**
	 *  Registering Recipes
	 */

	/* Register a new IRecipeHelper with JEI */
	void registerRecipeHelper(IRecipeHelper recipeHelper);

	/* Add new recipes to JEI */
	void addRecipes(Iterable<Object> recipes);

	/**
	 *  Using Recipes
	 */

	/* Returns true if there is an IRecipeHelper registered for the recipeClass */
	boolean hasRecipeHelper(Class recipeClass);

	/* Returns the IRecipeHelper associated with the recipeClass */
	IRecipeHelper getRecipeHelper(Class recipeClass);

	/* Returns a list of Recipe Classes that have the ItemStack as an input */
	List<Class> getInputRecipeClasses(ItemStack input);

	/* Returns a list of Recipe Classes that have the ItemStack as an output */
	List<Class> getOutputRecipeClasses(ItemStack output);

	/* Returns a list of Recipes of type recipeClass that have the ItemStack as an input */
	List<Object> getInputRecipes(Class recipeClass, ItemStack input);

	/* Returns a list of Recipes of type recipeClass that have the ItemStack as an output */
	List<Object> getOutputRecipes(Class recipeClass, ItemStack output);
}
