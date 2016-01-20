package mezz.jei.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;

/**
 * The IRecipeManager offers several functions for retrieving and handling recipes.
 * The IRecipeManager instance is provided in JEIManager.
 * Available to IModPlugins
 */
public interface IRecipeRegistry {

	/** Returns the IRecipeHandler associated with the recipeClass or null if there is none */
	@Nullable
	IRecipeHandler getRecipeHandler(@Nonnull Class recipeClass);

	/** Returns an unmodifiable list of all Recipe Categories */
	@Nonnull
	List<IRecipeCategory> getRecipeCategories();

	/** Returns an unmodifiable list of Recipe Categories */
	@Nonnull
	List<IRecipeCategory> getRecipeCategories(@Nonnull List<String> recipeCategoryUids);

	/** Returns an unmodifiable list of Recipe Categories that have the ItemStack as an input */
	@Nonnull
	List<IRecipeCategory> getRecipeCategoriesWithInput(@Nonnull ItemStack input);

	/** Returns an unmodifiable list of Recipe Categories that have the Fluid as an input */
	@Nonnull
	List<IRecipeCategory> getRecipeCategoriesWithInput(@Nonnull Fluid input);

	/** Returns an unmodifiable list of Recipe Categories that have the ItemStack as an output */
	@Nonnull
	List<IRecipeCategory> getRecipeCategoriesWithOutput(@Nonnull ItemStack output);

	/** Returns an unmodifiable list of Recipe Categories that have the Fluid as an output */
	@Nonnull
	List<IRecipeCategory> getRecipeCategoriesWithOutput(@Nonnull Fluid output);

	/** Returns an unmodifiable list of Recipes of recipeCategory that have the ItemStack as an input */
	@Nonnull
	List<Object> getRecipesWithInput(@Nonnull IRecipeCategory recipeCategory, @Nonnull ItemStack input);

	/** Returns an unmodifiable list of Recipes of recipeCategory that have the Fluid as an input */
	@Nonnull
	List<Object> getRecipesWithInput(@Nonnull IRecipeCategory recipeCategory, @Nonnull Fluid input);

	/** Returns an unmodifiable list of Recipes of recipeCategory that have the ItemStack as an output */
	@Nonnull
	List<Object> getRecipesWithOutput(@Nonnull IRecipeCategory recipeCategory, @Nonnull ItemStack output);

	/** Returns an unmodifiable list of Recipes of recipeCategory that have the Fluid as an output */
	@Nonnull
	List<Object> getRecipesWithOutput(@Nonnull IRecipeCategory recipeCategory, @Nonnull Fluid output);

	/** Returns an unmodifiable list of Recipes in recipeCategory */
	@Nonnull
	List<Object> getRecipes(@Nonnull IRecipeCategory recipeCategory);

	/**
	 * Add a new recipe while the game is running.
	 * This is only for things like gated recipes becoming available, like the ones in Thaumcraft.
	 * Use your IRecipeHandler.isValid to determine which recipes are hidden, and when a recipe becomes valid you can add it here.
	 * (note that IRecipeHandler.isValid must be true when the recipe is added here for it to work)
	 */
	void addRecipe(@Nonnull Object recipe);
}
