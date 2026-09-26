package mezz.jei.fabric.test;

import mezz.jei.common.Internal;
import mezz.jei.common.config.IngredientGridNavigationMode;
import mezz.jei.common.config.NavigationVisibility;
import mezz.jei.common.gui.elements.Scrollbar;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.gui.overlay.ingredients.IIngredientGridSource;
import mezz.jei.gui.overlay.ingredients.IngredientGrid;
import mezz.jei.gui.elements.ScrollbarWidget;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigation;
import net.mezzdev.config.api.value.IConfigValue;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Checks auto-hidden ingredient navigation after search results change in an open inventory. */
@SuppressWarnings("UnstableApiUsage")
final class IngredientNavigationClientTest {
	private static final Duration ASSERTION_TIMEOUT = Duration.ofSeconds(10);

	private IngredientNavigationClientTest() {
	}

	public static FabricClientTestRunner.ClientTestCase getTestCase() {
		return new FabricClientTestRunner.ClientTestCase(
			"fabric-client-gametest", IngredientNavigationClientTest.class.getSimpleName(), IngredientNavigationClientTest::runTest);
	}

	private static void runTest() {
		List<Runnable> cleanup = new ArrayList<>();
		try (FabricClientTestWorld ignored = FabricClientTestWorld.create()) {
			ClientTestUtil.waitUntil(
				() -> ClientTestUtil.computeOnClient(client -> Internal.getOptionalJeiRuntime().isPresent()),
				Duration.ofSeconds(60), () -> "Timed out waiting for JEI before testing ingredient navigation");
			try {
				ClientTestUtil.runOnClient(client -> {
					var config = Internal.getClientConfigs().getIngredientListConfig();
					set(cleanup, config.navigationVisibility(), NavigationVisibility.AUTO_HIDE);
					set(cleanup, config.maxColumns(), 4);
					set(cleanup, config.maxRows(), 3);
					set(cleanup, config.navigationMode(), IngredientGridNavigationMode.PAGED);
					var filter = Internal.getJeiRuntime().getIngredientFilter();
					String originalFilter = filter.getFilterText();
					cleanup.add(() -> filter.setFilterText(originalFilter));
				});
				for (IngredientGridNavigationMode mode : IngredientGridNavigationMode.values()) {
					assertSearchUpdatesNavigation(mode);
				}
			} finally {
				ClientTestUtil.runOnClient(client -> {
					client.setScreen(null);
					for (int i = cleanup.size() - 1; i >= 0; i--) {
						cleanup.get(i).run();
					}
				});
			}
		}
	}

	private static void assertSearchUpdatesNavigation(IngredientGridNavigationMode mode) {
		InventoryScreen inventory = ClientTestUtil.computeOnClient(client -> {
			client.setScreen(null);
			Internal.getClientConfigs().getIngredientListConfig().navigationMode().set(mode);
			Internal.getJeiRuntime().getIngredientFilter().setFilterText("");
			InventoryScreen screen = new InventoryScreen(Objects.requireNonNull(client.player));
			client.setScreen(screen);
			return screen;
		});
		ClientTestUtil.waitUntil(
			() -> ClientTestUtil.computeOnClient(client -> Internal.getJeiRuntime().getIngredientListOverlay().isListDisplayed()),
			ASSERTION_TIMEOUT, () -> "Timed out displaying the ingredient list for " + mode);
		assertNavigation(inventory, mode, true, "the unfiltered list");

		ClientTestUtil.runOnClient(client -> {
			Internal.getJeiRuntime().getIngredientFilter().setFilterText("oak slab");
			if (getNavigationState(mode).ingredientCount() == 0) {
				throw new AssertionError("Expected oak slabs in the search results");
			}
		});
		assertNavigation(inventory, mode, false, "search results that fit on one page");

		ClientTestUtil.runOnClient(client -> {
			Internal.getJeiRuntime().getIngredientFilter().setFilterText("jei-no-matching-ingredient-4543");
			if (getNavigationState(mode).ingredientCount() != 0) {
				throw new AssertionError("Expected no ingredients for the unmatched search");
			}
		});
		assertNavigation(inventory, mode, false, "empty search results");

		ClientTestUtil.runOnClient(client -> Internal.getJeiRuntime().getIngredientFilter().setFilterText(""));
		assertNavigation(inventory, mode, true, "clearing the search");
	}

	private static void assertNavigation(InventoryScreen inventory, IngredientGridNavigationMode mode, boolean expectedVisible, String operation) {
		ClientTestUtil.runOnClient(client -> {
			NavigationState state = getNavigationState(mode);
			if (expectedVisible != (state.ingredientCount() > state.capacity()) || state.capacity() == 0) {
				throw new AssertionError("Unexpected ingredient count for " + operation + ": " + state);
			}
		});
		String expectedChange;
		if (expectedVisible) {
			expectedChange = "appear";
		} else {
			expectedChange = "hide";
		}
		ClientTestUtil.waitUntil(
			() -> ClientTestUtil.computeOnClient(client -> {
				if (client.screen != inventory) {
					throw new AssertionError("Navigation must update without replacing the inventory screen");
				}
				return !getNavigationState(mode).area().isEmpty() == expectedVisible;
			}),
			ASSERTION_TIMEOUT,
			() -> "Navigation did not " + expectedChange + " after " + operation + " in " + mode + ": " +
				ClientTestUtil.computeOnClient(client -> getNavigationState(mode)));
	}

	private static NavigationState getNavigationState(IngredientGridNavigationMode mode) {
		ReflectionUtil reflection = new ReflectionUtil();
		IngredientGridWithNavigation contents = reflection.getFieldWithClass(
			Internal.getJeiRuntime().getIngredientListOverlay(), IngredientGridWithNavigation.class)
			.findFirst().orElseThrow();
		IngredientGrid grid = reflection.getFieldWithClass(contents, IngredientGrid.class).findFirst().orElseThrow();
		IIngredientGridSource source = reflection.getFieldWithClass(contents, IIngredientGridSource.class).findFirst().orElseThrow();
		ImmutableRect2i area;
		if (mode.usesScrollbar()) {
			ScrollbarWidget navigation = reflection.getFieldWithClass(contents, ScrollbarWidget.class).findFirst().orElseThrow();
			area = reflection.getFieldWithClass(navigation, Scrollbar.class).findFirst().orElseThrow().getArea();
		} else {
			area = contents.getNextPageButtonArea();
		}
		return new NavigationState(source.getElements().size(), grid.size(), area);
	}

	private static <T> void set(List<Runnable> cleanup, IConfigValue<T> config, T value) {
		T original = config.get();
		cleanup.add(() -> config.set(original));
		config.set(value);
	}

	private record NavigationState(int ingredientCount, int capacity, ImmutableRect2i area) {
	}
}
