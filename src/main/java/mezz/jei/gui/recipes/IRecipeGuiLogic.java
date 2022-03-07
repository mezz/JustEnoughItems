package mezz.jei.gui.recipes;

import java.util.List;
import java.util.stream.Stream;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;

import mezz.jei.api.recipe.category.IRecipeCategory;
import org.jetbrains.annotations.Unmodifiable;

public interface IRecipeGuiLogic {

	String getPageString();

	void setRecipesPerPage(int recipesPerPage);

	boolean hasMultipleCategories();

	boolean hasAllCategories();

	void previousRecipeCategory();

	void nextRecipeCategory();

	void setRecipeCategory(IRecipeCategory<?> category);

	boolean hasMultiplePages();

	void previousPage();

	void nextPage();

	boolean setFocus(IFocusGroup focuses);

	boolean back();

	void clearHistory();

	boolean setCategoryFocus();

	boolean setCategoryFocus(List<RecipeType<?>> recipeTypes);

	IRecipeCategory<?> getSelectedRecipeCategory();

	@Unmodifiable
	List<IRecipeCategory<?>> getRecipeCategories();

	Stream<ITypedIngredient<?>> getRecipeCatalysts();
	Stream<ITypedIngredient<?>> getRecipeCatalysts(IRecipeCategory<?> recipeCategory);

	List<RecipeLayout<?>> getRecipeLayouts(int posX, int posY, int spacingY);
}
