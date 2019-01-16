package mezz.jei.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapperFactory;
import mezz.jei.plugins.vanilla.furnace.FurnaceRecipeWrapper;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class RecipeValidator {
	private static final Logger LOGGER = LogManager.getLogger();

	public static class Results {
		private final List<IRecipe> craftingRecipes = new ArrayList<>();
		private final List<IRecipe> furnaceRecipes = new ArrayList<>();

		public List<IRecipe> getCraftingRecipes() {
			return craftingRecipes;
		}

		public List<IRecipe> getFurnaceRecipes() {
			return furnaceRecipes;
		}
	}

	private RecipeValidator() {
	}

	public static Results getValidRecipes(final IJeiHelpers jeiHelpers) {
		CraftingRecipeValidator<IShapedRecipe> shapedRecipesValidator = new CraftingRecipeValidator<>(recipe -> new ShapedRecipesWrapper(jeiHelpers, recipe));
		CraftingRecipeValidator<IRecipe> shapelessRecipesValidator = new CraftingRecipeValidator<>(recipe -> new ShapelessRecipeWrapper<>(jeiHelpers, recipe));
		CraftingRecipeValidator<FurnaceRecipe> furnaceRecipesValidator = new CraftingRecipeValidator<>(recipe -> new FurnaceRecipeWrapper(jeiHelpers, recipe));

		Results results = new Results();
		WorldClient world = Minecraft.getInstance().world;
		RecipeManager recipeManager = world.getRecipeManager();
		for (IRecipe recipe : recipeManager.getRecipes()) {
			if (recipe instanceof FurnaceRecipe) {
				if (furnaceRecipesValidator.isRecipeValid((FurnaceRecipe) recipe)) {
					results.furnaceRecipes.add(recipe);
				}
			} else if (recipe instanceof IShapedRecipe) {
				if (shapedRecipesValidator.isRecipeValid((IShapedRecipe) recipe)) {
					results.craftingRecipes.add(recipe);
				}
			} else {
				if (shapelessRecipesValidator.isRecipeValid(recipe)) {
					results.craftingRecipes.add(recipe);
				}
			}
		}
		return results;
	}

	private static final class CraftingRecipeValidator<T extends IRecipe> {
		private static final int INVALID_COUNT = -1;
		private final IRecipeWrapperFactory<T> recipeWrapperFactory;

		public CraftingRecipeValidator(IRecipeWrapperFactory<T> recipeWrapperFactory) {
			this.recipeWrapperFactory = recipeWrapperFactory;
		}

		public boolean isRecipeValid(T recipe) {
			if (recipe.isDynamic()) {
				return false;
			}
			ItemStack recipeOutput = recipe.getRecipeOutput();
			if (recipeOutput == null || recipeOutput.isEmpty()) {
				String recipeInfo = getInfo(recipe);
				LOGGER.error("Recipe has no output. {}", recipeInfo);
				return false;
			}
			List<Ingredient> ingredients = recipe.getIngredients();
			if (ingredients == null) {
				String recipeInfo = getInfo(recipe);
				LOGGER.error("Recipe has no input Ingredients. {}", recipeInfo);
				return false;
			}
			int inputCount = getInputCount(ingredients);
			if (inputCount == INVALID_COUNT) {
				return false;
			} else if (inputCount > 9) {
				String recipeInfo = getInfo(recipe);
				LOGGER.error("Recipe has too many inputs. {}", recipeInfo);
				return false;
			} else if (inputCount == 0) {
				String recipeInfo = getInfo(recipe);
				LOGGER.error("Recipe has no inputs. {}", recipeInfo);
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
				ItemStack[] input = ingredient.getMatchingStacks();
				if (input == null) {
					return INVALID_COUNT;
				} else {
					inputCount++;
				}
			}
			return inputCount;
		}
	}
}
