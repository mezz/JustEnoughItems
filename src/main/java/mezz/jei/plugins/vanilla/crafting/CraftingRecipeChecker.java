package mezz.jei.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public final class CraftingRecipeChecker {
	private CraftingRecipeChecker() {
	}

	public static List<IRecipe> getValidRecipes(final IJeiHelpers jeiHelpers) {
		CraftingRecipeValidator<ShapedOreRecipe> shapedOreRecipeValidator = new CraftingRecipeValidator<ShapedOreRecipe>() {
			@Override
			protected IRecipeWrapper getRecipeWrapper(ShapedOreRecipe recipe) {
				return new ShapedOreRecipeWrapper(jeiHelpers, recipe);
			}

			@Override
			protected int getInputCount(ShapedOreRecipe recipe) {
				return getInputCount(recipe.func_192400_c());
			}
		};

		CraftingRecipeValidator<ShapedRecipes> shapedRecipesValidator = new CraftingRecipeValidator<ShapedRecipes>() {
			@Override
			protected IRecipeWrapper getRecipeWrapper(ShapedRecipes recipe) {
				return new ShapedRecipesWrapper(recipe);
			}

			@Override
			protected int getInputCount(ShapedRecipes recipe) {
				return getInputCount(recipe.recipeItems);
			}
		};

		CraftingRecipeValidator<ShapelessOreRecipe> shapelessOreRecipeValidator = new CraftingRecipeValidator<ShapelessOreRecipe>() {
			@Override
			protected IRecipeWrapper getRecipeWrapper(ShapelessOreRecipe recipe) {
				return new ShapelessOreRecipeWrapper(jeiHelpers, recipe);
			}

			@Override
			protected int getInputCount(ShapelessOreRecipe recipe) {
				return getInputCount(recipe.func_192400_c());
			}
		};

		CraftingRecipeValidator<ShapelessRecipes> shapelessRecipesValidator = new CraftingRecipeValidator<ShapelessRecipes>() {
			@Override
			protected IRecipeWrapper getRecipeWrapper(ShapelessRecipes recipe) {
				return new ShapelessRecipesWrapper(recipe);
			}

			@Override
			protected int getInputCount(ShapelessRecipes recipe) {
				return getInputCount(recipe.recipeItems);
			}
		};

		Iterator<IRecipe> recipeIterator = CraftingManager.field_193380_a.iterator();
		List<IRecipe> validRecipes = new ArrayList<IRecipe>();
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

	// TODO Java 8 use lambdas to implement
	private static abstract class CraftingRecipeValidator<T extends IRecipe> {
		private static final int INVALID_COUNT = -1;

		public boolean isRecipeValid(T recipe) {
			ItemStack recipeOutput = recipe.getRecipeOutput();
			//noinspection ConstantConditions
			if (recipeOutput == null || recipeOutput.isEmpty()) {
				String recipeInfo = getInfo(recipe);
				Log.error("Recipe has no output. {}", recipeInfo);
				return false;
			}
			int inputCount = getInputCount(recipe);
			if (inputCount == INVALID_COUNT) {
				return false;
			} else if (inputCount > 9) {
				String recipeInfo = getInfo(recipe);
				Log.error("Recipe has too many inputs. {}", recipeInfo);
				return false;
			} else if (inputCount == 0) {
				String recipeInfo = getInfo(recipe);
				Log.error("Recipe has no inputs. {}", recipeInfo);
				return false;
			}
			return true;
		}

		private String getInfo(T recipe) {
			IRecipeWrapper recipeWrapper = getRecipeWrapper(recipe);
			return ErrorUtil.getInfoFromRecipe(recipe, recipeWrapper);
		}

		protected abstract IRecipeWrapper getRecipeWrapper(T recipe);

		protected abstract int getInputCount(T recipe);

		protected static int getInputCount(Object[] objectList) {
			return getInputCount(Arrays.asList(objectList));
		}

		protected static int getInputCount(List<?> objectList) {
			int inputCount = 0;
			for (Object input : objectList) {
				if (input instanceof List) {
					if (((List) input).isEmpty()) {
						// missing items for an oreDict name. This is normal behavior, but the recipe is invalid.
						return INVALID_COUNT;
					} else {
						inputCount++;
					}
				} else if (input instanceof ItemStack) {
					ItemStack itemStack = (ItemStack) input;
					if (!itemStack.isEmpty()) {
						inputCount++;
					}
				} else if (input != null) {
					inputCount++;
				}
			}
			return inputCount;
		}
	}
}
