package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IngredientGridLayoutMode;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.gui.input.GuiContainerWrapper;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.overlay.ingredients.IIngredientListOverlayContents;
import mezz.jei.gui.overlay.ingredients.IngredientGrid;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigation;
import mezz.jei.gui.recipes.CraftingStations;
import mezz.jei.gui.recipes.RecipesGui;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Checks focus occlusion against initialized screens and real ingredient-grid layouts. */
@SuppressWarnings("UnstableApiUsage")
final class RecipeFocusSourceClientTest {
	private RecipeFocusSourceClientTest() {}

	static void run(ClientGameTestContext context) {
		context.runOnClient(client -> {
			var runtime = Internal.getJeiRuntime();
			var toggles = Internal.getClientToggleState();
			boolean overlayEnabled = toggles.isOverlayEnabled();
			boolean bookmarksEnabled = toggles.isBookmarkEnabled();
			String filterText = runtime.getIngredientFilter().getFilterText();
			var bookmark = runtime.getIngredientManager().getAllTypedIngredients(VanillaTypes.ITEM_STACK)
				.stream().findFirst().orElseThrow();
			boolean addedBookmark = runtime.getBookmarkManager().add(bookmark);
			var layoutMode = Internal.getClientConfigs().getIngredientListConfig().layoutMode();
			var originalLayoutMode = layoutMode.get();
			KeyMapping toggleKey = Objects.requireNonNull(KeyMapping.get("key.jei.toggleOverlay"));
			var originalToggleKey = KeyMappingHelper.getBoundKeyOf(toggleKey);
			try {
				// An unbound toggle deliberately keeps the ingredient list visible.
				toggleKey.setKey(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_O));
				toggles.setOverlayEnabled(true);
				toggles.setBookmarkEnabled(true);
				runtime.getIngredientFilter().setFilterText("jei_focus_test_no_matching_ingredient");
				client.gui.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
				IngredientListOverlay ingredients = (IngredientListOverlay) runtime.getIngredientListOverlay();
				BookmarkOverlay bookmarks = (BookmarkOverlay) runtime.getBookmarkOverlay();
				IngredientGridWithNavigation contents = contents(ingredients);
				ImmutableRect2i ingredientArea = contents.getIngredientGridArea();
				ImmutableRect2i bookmarkArea = contents(bookmarks).getIngredientGridArea();
				check(!ingredientArea.isEmpty() && !bookmarkArea.isEmpty(), "Expected both ingredient panels to have room");
				check(ingredients.getIngredientUnderMouse(ingredientArea.x() + 1, ingredientArea.y() + 1).findAny().isEmpty(),
					"The filtered ingredient panel must be empty for this check");
				assertBlocks(ingredients, ingredientArea, true, "Empty visible ingredient panel");
				double emptyX = ingredientArea.x() + ingredientArea.width() / 2.0;
				double emptyY = ingredientArea.y() + ingredientArea.height() / 2.0;
				assertPointerBlocked(client.gui.screen(), emptyX, emptyY, true);
				assertBlocks(bookmarks, bookmarkArea, true, "Visible bookmark panel");
				check(!ingredients.isMouseOver(-1, -1) && !bookmarks.isMouseOver(-1, -1),
					"Panels must not block outside their bounds");

				toggles.setOverlayEnabled(false);
				check(!ingredients.isListDisplayed() && !bookmarks.isListDisplayed(), "Expected both panels to be hidden");
				assertBlocks(ingredients, ingredientArea, false, "Hidden ingredient panel");
				assertBlocks(bookmarks, bookmarkArea, false, "Hidden bookmark panel");
				assertPointerBlocked(client.gui.screen(), emptyX, emptyY, false);
				toggles.setOverlayEnabled(true);
				assertBlocks(ingredients, contents(ingredients).getIngredientGridArea(), true, "Reopened ingredient panel");

				GuiContainerWrapper container = new GuiContainerWrapper(runtime.getScreenHelper());
				var properties = runtime.getScreenHelper().getGuiProperties(client.gui.screen()).orElseThrow();
				check(container.isMouseOver(properties.guiLeft() + 1, properties.guiTop() + 1),
					"The container's blank border must block lookup");
				check(!container.isMouseOver(-1, -1), "The container must not block outside its bounds");

				// Keep a hole inside a real grid instead of moving the whole grid around the exclusion.
				layoutMode.set(IngredientGridLayoutMode.MAXIMIZE_AVAILABLE_SPACE);
				ingredients.isListDisplayed();
				ImmutableRect2i available = contents.getBackgroundArea();
				ImmutableRect2i exclusion = new ImmutableRect2i(available.x() + available.width() / 2,
					available.y() + available.height() / 2, 4, 4);
				contents.updateBounds(available, Set.of(exclusion), null);
				IngredientGrid grid = new ReflectionUtil().getFieldWithClass(contents, IngredientGrid.class).findFirst().orElseThrow();
				check(grid.getArea().contains(exclusion.x() + 1, exclusion.y() + 1), "The excluded point must remain inside the grid");
				assertBlocks(contents, exclusion, false, "Excluded navigation-grid region");
				assertBlocks(grid, exclusion, false, "Excluded ingredient-grid region");
				assertBlocks(ingredients, exclusion, false, "Excluded overlay region");
				assertPointerBlocked(client.gui.screen(), exclusion.x() + 1, exclusion.y() + 1, false);
				ImmutableRect2i visibleGrid = grid.getArea();
				assertBlocks(grid, visibleGrid, true, "Visible grid beside the exclusion");
				assertBlocks(contents, visibleGrid, true, "Visible navigation grid beside the exclusion");
				contents.close();
				assertBlocks(contents, visibleGrid, false, "Closed navigation grid");
				assertBlocks(grid, visibleGrid, false, "Closed ingredient grid");

				RecipesGui recipes = (RecipesGui) runtime.getRecipesGui();
				recipes.showTypes(List.of(RecipeTypes.CRAFTING));
				ImmutableRect2i recipeArea = recipes.getArea();
				assertBlocks(recipes, recipeArea, true, "Blank recipe-panel border");
				check(recipes.isMouseOver(recipeArea.x() + 3, recipeArea.y() - 1), "Recipe tabs must block lookup");
				CraftingStations stations = new ReflectionUtil().getFieldWithClass(recipes, CraftingStations.class).findFirst().orElseThrow();
				check(!stations.isEmpty(), "Expected the crafting-table station");
				int stationX = recipeArea.x() - stations.getWidth() + 1;
				int stationY = recipeArea.y() + 1;
				check(stations.getIngredientUnderMouse(stationX, stationY).findAny().isEmpty(), "Expected blank crafting-station padding");
				check(stations.isMouseOver(stationX, stationY) && recipes.isMouseOver(stationX, stationY),
					"Crafting-station padding must block lookup through the recipe GUI");
				check(!recipes.isMouseOver(-1, -1), "Recipe GUI must not block outside its bounds");
				recipes.onClose();
				assertBlocks(recipes, recipeArea, false, "Closed recipe GUI");
				client.gui.setScreen(null);
				assertBlocks(container, recipeArea, false, "Closed container");
			} finally {
				client.gui.setScreen(null);
				toggleKey.setKey(originalToggleKey);
				layoutMode.set(originalLayoutMode);
				runtime.getIngredientFilter().setFilterText(filterText);
				toggles.setOverlayEnabled(overlayEnabled);
				toggles.setBookmarkEnabled(bookmarksEnabled);
				if (addedBookmark) {
					runtime.getBookmarkManager().remove(bookmark);
				}
			}
		});
	}

	private static IngredientGridWithNavigation contents(Object overlay) {
		return (IngredientGridWithNavigation) new ReflectionUtil().getFieldWithClass(overlay, IIngredientListOverlayContents.class)
			.findFirst().orElseThrow();
	}

	static void assertPointerBlocked(Screen screen, double x, double y, boolean blocked) {
		MouseButtonEvent event = new MouseButtonEvent(x, y, new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0));
		check(ScreenMouseEvents.allowMouseClick(screen).invoker().allowMouseClick(screen, event) != blocked,
			"Click propagation must match the visible surface at " + x + ", " + y);
		check(ScreenMouseEvents.allowMouseRelease(screen).invoker().allowMouseRelease(screen, event) != blocked,
			"Click release must follow its captured surface");
		check(ScreenMouseEvents.allowMouseScroll(screen).invoker().allowMouseScroll(screen, x, y, 0, -1) != blocked,
			"Scroll propagation must match the visible surface");
	}

	private static void assertBlocks(IRecipeFocusSource source, ImmutableRect2i area, boolean expected, String description) {
		check(source.isMouseOver(area.x() + 1, area.y() + 1) == expected, description + ": expected blocking=" + expected);
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
