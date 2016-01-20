package mezz.jei.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import mezz.jei.api.recipe.IRecipeCategory;

public interface IRecipeGuiLogic {

	@Nonnull
	String getPageString();

	void setRecipesPerPage(int recipesPerPage);

	boolean hasMultipleCategories();

	void previousRecipeCategory();

	void nextRecipeCategory();

	boolean hasMultiplePages();

	void previousPage();

	void nextPage();

	boolean setFocus(@Nonnull Focus focus);

	boolean back();

	boolean setCategoryFocus();

	boolean setCategoryFocus(List<String> recipeCategoryUids);

	@Nullable
	Focus getFocus();

	@Nullable
	IRecipeCategory getRecipeCategory();

	@Nonnull
	List<RecipeLayout> getRecipeWidgets(int posX, int posY, int spacingY);
}
