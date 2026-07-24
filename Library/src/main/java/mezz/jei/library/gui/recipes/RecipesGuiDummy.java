package mezz.jei.library.gui.recipes;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RecipesGuiDummy implements IRecipesGui {
	public static final IRecipesGui INSTANCE = new RecipesGuiDummy();

	public RecipesGuiDummy() {

	}

	@Override
	public <V> void show(IFocus<V> focus) {

	}

	@Override
	public void show(List<IFocus<?>> focuses) {

	}

	@Override
	public void showTypes(List<RecipeType<?>> recipeTypes) {

	}

	@Override
	public <T> void showRecipes(IRecipeCategory<T> recipeCategory, List<T> recipes, List<IFocus<?>> focuses) {

	}

	@Override
	@Nullable
	public <T> T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		return null;
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated
	public void showCategories(List<ResourceLocation> recipeCategoryUids) {

	}
}
