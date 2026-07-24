package mezz.jei.gui.recipes;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.gui.elements.DrawableText;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class RecipeCategoryIconUtil {
	public static <T> IDrawable create(
		IRecipeCategory<T> recipeCategory,
		IRecipeManager recipeManager,
		IGuiHelper guiHelper
	) {
		IDrawable icon = recipeCategory.getIcon();
		if (icon != null) {
			return icon;
		}
		RecipeType<T> recipeType = recipeCategory.getRecipeType();
		Optional<ITypedIngredient<?>> firstCatalyst = recipeManager.createRecipeCatalystLookup(recipeType)
			.get()
			.findFirst();

		if (firstCatalyst.isPresent()) {
			ITypedIngredient<?> ingredient = firstCatalyst.get();
			return guiHelper.createDrawableIngredient(ingredient);
		} else {
			Component title = recipeCategory.getTitle();
			String text = title.getString().substring(0, 2);
			return new DrawableText(text, 16, 16, 0xE0E0E0);
		}
	}
}
