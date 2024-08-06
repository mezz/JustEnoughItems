package mezz.jei.gui.startup;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.filter.IFilterTextSource;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;

public final class OverlayHelper {
	private OverlayHelper() {}

	public static IngredientGridWithNavigation createIngredientGridWithNavigation(
		String debugName,
		IIngredientGridSource ingredientFilter,
		IIngredientManager ingredientManager,
		IIngredientGridConfig ingredientGridConfig,
		DrawableNineSliceTexture background,
		DrawableNineSliceTexture slotBackground,
		IInternalKeyMappings keyMappings,
		IIngredientFilterConfig ingredientFilterConfig,
		IClientConfig clientConfig,
		IClientToggleState toggleState,
		IConnectionToServer serverConnection,
		IColorHelper colorHelper,
		IScreenHelper screenHelper
	) {
		IngredientGrid ingredientListGrid = new IngredientGrid(
			ingredientManager,
			ingredientGridConfig,
			ingredientFilterConfig,
			clientConfig,
			toggleState,
			serverConnection,
			keyMappings,
			colorHelper
		);

		return new IngredientGridWithNavigation(
			debugName,
			ingredientFilter,
			ingredientListGrid,
			toggleState,
			clientConfig,
			serverConnection,
			ingredientGridConfig,
			background,
			slotBackground,
			screenHelper,
			ingredientManager
		);
	}

	public static IngredientListOverlay createIngredientListOverlay(
		IIngredientManager ingredientManager,
		IScreenHelper screenHelper,
		IIngredientGridSource ingredientFilter,
		IFilterTextSource filterTextSource,
		IInternalKeyMappings keyMappings,
		IIngredientGridConfig ingredientGridConfig,
		IClientConfig clientConfig,
		IClientToggleState toggleState,
		IConnectionToServer serverConnection,
		IIngredientFilterConfig ingredientFilterConfig,
		Textures textures,
		IColorHelper colorHelper
	) {
		IngredientGridWithNavigation ingredientListGridNavigation = createIngredientGridWithNavigation(
			"IngredientListOverlay",
			ingredientFilter,
			ingredientManager,
			ingredientGridConfig,
			textures.getIngredientListBackground(),
			textures.getIngredientListSlotBackground(),
			keyMappings,
			ingredientFilterConfig,
			clientConfig,
			toggleState,
			serverConnection,
			colorHelper,
			screenHelper
		);

		return new IngredientListOverlay(
			ingredientFilter,
			filterTextSource,
			screenHelper,
			ingredientListGridNavigation,
			clientConfig,
			toggleState,
			keyMappings
		);
	}

	public static BookmarkOverlay createBookmarkOverlay(
		IIngredientManager ingredientManager,
		IScreenHelper screenHelper,
		BookmarkList bookmarkList,
		IInternalKeyMappings keyMappings,
		IIngredientGridConfig bookmarkListConfig,
		IIngredientFilterConfig ingredientFilterConfig,
		IClientConfig clientConfig,
		IClientToggleState toggleState,
		IConnectionToServer serverConnection,
		Textures textures,
		IColorHelper colorHelper
	) {
		IngredientGridWithNavigation bookmarkListGridNavigation = createIngredientGridWithNavigation(
			"BookmarkOverlay",
			bookmarkList,
			ingredientManager,
			bookmarkListConfig,
			textures.getBookmarkListBackground(),
			textures.getBookmarkListSlotBackground(),
			keyMappings,
			ingredientFilterConfig,
			clientConfig,
			toggleState,
			serverConnection,
			colorHelper,
			screenHelper);

		return new BookmarkOverlay(
			bookmarkList,
			bookmarkListGridNavigation,
			toggleState,
			screenHelper,
			keyMappings
		);
	}
}
