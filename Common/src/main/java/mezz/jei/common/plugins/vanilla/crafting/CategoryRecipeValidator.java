package mezz.jei.common.plugins.vanilla.crafting;

import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.util.RecipeErrorUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public final class CategoryRecipeValidator<T extends Recipe<?>> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int INVALID_COUNT = -1;
	private final IRecipeCategory<T> recipeCategory;
	private final int maxInputs;

	public CategoryRecipeValidator(IRecipeCategory<T> recipeCategory, int maxInputs) {
		this.recipeCategory = recipeCategory;
		this.maxInputs = maxInputs;
	}

	public boolean isRecipeValid(T recipe) {
		return hasValidInputsAndOutputs(recipe);
	}

	public boolean isRecipeHandled(T recipe) {
		return this.recipeCategory.isHandled(recipe);
	}

	@SuppressWarnings("ConstantConditions")
	private boolean hasValidInputsAndOutputs(T recipe) {
		if (recipe.isSpecial()) {
			return true;
		}
		ItemStack recipeOutput = recipe.getResultItem();
		if (recipeOutput == null || recipeOutput.isEmpty()) {
			String recipeInfo = RecipeErrorUtil.getInfoFromRecipe(recipe, recipeCategory);
			LOGGER.error("Recipe has no output. {}", recipeInfo);
			return false;
		}
		List<Ingredient> ingredients = recipe.getIngredients();
		if (ingredients == null) {
			String recipeInfo = RecipeErrorUtil.getInfoFromRecipe(recipe, recipeCategory);
			LOGGER.error("Recipe has no input Ingredients. {}", recipeInfo);
			return false;
		}
		int inputCount = getInputCount(ingredients);
		if (inputCount == INVALID_COUNT) {
			return false;
		} else if (inputCount > maxInputs) {
			String recipeInfo = RecipeErrorUtil.getInfoFromRecipe(recipe, recipeCategory);
			LOGGER.error("Recipe has too many inputs. {}", recipeInfo);
			return false;
		} else if (inputCount == 0 && maxInputs > 0) {
			String recipeInfo = RecipeErrorUtil.getInfoFromRecipe(recipe, recipeCategory);
			LOGGER.error("Recipe has no inputs. {}", recipeInfo);
			return false;
		}
		return true;
	}

	@SuppressWarnings("ConstantConditions")
	private static int getInputCount(List<Ingredient> ingredientList) {
		int inputCount = 0;
		for (Ingredient ingredient : ingredientList) {
			ItemStack[] input = ingredient.getItems();
			if (input == null) {
				return INVALID_COUNT;
			} else {
				inputCount++;
			}
		}
		return inputCount;
	}
}
