package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.HistoryDisplaySide;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.IngredientGridNavigationMode;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.gui.overlay.ingredients.IngredientGrid;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.overlay.bookmarks.history.LookupHistory;
import mezz.jei.gui.overlay.bookmarks.history.LookupHistoryOverlay;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigation;
import mezz.jei.gui.overlay.ingredients.IIngredientListOverlayContents;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.mezzdev.config.api.value.IConfigValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

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
						for (boolean background : List.of(false, true)) {
							context.runOnClient(client -> {
								for (boolean bookmarks : List.of(false, true)) {
									IIngredientGridConfig config = gridConfig(bookmarks);
									set(cleanup, config.maxColumns(), 6);
									set(cleanup, config.maxRows(), 6);
									set(cleanup, config.navigationMode(), mode);
									set(cleanup, config.drawBackground(), background);
								}
							});
							context.waitTicks(3);
							assertGridResize(context, false);
							assertGridResize(context, true);
						}
					}
					assertHistoryResize(context, cleanup);
					assertRecipeResize(context);
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
			if (!gridConfig(bookmarks).drawBackground().get()) {
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
			BookmarkList bookmarks = (BookmarkList) Internal.getJeiRuntime().getBookmarkManager();
			bookmarks.getElements().forEach(element -> element.getBookmark().ifPresent(history::add));
			boolean bookmarkEnabled = Internal.getClientToggleState().isBookmarkEnabled();
			cleanup.add(() -> Internal.getClientToggleState().setBookmarkEnabled(bookmarkEnabled));
		});
		for (boolean bookmarks : List.of(false, true)) {
			for (IngredientGridNavigationMode mode : IngredientGridNavigationMode.values()) {
				for (boolean background : List.of(false, true)) {
					// Include history displayed on its own, with the bookmark panel hidden.
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
						set(cleanup, gridConfig(bookmarks).drawBackground(), background);
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
