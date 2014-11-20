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

	/* Register new IRecipeHelpers with JEI */
	void registerRecipeHelpers(IRecipeHelper... recipeHelpers);

	/* Add new recipes to JEI */
	void addRecipes(Iterable recipes);

	/**
	 *  Using Recipes
	 */

	/* Returns the IRecipeHelper associated with the recipeClass or null if there is none */
	IRecipeHelper getRecipeHelper(Class recipeClass);

	/* Returns a list of Recipe Types that have the ItemStack as an input */
	List<IRecipeType> getRecipeTypesForInput(ItemStack input);

	/* Returns a list of Recipe Types that have the ItemStack as an output */
	List<IRecipeType> getRecipeTypesForOutput(ItemStack output);

	/* Returns a list of Recipes of recipeType that have the ItemStack as an input */
	List<Object> getInputRecipes(IRecipeType recipeType, ItemStack input);

	/* Returns a list of Recipes of recipeType that have the ItemStack as an output */
	List<Object> getOutputRecipes(IRecipeType recipeType, ItemStack output);
}
