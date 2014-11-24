package mezz.jei.api.recipe;

import mezz.jei.api.recipe.type.IRecipeType;
import mezz.jei.api.recipe.type.IRecipeTypeKey;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

	void registerRecipeType(IRecipeTypeKey recipeTypeKey, IRecipeType recipeType);
	void registerRecipeHelpers(IRecipeHelper... recipeHelpers);
	void addRecipes(Iterable recipes);

	/**
	 *  Using Recipes
	 *  Available after JEI's FMLLoadCompleteEvent event
	 */

	@Nullable
	IRecipeType getRecipeType(IRecipeTypeKey recipeTypeKey);

	/* Returns the IRecipeHelper associated with the recipeClass or null if there is none */
	@Nullable
	IRecipeHelper getRecipeHelper(Class recipeClass);

	/* Returns a list of Recipe Types that have the ItemStack as an input */
	@Nonnull
	List<IRecipeType> getRecipeTypesForInput(ItemStack input);

	/* Returns a list of Recipe Types that have the ItemStack as an output */
	@Nonnull
	List<IRecipeType> getRecipeTypesForOutput(ItemStack output);

	/* Returns a list of Recipes of recipeType that have the ItemStack as an input */
	@Nonnull
	List<Object> getInputRecipes(IRecipeType recipeType, ItemStack input);

	/* Returns a list of Recipes of recipeType that have the ItemStack as an output */
	@Nonnull
	List<Object> getOutputRecipes(IRecipeType recipeType, ItemStack output);
}
