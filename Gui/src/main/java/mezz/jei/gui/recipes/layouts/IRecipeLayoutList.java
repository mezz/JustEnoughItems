package mezz.jei.gui.recipes.layouts;

import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.common.config.RecipeSorterStage;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.recipes.RecipeLayoutWithButtons;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.gui.recipes.lookups.IFocusedRecipes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IRecipeLayoutList {
	static IRecipeLayoutList create(
		Set<RecipeSorterStage> recipeSorterStages,
		@Nullable AbstractContainerMenu container,
		@Nullable Player player,
		IFocusedRecipes<?> selectedRecipes,
		IFocusGroup focusGroup,
		BookmarkList bookmarkList,
		IRecipeManager recipeManager,
		RecipesGui recipesGui
	) {
		return new LazyRecipeLayoutList<>(
			recipeSorterStages,
			container,
			player,
			selectedRecipes,
			bookmarkList,
			recipeManager,
			recipesGui,
			focusGroup
		);
	}

	int size();

	List<RecipeLayoutWithButtons<?>> subList(int from, int to);

	Optional<RecipeLayoutWithButtons<?>> findFirst();

	void tick();
}
