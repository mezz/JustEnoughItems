package mezz.jei.gui.recipes.layouts;

import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.common.config.RecipeSorterStage;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.recipes.IRecipeLayoutWithButtons;
import mezz.jei.gui.recipes.IRecipeLayoutWithButtonsFactory;
import mezz.jei.gui.recipes.lookups.IFocusedRecipes;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IRecipeLayoutList {
	static IRecipeLayoutList create(
		Set<RecipeSorterStage> recipeSorterStages,
		IFocusedRecipes<?> selectedRecipes,
		IFocusGroup focusGroup,
		BookmarkList bookmarks,
		IRecipeManager recipeManager,
		IRecipeLayoutWithButtonsFactory recipeLayoutFactory
	) {
		return createTyped(recipeSorterStages, selectedRecipes, focusGroup, bookmarks, recipeManager, recipeLayoutFactory);
	}

	private static <T> IRecipeLayoutList createTyped(
		Set<RecipeSorterStage> recipeSorterStages,
		IFocusedRecipes<T> selectedRecipes,
		IFocusGroup focusGroup,
		BookmarkList bookmarks,
		IRecipeManager recipeManager,
		IRecipeLayoutWithButtonsFactory recipeLayoutFactory
	) {
		return new LazyRecipeLayoutList<>(
			recipeSorterStages,
			selectedRecipes,
			focusGroup,
			bookmarks,
			recipeManager,
			recipeLayoutFactory
		);
	}

	int size();

	List<IRecipeLayoutWithButtons<?>> subList(int from, int to, @Nullable AbstractContainerMenu container);

	Optional<IRecipeLayoutWithButtons<?>> findFirst(@Nullable AbstractContainerMenu container);

	void tick(@Nullable AbstractContainerMenu container);
}
