package mezz.jei.api;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeType;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The IRecipeManager offers several functions for retrieving and handling recipes.
 * The IRecipeManager instance is provided in JEIManager.
 */
public interface IRecipeRegistry {

	/**
	 *  Available after JEI's FMLLoadCompleteEvent event
	 */

	/** Returns the IRecipeHandler associated with the recipeClass or null if there is none */
	@Nullable
	IRecipeHandler getRecipeHandler(Class recipeClass);

	/** Returns Recipe Types that have the ItemStack as an input */
	@Nonnull
	ImmutableList<IRecipeType> getRecipeTypesForInput(ItemStack input);

	/** Returns a list of Recipe Types that have the ItemStack as an output */
	@Nonnull
	ImmutableList<IRecipeType> getRecipeTypesForOutput(ItemStack output);

	/** Returns a list of Recipes of recipeType that have the ItemStack as an input */
	@Nonnull
	ImmutableList<Object> getInputRecipes(IRecipeType recipeType, ItemStack input);

	/** Returns a list of Recipes of recipeType that have the ItemStack as an output */
	@Nonnull
	ImmutableList<Object> getOutputRecipes(IRecipeType recipeType, ItemStack output);

}
