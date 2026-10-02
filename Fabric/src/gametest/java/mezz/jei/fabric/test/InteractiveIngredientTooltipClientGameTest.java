package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.Internal;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.input.PinnedTooltipManager;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.recipes.RecipeGuiLayouts;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

/** Exercises pinned tooltip bookmarking through Minecraft's keyboard callbacks. */
@SuppressWarnings("UnstableApiUsage")
public class InteractiveIngredientTooltipClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		JUnitXmlTestReporter.runAndReport("fabric-client-gametest", getClass().getSimpleName(), () -> {
			try (TestSingleplayerContext ignored = context.worldBuilder().create()) {
				JeiFabricClientGameTestAssertions.assertJeiStartedWithSyncedRecipes(context);
				RecipeFocusSourceClientTest.run(context);
				runBookmarkChecks(context);
			}
		});
	}

	private static void runBookmarkChecks(ClientGameTestContext context) {
		List<Runnable> cleanup = new ArrayList<>();
		try {
			context.runOnClient(client -> {
				var runtime = Internal.getJeiRuntime();
				var sortBookmarks = Internal.getClientConfigs().getClientConfig().recipeSortingBookmarksEnabled();
				boolean originalSortBookmarks = sortBookmarks.get();
				cleanup.add(() -> sortBookmarks.set(originalSortBookmarks));
				sortBookmarks.set(false);
				var originalBookmarks = bookmarks().getElements().stream()
					.flatMap(element -> element.getBookmark().stream())
					.toList();
				cleanup.add(() -> replaceBookmarks(originalBookmarks));
				replaceBookmarks(List.of());
				client.gui.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
				var focus = runtime.getJeiHelpers().getFocusFactory().createFocus(
					RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, new ItemStack(Items.CRAFTING_TABLE)
				);
				gui().show(List.of(focus));
			});
			context.waitTicks(2);
			Point source = context.computeOnClient(client -> findPoint((x, y) -> layouts().getRecipeLayoutUnderMouse(x, y)
				.flatMap(layout -> layout.getRecipeLayout().getSlotUnderMouse(x, y))
				.filter(slot -> slot.slot().getDisplayedIngredients().count() > 1)
				.isPresent()));
			context.getInput().holdKey(InputConstants.KEY_LSHIFT);
			move(context, source);
			context.waitTicks(2);
			Point blank = context.computeOnClient(client -> findPoint((x, y) -> {
				var foreground = gui().getForegroundInputLayer();
				return foreground.isMouseOver(x, y) &&
					gui().getIngredientUnderMouse(x, y).findAny().isEmpty() &&
					layouts().getRecipeLayoutUnderMouse(x, y)
						.filter(layout -> layout.getRecipeLayout().getSlotUnderMouse(x, y).isEmpty())
						.isPresent();
			}));
			move(context, blank);
			context.getInput().pressKey(InputConstants.KEY_A);
			boolean blankIgnored = context.computeOnClient(client -> bookmarks().getElements().isEmpty());
			context.runOnClient(client -> replaceBookmarks(List.of()));

			Point ingredientPoint = context.computeOnClient(client -> findPoint((x, y) -> {
				var foreground = gui().getForegroundInputLayer();
				return foreground.isMouseOver(x, y) &&
					gui().getIngredientUnderMouse(x, y).findAny().isPresent() &&
					layouts().getRecipeLayoutUnderMouse(x, y)
						.filter(layout -> layout.getRecipeLayout().getSlotUnderMouse(x, y).isEmpty())
						.isPresent();
			}));
			var ingredient = context.computeOnClient(client -> gui()
				.getIngredientUnderMouse(ingredientPoint.x(), ingredientPoint.y()).findFirst().orElseThrow().getTypedIngredient());
			move(context, ingredientPoint);
			context.getInput().pressKey(InputConstants.KEY_A);
			boolean ingredientBookmarked = context.computeOnClient(client -> Internal.getJeiRuntime().getBookmarkManager().contains(ingredient));
			context.takeScreenshot("jei-interactive-tooltip-bookmark");
			boolean obscuredBookmarkIgnored = checkObscuredBookmark(context, source);

			// Closing via another screen must retire the foreground focus source too.
			context.runOnClient(client -> client.gui.setScreen(new InventoryScreen(Objects.requireNonNull(client.player))));
			boolean closedTooltipIgnored = context.computeOnClient(client -> gui()
				.getIngredientUnderMouse(ingredientPoint.x(), ingredientPoint.y()).findAny().isEmpty());
			boolean externalTooltipsRestored = context.computeOnClient(client -> !PinnedTooltipManager.shouldSuppressExternalTooltip());
			if (!blankIgnored || !ingredientBookmarked || !obscuredBookmarkIgnored || !closedTooltipIgnored || !externalTooltipsRestored) {
				throw new AssertionError("Pinned tooltip input: blankIgnored=" + blankIgnored +
					", ingredientBookmarked=" + ingredientBookmarked + ", obscuredBookmarkIgnored=" + obscuredBookmarkIgnored +
					", closedTooltipIgnored=" + closedTooltipIgnored +
					", externalTooltipsRestored=" + externalTooltipsRestored);
			}
		} finally {
			context.getInput().releaseKey(InputConstants.KEY_LSHIFT);
			context.runOnClient(client -> {
				gui().onClose();
				client.gui.setScreen(null);
				cleanup.reversed().forEach(Runnable::run);
			});
		}
	}

	private static Point findPoint(BiPredicate<Integer, Integer> predicate) {
		return findPoint(gui().getArea(), predicate);
	}

	private static Point findPoint(ImmutableRect2i area, BiPredicate<Integer, Integer> predicate) {
		for (int y = area.y(); y < area.y() + area.height(); y++) {
			for (int x = area.x(); x < area.x() + area.width(); x++) {
				if (predicate.test(x, y)) {
					return new Point(x, y);
				}
			}
		}
		throw new AssertionError("Expected a tooltip test target over a recipe layout");
	}

	private static boolean checkObscuredBookmark(ClientGameTestContext context, Point source) {
		context.getInput().releaseKey(InputConstants.KEY_LSHIFT);
		move(context, source);
		context.runOnClient(client -> {
			var runtime = Internal.getJeiRuntime();
			var slot = layouts().getRecipeLayoutUnderMouse(source.x(), source.y()).orElseThrow()
				.getRecipeLayout().getSlotUnderMouse(source.x(), source.y()).orElseThrow().slot();
			// A wide tooltip reaches across the bookmark list, as long translated item names can do.
			var candidates = slot.getDisplayedIngredients().flatMap(ingredient -> ingredient.getItemStack().stream())
				.map(stack -> {
					stack = stack.copy();
					stack.set(DataComponents.CUSTOM_NAME, Component.literal("Wide interactive tooltip for bookmark occlusion regression"));
					return stack;
				})
				.toList();
			slot.createDisplayOverrides().addItemStacks(candidates);
			runtime.getIngredientManager().getAllTypedIngredients(VanillaTypes.ITEM_STACK)
				.stream().limit(80).forEach(runtime.getBookmarkManager()::add);
		});
		context.getInput().holdKey(InputConstants.KEY_LSHIFT);
		move(context, new Point(source.x() + 1, source.y() + 1));
		context.waitTicks(2);
		Point covered = context.computeOnClient(client -> findPoint(
			new ImmutableRect2i(0, 0, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight()),
			(x, y) -> gui().getForegroundInputLayer().isMouseOver(x, y) &&
				gui().getIngredientUnderMouse(x, y).findAny().isEmpty() &&
				((BookmarkOverlay) Internal.getJeiRuntime().getBookmarkOverlay()).getIngredientUnderMouse(x, y).findAny().isPresent()
		));
		IBookmark hiddenBookmark = context.computeOnClient(client -> ((BookmarkOverlay) Internal.getJeiRuntime().getBookmarkOverlay())
			.getIngredientUnderMouse(covered.x(), covered.y()).findFirst().orElseThrow().getElement().getBookmark().orElseThrow());
		move(context, covered);
		context.runOnClient(client -> RecipeFocusSourceClientTest.assertPointerBlocked(client.gui.screen(), covered.x(), covered.y(), true));
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		move(context, new Point(covered.x() + 8, covered.y() + 8));
		context.runOnClient(client -> {
			var overlay = (BookmarkOverlay) Internal.getJeiRuntime().getBookmarkOverlay();
			if (overlay.hasBookmarkDrag()) {
				throw new AssertionError("A blank pinned tooltip must not start dragging the bookmark underneath it");
			}
		});
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		move(context, covered);
		context.getInput().pressKey(InputConstants.KEY_A);
		return context.computeOnClient(client -> bookmarks().contains(hiddenBookmark));
	}

	private static void move(ClientGameTestContext context, Point point) {
		double scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
		context.getInput().setCursorPos(point.x() * scale, point.y() * scale);
		context.waitTick();
	}

	private static RecipesGui gui() {
		return (RecipesGui) Internal.getJeiRuntime().getRecipesGui();
	}

	private static RecipeGuiLayouts layouts() {
		return gui().getRecipeLayouts();
	}

	private static BookmarkList bookmarks() {
		return ((BookmarkOverlay) Internal.getJeiRuntime().getBookmarkOverlay()).getBookmarkList();
	}

	private static void replaceBookmarks(List<IBookmark> replacements) {
		BookmarkList bookmarks = bookmarks();
		bookmarks.getElements().stream()
			.flatMap(element -> element.getBookmark().stream())
			.toList().forEach(bookmarks::remove);
		if (Internal.getClientConfigs().getClientConfig().bookmarkAddPosition().get().isFront()) {
			replacements = replacements.reversed();
		}
		replacements.forEach(bookmarks::add);
	}

	private record Point(int x, int y) {}
}
