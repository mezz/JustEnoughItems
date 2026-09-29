package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.common.Internal;
import mezz.jei.common.config.HistoryDisplaySide;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.IngredientGridBackgroundStyle;
import mezz.jei.common.config.IngredientGridNavigationMode;
import mezz.jei.common.config.RecipeGuiNavigationMode;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.fabric.input.FabricKeyMapping;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.overlay.bookmarks.history.LookupHistory;
import mezz.jei.gui.overlay.bookmarks.history.LookupHistoryOverlay;
import mezz.jei.gui.overlay.ingredients.IIngredientListOverlayContents;
import mezz.jei.gui.overlay.ingredients.IngredientGrid;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigation;
import mezz.jei.gui.recipes.IRecipeGuiLogic;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.mezzdev.config.api.value.IConfigValue;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Exercises resizing through Minecraft's mouse callbacks and the loader's input routing. */
@SuppressWarnings("UnstableApiUsage")
public class JeiGuiResizeClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		JUnitXmlTestReporter.runAndReport("fabric-client-gametest", getClass().getSimpleName(), () -> {
			try (TestSingleplayerContext ignored = context.worldBuilder().create()) {
				JeiFabricClientGameTestAssertions.assertJeiStartedWithSyncedRecipes(context);
				List<Runnable> cleanup = new ArrayList<>();
				int[] windowSize = context.computeOnClient(client -> new int[]{client.getWindow().getScreenWidth(), client.getWindow().getScreenHeight()});
				try {
					context.runOnClient(client -> {
						int scale = client.options.guiScale().get();
						cleanup.add(() -> {
							client.options.guiScale().set(scale);
							client.resizeGui();
						});
						client.options.guiScale().set(2);
						client.resizeGui();
						var configs = Internal.getClientConfigs();
						set(cleanup, configs.getClientConfig().guiResizeEnabled(), true);
						set(cleanup, configs.getClientConfig().recipeGuiWidth(), 240);
						set(cleanup, configs.getClientConfig().maxRecipeGuiHeight(), 260);
						set(cleanup, configs.getClientConfig().lookupHistoryEnabled(), false);
						var runtime = Internal.getJeiRuntime();
						for (var bookmark : runtime.getIngredientManager().getAllTypedIngredients(VanillaTypes.ITEM_STACK).stream().limit(100).toList()) {
							if (runtime.getBookmarkManager().add(bookmark)) {
								cleanup.add(() -> runtime.getBookmarkManager().remove(bookmark));
							}
						}
					});
					context.getInput().resizeWindow(1400, 1000);
					context.runOnClient(client -> client.gui.setScreen(new InventoryScreen(Objects.requireNonNull(client.player))));
					for (IngredientGridNavigationMode mode : IngredientGridNavigationMode.values()) {
						for (IngredientGridBackgroundStyle backgroundStyle : IngredientGridBackgroundStyle.values()) {
							context.runOnClient(client -> {
								for (boolean bookmarks : List.of(false, true)) {
									IIngredientGridConfig config = gridConfig(bookmarks);
									set(cleanup, config.maxColumns(), 6);
									set(cleanup, config.maxRows(), 6);
									set(cleanup, config.navigationMode(), mode);
									set(cleanup, config.backgroundStyle(), backgroundStyle);
								}
							});
							context.waitTicks(3);
							assertGridResize(context, false);
							assertGridResize(context, true);
						}
					}
					assertBookmarkDragCapture(context, cleanup);
					assertPageShortcuts(context, cleanup);
					assertShortcutPriority(context);
					assertHistoryResize(context, cleanup);
					assertRecipeResize(context);
					assertRecipeShortcuts(context, cleanup);
					RecipeInputCaptureClientTest.run(context);
				} finally {
					context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
					context.runOnClient(client -> {
						client.gui.setScreen(null);
						cleanup.reversed().forEach(Runnable::run);
					});
					context.getInput().resizeWindow(windowSize[0], windowSize[1]);
				}
			}
		});
	}

	private static <T> void set(List<Runnable> cleanup, IConfigValue<T> config, T value) {
		T original = config.get();
		cleanup.add(() -> config.set(original));
		config.set(value);
	}

	private static void assertBookmarkDragCapture(ClientGameTestContext context, List<Runnable> cleanup) {
		var bookmark = context.computeOnClient(client -> {
			var config = Internal.getClientConfigs().getClientConfig();
			set(cleanup, config.dragDelayMs(), 0);
			set(cleanup, config.dragToRearrangeBookmarksEnabled(), true);
			var firstSlot = grid(true).getAllSlots().getFirst();
			return firstSlot.getOptionalElement().orElseThrow().getBookmark().orElseThrow();
		});
		List<ImmutableRect2i> slots = context.computeOnClient(client -> grid(true).getAllSlots().stream().limit(2).map(slot -> slot.getArea()).toList());
		int originalIndex = context.computeOnClient(client -> {
			BookmarkList bookmarks = ((BookmarkOverlay) Internal.getJeiRuntime().getBookmarkOverlay()).getBookmarkList();
			int index = bookmarks.getElements().indexOf(bookmark.getElement());
			cleanup.add(() -> bookmarks.moveBookmark(bookmark, index));
			return index;
		});
		ImmutableRect2i origin = slots.getFirst();
		ImmutableRect2i target = slots.get(1);
		move(context, origin.x() + 9, origin.y() + 9);
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		context.runOnClient(client -> {
			var screen = Objects.requireNonNull(client.gui.screen());
			var extraPress = new MouseButtonEvent(-1, -1, new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0));
			check(!ScreenMouseEvents.allowMouseClick(screen).invoker().allowMouseClick(screen, extraPress),
				"An extra press outside JEI must preserve the active bookmark drag");
			check(!ScreenMouseEvents.allowMouseDrag(screen).invoker().allowMouseDrag(screen, extraPress, -1, -1),
				"A captured drag must not reach the container outside JEI");
		});
		move(context, target.x() + 9, target.y() + 9);
		context.waitTicks(2);
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		context.runOnClient(client -> {
			BookmarkList bookmarks = ((BookmarkOverlay) Internal.getJeiRuntime().getBookmarkOverlay()).getBookmarkList();
			check(bookmarks.getElements().indexOf(bookmark.getElement()) == originalIndex + 1,
				"Releasing the captured drag must reorder the original bookmark");
		});

		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		move(context, origin.x() + 9, origin.y() + 9);
		context.waitTicks(2);
		context.runOnClient(client -> {
			check(!bookmark.isVisible(), "The second drag must lift the bookmark from its slot");
			client.gui.setScreen(null);
			check(bookmark.isVisible(), "Closing directly to gameplay must restore the dragged bookmark");
			client.gui.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
		});
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
	}

	private static void assertPageShortcuts(ClientGameTestContext context, List<Runnable> cleanup) {
		context.runOnClient(client -> {
			set(cleanup, gridConfig(false).navigationMode(), IngredientGridNavigationMode.PAGED);
			set(cleanup, gridConfig(true).navigationMode(), IngredientGridNavigationMode.PAGED);
		});
		var bookmarkArea = context.computeOnClient(client -> grid(true).getIngredientGridArea());
		move(context, bookmarkArea.x() + 9, bookmarkArea.y() + 9);
		context.waitTick();
		context.runOnClient(client -> {
			var grid = grid(false);
			var area = grid.getIngredientGridArea();
			var pages = grid.getPageDelegate();
			check(pages.getPageCount() > 2, "Expected several pages for shortcut navigation");
			var screen = Objects.requireNonNull(client.gui.screen());
			FabricKeyMapping mapping = (FabricKeyMapping) Objects.requireNonNull(KeyMapping.get("key.jei.nextPage"));
			var originalKey = mapping.getRealKey();
			try {
				mapping.setKey(InputConstants.Type.MOUSE.getOrCreate(4));
				int initialPage = pages.getPageNumber();
				var click = new MouseButtonEvent(area.x() + 9, area.y() + 9, new MouseButtonInfo(4, 0));
				check(!ScreenMouseEvents.allowMouseClick(screen).invoker().allowMouseClick(screen, click), "The remapped page press must be consumed");
				check(pages.getPageNumber() == initialPage, "A mouse-bound page shortcut must wait for release");
				mapping.setKey(InputConstants.Type.KEYBOARD.getOrCreate(InputConstants.KEY_P));
				check(!ScreenMouseEvents.allowMouseRelease(screen).invoker().allowMouseRelease(screen, click), "The captured page release must be consumed");
				int nextPage = (initialPage + 1) % pages.getPageCount();
				check(pages.getPageNumber() == nextPage, "A captured action must execute once even if its binding changes before release");

				var bookmarkPages = grid(true).getPageDelegate();
				check(bookmarkPages.getPageCount() > 1, "Expected several bookmark pages for shortcut navigation");
				int bookmarkPage = bookmarkPages.getPageNumber();
				check(!ScreenKeyboardEvents.allowKeyPress(screen).invoker().allowKeyPress(screen, new KeyEvent(InputConstants.KEY_P, 0, 0)),
					"The keyboard page shortcut must work while the pointer is over bookmarks");
				check(bookmarkPages.getPageNumber() == (bookmarkPage + 1) % bookmarkPages.getPageCount(), "The hovered list's keyboard shortcut must advance immediately");
				check(pages.getPageNumber() == nextPage, "Only the hovered list must advance when both lists use the same shortcut");
			} finally {
				mapping.setKey(originalKey);
			}
		});
	}

	private static void assertShortcutPriority(ClientGameTestContext context) {
		context.runOnClient(client -> {
			var screen = Objects.requireNonNull(client.gui.screen());
			var grid = grid(false);
			var pages = grid.getPageDelegate();
			FabricKeyMapping nextPage = (FabricKeyMapping) Objects.requireNonNull(KeyMapping.get("key.jei.nextPage"));
			FabricKeyMapping previousPage = (FabricKeyMapping) Objects.requireNonNull(KeyMapping.get("key.jei.previousPage"));
			FabricKeyMapping editMode = (FabricKeyMapping) Objects.requireNonNull(KeyMapping.get("key.jei.toggleEditMode"));
			var originalNext = nextPage.getRealKey();
			var originalPrevious = previousPage.getRealKey();
			var originalEdit = editMode.getRealKey();
			var toggles = Internal.getClientToggleState();
			boolean originalEditMode = toggles.isEditModeEnabled();
			try {
				toggles.setEditModeEnabled(false);
				check(pages.getPageCount() > 2, "Conflicting next/previous bindings require more than two pages");

				// The next-page button must beat both a hovered previous-page shortcut and a global toggle.
				var leftClick = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT);
				previousPage.setKey(leftClick);
				editMode.setKey(leftClick);
				var button = grid.getNextPageButtonArea();
				int initialPage = pages.getPageNumber();
				var click = new MouseButtonEvent(button.x() + button.width() / 2.0, button.y() + button.height() / 2.0,
					new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0));
				check(!ScreenMouseEvents.allowMouseClick(screen).invoker().allowMouseClick(screen, click), "The page button must accept the press");
				check(pages.getPageNumber() == initialPage, "The public button callback must simulate on mouse press");
				check(!ScreenMouseEvents.allowMouseRelease(screen).invoker().allowMouseRelease(screen, click), "The page button must consume its release");
				check(pages.getPageNumber() == (initialPage + 1) % pages.getPageCount(), "The page button must win over the conflicting previous-page command");
				check(!toggles.isEditModeEnabled(), "A handled control must prevent the global command");

				var shortcut = InputConstants.Type.MOUSE.getOrCreate(4);
				nextPage.setKey(shortcut);
				editMode.setKey(shortcut);
				var area = grid.getIngredientGridArea();
				initialPage = pages.getPageNumber();
				clickShortcut(screen, area.x() + 9, area.y() + 9);
				check(pages.getPageNumber() == (initialPage + 1) % pages.getPageCount(), "The hovered input area's command must win over a global command");
				check(!toggles.isEditModeEnabled(), "The conflicting global command must not also execute");

				initialPage = pages.getPageNumber();
				clickShortcut(screen, 0, 0);
				check(toggles.isEditModeEnabled(), "A global command must win over a non-hovered input area's command");
				check(pages.getPageNumber() == initialPage, "The conflicting non-hovered page command must not also execute");
				toggles.setEditModeEnabled(false);

				editMode.setKey(InputConstants.UNKNOWN);
				initialPage = pages.getPageNumber();
				clickShortcut(screen, 0, 0);
				check(pages.getPageNumber() == (initialPage + 1) % pages.getPageCount(), "An unbound global command must allow the screen-scoped page command");
			} finally {
				nextPage.setKey(originalNext);
				previousPage.setKey(originalPrevious);
				editMode.setKey(originalEdit);
				toggles.setEditModeEnabled(originalEditMode);
			}
		});
	}

	private static void clickShortcut(Screen screen, double x, double y) {
		var click = new MouseButtonEvent(x, y, new MouseButtonInfo(4, 0));
		check(!ScreenMouseEvents.allowMouseClick(screen).invoker().allowMouseClick(screen, click), "The shortcut press must be consumed");
		check(!ScreenMouseEvents.allowMouseRelease(screen).invoker().allowMouseRelease(screen, click), "The shortcut release must be consumed");
	}

	private static void assertRecipeShortcuts(ClientGameTestContext context, List<Runnable> cleanup) {
		move(context, 0, 0);
		context.runOnClient(client -> {
			set(cleanup, Internal.getClientConfigs().getClientConfig().recipeGuiNavigationMode(), RecipeGuiNavigationMode.PAGED);
			RecipesGui recipes = (RecipesGui) Internal.getJeiRuntime().getRecipesGui();
			recipes.showTypes(List.of(RecipeTypes.CRAFTING));
			IRecipeGuiLogic logic = new ReflectionUtil().getFieldWithClass(recipes, IRecipeGuiLogic.class).findFirst().orElseThrow();
			logic.goToFirstPage();
			check(!recipes.isMouseOver(0, 0), "Shortcut test must run outside the recipe panel");
			check(logic.hasMultiplePages(), "Expected multiple crafting pages");
			String firstPage = logic.getPageString();
			String secondPage = "2/" + firstPage.split("/")[1];
			assertMouseRecipeShortcut("key.jei.nextRecipePage", logic::getPageString, secondPage);
			assertMouseRecipeShortcut("key.jei.previousRecipePage", logic::getPageString, firstPage);

			recipes.showTypes(List.of(RecipeTypes.SMELTING));
			assertMouseRecipeShortcut("key.jei.recipeBack", () -> logic.getSelectedRecipeCategory().getRecipeType(), RecipeTypes.CRAFTING);
			assertMouseRecipeShortcut("key.jei.recipeForward", () -> logic.getSelectedRecipeCategory().getRecipeType(), RecipeTypes.SMELTING);
			assertMouseRecipeShortcut("key.jei.closeRecipeGui", recipes::isOpen, false);

			var parent = Objects.requireNonNull(client.gui.screen());
			check(parent instanceof InventoryScreen, "Close shortcut must restore the parent screen");
		});
	}

	private static <T> void assertMouseRecipeShortcut(String keyName, Supplier<T> state, T expected) {
		var screen = Objects.requireNonNull(Minecraft.getInstance().gui.screen());
		FabricKeyMapping mapping = (FabricKeyMapping) Objects.requireNonNull(KeyMapping.get(keyName));
		var originalKey = mapping.getRealKey();
		try {
			mapping.setKey(InputConstants.Type.MOUSE.getOrCreate(4));
			T initial = state.get();
			var click = new MouseButtonEvent(0, 0, new MouseButtonInfo(4, 0));
			check(!ScreenMouseEvents.allowMouseClick(screen).invoker().allowMouseClick(screen, click), keyName + " must accept presses outside the panel");
			check(Objects.equals(state.get(), initial), keyName + " must wait for release");
			check(!ScreenMouseEvents.allowMouseRelease(screen).invoker().allowMouseRelease(screen, click), keyName + " must consume the captured release");
			check(Objects.equals(state.get(), expected), keyName + " must execute outside the panel");
		} finally {
			mapping.setKey(originalKey);
		}
	}

	private static IngredientGridWithNavigation grid(boolean bookmarks) {
		var runtime = Internal.getJeiRuntime();
		Object overlay = runtime.getIngredientListOverlay();
		if (bookmarks) {
			overlay = runtime.getBookmarkOverlay();
		}
		return (IngredientGridWithNavigation) new ReflectionUtil().getFieldWithClass(overlay, IIngredientListOverlayContents.class).findFirst().orElseThrow();
	}

	private static IIngredientGridConfig gridConfig(boolean bookmarks) {
		var configs = Internal.getClientConfigs();
		if (bookmarks) {
			return configs.getBookmarkListConfig();
		}
		return configs.getIngredientListConfig();
	}

	private static void assertGridResize(ClientGameTestContext context, boolean bookmarks) {
		ImmutableRect2i initial = context.computeOnClient(client -> {
			ImmutableRect2i area = grid(bookmarks).getBackgroundArea();
			if (!gridConfig(bookmarks).backgroundStyle().get().isEnabled()) {
				return area.expandBy(5);
			}
			return area;
		});
		check(!initial.isEmpty(), "Expected a visible grid before resizing");
		double x = initial.x() + 1;
		int deltaX = 18;
		String screenshot = "jei-ingredients-resized";
		if (bookmarks) {
			x = initial.x() + initial.width() - 1;
			deltaX = -18;
			screenshot = "jei-bookmarks-resized";
		}
		double y = initial.y() + initial.height() - 1;
		assertResizingDisabled(context, x, y, -deltaX, 18, () -> grid(bookmarks).getBackgroundArea());
		move(context, x, y);
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		move(context, x - deltaX, y + 18);
		context.waitTicks(2);
		assertGridSize(context, bookmarks, 7);
		context.runOnClient(client -> {
			var screen = Objects.requireNonNull(client.gui.screen());
			ScreenKeyboardEvents.allowKeyPress(screen).invoker().allowKeyPress(screen, new KeyEvent(InputConstants.KEY_LSHIFT, 0, 0));
		});
		move(context, x - 2 * deltaX, y + 36);
		context.waitTicks(2);
		assertGridSize(context, bookmarks, 8);
		context.takeScreenshot(screenshot + "-growing");
		move(context, x + deltaX, y - 18);
		context.waitTicks(2);
		assertGridSize(context, bookmarks, 5);
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		context.runOnClient(client -> {
			check(gridConfig(bookmarks).maxColumns().get() == 5, "Release should retain columns");
			check(gridConfig(bookmarks).maxRows().get() == 5, "Release should retain rows");
		});
		context.takeScreenshot(screenshot);
	}

	private static void assertGridSize(ClientGameTestContext context, boolean bookmarks, int size) {
		context.runOnClient(client -> {
			// Observe the rendered grid without forcing a pending layout update through the navigation getters.
			IngredientGrid grid = new ReflectionUtil().getFieldWithClass(grid(bookmarks), IngredientGrid.class).findFirst().orElseThrow();
			check(grid.getArea().width() == size * 18, "Live grid should have " + size + " columns, got " + grid.getArea().width());
			check(grid.getArea().height() == size * 18, "Live grid should have " + size + " rows, got " + grid.getArea().height());
			check(grid.size() == size * size, "Live grid should update its slots");
			check(grid.getVisibleElements().count() == size * size, "Live grid should populate its new slots");
			check(gridConfig(bookmarks).maxColumns().get() == size, "Dragging must update the configured columns");
			check(gridConfig(bookmarks).maxRows().get() == size, "Dragging must update the configured rows");
			check(Objects.requireNonNull(client.player).containerMenu.getCarried().isEmpty(), "Resizing must not pick up an ingredient");
		});
	}

	private static LookupHistoryOverlay historyOverlay(boolean bookmarks) {
		Object overlay = Internal.getJeiRuntime().getIngredientListOverlay();
		if (bookmarks) {
			overlay = Internal.getJeiRuntime().getBookmarkOverlay();
		}
		return new ReflectionUtil().getFieldWithClass(overlay, LookupHistoryOverlay.class).findFirst().orElseThrow();
	}

	private static IngredientGrid historyGrid(boolean bookmarks) {
		IngredientGridWithNavigation contents = new ReflectionUtil().getFieldWithClass(historyOverlay(bookmarks), IngredientGridWithNavigation.class).findFirst().orElseThrow();
		return new ReflectionUtil().getFieldWithClass(contents, IngredientGrid.class).findFirst().orElseThrow();
	}

	private static void assertHistoryResize(ClientGameTestContext context, List<Runnable> cleanup) {
		context.runOnClient(client -> {
			IClientConfig config = Internal.getClientConfigs().getClientConfig();
			set(cleanup, config.lookupHistoryEnabled(), true);
			set(cleanup, config.maxLookupHistoryIngredients(), 100);
			LookupHistory history = (LookupHistory) historyOverlay(true).getLookupHistory();
			BookmarkList bookmarks = ((BookmarkOverlay) Internal.getJeiRuntime().getBookmarkOverlay()).getBookmarkList();
			bookmarks.getElements().forEach(element -> element.getBookmark().ifPresent(history::add));
			boolean bookmarkEnabled = Internal.getClientToggleState().isBookmarkEnabled();
			cleanup.add(() -> Internal.getClientToggleState().setBookmarkEnabled(bookmarkEnabled));
		});
		for (boolean bookmarks : List.of(false, true)) {
			for (IngredientGridNavigationMode mode : IngredientGridNavigationMode.values()) {
				for (IngredientGridBackgroundStyle backgroundStyle : IngredientGridBackgroundStyle.values()) {
					boolean background = backgroundStyle.isEnabled();
					// Include history displayed on its own, with the bookmark list hidden.
					boolean ownerVisible = !bookmarks || mode == IngredientGridNavigationMode.PAGED;
					final int ownerRows;
					if (!bookmarks && mode == IngredientGridNavigationMode.SCROLLING && background) {
						ownerRows = 100;
					} else {
						ownerRows = 6;
					}
					context.runOnClient(client -> {
						IClientConfig config = Internal.getClientConfigs().getClientConfig();
						HistoryDisplaySide side = HistoryDisplaySide.RIGHT;
						if (bookmarks) {
							side = HistoryDisplaySide.LEFT;
						}
						set(cleanup, config.lookupHistoryDisplaySide(), side);
						set(cleanup, config.maxLookupHistoryColumns(), 6);
						set(cleanup, config.maxLookupHistoryRows(), 2);
						set(cleanup, gridConfig(bookmarks).maxColumns(), 6);
						set(cleanup, gridConfig(bookmarks).maxRows(), ownerRows);
						set(cleanup, gridConfig(bookmarks).navigationMode(), mode);
						set(cleanup, gridConfig(bookmarks).backgroundStyle(), backgroundStyle);
						Internal.getClientToggleState().setBookmarkEnabled(ownerVisible);
					});
					context.waitTicks(3);
					if (ownerVisible && ownerRows == 6) {
						assertOwnerResizeLeavesHistoryIndependent(context, bookmarks, background);
					}
					assertHistoryDrag(context, bookmarks, background, ownerVisible, ownerRows);
				}
			}
		}
	}

	private static void assertOwnerResizeLeavesHistoryIndependent(ClientGameTestContext context, boolean bookmarks, boolean background) {
		ImmutableRect2i historyArea = context.computeOnClient(client -> historyGrid(bookmarks).getArea());
		ImmutableRect2i ownerArea = context.computeOnClient(client -> {
			ImmutableRect2i area = grid(bookmarks).getBackgroundArea();
			if (!background) {
				return area.expandBy(5);
			}
			return area;
		});
		double x = ownerArea.x() + 1;
		int dx = -36;
		if (bookmarks) {
			x = ownerArea.x() + ownerArea.width() - 1;
			dx = 36;
		}
		double y = ownerArea.y() + ownerArea.height() - 1;
		move(context, x, y);
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		move(context, x + dx, y + 36);
		context.waitTicks(2);
		assertGridSize(context, bookmarks, 8);
		context.runOnClient(client -> check(historyGrid(bookmarks).getArea().equals(historyArea), "Resizing the upper panel must leave history's size and position unchanged"));
		context.takeScreenshot("jei-history-independent-panels");
		context.runOnClient(client -> Internal.getClientConfigs().getClientConfig().guiResizeEnabled().set(false));
		context.waitTick();
		context.runOnClient(client -> Internal.getClientConfigs().getClientConfig().guiResizeEnabled().set(true));
		move(context, x + dx * 2, y + 54);
		context.waitTicks(2);
		assertGridSize(context, bookmarks, 8);
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		context.runOnClient(client -> {
			check(historyGrid(bookmarks).getArea().equals(historyArea), "Saving the upper panel size must leave history unchanged");
			check(Internal.getClientConfigs().getClientConfig().maxLookupHistoryColumns().get() == 6, "The history width setting must remain independent");
			gridConfig(bookmarks).maxColumns().set(6);
			gridConfig(bookmarks).maxRows().set(6);
		});
		context.waitTicks(2);
	}

	private static void assertHistoryDrag(ClientGameTestContext context, boolean bookmarks, boolean background, boolean ownerVisible, int ownerRows) {
		ImmutableRect2i initial = context.computeOnClient(client -> {
			check(historyOverlay(bookmarks).isListDisplayed(), "History must be visible before resizing");
			check(historyGrid(bookmarks).getColumnCount() == 6, "History should use its own configured width");
			ImmutableRect2i area = historyOverlay(bookmarks).getBackgroundArea();
			if (ownerVisible) {
				ImmutableRect2i owner = grid(bookmarks).getBackgroundArea();
				check(area.y() > owner.y() + owner.height(), "History and the upper panel must have a visible gap");
			}
			if (!background) {
				return area.expandBy(5);
			}
			return area;
		});
		double x = initial.x() + 1;
		int dx = -18;
		if (bookmarks) {
			x = initial.x() + initial.width() - 1;
			dx = 18;
		}
		double y = initial.y() + 1;
		int dy = -18;
		assertResizingDisabled(context, x, y, dx, dy, () -> historyOverlay(bookmarks).getBackgroundArea());
		move(context, x, y);
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		move(context, x + Math.signum(dx), y + Math.signum(dy));
		context.waitTick();
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		context.runOnClient(client -> check(Internal.getClientConfigs().getClientConfig().maxLookupHistoryColumns().get() == 6, "A one-pixel drag must preserve the history width"));
		move(context, x, y);
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		move(context, x + dx, y + dy);
		context.waitTicks(2);
		assertHistorySize(context, bookmarks, 7, 3, ownerRows);
		move(context, x + dx, y + 8 * dy);
		context.waitTicks(2);
		assertHistorySize(context, bookmarks, 7, 7, ownerRows);
		context.takeScreenshot("jei-history-resized");
		move(context, x - dx, y - dy);
		context.waitTicks(2);
		assertHistorySize(context, bookmarks, 5, 1, ownerRows);
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		context.runOnClient(client -> {
			IClientConfig config = Internal.getClientConfigs().getClientConfig();
			check(config.maxLookupHistoryColumns().get() == 5 && config.maxLookupHistoryRows().get() == 1, "History should retain its independent size on release");
			client.gui.setScreen(null);
			client.gui.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
		});
		context.waitTicks(2);
		ImmutableRect2i resized = context.computeOnClient(client -> {
			check(historyGrid(bookmarks).getColumnCount() == 5 && historyGrid(bookmarks).getRowCount() == 1, "History size must survive reopening");
			ImmutableRect2i area = historyOverlay(bookmarks).getBackgroundArea();
			if (!background) {
				return area.expandBy(5);
			}
			return area;
		});
		// Closing a screen must end the drag and retain the last applied size on either side.
		double edgeY = resized.y() + 1;
		double centerX = resized.x() + resized.width() / 2.0;
		move(context, centerX, edgeY);
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		move(context, centerX, edgeY + dy);
		context.waitTicks(2);
		context.runOnClient(client -> {
			check(historyGrid(bookmarks).getRowCount() == 2, "Second history drag must add another row");
			client.gui.setScreen(null);
		});
		context.runOnClient(client -> client.gui.setScreen(new InventoryScreen(Objects.requireNonNull(client.player))));
		context.waitTicks(2);
		move(context, centerX, edgeY + 2 * dy);
		context.waitTicks(2);
		context.runOnClient(client -> check(historyGrid(bookmarks).getRowCount() == 2, "Reopening must retain the last size without continuing the old drag"));
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
	}

	private static void assertHistorySize(ClientGameTestContext context, boolean bookmarks, int columns, int rows, int ownerRows) {
		context.runOnClient(client -> {
			IngredientGrid grid = historyGrid(bookmarks);
			check(grid.getColumnCount() == columns && grid.getRowCount() == rows, "Live history should be " + columns + "x" + rows + ", got " + grid.getColumnCount() + "x" + grid.getRowCount());
			ImmutableRect2i area = grid.getArea();
			IRecipeFocusSource overlay = (IRecipeFocusSource) Internal.getJeiRuntime().getIngredientListOverlay();
			if (bookmarks) {
				overlay = (IRecipeFocusSource) Internal.getJeiRuntime().getBookmarkOverlay();
			}
			check(historyOverlay(bookmarks).isMouseOver(area.x() + 1, area.y() + 1), "Visible history must block ingredient lookup");
			check(overlay.isMouseOver(area.x() + 1, area.y() + 1), "The owning overlay must block lookup over history, even when its main panel is hidden");
			check(grid.getVisibleElements().count() == columns * rows, "History should populate new slots during the drag");
			IClientConfig config = Internal.getClientConfigs().getClientConfig();
			check(config.maxLookupHistoryColumns().get() == columns && config.maxLookupHistoryRows().get() == rows, "History dimensions must update during the drag");
			check(gridConfig(bookmarks).maxColumns().get() == 6 && gridConfig(bookmarks).maxRows().get() == ownerRows, "History resizing must not change its owner's saved size");
			IngredientGrid owner = new ReflectionUtil().getFieldWithClass(grid(bookmarks), IngredientGrid.class).findFirst().orElseThrow();
			check(owner.getColumnCount() == 6, "History resizing must leave the upper panel width unchanged");
			if (ownerRows == 6) {
				check(owner.getRowCount() == 6, "History resizing must leave the upper panel height unchanged when both panels fit");
			}
		});
	}

	private static void assertRecipeResize(ClientGameTestContext context) {
		context.runOnClient(client -> Internal.getJeiRuntime().getRecipesGui().showTypes(List.of(RecipeTypes.CRAFTING)));
		context.waitTicks(2);
		ImmutableRect2i initial = context.computeOnClient(client -> ((RecipesGui) client.gui.screen()).getArea());
		double x = initial.x() + initial.width() - 1;
		double y = initial.y() + initial.height() - 1;
		assertResizingDisabled(context, x, y, 20, 20, () -> ((RecipesGui) Minecraft.getInstance().gui.screen()).getArea());
		move(context, x, y);
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		move(context, x + 20, y + 20);
		context.waitTicks(2);
		context.runOnClient(client -> {
			RecipesGui screen = (RecipesGui) client.gui.screen();
			check(screen.getArea().width() == initial.width() + 40, "Centered resize should move both horizontal edges");
			check(screen.getArea().height() == initial.height() + 40, "Centered resize should move both vertical edges");
			IClientConfig config = Internal.getClientConfigs().getClientConfig();
			check(config.recipeGuiWidth().get() == 280 && config.maxRecipeGuiHeight().get() == 300, "Recipe dimensions must update during the drag");
		});
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		ImmutableRect2i resized = context.computeOnClient(client -> ((RecipesGui) client.gui.screen()).getArea());
		context.takeScreenshot("jei-recipes-resized");
		context.runOnClient(client -> {
			IClientConfig config = Internal.getClientConfigs().getClientConfig();
			check(config.recipeGuiWidth().get() == 280 && config.maxRecipeGuiHeight().get() == 300, "Recipe size should remain after release");
			client.gui.screen().onClose();
			Internal.getJeiRuntime().getRecipesGui().showTypes(List.of(RecipeTypes.CRAFTING));
			check(((RecipesGui) client.gui.screen()).getArea().equals(resized), "Recipe size must survive reopening");
		});
		// Disabling resizing during a drag must end it, even if it is enabled again before release.
		move(context, resized.x() + resized.width() - 1, resized.y() + resized.height() - 1);
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		context.runOnClient(client -> Internal.getClientConfigs().getClientConfig().guiResizeEnabled().set(false));
		context.waitTick();
		context.runOnClient(client -> Internal.getClientConfigs().getClientConfig().guiResizeEnabled().set(true));
		move(context, resized.x() + resized.width() + 19, resized.y() + resized.height() + 19);
		context.waitTicks(2);
		context.runOnClient(client -> check(((RecipesGui) client.gui.screen()).getArea().equals(resized), "Disabling resizing must end an active recipe drag"));
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);

		// Closing a screen during a drag keeps the applied dimensions without retaining the drag.
		move(context, resized.x() + resized.width() - 1, resized.y() + resized.height() - 1);
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		move(context, resized.x() + resized.width() + 19, resized.y() + resized.height() + 19);
		context.waitTicks(2);
		ImmutableRect2i lastSize = context.computeOnClient(client -> ((RecipesGui) client.gui.screen()).getArea());
		context.runOnClient(client -> {
			client.gui.screen().onClose();
			Internal.getJeiRuntime().getRecipesGui().showTypes(List.of(RecipeTypes.CRAFTING));
		});
		context.waitTick();
		move(context, lastSize.x() + lastSize.width() + 19, lastSize.y() + lastSize.height() + 19);
		context.waitTicks(2);
		context.runOnClient(client -> check(((RecipesGui) client.gui.screen()).getArea().equals(lastSize), "Reopening must retain the recipe size without continuing the old drag"));
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.getInput().resizeWindow(640, 600);
		context.waitTick();
		context.runOnClient(client -> {
			RecipesGui screen = (RecipesGui) client.gui.screen();
			check(screen.getArea().width() < lastSize.width(), "The narrow viewport must exercise width clamping");
			check(Objects.requireNonNull(screen.getProperties()).guiLeft() >= 0, "A smaller window must keep the recipe side controls on screen");
			check(Internal.getClientConfigs().getClientConfig().recipeGuiWidth().get() == 320, "Viewport clamping must not overwrite the preferred width");
		});
		context.getInput().resizeWindow(1400, 1000);
		context.waitTick();
		context.runOnClient(client -> check(((RecipesGui) client.gui.screen()).getArea().equals(lastSize), "Growing the window must restore the preferred size"));
	}

	private static void assertResizingDisabled(ClientGameTestContext context, double x, double y, double dx, double dy, Supplier<ImmutableRect2i> area) {
		ImmutableRect2i initial = context.computeOnClient(client -> {
			Internal.getClientConfigs().getClientConfig().guiResizeEnabled().set(false);
			return area.get();
		});
		context.waitTick();
		move(context, x, y);
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		move(context, x + dx, y + dy);
		context.waitTicks(2);
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		context.runOnClient(client -> {
			check(area.get().equals(initial), "Disabled resizing must leave the panel unchanged");
			Internal.getClientConfigs().getClientConfig().guiResizeEnabled().set(true);
		});
		context.waitTick();
	}

	private static void move(ClientGameTestContext context, double x, double y) {
		double[] position = context.computeOnClient(client -> new double[]{
			x * client.getWindow().getScreenWidth() / client.getWindow().getGuiScaledWidth(),
			y * client.getWindow().getScreenHeight() / client.getWindow().getGuiScaledHeight()
		});
		context.getInput().setCursorPos(position[0], position[1]);
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
