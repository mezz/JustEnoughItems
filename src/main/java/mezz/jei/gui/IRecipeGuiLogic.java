package mezz.jei.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import mezz.jei.api.recipe.IRecipeCategory;

public interface IRecipeGuiLogic {
	enum Mode {
		INPUT, OUTPUT
	}

	@Nonnull
	String getPageString();

	void setRecipesPerPage(int recipesPerPage);

	boolean hasMultipleCategories();

	void previousRecipeCategory();

	void nextRecipeCategory();

	boolean hasMultiplePages();

	void previousPage();

	void nextPage();

	boolean setFocus(@Nonnull Focus focus, @Nonnull Mode mode);

	@Nullable
	IRecipeCategory getRecipeCategory();

	@Nonnull
	List<RecipeWidget> getRecipeWidgets();
}
