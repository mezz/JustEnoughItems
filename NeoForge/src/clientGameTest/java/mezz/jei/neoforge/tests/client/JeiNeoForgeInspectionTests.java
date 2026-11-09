package mezz.jei.neoforge.tests.client;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.buttons.ButtonSnapshot;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiGuiRuntime;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.Internal;
import mezz.jei.gui.startup.ResourceReloadHandler;
import mezz.jei.library.gui.BookmarkManagerDummy;
import mezz.jei.library.gui.BookmarkOverlayDummy;
import mezz.jei.library.gui.IngredientListOverlayDummy;
import mezz.jei.library.gui.recipes.RecipesGuiDummy;
import mezz.jei.library.ingredients.IngredientFilterApiDummy;
import mezz.jei.library.load.PluginLoader;
import mezz.jei.library.load.registration.RuntimeRegistration;
import mezz.jei.library.runtime.JeiRuntime;
import mezz.jei.neoforge.plugins.neoforge.NeoForgeGuiPlugin;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

final class JeiNeoForgeInspectionTests {
	private static final Duration TIMEOUT = Duration.ofSeconds(60);

	private JeiNeoForgeInspectionTests() {
	}

	static void run() {
		ClientTestUtil.runOnClient(client -> {
			client.setScreen(new InventoryScreen(client.player));
		});
		waitForOverlay();
		verifyGuiRuntimeRegistration();
		ButtonSnapshot nextButton = ClientTestUtil.computeOnClient(client -> InspectionTestPlugin.runtime.getGuiRuntime().getIngredientListOverlay().getNextButton());
		ButtonSnapshot backButton = ClientTestUtil.computeOnClient(client -> InspectionTestPlugin.runtime.getGuiRuntime().getIngredientListOverlay().getBackButton());
		ButtonSnapshot configButton = ClientTestUtil.computeOnClient(client -> InspectionTestPlugin.runtime.getGuiRuntime().getConfigButton().orElseThrow());
		ButtonSnapshot toggleButton = ClientTestUtil.computeOnClient(client -> InspectionTestPlugin.runtime.getGuiRuntime().getBookmarkToggleButton().orElseThrow());
		ClientTestUtil.runOnClient(client -> {
			IJeiRuntime runtime = InspectionTestPlugin.runtime;
			var overlay = runtime.getGuiRuntime().getIngredientListOverlay();
			check(overlay.isListDisplayed() && overlay.hasRoom(), "Ingredient overlay must fit beside the inventory");
			checkContains(overlay.getBounds(), overlay.getGridBounds(), "Ingredient grid must fit inside the panel");
			check(overlay.getPageCount() > 1 && overlay.getPageIndex() >= 0 && overlay.getPageIndex() < overlay.getPageCount(),
				"The unfiltered ingredient list must expose valid pagination");
			check(configButton.visible() && configButton.active(), "Inventory must expose the config control");
			checkScreenBounds(configButton.bounds(), client.screen.width, client.screen.height, "Config control");
			if (nextButton.visible()) {
				check(nextButton.active(), "Multiple pages must enable visible forward navigation");
				checkContains(overlay.getBounds(), nextButton.bounds(), "Forward navigation must fit inside the panel");
			}
			if (backButton.visible()) {
				check(backButton.active(), "Multiple pages must enable visible backward navigation");
				checkContains(overlay.getBounds(), backButton.bounds(), "Backward navigation must fit inside the panel");
			}
			check(toggleButton.visible() && toggleButton.active(), "Inventory must expose the bookmark toggle");
			checkScreenBounds(toggleButton.bounds(), client.screen.width, client.screen.height, "Bookmark toggle");
			var toggleState = Internal.getClientToggleState();
			boolean overlayEnabled = toggleState.isOverlayEnabled();
			boolean bookmarksEnabled = toggleState.isBookmarkEnabled();
			boolean nextActive = nextButton.active();
			boolean backActive = backButton.active();
			boolean nextVisible = nextButton.visible();
			boolean backVisible = backButton.visible();
			ScreenRectangle nextBounds = nextButton.bounds();
			ScreenRectangle backBounds = backButton.bounds();
			try {
				toggleState.setOverlayEnabled(false);
				toggleState.setBookmarkEnabled(false);
				ButtonSnapshot hiddenNext = overlay.getNextButton();
				ButtonSnapshot hiddenBack = overlay.getBackButton();
				ButtonSnapshot disabledListConfig = runtime.getGuiRuntime().getConfigButton().orElseThrow();
				ButtonSnapshot disabledListToggle = runtime.getGuiRuntime().getBookmarkToggleButton().orElseThrow();
				check(!overlay.isListDisplayed() && !hiddenNext.visible() && !hiddenBack.visible(),
					"Disabling the overlay must hide its list and page controls");
				check(hiddenNext.active() == nextActive && hiddenBack.active() == backActive,
					"Page controls must preserve their enabled state while hidden");
				check(nextButton.visible() == nextVisible && backButton.visible() == backVisible &&
					nextButton.active() == nextActive && backButton.active() == backActive &&
					nextButton.bounds().equals(nextBounds) && backButton.bounds().equals(backBounds),
					"Disabling the overlay must not alter previously captured button snapshots");
				check(disabledListConfig.visible() && disabledListConfig.active(),
					"The config control must remain available while the overlay is disabled");
				check(!runtime.getGuiRuntime().getBookmarkOverlay().isListDisplayed() && disabledListToggle.visible() && disabledListToggle.active(),
					"The bookmark toggle must remain available while the bookmark list is disabled");
			} finally {
				toggleState.setOverlayEnabled(overlayEnabled);
				toggleState.setBookmarkEnabled(bookmarksEnabled);
			}
			runtime.getGuiRuntime().getIngredientFilter().setFilterText("inspection_no_matching_ingredient_7fd692");
		});
		ClientTestUtil.waitUntil(() -> ClientTestUtil.computeOnClient(client -> InspectionTestPlugin.runtime.getGuiRuntime().getIngredientFilter().getFilteredIngredients(VanillaTypes.ITEM_STACK).isEmpty()),
			TIMEOUT, () -> "Expected an empty search result");
		ClientTestUtil.runOnClient(client -> {
			IJeiRuntime runtime = InspectionTestPlugin.runtime;
			var empty = runtime.getGuiRuntime().getIngredientListOverlay();
			ButtonSnapshot emptyNext = empty.getNextButton();
			ButtonSnapshot emptyBack = empty.getBackButton();
			ButtonSnapshot emptyConfig = runtime.getGuiRuntime().getConfigButton().orElseThrow();
			check(!empty.isListDisplayed() && empty.hasRoom() && emptyConfig.visible() && emptyConfig.active(),
				"Empty search must hide the ingredient panel while retaining its config control");
			check(empty.getPageIndex() == 0 && empty.getPageCount() == 1, "Empty search must reset pagination to its only page");
			check(!emptyNext.visible() && !emptyBack.visible() && !emptyNext.active() && !emptyBack.active(),
				"Empty search must hide and disable ingredient-page controls");
			check(nextButton.active() && backButton.active(), "Filtering must not alter the initial page-button snapshots");
			checkScreenBounds(emptyConfig.bounds(), client.screen.width, client.screen.height, "Config control after empty search");
			runtime.getGuiRuntime().getIngredientFilter().setFilterText("");
			check(empty.getNextButton().active() && empty.getBackButton().active(), "Clearing the search must re-enable page controls");
			check(!emptyNext.active() && !emptyBack.active(), "Clearing the search must not alter the empty-search button snapshots");
			IRecipesGui gui = runtime.getGuiRuntime().getRecipesGui();
			var focus = runtime.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.INPUT, VanillaTypes.ITEM_STACK, new ItemStack(Items.OAK_LOG));
			gui.show(focus);
			check(gui.isOpen() && gui.getFocuses().size() == 1, "Current recipe focuses");
			check(gui.setRecipeCategory(InspectionTestPlugin.TYPE), "Select fixture category among current categories");
			check(gui.getSelectedRecipeCategory().orElseThrow().getRecipeType().equals(InspectionTestPlugin.TYPE), "Selected category");
			check(!gui.setRecipeCategory(RecipeType.create("jeiclienttests", "missing", Object.class)), "Unavailable category must fail");
			check(gui.getPageIndex() == 0 && gui.getPageCount() > 1, "Numeric recipe pagination");
			IRecipeLayoutDrawable<?> layout = firstLayout(gui);
			check(layout.isShapeless() && !layout.getWidgets().isEmpty(), "Visible layout extras");
			var tooltip = runtime.getJeiHelpers().getGuiHelper().createTooltipBuilder();
			layout.getTooltip(tooltip, layout.getRect().getX() + 4, layout.getRect().getY() + 35);
			check(tooltip.getLines().stream().anyMatch(line -> line.left().map(text -> text.getString().contains("Inspection category tooltip")).orElse(false)), "Recipe tooltip query");
			tooltip.clear();
			layout.getTooltip(tooltip, layout.getRect().getX() + 5, layout.getRect().getY() + 5);
			check(!tooltip.getLines().isEmpty() && tooltip.getLines().stream().noneMatch(line -> line.left().map(text -> text.getString().contains("Inspection category tooltip")).orElse(false)), "Slot tooltip must take precedence over category tooltip");

			InspectionTestPlugin.attempts = 0;
			InspectionTestPlugin.starts = 0;
			InspectionTestPlugin.completions = 0;
			if (!(gui.getParentScreen().orElseThrow() instanceof AbstractContainerScreen<?> containerScreen)) {
				throw new AssertionError("Expected a container behind the recipe view");
			}
			var transfers = runtime.getRecipeTransferManager();
			check(transfers.transferRecipe(containerScreen, layout, client.player, false, false).isEmpty(), "Transfer dry run");
			check(gui.isOpen() && InspectionTestPlugin.attempts == 0 && InspectionTestPlugin.starts == 0 && InspectionTestPlugin.completions == 0, "Dry run must not perform transfers or call listeners");
			var otherScreen = new AbstractContainerScreen<InventoryMenu>(
				new InventoryMenu(client.player.getInventory(), false, client.player), client.player.getInventory(), Component.empty()
			) {
				@Override
				protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
					throw new AssertionError("An inactive transfer target must not be rendered");
				}
			};
			check(transfers.getRecipeTransferHandler(otherScreen.getMenu(), layout.getRecipeCategory()).isPresent(),
				"The inactive target must have a handler so rejection tests the menu identity");
			check(transfers.transferRecipe(otherScreen, layout, client.player, false, false).isPresent() &&
				transfers.transferRecipe(otherScreen, layout, client.player, false, true).isPresent(),
				"A target other than the player's current menu must reject transfer checks and requests");
			check(gui.isOpen() && InspectionTestPlugin.attempts == 0 && InspectionTestPlugin.starts == 0 && InspectionTestPlugin.completions == 0,
				"An inactive target must not invoke the transfer handler or listeners");
			InspectionTestPlugin.blocked = true;
			try {
				check(transfers.transferRecipe(containerScreen, layout, client.player, false, true).isPresent(), "Blocked transfer error");
				check(gui.isOpen() && InspectionTestPlugin.attempts == 0, "Rejected transfer leaves recipe view open");
			} finally {
				InspectionTestPlugin.blocked = false;
			}
			check(transfers.transferRecipe(containerScreen, layout, client.player, false, true).isEmpty(), "Transfer once");
			check(gui.isOpen() && InspectionTestPlugin.attempts == 1 && InspectionTestPlugin.completions == 1 && !InspectionTestPlugin.lastMax,
				"Transfer once must complete without closing the recipe view");
			gui.showTypes(List.of(InspectionTestPlugin.TYPE));
			check(gui.getFocuses().isEmpty(), "Category view has no focus");
			IRecipeLayoutDrawable<?> independent = createIndependentLayout(runtime, firstLayout(gui));
			showDifferentRecipe(gui, independent.getRecipeCategory());
			check(transfers.transferRecipe(containerScreen, independent, client.player, true, true).isEmpty(), "Transfer an independently created layout after navigation");
			check(gui.isOpen() && InspectionTestPlugin.attempts == 2 && InspectionTestPlugin.completions == 2 && InspectionTestPlugin.lastMax,
				"Maximum transfer must complete without closing the recipe view");
			client.screen.onClose();
			check(!gui.isOpen() && gui.getFocuses().isEmpty() && gui.getRecipeCategories().isEmpty() && gui.getSelectedRecipeCategory().isEmpty() && gui.getPageCount() == 0 && gui.getPageIndex() == -1 && gui.getVisibleRecipeLayouts().isEmpty(), "Closed view must not expose stale state");
			check(transfers.transferRecipe(containerScreen, independent, client.player, false, true).isEmpty(), "Transfer an independently created layout with the recipe view closed");
			check(!gui.isOpen() && client.screen == containerScreen && InspectionTestPlugin.attempts == 3 && InspectionTestPlugin.completions == 3 && !InspectionTestPlugin.lastMax,
				"Transfer without the recipe view must complete and preserve the current screen");
			client.setScreen(null);
		});
		ClientTestUtil.waitUntil(() -> ClientTestUtil.computeOnClient(client -> !InspectionTestPlugin.runtime.getGuiRuntime().getIngredientListOverlay().isListDisplayed()),
			TIMEOUT, () -> "Closed inventory must hide overlay");
		ClientTestUtil.runOnClient(client -> {
			var overlay = InspectionTestPlugin.runtime.getGuiRuntime().getIngredientListOverlay();
			var bookmarks = InspectionTestPlugin.runtime.getGuiRuntime().getBookmarkOverlay();
			ButtonSnapshot closedConfig = InspectionTestPlugin.runtime.getGuiRuntime().getConfigButton().orElseThrow();
			ButtonSnapshot closedToggle = InspectionTestPlugin.runtime.getGuiRuntime().getBookmarkToggleButton().orElseThrow();
			check(!closedConfig.visible() && !overlay.getNextButton().visible() && !overlay.getBackButton().visible(),
				"Closing the inventory must hide ingredient controls while keeping their snapshots available");
			check(!bookmarks.isListDisplayed() && !closedToggle.visible() && !bookmarks.getNextButton().visible() && !bookmarks.getBackButton().visible(),
				"Closing the inventory must hide bookmarks and their controls while keeping their snapshots available");
			check(configButton.visible() && toggleButton.visible(), "Closing the inventory must not alter the initial control snapshots");
			var filter = InspectionTestPlugin.runtime.getGuiRuntime().getIngredientFilter();
			String previousFilter = filter.getFilterText();
			try {
				filter.setFilterText("oak");
				ScreenRectangle emptyBounds = new ScreenRectangle(0, 0, 0, 0);
				check(!overlay.hasRoom() && overlay.getBounds().equals(emptyBounds) && overlay.getGridBounds().equals(emptyBounds),
					"Changing the filter with no screen open must not restore the previous overlay layout");
				check(overlay.getPageIndex() == 0 && overlay.getPageCount() == 0,
					"A closed overlay must not expose pages after a filter change");
			} finally {
				filter.setFilterText(previousFilter);
			}
		});
		ClientTestUtil.runOnClient(client -> client.setScreen(new InventoryScreen(client.player)));
		waitForOverlay();
		ClientTestUtil.runOnClient(client -> {
			ButtonSnapshot reopenedToggle = InspectionTestPlugin.runtime.getGuiRuntime().getBookmarkToggleButton().orElseThrow();
			ButtonSnapshot reopenedConfig = InspectionTestPlugin.runtime.getGuiRuntime().getConfigButton().orElseThrow();
			check(reopenedToggle.visible() && reopenedToggle.active(), "Reopening the inventory must expose the bookmark toggle");
			check(reopenedConfig.visible() && reopenedConfig.active(), "Reopening the inventory must expose the config control");
			checkScreenBounds(reopenedToggle.bounds(), client.screen.width, client.screen.height, "Bookmark toggle after reopening inventory");
			checkScreenBounds(reopenedConfig.bounds(), client.screen.width, client.screen.height, "Config control after reopening inventory");
		});
		verifyVanillaTransfers();
		verifyRuntimeReplacement();
		verifyInPlaceRefresh();
	}

	private static void verifyInPlaceRefresh() {
		IJeiRuntime runtime = ClientTestUtil.computeOnClient(client -> InspectionTestPlugin.runtime);
		int availableBefore = ClientTestUtil.computeOnClient(client -> InspectionTestPlugin.availableCallbacks);
		int unavailableBefore = ClientTestUtil.computeOnClient(client -> InspectionTestPlugin.unavailableCallbacks);
		int refreshedBefore = ClientTestUtil.computeOnClient(client -> InspectionTestPlugin.refreshCallbacks);
		ClientTestUtil.computeOnClient(client -> client.reloadResourcePacks())
			.orTimeout(TIMEOUT.toSeconds(), TimeUnit.SECONDS)
			.join();
		ClientTestUtil.runOnClient(client -> {
			check(InspectionTestPlugin.runtime == runtime,
				"Filter refresh must preserve the runtime");
			check(InspectionTestPlugin.refreshCallbacks == refreshedBefore + 1,
				"Filter refresh must notify plugins exactly once");
			check(InspectionTestPlugin.availableCallbacks == availableBefore && InspectionTestPlugin.unavailableCallbacks == unavailableBefore,
				"Filter refresh must not replace the runtime or repeat availability callbacks");
		}, TIMEOUT);
	}

	private static void verifyRuntimeReplacement() {
		IJeiRuntime oldRuntime = ClientTestUtil.computeOnClient(client -> InspectionTestPlugin.runtime);
		ResourceReloadHandler oldReloadHandler = ClientTestUtil.computeOnClient(client -> NeoForgeGuiPlugin.getResourceReloadHandler().orElseThrow());
		int availableBefore = ClientTestUtil.computeOnClient(client -> InspectionTestPlugin.availableCallbacks);
		int unavailableBefore = ClientTestUtil.computeOnClient(client -> InspectionTestPlugin.unavailableCallbacks);
		int refreshedBefore = ClientTestUtil.computeOnClient(client -> InspectionTestPlugin.refreshCallbacks);
		replaceRuntime(oldRuntime);
		ClientTestUtil.runOnClient(client -> check(
			InspectionTestPlugin.availableCallbacks == availableBefore + 1 && InspectionTestPlugin.unavailableCallbacks == unavailableBefore + 1 &&
				InspectionTestPlugin.refreshCallbacks == refreshedBefore,
			"Recipe reload must use availability callbacks instead of reporting an in-place refresh"));
		verifyStaleRefreshHandler(oldReloadHandler);
	}

	private static void replaceRuntime(IJeiRuntime oldRuntime) {
		ClientTestUtil.runOnClient(client -> client.getConnection().handleUpdateRecipes(new ClientboundUpdateRecipesPacket(Internal.getClientSyncedRecipes())));
		ClientTestUtil.waitUntil(() -> ClientTestUtil.computeOnClient(client -> InspectionTestPlugin.runtime != null && InspectionTestPlugin.runtime != oldRuntime),
			TIMEOUT, () -> "Recipe reload must replace the runtime");
	}

	private static void verifyStaleRefreshHandler(ResourceReloadHandler handler) {
		ClientTestUtil.runOnClient(client -> {
			int refreshedBefore = InspectionTestPlugin.refreshCallbacks;
			handler.onResourceManagerReload(client.getResourceManager());
			check(InspectionTestPlugin.refreshCallbacks == refreshedBefore, "A replaced runtime's handler must not report a refresh of the current runtime");
		}, TIMEOUT);
	}

	@SuppressWarnings("unchecked")
	private static void showDifferentRecipe(IRecipesGui gui, IRecipeCategory<?> category) {
		gui.showRecipes((IRecipeCategory<Integer>) category, List.of(99), List.of());
	}

	private static IRecipeLayoutDrawable<?> firstLayout(IRecipesGui gui) {
		check(!gui.getVisibleRecipeLayouts().isEmpty(), "Expected visible recipe layouts");
		return gui.getVisibleRecipeLayouts().getFirst();
	}

	private static <R> IRecipeLayoutDrawable<R> createIndependentLayout(IJeiRuntime runtime, IRecipeLayoutDrawable<R> source) {
		return runtime.getRecipeManager().createRecipeLayoutDrawable(
				source.getRecipeCategory(), source.getRecipe(), runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup()
			)
			.orElseThrow(() -> new AssertionError("Expected an independently created recipe layout"));
	}

	@SuppressWarnings("removal")
	private static void verifyGuiRuntimeRegistration() {
		ClientTestUtil.runOnClient(client -> {
			IJeiRuntime runtime = InspectionTestPlugin.runtime;
			IJeiGuiRuntime guiRuntime = runtime.getGuiRuntime();
			var recipesGui = guiRuntime.getRecipesGui();
			var ingredientOverlay = guiRuntime.getIngredientListOverlay();
			var bookmarkOverlay = guiRuntime.getBookmarkOverlay();
			var ingredientFilter = guiRuntime.getIngredientFilter();
			var bookmarkManager = guiRuntime.getBookmarkManager();
			check(runtime.getRecipesGui() == recipesGui && runtime.getIngredientListOverlay() == ingredientOverlay &&
				runtime.getBookmarkOverlay() == bookmarkOverlay && runtime.getIngredientFilter() == ingredientFilter &&
				runtime.getBookmarkManager() == bookmarkManager,
				"Legacy and grouped GUI accessors must expose the same components");
			RuntimeRegistration registration = new RuntimeRegistration(
				runtime.getRecipeManager(), runtime.getJeiHelpers(), runtime.getEditModeConfig(),
				runtime.getIngredientManager(), runtime.getRecipeTransferManager(), runtime.getScreenHelper(),
				PluginLoader.createSearchStorageFactory(List.of())
			);
			IJeiGuiRuntime disabled = registration.getGuiRuntime();
			check(disabled.getRecipesGui() == RecipesGuiDummy.INSTANCE &&
				disabled.getIngredientListOverlay() == IngredientListOverlayDummy.INSTANCE &&
				disabled.getBookmarkOverlay() == BookmarkOverlayDummy.INSTANCE &&
				disabled.getIngredientFilter() == IngredientFilterApiDummy.INSTANCE &&
				disabled.getBookmarkManager() == BookmarkManagerDummy.INSTANCE &&
				disabled.getConfigButton().isEmpty() && disabled.getBookmarkToggleButton().isEmpty(),
				"Disabled GUI must provide component dummies without GUI controls");
			registration.setRecipesGui(recipesGui);
			registration.setIngredientListOverlay(ingredientOverlay);
			registration.setBookmarkOverlay(bookmarkOverlay);
			registration.setIngredientFilter(ingredientFilter);
			registration.setBookmarkManager(bookmarkManager);
			IJeiGuiRuntime legacyOnly = registration.getGuiRuntime();
			checkSameGuiComponents(guiRuntime, legacyOnly, "Individual registrations must work without registering a GUI runtime");
			check(legacyOnly.getConfigButton().isEmpty() && legacyOnly.getBookmarkToggleButton().isEmpty(),
				"Individual component registrations must not introduce GUI controls");

			registration.setRecipesGui(RecipesGuiDummy.INSTANCE);
			registration.setIngredientListOverlay(IngredientListOverlayDummy.INSTANCE);
			registration.setBookmarkOverlay(BookmarkOverlayDummy.INSTANCE);
			registration.setIngredientFilter(IngredientFilterApiDummy.INSTANCE);
			registration.setBookmarkManager(BookmarkManagerDummy.INSTANCE);
			registration.setGuiRuntime(guiRuntime);
			IJeiGuiRuntime registered = registration.getGuiRuntime();
			checkSameGuiComponents(guiRuntime, registered, "Registering a whole GUI runtime must replace earlier individual registrations");

			var configButton = guiRuntime.getConfigButton();
			var toggleButton = guiRuntime.getBookmarkToggleButton();
			registration.setRecipesGui(RecipesGuiDummy.INSTANCE);
			registration.setIngredientListOverlay(IngredientListOverlayDummy.INSTANCE);
			registration.setBookmarkOverlay(BookmarkOverlayDummy.INSTANCE);
			IJeiGuiRuntime overridden = registration.getGuiRuntime();
			check(overridden.getRecipesGui() == RecipesGuiDummy.INSTANCE &&
				overridden.getIngredientListOverlay() == IngredientListOverlayDummy.INSTANCE &&
				overridden.getBookmarkOverlay() == BookmarkOverlayDummy.INSTANCE &&
				overridden.getIngredientFilter() == ingredientFilter &&
				overridden.getBookmarkManager() == bookmarkManager &&
				overridden.getConfigButton().equals(configButton) && overridden.getBookmarkToggleButton().equals(toggleButton),
				"Individual registrations must replace their components and preserve independent GUI controls");
			registration.setGuiRuntime(guiRuntime);
			checkSameGuiComponents(guiRuntime, registration.getGuiRuntime(), "A later whole GUI runtime registration must replace individual registrations");

			IJeiRuntime compatibilityRuntime = new JeiRuntime(
				runtime.getRecipeManager(), runtime.getIngredientManager(), runtime.getKeyMappings(), runtime.getJeiHelpers(),
				runtime.getScreenHelper(), runtime.getRecipeTransferManager(), runtime.getEditModeConfig(), overridden, runtime.getConfigManager()
			);
			check(compatibilityRuntime.getRecipesGui() == RecipesGuiDummy.INSTANCE &&
				compatibilityRuntime.getIngredientListOverlay() == IngredientListOverlayDummy.INSTANCE &&
				compatibilityRuntime.getBookmarkOverlay() == BookmarkOverlayDummy.INSTANCE &&
				compatibilityRuntime.getIngredientFilter() == ingredientFilter &&
				compatibilityRuntime.getBookmarkManager() == bookmarkManager,
				"Legacy access must expose registered components and preserve the remaining GUI components");
			registration.setGuiRuntime(disabled);
			checkSameGuiComponents(guiRuntime, registered, "Later registrations must not alter a previously resolved GUI runtime");
			checkSameGuiComponents(guiRuntime, legacyOnly, "Later registrations must not alter previously resolved individual registrations");
			check(overridden.getRecipesGui() == RecipesGuiDummy.INSTANCE &&
				legacyOnly.getConfigButton().isEmpty() && overridden.getConfigButton().equals(configButton),
				"Later registrations must preserve previously resolved overrides and controls");
		});
	}

	private static void checkSameGuiComponents(IJeiGuiRuntime expected, IJeiGuiRuntime actual, String message) {
		check(actual.getRecipesGui() == expected.getRecipesGui() &&
			actual.getIngredientListOverlay() == expected.getIngredientListOverlay() &&
			actual.getBookmarkOverlay() == expected.getBookmarkOverlay() &&
			actual.getIngredientFilter() == expected.getIngredientFilter() &&
			actual.getBookmarkManager() == expected.getBookmarkManager(), message);
	}

	private static void waitForOverlay() {
		ClientTestUtil.waitUntil(() -> ClientTestUtil.computeOnClient(client -> InspectionTestPlugin.runtime.getGuiRuntime().getIngredientListOverlay().isListDisplayed()),
			TIMEOUT, () -> "Expected ingredient overlay on inventory screen");
	}

	private static void checkContains(ScreenRectangle outer, ScreenRectangle inner, String message) {
		check(inner.width() > 0 && inner.height() > 0 && inner.left() >= outer.left() && inner.top() >= outer.top() &&
			inner.left() + inner.width() <= outer.left() + outer.width() && inner.top() + inner.height() <= outer.top() + outer.height(),
			message + ": panel=" + outer + ", contents=" + inner);
	}

	private static void checkScreenBounds(ScreenRectangle bounds, int width, int height, String message) {
		checkContains(new ScreenRectangle(0, 0, width, height), bounds, message + " must use GUI-scaled screen coordinates");
	}

	private static void verifyVanillaTransfers() {
		for (boolean max : new boolean[]{false, true}) {
			ClientTestUtil.runOnClient(client -> client.setScreen(new InventoryScreen(client.player)));
			waitForOverlay();
			runOnServer(player -> {
				player.getInventory().clearContent();
				for (int slot = 1; slot <= 4; slot++) {
					player.inventoryMenu.getSlot(slot).set(ItemStack.EMPTY);
				}
				player.getInventory().setItem(9, new ItemStack(Items.OAK_LOG, 16));
				player.inventoryMenu.broadcastChanges();
			});
			ClientTestUtil.waitUntil(() -> ClientTestUtil.computeOnClient(client -> countLogs(client.player.getInventory()) == 16 && craftingLogCount(client.player.inventoryMenu) == 0),
				TIMEOUT, () -> "Expected server-synchronized oak logs before recipe transfer");
			int completedBefore = ClientTestUtil.computeOnClient(client -> InspectionTestPlugin.vanillaCompletions);
			ClientTestUtil.runOnClient(client -> {
				IJeiRuntime runtime = InspectionTestPlugin.runtime;
				IRecipesGui gui = runtime.getGuiRuntime().getRecipesGui();
				var focus = runtime.getJeiHelpers().getFocusFactory()
					.createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, new ItemStack(Items.OAK_PLANKS));
				gui.show(focus);
				check(gui.setRecipeCategory(RecipeTypes.CRAFTING), "Select the crafting category for oak planks");
				IRecipeLayoutDrawable<?> layout = findOakPlanksLayout(gui);
				if (!(gui.getParentScreen().orElseThrow() instanceof AbstractContainerScreen<?> containerScreen)) {
					throw new AssertionError("Expected the inventory behind the crafting recipe");
				}
				var transfers = runtime.getRecipeTransferManager();
				check(transfers.transferRecipe(containerScreen, layout, client.player, max, false).isEmpty(), "Vanilla transfer dry run");
				check(gui.isOpen() && InspectionTestPlugin.vanillaCompletions == completedBefore &&
					craftingLogCount(client.player.inventoryMenu) == 0 && countLogs(client.player.getInventory()) == 16,
					"Vanilla dry run must keep the view open and leave inventory contents unchanged");
				if (max) {
					layout = createIndependentLayout(runtime, layout);
					client.screen.onClose();
				}
				check(transfers.transferRecipe(containerScreen, layout, client.player, max, true).isEmpty(), "Vanilla transfer request");
				check(gui.isOpen() != max, "Vanilla transfer must preserve whether the recipe view is open");
			});
			int expectedGridCount;
			String transferMode;
			if (max) {
				expectedGridCount = 16;
				transferMode = "maximum";
			} else {
				expectedGridCount = 1;
				transferMode = "once";
			}
			ClientTestUtil.waitUntil(() -> ClientTestUtil.computeOnClient(client -> InspectionTestPlugin.vanillaCompletions == completedBefore + 1 &&
				craftingLogCount(client.player.inventoryMenu) == expectedGridCount &&
				countLogs(client.player.getInventory()) == 16 - expectedGridCount &&
				client.player.inventoryMenu.getSlot(0).getItem().is(Items.OAK_PLANKS)),
				TIMEOUT, () -> "Expected " + transferMode + " transfer server acknowledgment and synchronized crafting ingredients");
			runOnServer(player -> {
				check(craftingLogCount(player.inventoryMenu) == expectedGridCount, "Server crafting grid after recipe transfer");
				check(countLogs(player.getInventory()) == 16 - expectedGridCount, "Server inventory after recipe transfer");
				check(player.inventoryMenu.getSlot(0).getItem().is(Items.OAK_PLANKS), "Server crafting result after recipe transfer");
			});
			ClientTestUtil.runOnClient(client -> {
				IRecipesGui gui = InspectionTestPlugin.runtime.getGuiRuntime().getRecipesGui();
				check(gui.isOpen() != max, "Server acknowledgment must preserve whether the recipe view is open");
				if (gui.isOpen()) {
					client.screen.onClose();
				}
				check(client.screen instanceof InventoryScreen, "Closing the recipe view returns to inventory");
			});
		}
		org.apache.logging.log4j.LogManager.getLogger().info("Vanilla recipe transfers passed: once and maximum, server acknowledgments and inventory contents verified");
	}

	private static IRecipeLayoutDrawable<?> findOakPlanksLayout(IRecipesGui gui) {
		return gui.getVisibleRecipeLayouts().stream()
			.filter(layout -> layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.OUTPUT).stream()
				.flatMap(IRecipeSlotView::getItemStacks)
				.anyMatch(stack -> stack.is(Items.OAK_PLANKS)))
			.filter(layout -> layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.INPUT).stream()
				.flatMap(IRecipeSlotView::getItemStacks)
				.anyMatch(stack -> stack.is(Items.OAK_LOG)))
			.findFirst()
			.orElseThrow(() -> new AssertionError("Expected a visible oak planks recipe using oak logs"));
	}

	private static int countLogs(net.minecraft.world.Container inventory) {
		int count = 0;
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.is(Items.OAK_LOG)) {
				count += stack.getCount();
			}
		}
		return count;
	}

	private static int craftingLogCount(net.minecraft.world.inventory.InventoryMenu menu) {
		int count = 0;
		for (int slot = 1; slot <= 4; slot++) {
			ItemStack stack = menu.getSlot(slot).getItem();
			if (stack.is(Items.OAK_LOG)) {
				count += stack.getCount();
			}
		}
		return count;
	}

	private static void runOnServer(java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> action) {
		java.util.concurrent.CompletableFuture<Void> completion = ClientTestUtil.computeOnClient(client -> {
			java.util.concurrent.CompletableFuture<Void> future = new java.util.concurrent.CompletableFuture<>();
			var server = client.getSingleplayerServer();
			var playerId = client.player.getUUID();
			server.execute(() -> {
				try {
					action.accept(server.getPlayerList().getPlayer(playerId));
					future.complete(null);
				} catch (Throwable t) {
					future.completeExceptionally(t);
				}
			});
			return future;
		});
		try {
			completion.get(10, java.util.concurrent.TimeUnit.SECONDS);
		} catch (Exception e) {
			throw new AssertionError("Integrated server transfer assertion failed", e);
		}
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
