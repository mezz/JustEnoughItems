package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.recipes.lookups.IFocusedRecipes;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

	boolean showFocus(IFocusGroup focuses);

	boolean showRecipes(IFocusedRecipes<?> recipes, IFocusGroup focuses);

	boolean back();

	void clearHistory();

	boolean showAllRecipes();

	boolean showCategories(List<RecipeType<?>> recipeTypes);

	IRecipeCategory<?> getSelectedRecipeCategory();

	@Unmodifiable
	List<IRecipeCategory<?>> getRecipeCategories();

	Stream<ITypedIngredient<?>> getRecipeCatalysts();
	Stream<ITypedIngredient<?>> getRecipeCatalysts(IRecipeCategory<?> recipeCategory);

	List<IRecipeLayoutDrawable<?>> getRecipeLayouts();

	Optional<ImmutableRect2i> getRecipeLayoutSizeWithBorder();
}
