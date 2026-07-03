package mezz.jei.gui.overlay;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.overlay.bookmarks.history.LookupHistoryOverlay;

import java.util.Optional;

class IngredientListOverlayLayout {
	private static final int BORDER_MARGIN = 6;
	private static final int INNER_PADDING = 2;
	private static final int BUTTON_SIZE = 20;
	private static final int SEARCH_HEIGHT = BUTTON_SIZE;

	static Layout calculate(
		IGuiProperties guiProperties,
		boolean centerSearchBarEnabled,
		boolean lookupHistoryEnabled,
		boolean lookupHistoryDisplayedOnThisSide,
		int maxLookupHistoryRows
	) {
		ImmutableRect2i displayArea = createDisplayArea(guiProperties);
		boolean searchBarCentered = isSearchBarCentered(centerSearchBarEnabled, guiProperties);
		ImmutableRect2i availableContentsArea = getAvailableContentsArea(displayArea, searchBarCentered);
		Optional<ImmutableRect2i> lookupHistoryArea = Optional.empty();

		if (lookupHistoryEnabled && lookupHistoryDisplayedOnThisSide) {
			int lookupHistoryHeight = maxLookupHistoryRows * LookupHistoryOverlay.SLOT_HEIGHT;
			availableContentsArea = availableContentsArea.cropBottom(lookupHistoryHeight);
			lookupHistoryArea = Optional.of(
				displayArea
					.insetBy(BORDER_MARGIN)
					.moveUp(BUTTON_SIZE + INNER_PADDING)
					.keepBottom(lookupHistoryHeight)
			);
		}

		return new Layout(guiProperties, displayArea, availableContentsArea, lookupHistoryArea, searchBarCentered);
	}

	private static ImmutableRect2i createDisplayArea(IGuiProperties guiProperties) {
		return new ImmutableRect2i(0, 0, guiProperties.screenWidth(), guiProperties.screenHeight())
			.cropLeft(guiProperties.guiRight());
	}

	private static boolean isSearchBarCentered(boolean centerSearchBarEnabled, IGuiProperties guiProperties) {
		return centerSearchBarEnabled &&
			guiProperties.guiBottom() + SEARCH_HEIGHT < guiProperties.screenHeight();
	}

	private static ImmutableRect2i getAvailableContentsArea(ImmutableRect2i displayArea, boolean searchBarCentered) {
		if (searchBarCentered) {
			return displayArea;
		}
		return displayArea.cropBottom(SEARCH_HEIGHT + INNER_PADDING);
	}

	record Layout(
		IGuiProperties guiProperties,
		ImmutableRect2i displayArea,
		ImmutableRect2i availableContentsArea,
		Optional<ImmutableRect2i> lookupHistoryArea,
		boolean searchBarCentered
	) {
		SearchAndConfigAreas getSearchAndConfigAreas(boolean contentsHasRoom, ImmutableRect2i contentsArea) {
			ImmutableRect2i searchAndConfigArea = getSearchAndConfigArea(contentsHasRoom, contentsArea);
			ImmutableRect2i searchArea = searchAndConfigArea.cropRight(BUTTON_SIZE);
			ImmutableRect2i configButtonArea = searchAndConfigArea.keepRight(BUTTON_SIZE);
			return new SearchAndConfigAreas(searchArea, configButtonArea);
		}

		private ImmutableRect2i getSearchAndConfigArea(boolean contentsHasRoom, ImmutableRect2i contentsArea) {
			ImmutableRect2i insetDisplayArea = displayArea.insetBy(BORDER_MARGIN);
			if (searchBarCentered) {
				return insetDisplayArea
					.keepBottom(SEARCH_HEIGHT)
					.matchWidthAndX(new ImmutableRect2i(guiProperties.guiLeft(), guiProperties.guiTop(), guiProperties.guiXSize(), guiProperties.guiYSize()));
			} else if (contentsHasRoom) {
				return insetDisplayArea
					.keepBottom(SEARCH_HEIGHT)
					.matchWidthAndX(contentsArea);
			} else {
				return insetDisplayArea.keepBottom(SEARCH_HEIGHT);
			}
		}
	}

	record SearchAndConfigAreas(ImmutableRect2i searchArea, ImmutableRect2i configButtonArea) {

	}
}
