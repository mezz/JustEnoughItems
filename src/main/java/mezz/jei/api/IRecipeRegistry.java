package mezz.jei.api;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * The IRecipeManager offers several functions for retrieving and handling recipes.
 * The IRecipeManager instance is provided in JEIManager.
 * Get the instance from {@link IJeiRuntime#getRecipeRegistry()}.
 */
public interface IRecipeRegistry {

	/** Returns the IRecipeHandler associated with the recipeClass or null if there is none */
	@Nullable
	<T> IRecipeHandler<T> getRecipeHandler(Class<? extends T> recipeClass);

	/** Returns an unmodifiable list of all Recipe Categories */
	List<IRecipeCategory> getRecipeCategories();

	/** Returns an unmodifiable list of Recipe Categories */
	List<IRecipeCategory> getRecipeCategories(List<String> recipeCategoryUids);

	/** Returns an unmodifiable list of Recipe Categories that have the ItemStack as an input */
	List<IRecipeCategory> getRecipeCategoriesWithInput(ItemStack input);

	/** Returns an unmodifiable list of Recipe Categories that have the Fluid as an input */
	List<IRecipeCategory> getRecipeCategoriesWithInput(FluidStack input);

	/** Returns an unmodifiable list of Recipe Categories that have the ItemStack as an output */
	List<IRecipeCategory> getRecipeCategoriesWithOutput(ItemStack output);

	/** Returns an unmodifiable list of Recipe Categories that have the Fluid as an output */
	List<IRecipeCategory> getRecipeCategoriesWithOutput(FluidStack output);

	/** Returns an unmodifiable list of Recipes of recipeCategory that have the ItemStack as an input */
	List<Object> getRecipesWithInput(IRecipeCategory recipeCategory, ItemStack input);

	/** Returns an unmodifiable list of Recipes of recipeCategory that have the Fluid as an input */
	List<Object> getRecipesWithInput(IRecipeCategory recipeCategory, FluidStack input);

	/** Returns an unmodifiable list of Recipes of recipeCategory that have the ItemStack as an output */
	List<Object> getRecipesWithOutput(IRecipeCategory recipeCategory, ItemStack output);

	/** Returns an unmodifiable list of Recipes of recipeCategory that have the Fluid as an output */
	List<Object> getRecipesWithOutput(IRecipeCategory recipeCategory, FluidStack output);

	/** Returns an unmodifiable list of Recipes in recipeCategory */
	List<Object> getRecipes(IRecipeCategory recipeCategory);

	/**
	 * Returns an unmodifiable collection of ItemStacks that can craft recipes from recipeCategory.
	 * These are registered with {@link IModRegistry#addRecipeCategoryCraftingItem(ItemStack, String...)}.
	 *
	 * @since JEI 3.3.0
	 */
	Collection<ItemStack> getCraftingItems(IRecipeCategory recipeCategory);

	/**
	 * Add a new recipe while the game is running.
	 * This is only for things like gated recipes becoming available, like the ones in Thaumcraft.
	 * Use your IRecipeHandler.isValid to determine which recipes are hidden, and when a recipe becomes valid you can add it here.
	 * (note that IRecipeHandler.isValid must be true when the recipe is added here for it to work)
	 */
	void addRecipe(Object recipe);
}
