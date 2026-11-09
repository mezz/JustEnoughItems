package mezz.jei.library.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;

public class RecipesGuiDummy implements IRecipesGui {
	public static final IRecipesGui INSTANCE = new RecipesGuiDummy();

	public RecipesGuiDummy() {

	}

	@Override
	public void show(List<IFocus<?>> focuses) {

	}

	@Override
	public List<IFocus<?>> getFocuses() {
		return List.of();
	}

	@Override
	public void showTypes(List<RecipeType<?>> recipeTypes) {

	}

	@Override
	public List<IRecipeCategory<?>> getRecipeCategories() {
		return List.of();
	}

	@Override
	public Optional<IRecipeCategory<?>> getSelectedRecipeCategory() {
		return Optional.empty();
	}

	@Override
	public boolean setRecipeCategory(RecipeType<?> recipeType) {
		return false;
	}

	@Override
	public <T> void showRecipes(IRecipeCategory<T> recipeCategory, List<T> recipes, List<IFocus<?>> focuses) {

	}

	@Override
	public int getPageIndex() {
		return -1;
	}

	@Override
	public int getPageCount() {
		return 0;
	}

	@Override
	public List<IRecipeLayoutDrawable<?>> getVisibleRecipeLayouts() {
		return List.of();
	}

	@Override
	public <T> Optional<T> getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		return Optional.empty();
	}

	@Override
	public boolean isOpen() {
		return false;
	}

	@Override
	public Optional<Screen> getParentScreen() {
		return Optional.empty();
	}
}
