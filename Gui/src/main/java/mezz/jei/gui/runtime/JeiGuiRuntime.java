package mezz.jei.gui.runtime;

import mezz.jei.api.gui.buttons.ButtonSnapshot;
import mezz.jei.api.runtime.IBookmarkManager;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiGuiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.recipes.RecipesGui;

import java.util.Optional;

public record JeiGuiRuntime(
	IngredientListOverlay ingredientListOverlay,
	BookmarkOverlay bookmarkOverlay,
	RecipesGui recipesGui,
	IIngredientFilter ingredientFilter,
	IBookmarkManager bookmarkManager
) implements IJeiGuiRuntime {
	@Override
	public IRecipesGui getRecipesGui() {
		return recipesGui;
	}

	@Override
	public IIngredientListOverlay getIngredientListOverlay() {
		return ingredientListOverlay;
	}

	@Override
	public IIngredientFilter getIngredientFilter() {
		return ingredientFilter;
	}

	@Override
	public IBookmarkOverlay getBookmarkOverlay() {
		return bookmarkOverlay;
	}

	@Override
	public IBookmarkManager getBookmarkManager() {
		return bookmarkManager;
	}

	@Override
	public Optional<ButtonSnapshot> getConfigButton() {
		return ingredientListOverlay.getConfigButton();
	}

	@Override
	public Optional<ButtonSnapshot> getBookmarkToggleButton() {
		return bookmarkOverlay.getBookmarkToggleButton();
	}
}
