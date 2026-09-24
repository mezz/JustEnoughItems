package mezz.jei.gui.overlay;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.filter.IFilterTextSource;
import mezz.jei.gui.overlay.bookmarks.history.ILookupHistoryOverlay;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.history.LookupHistoryOverlayLayout;
import mezz.jei.gui.overlay.ingredients.IIngredientGridPageNavigation;
import mezz.jei.gui.overlay.ingredients.IIngredientGridView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.function.BooleanSupplier;

class IngredientListOverlayController {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IGuiPropertiesCache guiPropertiesCache;
	private final Config config;
	private final BooleanSupplier overlayEnabled;
	private final BooleanSupplier toggleOverlayUnbound;
	private final IFilterTextSource filterTextSource;
	private final IIngredientGridView contentsView;
	private final IIngredientGridPageNavigation contentsPageNavigation;
	private final ILookupHistoryOverlay lookupHistory;
	private final ISearchField searchField;
	private final IConfigButton configButton;
	private boolean hasValidScreen = false;

	static IngredientListOverlayController create(
		IGuiPropertiesCache guiPropertiesCache,
		IClientConfig clientConfig,
		IClientToggleState toggleState,
		IInternalKeyMappings keyBindings,
		IFilterTextSource filterTextSource,
		IIngredientGridView contentsView,
		IIngredientGridPageNavigation contentsPageNavigation,
		ILookupHistoryOverlay lookupHistory,
		ISearchField searchField,
		IConfigButton configButton
	) {
		return new IngredientListOverlayController(
			guiPropertiesCache,
			getConfig(clientConfig),
			toggleState::isOverlayEnabled,
			() -> keyBindings.getToggleOverlay().isUnbound(),
			filterTextSource,
			contentsView,
			contentsPageNavigation,
			lookupHistory,
			searchField,
			configButton
		);
	}

	private static Config getConfig(IClientConfig clientConfig) {
		return new Config() {
			@Override
			public boolean isCenterSearchBarEnabled() {
				return clientConfig.searchBarPosition().get().isCentered();
			}

			@Override
			public boolean isLookupHistoryEnabled() {
				return clientConfig.lookupHistoryEnabled().get();
			}
		};
	}

	IngredientListOverlayController(
		IGuiPropertiesCache guiPropertiesCache,
		Config config,
		BooleanSupplier overlayEnabled,
		BooleanSupplier toggleOverlayUnbound,
		IFilterTextSource filterTextSource,
		IIngredientGridView contentsView,
		IIngredientGridPageNavigation contentsPageNavigation,
		ILookupHistoryOverlay lookupHistory,
		ISearchField searchField,
		IConfigButton configButton
	) {
		this.guiPropertiesCache = guiPropertiesCache;
		this.config = config;
		this.overlayEnabled = overlayEnabled;
		this.toggleOverlayUnbound = toggleOverlayUnbound;
		this.filterTextSource = filterTextSource;
		this.contentsView = contentsView;
		this.contentsPageNavigation = contentsPageNavigation;
		this.lookupHistory = lookupHistory;
		this.searchField = searchField;
		this.configButton = configButton;
	}

	void init() {
		this.searchField.setValue(this.filterTextSource.getFilterText());
		this.searchField.setFocused(false);
		this.filterTextSource.addListener(this::onFilterTextChanged);
	}

	boolean isListDisplayed() {
		return (overlayEnabled.getAsBoolean() || toggleOverlayUnbound.getAsBoolean()) &&
			hasValidScreen &&
			contentsView.hasRoom();
	}

	boolean hasValidScreen() {
		return hasValidScreen;
	}

	IScreenPropertiesUpdater getScreenPropertiesUpdater() {
		return this.guiPropertiesCache.createUpdater(this::onGuiPropertiesChanged);
	}

	void updateScreenProperties() {
		onGuiPropertiesChanged();
	}

