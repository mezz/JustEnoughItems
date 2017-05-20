package mezz.jei.util;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public final class LegacyUtil {
	private LegacyUtil() {
	}

	public static <T> List<String> getTooltip(IIngredientRenderer<T> ingredientRenderer, Minecraft minecraft, T ingredient, boolean advanced) {
		try {
			return ingredientRenderer.getTooltip(minecraft, ingredient, advanced);
		} catch (AbstractMethodError ignored) {
			//noinspection deprecation
			return ingredientRenderer.getTooltip(minecraft, ingredient);
		}
	}

	public static <T> T getIngredientCopy(T value, IIngredientHelper<T> ingredientHelper) {
		try {
			return ingredientHelper.copyIngredient(value);
		} catch (AbstractMethodError ignored) {
			return value;
		}
	}

	public static List<String> getTooltipStrings(IRecipeCategory<?> recipeCategory, int recipeMouseX, int recipeMouseY) {
		try {
			return recipeCategory.getTooltipStrings(recipeMouseX, recipeMouseY);
		} catch (AbstractMethodError ignored) {
			return new ArrayList<String>();
		}
	}

	public static <T> String getResourceId(T ingredient, IIngredientHelper<T> ingredientHelper) {
		try {
			return ingredientHelper.getResourceId(ingredient);
		} catch (AbstractMethodError ignored) {
			return ingredientHelper.getUniqueId(ingredient);
		}
	}

	public static <T> ItemStack cheatIngredient(T focusValue, IIngredientHelper<T> ingredientHelper, boolean fullStack) {
		try {
			return ingredientHelper.cheatIngredient(focusValue, fullStack);
		} catch (AbstractMethodError ignored) {
			return ItemStack.EMPTY;
		}
	}
}
