package mezz.jei.library.load.registration;

import mezz.jei.api.gui.buttons.ButtonSnapshot;
import mezz.jei.api.runtime.IBookmarkManager;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiGuiRuntime;
import mezz.jei.api.runtime.IRecipesGui;

import java.util.Optional;

record RegisteredGuiRuntime(
	IRecipesGui recipesGui,
	IIngredientListOverlay ingredientListOverlay,
	IIngredientFilter ingredientFilter,
	IBookmarkOverlay bookmarkOverlay,
	IBookmarkManager bookmarkManager,
	IJeiGuiRuntime buttonSource
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
		return buttonSource.getConfigButton();
	}

	@Override
	public Optional<ButtonSnapshot> getBookmarkToggleButton() {
		return buttonSource.getBookmarkToggleButton();
	}
}
