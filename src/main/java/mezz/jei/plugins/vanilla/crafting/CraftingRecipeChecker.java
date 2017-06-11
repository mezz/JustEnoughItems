package mezz.jei.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapperFactory;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public final class CraftingRecipeChecker {
	private CraftingRecipeChecker() {
	}

	public static List<IRecipe> getValidRecipes(final IJeiHelpers jeiHelpers) {
		CraftingRecipeValidator<ShapedOreRecipe> shapedOreRecipeValidator = new CraftingRecipeValidator<>(
				recipe -> new ShapedOreRecipeWrapper(jeiHelpers, recipe),
				ShapedOreRecipe::func_192400_c);

		CraftingRecipeValidator<ShapedRecipes> shapedRecipesValidator = new CraftingRecipeValidator<>(
				ShapedRecipesWrapper::new,
				recipe -> recipe.recipeItems);

		CraftingRecipeValidator<ShapelessOreRecipe> shapelessOreRecipeValidator = new CraftingRecipeValidator<>(
				recipe -> new ShapelessOreRecipeWrapper(jeiHelpers, recipe),
				ShapelessOreRecipe::func_192400_c);

		CraftingRecipeValidator<ShapelessRecipes> shapelessRecipesValidator = new CraftingRecipeValidator<>(
				ShapelessRecipesWrapper::new,
				recipe -> recipe.recipeItems);

		Iterator<IRecipe> recipeIterator = CraftingManager.field_193380_a.iterator();
		List<IRecipe> validRecipes = new ArrayList<>();
		while (recipeIterator.hasNext()) {
			IRecipe recipe = recipeIterator.next();
			if (recipe instanceof ShapedOreRecipe) {
				if (shapedOreRecipeValidator.isRecipeValid((ShapedOreRecipe) recipe)) {
					validRecipes.add(recipe);
				}
			} else if (recipe instanceof ShapedRecipes) {
				if (shapedRecipesValidator.isRecipeValid((ShapedRecipes) recipe)) {
					validRecipes.add(recipe);
				}
			} else if (recipe instanceof ShapelessOreRecipe) {
				if (shapelessOreRecipeValidator.isRecipeValid((ShapelessOreRecipe) recipe)) {
					validRecipes.add(recipe);
				}
			} else if (recipe instanceof ShapelessRecipes) {
				if (shapelessRecipesValidator.isRecipeValid((ShapelessRecipes) recipe)) {
					validRecipes.add(recipe);
				}
			} else {
				validRecipes.add(recipe);
			}
		}
		return validRecipes;
	}

	private static final class CraftingRecipeValidator<T extends IRecipe> {
		private static final int INVALID_COUNT = -1;
		private final IRecipeWrapperFactory<T> recipeWrapperFactory;
		private final IIngredientGetter<T> ingredientGetter;

		public CraftingRecipeValidator(IRecipeWrapperFactory<T> recipeWrapperFactory, IIngredientGetter<T> ingredientGetter) {
			this.recipeWrapperFactory = recipeWrapperFactory;
			this.ingredientGetter = ingredientGetter;
		}

		public boolean isRecipeValid(T recipe) {
			ItemStack recipeOutput = recipe.getRecipeOutput();
			//noinspection ConstantConditions
			if (recipeOutput == null || recipeOutput.isEmpty()) {
				String recipeInfo = getInfo(recipe);
				Log.get().error("Recipe has no output. {}", recipeInfo);
				return false;
			}
			List<Ingredient> ingredients = ingredientGetter.getIngredients(recipe);
			int inputCount = getInputCount(ingredients);
			if (inputCount == INVALID_COUNT) {
				return false;
			} else if (inputCount > 9) {
				String recipeInfo = getInfo(recipe);
				Log.get().error("Recipe has too many inputs. {}", recipeInfo);
				return false;
			} else if (inputCount == 0) {
				String recipeInfo = getInfo(recipe);
				Log.get().error("Recipe has no inputs. {}", recipeInfo);
				return false;
			}
			return true;
		}

		private String getInfo(T recipe) {
			IRecipeWrapper recipeWrapper = recipeWrapperFactory.getRecipeWrapper(recipe);
			return ErrorUtil.getInfoFromRecipe(recipe, recipeWrapper);
		}

		protected static int getInputCount(List<Ingredient> ingredientList) {
			int inputCount = 0;
			for (Ingredient ingredient : ingredientList) {
				ItemStack[] input = ingredient.func_193365_a();
				//noinspection ConstantConditions
				if (input == null) {
					return INVALID_COUNT;
				} else if (input.length >= 0) {
					inputCount++;
				}
			}
			return inputCount;
		}

		@FunctionalInterface
		interface IIngredientGetter<T extends IRecipe> {
			List<Ingredient> getIngredients(T recipe);
		}
	}
}
