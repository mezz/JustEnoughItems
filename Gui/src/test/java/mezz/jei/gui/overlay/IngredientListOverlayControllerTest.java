package mezz.jei.gui.overlay;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.filter.FilterTextSource;
import mezz.jei.gui.overlay.bookmarks.history.ILookupHistoryOverlay;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientElement;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IngredientListOverlayControllerTest {
	private static final IIngredientType<Object> OBJECT_TYPE = () -> Object.class;

	@Test
	public void clearingSearchReturnsToFirstPage() {
		// Setup: the overlay controller is listening to the shared filter text source.
		Fixture fixture = Fixture.create();
		fixture.controller.init();

		// Operation: the user types a search and then clears it.
		fixture.filterTextSource.setFilterText("e");
		fixture.filterTextSource.setFilterText("");

		// Assertions: the search field mirrors both changes, and the ingredient list returns to page one.
		assertEquals(List.of("", "e", ""), fixture.searchField.values);
		assertEquals(1, fixture.contents.firstPageUpdates);
	}

	@Test
	public void changingSearchDoesNotReturnToFirstPage() {
		// Setup: the overlay controller is listening to the shared filter text source.
		Fixture fixture = Fixture.create();
		fixture.controller.init();

		// Operation: the user changes one non-empty search to another non-empty search.
		fixture.filterTextSource.setFilterText("e");
		fixture.filterTextSource.setFilterText("stone");

		// Assertions: changing the search text does not force the ingredient list back to the first page.
		assertEquals(0, fixture.contents.firstPageUpdates);
	}

	@Test
	public void screenUpdateLaysOutRightSideOverlayComponents() {
		// Setup: lookup history is shown beside the ingredient list, and contents report a usable background.
		Fixture fixture = Fixture.create();
		fixture.config.lookupHistoryEnabled = true;
		fixture.config.maxLookupHistoryRows = 2;
		fixture.lookupHistory.displayedOnThisSide = true;
		fixture.contents.backgroundArea = new ImmutableRect2i(160, 8, 40, 40);
		fixture.controller.init();
		IGuiProperties guiProperties = guiProperties(50, 20, 100, 50, 200, 100);

		// Operation: the controller updates layout for the current screen.
		fixture.updateScreen(guiProperties);

		// Assertions: contents, side history, search field, and config button are placed in the right overlay area.
		ImmutableRect2i contentsArea = area("contents", fixture.contents.availableArea);
		ImmutableRect2i lookupHistoryArea = area("lookup history", fixture.lookupHistory.availableArea);
		ImmutableRect2i searchArea = area("search field", fixture.searchField.area);
		ImmutableRect2i configButtonArea = area("config button", fixture.configButton.area);

		assertRightOfGui(contentsArea, guiProperties);
		assertRightOfGui(lookupHistoryArea, guiProperties);
		assertRightOfGui(searchArea, guiProperties);
		assertRightOfGui(configButtonArea, guiProperties);
		assertWithinScreen(contentsArea, guiProperties);
		assertWithinScreen(lookupHistoryArea, guiProperties);
		assertWithinScreen(searchArea, guiProperties);
		assertWithinScreen(configButtonArea, guiProperties);
		assertPositiveArea(contentsArea);
		assertPositiveArea(lookupHistoryArea);
		assertPositiveArea(searchArea);
		assertPositiveArea(configButtonArea);
		assertSharedControlRow(searchArea, configButtonArea);
		assertEquals(1, fixture.lookupHistory.layoutUpdates);
	}

	@Test
	public void screenUpdateKeepsPageAnchorVisibleAfterBoundsChange() {
		// Setup: the grid has a page anchor before bounds are recalculated, but updating bounds clears it.
		Fixture fixture = Fixture.create();
		IElement<?> pageAnchorElement = element();
		fixture.contents.pageAnchorElement = pageAnchorElement;
		fixture.contents.clearPageAnchorOnUpdateBounds = true;
		fixture.controller.init();
		IGuiProperties guiProperties = guiProperties(50, 20, 100, 50, 200, 100);

		// Operation: the controller updates layout for the current screen.
		fixture.updateScreen(guiProperties);

		// Assertions: the anchor captured before the bounds update is still used for page-preserving layout.
		assertSame(pageAnchorElement, fixture.contents.layoutPageAnchorElement);
		assertEquals(1, fixture.contents.keepAnchorLayoutUpdates);
	}

	@Test
	public void screenPropertiesUpdatePassesExclusionAreasToContentsAndLookupHistory() {
		// Setup: the cache has valid screen properties and an exclusion area from the active screen.
		Fixture fixture = Fixture.create();
		fixture.config.lookupHistoryEnabled = true;
		fixture.config.maxLookupHistoryRows = 1;
		fixture.lookupHistory.displayedOnThisSide = true;
		Set<ImmutableRect2i> guiExclusionAreas = Set.of(new ImmutableRect2i(170, 10, 10, 10));
		fixture.guiPropertiesCache.guiProperties = guiProperties(50, 20, 100, 50, 200, 100);
		fixture.guiPropertiesCache.guiExclusionAreas = guiExclusionAreas;
		fixture.controller.init();

		// Operation: the controller reacts to a screen-properties change from the cache.
		fixture.controller.updateScreenProperties();

		// Assertions: the same exclusion areas are passed to both overlay content regions.
		assertTrue(fixture.controller.hasValidScreen());
		assertEquals(guiExclusionAreas, fixture.contents.guiExclusionAreas);
		assertEquals(guiExclusionAreas, fixture.lookupHistory.guiExclusionAreas);
	}

	@Test
	public void screenUpdatePlacesCenteredSearchBarWithinGuiWidth() {
		// Setup: center-search is enabled and the current screen leaves room below the GUI.
		Fixture fixture = Fixture.create();
		fixture.config.centerSearchBarEnabled = true;
		fixture.contents.backgroundArea = new ImmutableRect2i(210, 8, 40, 40);
		fixture.controller.init();
		IGuiProperties guiProperties = guiProperties(50, 20, 100, 50, 260, 100);

		// Operation: the controller updates layout for the current screen.
		fixture.updateScreen(guiProperties);

		// Assertions: contents stay to the right of the GUI and centered search controls are placed under the GUI.
		ImmutableRect2i contentsArea = area("contents", fixture.contents.availableArea);
		ImmutableRect2i searchArea = area("search field", fixture.searchField.area);
		ImmutableRect2i configButtonArea = area("config button", fixture.configButton.area);

		assertRightOfGui(contentsArea, guiProperties);
		assertWithinScreen(contentsArea, guiProperties);
		assertPositiveArea(contentsArea);
		assertWithinScreen(searchArea, guiProperties);
		assertWithinScreen(configButtonArea, guiProperties);
		assertPositiveArea(searchArea);
		assertPositiveArea(configButtonArea);
		assertSharedControlRow(searchArea, configButtonArea);
		assertControlRowWithinGuiWidth(searchArea, configButtonArea, guiProperties);
		assertTrue(
			searchArea.y() >= guiProperties.guiBottom(),
			"centered search controls should be below the GUI"
		);
	}

	@Test
	public void overlayIsDisplayedWhenEnabledAndScreenHasRoom() {
		// Setup: the overlay toggle is enabled and the ingredient grid reports that it has room.
		Fixture fixture = Fixture.create();
		fixture.controller.init();
		IGuiProperties guiProperties = guiProperties(50, 20, 100, 50, 200, 100);

		// Operation: the controller receives valid screen properties.
		fixture.updateScreen(guiProperties);

		// Assertions: the ingredient list is displayed.
		assertTrue(fixture.controller.isListDisplayed());
	}

	@Test
	public void overlayIsDisplayedWhenToggleKeyIsUnbound() {
		// Setup: the overlay toggle is disabled, but there is no key binding available to toggle it back on.
		Fixture fixture = Fixture.create();
		fixture.overlayEnabled.value = false;
		fixture.toggleOverlayUnbound.value = true;
		fixture.controller.init();
		IGuiProperties guiProperties = guiProperties(50, 20, 100, 50, 200, 100);

		// Operation: the controller receives valid screen properties.
		fixture.updateScreen(guiProperties);

		// Assertions: the ingredient list is still displayed so the overlay is not permanently hidden.
		assertTrue(fixture.controller.isListDisplayed());
	}

	@Test
	public void overlayIsHiddenWhenDisabledAndToggleKeyIsBound() {
		// Setup: the overlay toggle is disabled and the user has a key binding that can re-enable it.
		Fixture fixture = Fixture.create();
		fixture.overlayEnabled.value = false;
		fixture.toggleOverlayUnbound.value = false;
		fixture.controller.init();
		IGuiProperties guiProperties = guiProperties(50, 20, 100, 50, 200, 100);

		// Operation: the controller receives valid screen properties.
		fixture.updateScreen(guiProperties);

		// Assertions: the ingredient list remains hidden.
		assertFalse(fixture.controller.isListDisplayed());
	}

	@Test
	public void overlayIsHiddenWhenContentsHasNoRoom() {
		// Setup: the overlay is enabled, but the ingredient grid cannot render any contents.
		Fixture fixture = Fixture.create();
		fixture.contents.hasRoom = false;
		fixture.controller.init();
		IGuiProperties guiProperties = guiProperties(50, 20, 100, 50, 200, 100);

		// Operation: the controller receives valid screen properties.
		fixture.updateScreen(guiProperties);

		// Assertions: the ingredient list is not displayed without usable grid room.
		assertFalse(fixture.controller.isListDisplayed());
	}

	@Test
	public void invalidScreenClosesContentsAndUnfocusesSearch() {
		// Setup: the screen properties cache does not have a valid screen.
		Fixture fixture = Fixture.create();
		fixture.controller.init();

		// Operation: the controller reacts to a screen-properties change from the cache.
		fixture.controller.updateScreenProperties();

		// Assertions: overlay contents close, search focus is cleared, and the screen is marked invalid.
		assertEquals(1, fixture.contents.closeCount);
		assertEquals(List.of(false, false), fixture.searchField.focusValues);
		assertFalse(fixture.controller.hasValidScreen());
	}

	@Test
	public void screenPropertiesUpdateFailureClosesContentsAndLookupHistory() {
		// Setup: the cache has a valid screen, but updating content bounds throws during layout.
		Fixture fixture = Fixture.create();
		fixture.guiPropertiesCache.guiProperties = guiProperties(50, 20, 100, 50, 200, 100);
		fixture.contents.throwOnUpdateBounds = true;
		fixture.controller.init();

		// Operation: the controller reacts to a screen-properties change from the cache.
		fixture.controller.updateScreenProperties();

		// Assertions: the failed screen update clears all overlay state that depends on the invalid layout.
		assertFalse(fixture.controller.hasValidScreen());
		assertEquals(1, fixture.contents.closeCount);
		assertEquals(1, fixture.lookupHistory.closeCount);
		assertEquals(List.of(false, false), fixture.searchField.focusValues);
	}

	private static class Fixture {
		final TestConfig config = new TestConfig();
		final MutableBooleanSupplier overlayEnabled = new MutableBooleanSupplier(true);
		final MutableBooleanSupplier toggleOverlayUnbound = new MutableBooleanSupplier(false);
		final FilterTextSource filterTextSource = new FilterTextSource();
		final TestContents contents = new TestContents();
		final TestLookupHistory lookupHistory = new TestLookupHistory();
		final TestSearchField searchField = new TestSearchField();
		final TestConfigButton configButton = new TestConfigButton();
		final TestGuiPropertiesCache guiPropertiesCache = new TestGuiPropertiesCache();
		final IngredientListOverlayController controller;

		private Fixture() {
			this.controller = new IngredientListOverlayController(
				guiPropertiesCache,
				config,
				overlayEnabled,
				toggleOverlayUnbound,
				filterTextSource,
				contents,
				contents,
				lookupHistory,
				searchField,
				configButton
			);
		}

		static Fixture create() {
			return new Fixture();
		}

		void updateScreen(IGuiProperties guiProperties) {
			this.controller.updateScreen(guiProperties, Set.of());
		}
	}

	private static IGuiProperties guiProperties(int guiLeft, int guiTop, int guiXSize, int guiYSize, int screenWidth, int screenHeight) {
		return new TestGuiProperties(guiLeft, guiTop, guiXSize, guiYSize, screenWidth, screenHeight);
	}

	private static ImmutableRect2i area(String name, @Nullable ImmutableRect2i area) {
		assertNotNull(area, name + " area should be set");
		return area;
	}

	private static void assertRightOfGui(ImmutableRect2i area, IGuiProperties guiProperties) {
		assertTrue(
			area.x() >= guiProperties.guiRight(),
			() -> area + " should be to the right of the GUI"
		);
	}

	private static void assertWithinScreen(ImmutableRect2i area, IGuiProperties guiProperties) {
		assertTrue(area.x() >= 0, () -> area + " should start within the screen horizontally");
		assertTrue(area.y() >= 0, () -> area + " should start within the screen vertically");
		assertTrue(right(area) <= guiProperties.screenWidth(), () -> area + " should end within the screen horizontally");
		assertTrue(bottom(area) <= guiProperties.screenHeight(), () -> area + " should end within the screen vertically");
	}

	private static void assertPositiveArea(ImmutableRect2i area) {
		assertTrue(area.width() > 0, () -> area + " should have positive width");
		assertTrue(area.height() > 0, () -> area + " should have positive height");
	}

	private static void assertSharedControlRow(ImmutableRect2i searchArea, ImmutableRect2i configButtonArea) {
		assertEquals(searchArea.y(), configButtonArea.y(), "search field and config button should share a row");
		assertEquals(searchArea.height(), configButtonArea.height(), "search field and config button should have the same height");
		assertTrue(
			configButtonArea.x() >= right(searchArea),
			"config button should be to the right of the search field"
		);
	}

	private static void assertControlRowWithinGuiWidth(ImmutableRect2i searchArea, ImmutableRect2i configButtonArea, IGuiProperties guiProperties) {
		assertTrue(
			searchArea.x() >= guiProperties.guiLeft(),
			"centered search row should start within the GUI width"
		);
		assertTrue(
			right(configButtonArea) <= guiProperties.guiRight(),
			"centered search row should end within the GUI width"
		);
	}

	private static int right(ImmutableRect2i area) {
		return area.x() + area.width();
	}

	private static int bottom(ImmutableRect2i area) {
		return area.y() + area.height();
	}

	private static IElement<?> element() {
		return new IngredientElement<>(new TestTypedIngredient<>(OBJECT_TYPE, new Object()));
	}

	private record TestTypedIngredient<T>(IIngredientType<T> type, T ingredient) implements ITypedIngredient<T> {
		@Override
		public IIngredientType<T> getType() {
			return type;
		}

		@Override
		public T getIngredient() {
			return ingredient;
		}
	}

	private static class TestGuiPropertiesCache implements IGuiPropertiesCache {
		@Nullable IGuiProperties guiProperties;
		Set<ImmutableRect2i> guiExclusionAreas = Set.of();

		@Override
		public IScreenPropertiesUpdater createUpdater(Runnable onChange) {
			throw new UnsupportedOperationException("Unexpected updater request");
		}

		@Override
		public @Nullable IGuiProperties getGuiProperties() {
			return guiProperties;
		}

		@Override
		public Set<ImmutableRect2i> getGuiExclusionAreas() {
			return guiExclusionAreas;
		}
	}

	private static class TestConfig implements IngredientListOverlayController.Config {
		boolean centerSearchBarEnabled = false;
		boolean lookupHistoryEnabled = false;
		int maxLookupHistoryRows = 0;

		@Override
		public boolean isCenterSearchBarEnabled() {
			return centerSearchBarEnabled;
		}

		@Override
		public boolean isLookupHistoryEnabled() {
			return lookupHistoryEnabled;
		}

		@Override
		public int getMaxLookupHistoryRows() {
			return maxLookupHistoryRows;
		}
	}

	private static class MutableBooleanSupplier implements BooleanSupplier {
		boolean value;

		private MutableBooleanSupplier(boolean value) {
			this.value = value;
		}

		@Override
		public boolean getAsBoolean() {
			return value;
		}
	}

	private static class TestContents implements IIngredientGridView, IIngredientGridPageNavigation {
		boolean hasRoom = true;
		int closeCount = 0;
		int firstPageUpdates = 0;
		int keepAnchorLayoutUpdates = 0;
		boolean clearPageAnchorOnUpdateBounds = false;
		boolean throwOnUpdateBounds = false;
		Set<ImmutableRect2i> guiExclusionAreas = Set.of();
		@Nullable
		IElement<?> pageAnchorElement;
		@Nullable
		IElement<?> layoutPageAnchorElement;
		ImmutableRect2i backgroundArea = ImmutableRect2i.EMPTY;
		@Nullable
		ImmutableRect2i availableArea;

		@Override
		public boolean hasRoom() {
			return hasRoom;
		}

		@Override
		public void close() {
			closeCount++;
		}

		@Override
		public @Nullable IElement<?> getPageAnchorElement() {
			return pageAnchorElement;
		}

		@Override
		public void updateBounds(ImmutableRect2i availableArea, Set<ImmutableRect2i> guiExclusionAreas, @Nullable ImmutablePoint2i mouseExclusionPoint) {
			if (throwOnUpdateBounds) {
				throw new IllegalStateException("Test update failure");
			}
			this.availableArea = availableArea;
			this.guiExclusionAreas = guiExclusionAreas;
			if (clearPageAnchorOnUpdateBounds) {
				this.pageAnchorElement = null;
			}
		}

		@Override
		public void updateLayoutKeepingPageAnchorVisible(@Nullable IElement<?> pageAnchorElement) {
			this.layoutPageAnchorElement = pageAnchorElement;
			this.keepAnchorLayoutUpdates++;
		}

		@Override
		public void updateLayoutToFirstPage() {
			firstPageUpdates++;
		}

		@Override
		public ImmutableRect2i getBackgroundArea() {
			return backgroundArea;
		}
	}

	private static class TestLookupHistory implements ILookupHistoryOverlay {
		boolean displayedOnThisSide = false;
		int closeCount = 0;
		int layoutUpdates = 0;
		Set<ImmutableRect2i> guiExclusionAreas = Set.of();
		@Nullable
		ImmutableRect2i availableArea;

		@Override
		public boolean isDisplayedOnThisSide() {
			return displayedOnThisSide;
		}

		@Override
		public void close() {
			closeCount++;
		}

		@Override
		public void updateBounds(ImmutableRect2i availableArea, Set<ImmutableRect2i> guiExclusionAreas, @Nullable ImmutablePoint2i mouseExclusionPoint) {
			this.availableArea = availableArea;
			this.guiExclusionAreas = guiExclusionAreas;
		}

		@Override
		public void updateLayout() {
			layoutUpdates++;
		}
	}

	private static class TestSearchField implements ISearchField {
		final List<String> values = new ArrayList<>();
		final List<Boolean> focusValues = new ArrayList<>();
		@Nullable
		ImmutableRect2i area;

		@Override
		public void setValue(String filterText) {
			values.add(filterText);
		}

		@Override
		public void setFocused(boolean focused) {
			focusValues.add(focused);
		}

		@Override
		public void updateBounds(ImmutableRect2i area) {
			this.area = area;
		}
	}

	private static class TestConfigButton implements IConfigButton {
		@Nullable
		ImmutableRect2i area;

		@Override
		public void updateBounds(ImmutableRect2i area) {
			this.area = area;
		}
	}
}
