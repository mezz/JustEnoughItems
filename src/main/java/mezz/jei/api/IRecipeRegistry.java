package mezz.jei.api;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The IRecipeManager offers several functions for retrieving and handling recipes.
 * The IRecipeManager instance is provided in JEIManager.
 * Available after JEI's FMLLoadCompleteEvent event
 */
public interface IRecipeRegistry {

	/** Returns the IRecipeHandler associated with the recipeClass or null if there is none */
	@Nullable IRecipeHandler getRecipeHandler(Class recipeClass);

	/** Returns a list of Recipe Categories that have the ItemStack as an input */
	@Nonnull ImmutableList<IRecipeCategory> getRecipeCategoriesForInput(ItemStack input);

	/** Returns a list of Recipe Categories that have the ItemStack as an output */
	@Nonnull ImmutableList<IRecipeCategory> getRecipeCategoriesForOutput(ItemStack output);

	/** Returns a list of Recipes of recipeCategory that have the ItemStack as an input */
	@Nonnull ImmutableList<Object> getInputRecipes(IRecipeCategory recipeCategory, ItemStack input);

	/** Returns a list of Recipes of recipeCategory that have the ItemStack as an output */
	@Nonnull ImmutableList<Object> getOutputRecipes(IRecipeCategory recipeCategory, ItemStack output);

}
