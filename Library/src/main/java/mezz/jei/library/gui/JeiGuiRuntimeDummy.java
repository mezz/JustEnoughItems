package mezz.jei.library.gui;

import mezz.jei.api.gui.buttons.ButtonSnapshot;
import mezz.jei.api.runtime.IBookmarkManager;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiGuiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.library.gui.recipes.RecipesGuiDummy;
import mezz.jei.library.ingredients.IngredientFilterApiDummy;

import java.util.Optional;

public final class JeiGuiRuntimeDummy implements IJeiGuiRuntime {
	public static final IJeiGuiRuntime INSTANCE = new JeiGuiRuntimeDummy();

	private JeiGuiRuntimeDummy() {
	}

	@Override
	public IRecipesGui getRecipesGui() {
		return RecipesGuiDummy.INSTANCE;
	}

	@Override
	public IIngredientListOverlay getIngredientListOverlay() {
		return IngredientListOverlayDummy.INSTANCE;
	}

	@Override
	public IIngredientFilter getIngredientFilter() {
		return IngredientFilterApiDummy.INSTANCE;
	}

	@Override
	public IBookmarkOverlay getBookmarkOverlay() {
		return BookmarkOverlayDummy.INSTANCE;
	}

	@Override
	public IBookmarkManager getBookmarkManager() {
		return BookmarkManagerDummy.INSTANCE;
	}

	@Override
	public Optional<ButtonSnapshot> getConfigButton() {
		return Optional.empty();
	}

	@Override
	public Optional<ButtonSnapshot> getBookmarkToggleButton() {
		return Optional.empty();
	}
}
