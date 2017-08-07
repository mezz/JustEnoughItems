package mezz.jei.recipes;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import mezz.jei.startup.StackHelper;
import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class DefaultCraftingRecipeWrapper implements IRecipeWrapper {
	@Nullable
	public static IRecipeWrapper create(IRecipe recipe) {
		ItemStack recipeOutput = recipe.getRecipeOutput();
		if (recipeOutput.isEmpty()) {
			return null;
		}

		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		if (!containsAnItemStack(ingredients)) {
			return null;
		}

		int width = 1;
		int height = 1;
		while (!recipe.canFit(width, height)) {
			if (width == height) {
				width++;
				height = 1;
				if (width > 3) {
					return null;
				}
			} else {
				int t = width;
				width = height;
				height = t;
				if (width > height) {
					height++;
				}
			}
		}

		// TODO: detect shapeless recipes

		Log.get().info("Created default recipe handler for {} -> {}", recipe.getClass(), Internal.getStackHelper().getUniqueIdentifierForStack(recipeOutput));
		return new DefaultShapedCraftingRecipeWrapper(recipe, width, height);
	}

	private static boolean containsAnItemStack(NonNullList<Ingredient> ingredients) {
		if (ingredients.isEmpty()) {
			return false;
		}
		for (Ingredient ingredient : ingredients) {
			ItemStack[] matchingStacks = ingredient.getMatchingStacks();
			for (ItemStack stack : matchingStacks) {
				if (!stack.isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

	private final IRecipe recipe;

	private DefaultCraftingRecipeWrapper(IRecipe recipe) {
		this.recipe = recipe;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		StackHelper stackHelper = Internal.getStackHelper();
		List<List<ItemStack>> inputs = stackHelper.expandRecipeItemStackInputs(recipe.getIngredients());
		ingredients.setInputLists(ItemStack.class, inputs);
		ingredients.setOutput(ItemStack.class, recipe.getRecipeOutput());
	}

	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY) {
		return Collections.singletonList("Default recipe");
	}

	public static class DefaultShapedCraftingRecipeWrapper extends DefaultCraftingRecipeWrapper implements IShapedCraftingRecipeWrapper {
		private final int width;
		private final int height;

		private DefaultShapedCraftingRecipeWrapper(IRecipe recipe, int width, int height) {
			super(recipe);
			this.width = width;
			this.height = height;
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}
	}
}
