package mezz.jei.gui.recipes;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.elements.IScrollbarController;
import mezz.jei.gui.recipes.lookups.IFocusedRecipes;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface IRecipeGuiLogic extends IScrollbarController {

	String getPageString();

	boolean isScrolling();

	RecipeGuiScrollState getScrollState();

	boolean scrollRecipes(double pixels);

	boolean hasMultipleCategories();

	boolean hasAllCategories();

	boolean previousRecipeCategory();

	RecipeGuiGrid getRecipeGuiGrid();

	boolean nextRecipeCategory();

	void setRecipeCategory(IRecipeCategory<?> category);

	boolean hasMultiplePages();

	void goToFirstPage();

	boolean previousPage();

	boolean nextPage();

	void tick();

	boolean showFocus(IFocusGroup focuses);

	boolean showRecipes(IFocusedRecipes<?> recipes, IFocusGroup focuses);

	boolean back();

	boolean forward();

	void clearHistory();

	boolean showAllRecipes();

	boolean showCategories(List<IRecipeType<?>> recipeTypes);

	IRecipeCategory<?> getSelectedRecipeCategory();

	@Unmodifiable
	List<IRecipeCategory<?>> getRecipeCategories();

	Stream<Consumer<IIngredientAcceptor<?>>> getCraftingStations();

	List<IRecipeLayoutWithButtons<?>> getVisibleRecipeLayoutsWithButtons(
		ImmutableSize2i availableSize,
		@Nullable AbstractContainerMenu container,
		BookmarkList bookmarkList,
		RecipesGui recipesGui
	);
}
