package mezz.jei.api;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;

/**
 * The IRecipeManager offers several functions for retrieving and handling recipes.
 * The IRecipeManager instance is provided in JEIManager.
 * Available after JEI's FMLLoadCompleteEvent event
 */
public interface IRecipeRegistry {

	/** Returns the IRecipeHandler associated with the recipeClass or null if there is none */
	@Nullable
	IRecipeHandler getRecipeHandler(Class recipeClass);

	/** Returns a list of Recipe Categories that have the ItemStack as an input */
	@Nonnull
	ImmutableList<IRecipeCategory> getRecipeCategoriesWithInput(ItemStack input);

	/** Returns a list of Recipe Categories that have the Fluid as an input */
	@Nonnull
	ImmutableList<IRecipeCategory> getRecipeCategoriesWithInput(Fluid input);

	/** Returns a list of Recipe Categories that have the ItemStack as an output */
	@Nonnull
	ImmutableList<IRecipeCategory> getRecipeCategoriesWithOutput(ItemStack output);

	/** Returns a list of Recipe Categories that have the Fluid as an output */
	@Nonnull
	ImmutableList<IRecipeCategory> getRecipeCategoriesWithOutput(Fluid output);

	/** Returns a list of Recipes of recipeCategory that have the ItemStack as an input */
	@Nonnull
	ImmutableList<Object> getRecipesWithInput(IRecipeCategory recipeCategory, ItemStack input);

	/** Returns a list of Recipes of recipeCategory that have the Fluid as an input */
	@Nonnull
	ImmutableList<Object> getRecipesWithInput(IRecipeCategory recipeCategory, Fluid input);

	/** Returns a list of Recipes of recipeCategory that have the ItemStack as an output */
	@Nonnull
	ImmutableList<Object> getRecipesWithOutput(IRecipeCategory recipeCategory, ItemStack output);

	/** Returns a list of Recipes of recipeCategory that have the Fluid as an output */
	@Nonnull
	ImmutableList<Object> getRecipesWithOutput(IRecipeCategory recipeCategory, Fluid output);

}
