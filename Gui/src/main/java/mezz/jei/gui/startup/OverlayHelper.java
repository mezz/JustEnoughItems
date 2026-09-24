package mezz.jei.gui.startup;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.config.HistoryDisplaySide;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.filter.IFilterTextSource;
import mezz.jei.gui.overlay.ingredients.IIngredientGridSource;
import mezz.jei.gui.overlay.ingredients.IngredientGridBackgroundRenderer;
import mezz.jei.gui.overlay.ingredients.IngredientGrid;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigation;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.history.LookupHistoryGridConfig;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.overlay.bookmarks.history.LookupHistoryOverlay;

public final class OverlayHelper {
	private OverlayHelper() {}

	public static IngredientGridWithNavigation createIngredientGridWithNavigation(
		String debugName,
		IIngredientGridSource ingredientFilter,
		IIngredientManager ingredientManager,
		IIngredientGridConfig ingredientGridConfig,
		IInternalKeyMappings keyMappings,
		IIngredientFilterConfig ingredientFilterConfig,
		IClientConfig clientConfig,
		IClientToggleState toggleState,
		IConnectionToServer serverConnection,
		IColorHelper colorHelper,
		IScreenHelper screenHelper,
		boolean supportsEditMode
	) {
		IngredientGrid ingredientListGrid = new IngredientGrid(
			ingredientManager,
			ingredientGridConfig,
			ingredientFilterConfig,
			clientConfig,
			toggleState,
			serverConnection,
			keyMappings,
			colorHelper,
			supportsEditMode
		);

		return new IngredientGridWithNavigation(
			debugName,
			ingredientFilter,
			ingredientListGrid,
			toggleState,
			clientConfig,
			serverConnection,
			ingredientGridConfig,
			screenHelper,
			ingredientManager
		);
	}

	public static IngredientListOverlay createIngredientListOverlay(
		IIngredientManager ingredientManager,
		IScreenHelper screenHelper,
		IIngredientGridSource ingredientFilter,
		IIngredientGridSource historyList,
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
			keyMappings,
			ingredientFilterConfig,
			clientConfig,
			toggleState,
			serverConnection,
			colorHelper,
			screenHelper,
			true
		);

		IIngredientGridConfig lookupHistoryGridConfig = new LookupHistoryGridConfig(
			ingredientGridConfig,
			clientConfig.maxLookupHistoryRows()
		);
		IngredientGridWithNavigation lookupHistoryGridNavigation = createIngredientGridWithNavigation(
			"IngredientListLookupHistory",
			historyList,
			ingredientManager,
			lookupHistoryGridConfig,
			keyMappings,
			ingredientFilterConfig,
			clientConfig,
			toggleState,
			serverConnection,
			colorHelper,
			screenHelper,
			false
		);
		LookupHistoryOverlay lookupHistoryOverlay = new LookupHistoryOverlay(
			historyList,
			lookupHistoryGridNavigation,
			lookupHistoryGridConfig,
			clientConfig,
			HistoryDisplaySide.RIGHT
		);
		IngredientGridBackgroundRenderer backgroundRenderer = new IngredientGridBackgroundRenderer(
			textures.getIngredientListBackground(),
			textures.getIngredientListSlotBackground(),
			textures.getExclusionAreaShadow()
		);

		return new IngredientListOverlay(
			ingredientFilter,
			filterTextSource,
			screenHelper,
			ingredientListGridNavigation,
			lookupHistoryOverlay,
			backgroundRenderer,
			ingredientGridConfig,
			clientConfig,
			toggleState,
			keyMappings
		);
	}

	public static BookmarkOverlay createBookmarkOverlay(
		IIngredientManager ingredientManager,
		IScreenHelper screenHelper,
		BookmarkList bookmarkList,
		RecipeTransferService recipeTransferService,
		IIngredientGridSource lookupHistory,
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
			keyMappings,
			ingredientFilterConfig,
			clientConfig,
			toggleState,
			serverConnection,
			colorHelper,
			screenHelper,
			false
		);

		IIngredientGridConfig lookupHistoryGridConfig = new LookupHistoryGridConfig(
			bookmarkListConfig,
			clientConfig.maxLookupHistoryRows()
		);
		IngredientGridWithNavigation lookupHistoryGridNavigation = createIngredientGridWithNavigation(
			"BookmarkLookupHistory",
			lookupHistory,
			ingredientManager,
			lookupHistoryGridConfig,
			keyMappings,
			ingredientFilterConfig,
			clientConfig,
			toggleState,
			serverConnection,
			colorHelper,
			screenHelper,
			false
		);
		LookupHistoryOverlay lookupHistoryOverlay = new LookupHistoryOverlay(
			lookupHistory,
			lookupHistoryGridNavigation,
			lookupHistoryGridConfig,
			clientConfig,
			HistoryDisplaySide.LEFT
		);
		IngredientGridBackgroundRenderer backgroundRenderer = new IngredientGridBackgroundRenderer(
			textures.getBookmarkListBackground(),
			textures.getBookmarkListSlotBackground(),
			textures.getExclusionAreaShadow()
		);

		return new BookmarkOverlay(
			bookmarkList,
			recipeTransferService,
			bookmarkListGridNavigation,
			lookupHistoryOverlay,
			backgroundRenderer,
			toggleState,
			clientConfig,
			bookmarkListConfig,
			screenHelper,
			keyMappings
		);
	}
}