	private void onFilterTextChanged(String oldFilterText, String newFilterText) {
		this.searchField.setValue(newFilterText);
		if (!oldFilterText.isEmpty() && newFilterText.isEmpty()) {
			this.contentsPageNavigation.updateLayoutToFirstPage();
		}
	}

	private void onGuiPropertiesChanged() {
		IGuiProperties guiProperties = this.guiPropertiesCache.getGuiProperties();
		if (guiProperties == null) {
			clearScreen();
			return;
		}
		Set<ImmutableRect2i> guiExclusionAreas = this.guiPropertiesCache.getGuiExclusionAreas();
		try {
			updateScreen(
				guiProperties,
				guiExclusionAreas
			);
		} catch (RuntimeException e) {
			LOGGER.error("Failed to update JEI bounds for screen with properties : {}", guiProperties, e);
			clearScreenAfterUpdateError();
		}
	}

	void updateScreen(IGuiProperties guiProperties, Set<ImmutableRect2i> guiExclusionAreas) {
		this.hasValidScreen = true;
		updateBounds(guiProperties, guiExclusionAreas);
	}

	void clearScreen() {
		this.hasValidScreen = false;
		this.contentsView.close();
		this.searchField.setFocused(false);
	}

	void clearScreenAfterUpdateError() {
		clearScreen();
		this.lookupHistory.close();
	}

	private void updateBounds(IGuiProperties guiProperties, Set<ImmutableRect2i> guiExclusionAreas) {
		IngredientListOverlayLayout.Layout layout = IngredientListOverlayLayout.calculate(
			guiProperties,
			config.isCenterSearchBarEnabled(),
			config.isLookupHistoryEnabled(),
			lookupHistory.isDisplayedOnThisSide(),
			lookupHistory.getDisplayHeight()
		);

		IElement<?> pageAnchorElement = this.contentsPageNavigation.getPageAnchorElement();
		this.contentsView.updateBounds(layout.availableContentsArea(), guiExclusionAreas, null);
		this.contentsPageNavigation.updateLayoutKeepingPageAnchorVisible(pageAnchorElement);

		layout.lookupHistoryArea()
			.ifPresent(lookupHistoryArea -> {
				ImmutableRect2i alignedArea = alignLookupHistoryArea(lookupHistoryArea);
				this.lookupHistory.updateBounds(alignedArea, guiExclusionAreas, null);
				this.lookupHistory.updateLayout();
				ImmutableRect2i positionedArea = positionLookupHistoryArea(alignedArea);
				if (!positionedArea.equals(alignedArea)) {
					this.lookupHistory.updateBounds(positionedArea, guiExclusionAreas, null);
					this.lookupHistory.updateLayout();
				}
			});

		IngredientListOverlayLayout.SearchAndConfigAreas searchAndConfigAreas = layout.getSearchAndConfigAreas(
			this.contentsView.hasRoom(),
			this.contentsView.getBackgroundArea()
		);
		this.searchField.setValue(filterTextSource.getFilterText());
		this.searchField.updateBounds(searchAndConfigAreas.searchArea());
		this.configButton.updateBounds(searchAndConfigAreas.configButtonArea());
	}

	private ImmutableRect2i alignLookupHistoryArea(ImmutableRect2i lookupHistoryArea) {
		return LookupHistoryOverlayLayout.alignToOwnerBackground(
			lookupHistoryArea,
			this.contentsView.getBackgroundArea()
		);
	}

	private ImmutableRect2i positionLookupHistoryArea(ImmutableRect2i lookupHistoryArea) {
		boolean combineBackgrounds = this.contentsView.isBackgroundEnabled() &&
			this.lookupHistory.isBackgroundEnabled() &&
			this.contentsView.hasRoom();
		if (!combineBackgrounds) {
			return lookupHistoryArea;
		}
		return LookupHistoryOverlayLayout.moveNextToOwner(
			lookupHistoryArea,
			this.lookupHistory.getBackgroundArea(),
			this.contentsView.getBackgroundArea()
		);
	}

	interface Config {
		boolean isCenterSearchBarEnabled();

		boolean isLookupHistoryEnabled();
	}

}
