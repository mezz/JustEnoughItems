package mezz.jei.fabric.test;

import mezz.jei.common.Internal;
import mezz.jei.common.config.IngredientGridNavigationMode;
import mezz.jei.common.config.NavigationVisibility;
import mezz.jei.common.gui.elements.Scrollbar;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.elements.ScrollbarWidget;
import mezz.jei.gui.overlay.ingredients.IIngredientGridSource;
import mezz.jei.gui.overlay.ingredients.IIngredientListOverlayContents;
import mezz.jei.gui.overlay.ingredients.IngredientGrid;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigation;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.mezzdev.config.api.value.IConfigValue;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Checks auto-hidden ingredient navigation after search results change in an open inventory. */
@SuppressWarnings("UnstableApiUsage")
public class IngredientNavigationClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		JUnitXmlTestReporter.runAndReport("fabric-client-gametest", getClass().getSimpleName(), () -> {
			try (TestSingleplayerContext ignored = context.worldBuilder().create()) {
				JeiFabricClientGameTestAssertions.assertJeiStartedWithSyncedRecipes(context);
				List<Runnable> cleanup = new ArrayList<>();
				try {
					context.runOnClient(client -> {
						var configs = Internal.getClientConfigs();
						var gridConfig = configs.getIngredientListConfig();
						set(cleanup, gridConfig.navigationVisibility(), NavigationVisibility.AUTO_HIDE);
						set(cleanup, gridConfig.maxColumns(), 4);
						set(cleanup, gridConfig.maxRows(), 3);
						set(cleanup, gridConfig.shrinkToFit(), false);
						set(cleanup, gridConfig.navigationMode(), IngredientGridNavigationMode.PAGED);
						set(cleanup, configs.getClientConfig().smoothScrollingEnabled(), false);
						var filter = Internal.getJeiRuntime().getIngredientFilter();
						String originalFilter = filter.getFilterText();
						cleanup.add(() -> filter.setFilterText(originalFilter));
					});
					assertSearchUpdatesNavigation(context, IngredientGridNavigationMode.PAGED, false);
					assertSearchUpdatesNavigation(context, IngredientGridNavigationMode.SCROLLING, false);
					assertSearchUpdatesNavigation(context, IngredientGridNavigationMode.SCROLLING, true);
				} finally {
					context.runOnClient(client -> {
						client.gui.setScreen(null);
						cleanup.reversed().forEach(Runnable::run);
					});
				}
			}
		});
	}

	private static void assertSearchUpdatesNavigation(ClientGameTestContext context, IngredientGridNavigationMode mode, boolean smoothScrolling) {
		InventoryScreen inventory = context.computeOnClient(client -> {
			client.gui.setScreen(null);
			var configs = Internal.getClientConfigs();
			configs.getIngredientListConfig().navigationMode().set(mode);
			configs.getClientConfig().smoothScrollingEnabled().set(smoothScrolling);
			Internal.getJeiRuntime().getIngredientFilter().setFilterText("");
			InventoryScreen screen = new InventoryScreen(Objects.requireNonNull(client.player));
			client.gui.setScreen(screen);
			return screen;
		});
		context.waitFor(client -> Internal.getJeiRuntime().getIngredientListOverlay().isListDisplayed());
		assertNavigation(context, inventory, mode, true, "the unfiltered list");

		context.runOnClient(client -> {
			Internal.getJeiRuntime().getIngredientFilter().setFilterText("oak slab");
			if (getNavigationState(mode).ingredientCount() == 0) {
				throw new AssertionError("Expected oak slabs in the search results");
			}
		});
		assertNavigation(context, inventory, mode, false, "search results that fit on one page");

		context.runOnClient(client -> {
			Internal.getJeiRuntime().getIngredientFilter().setFilterText("jei-no-matching-ingredient-4543");
			if (getNavigationState(mode).ingredientCount() != 0) {
				throw new AssertionError("Expected no ingredients for the unmatched search");
			}
		});
		assertNavigation(context, inventory, mode, false, "empty search results");

		context.runOnClient(client -> Internal.getJeiRuntime().getIngredientFilter().setFilterText(""));
		assertNavigation(context, inventory, mode, true, "clearing the search");
	}

	private static void assertNavigation(ClientGameTestContext context, InventoryScreen inventory, IngredientGridNavigationMode mode, boolean expectedVisible, String operation) {
		// Let the open inventory render the changed results before inspecting navigation bounds.
		context.waitTicks(3);
		context.runOnClient(client -> {
			if (client.gui.screen() != inventory) {
				throw new AssertionError("Navigation must update without replacing the inventory screen");
			}
			NavigationState state = getNavigationState(mode);
			if (expectedVisible != (state.ingredientCount() > state.capacity()) || state.capacity() == 0) {
				throw new AssertionError("Unexpected ingredient count for " + operation + ": " + state);
			}
			if (!state.area().isEmpty() != expectedVisible) {
				throw new AssertionError("Unexpected navigation visibility after " + operation + " in " + mode + ": " + state);
			}
		});
	}

	private static NavigationState getNavigationState(IngredientGridNavigationMode mode) {
		ReflectionUtil reflection = new ReflectionUtil();
		IngredientGridWithNavigation contents = (IngredientGridWithNavigation) reflection.getFieldWithClass(
			Internal.getJeiRuntime().getIngredientListOverlay(), IIngredientListOverlayContents.class)
			.findFirst().orElseThrow();
		IngredientGrid grid = reflection.getFieldWithClass(contents, IngredientGrid.class).findFirst().orElseThrow();
		IIngredientGridSource source = reflection.getFieldWithClass(contents, IIngredientGridSource.class).findFirst().orElseThrow();
		ImmutableRect2i area;
		if (mode.usesScrollbar()) {
			ScrollbarWidget navigation = reflection.getFieldWithClass(contents, ScrollbarWidget.class).findFirst().orElseThrow();
			area = reflection.getFieldWithClass(navigation, Scrollbar.class).findFirst().orElseThrow().getArea();
		} else {
			PageNavigation navigation = reflection.getFieldWithClass(contents, PageNavigation.class).findFirst().orElseThrow();
			area = navigation.getNextButtonArea();
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
