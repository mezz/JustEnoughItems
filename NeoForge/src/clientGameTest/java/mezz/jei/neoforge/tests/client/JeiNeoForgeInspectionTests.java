package mezz.jei.neoforge.tests.client;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.time.Duration;
import java.util.List;

final class JeiNeoForgeInspectionTests {
	private static final Duration TIMEOUT = Duration.ofSeconds(60);

	private JeiNeoForgeInspectionTests() {
	}

	static void run() {
		ClientTestUtil.runOnClient(client -> {
			client.gui.setScreen(new InventoryScreen(client.player));
		});
		waitForOverlay();
		ClientTestUtil.runOnClient(client -> {
			IJeiRuntime runtime = InspectionTestPlugin.runtime;
			IRecipesGui gui = runtime.getRecipesGui();
			var focus = runtime.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.INPUT, VanillaTypes.ITEM_STACK, new ItemStack(Items.OAK_LOG));
			gui.show(focus);
			check(gui.isOpen() && gui.getFocuses().size() == 1, "Current recipe focuses");
			check(gui.setRecipeCategory(InspectionTestPlugin.TYPE), "Select fixture category among current categories");
			check(gui.getSelectedRecipeCategory().orElseThrow().getRecipeType().equals(InspectionTestPlugin.TYPE), "Selected category");
			check(!gui.setRecipeCategory(IRecipeType.create("jeiclienttests", "missing", Object.class)), "Unavailable category must fail");
			check(gui.getPageIndex() == 0 && gui.getPageCount() > 1, "Numeric recipe pagination");
			IRecipeLayoutDrawable<?> layout = firstLayout(gui);
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
				public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
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
			client.gui.screen().onClose();
			check(!gui.isOpen() && gui.getFocuses().isEmpty() && gui.getRecipeCategories().isEmpty() && gui.getSelectedRecipeCategory().isEmpty() && gui.getPageCount() == 0 && gui.getPageIndex() == -1 && gui.getVisibleRecipeLayouts().isEmpty(), "Closed view must not expose stale state");
			check(transfers.transferRecipe(containerScreen, independent, client.player, false, true).isEmpty(), "Transfer an independently created layout with the recipe view closed");
			check(!gui.isOpen() && client.gui.screen() == containerScreen && InspectionTestPlugin.attempts == 3 && InspectionTestPlugin.completions == 3 && !InspectionTestPlugin.lastMax,
				"Transfer without the recipe view must complete and preserve the current screen");
			client.gui.setScreen(null);
		});
		verifyVanillaTransfers();
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

	private static void waitForOverlay() {
		ClientTestUtil.waitUntil(() -> ClientTestUtil.computeOnClient(client -> InspectionTestPlugin.runtime.getIngredientListOverlay().isListDisplayed()),
			TIMEOUT, () -> "Expected ingredient overlay on inventory screen");
	}

	private static void verifyVanillaTransfers() {
		for (boolean max : new boolean[]{false, true}) {
			ClientTestUtil.runOnClient(client -> client.gui.setScreen(new InventoryScreen(client.player)));
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
				IRecipesGui gui = runtime.getRecipesGui();
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
					client.gui.screen().onClose();
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
				IRecipesGui gui = InspectionTestPlugin.runtime.getRecipesGui();
				check(gui.isOpen() != max, "Server acknowledgment must preserve whether the recipe view is open");
				if (gui.isOpen()) {
					client.gui.screen().onClose();
				}
				check(client.gui.screen() instanceof InventoryScreen, "Closing the recipe view returns to inventory");
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
