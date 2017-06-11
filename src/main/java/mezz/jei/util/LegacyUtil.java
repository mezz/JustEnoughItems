package mezz.jei.util;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.item.ItemStack;

public final class LegacyUtil {
	private LegacyUtil() {
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
			return Collections.emptyList();
		}
	}

	public static <T> String getResourceId(T ingredient, IIngredientHelper<T> ingredientHelper) {
		try {
			return ingredientHelper.getResourceId(ingredient);
		} catch (AbstractMethodError ignored) {
			return ingredientHelper.getUniqueId(ingredient);
		}
	}

	@Nullable
	public static String getModName(IRecipeCategory recipeCategory) {
		try {
			return recipeCategory.getModName();
		} catch (AbstractMethodError ignored) {
			return null;
		}
	}
}
