package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.input.PinnedTooltipManager;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.recipes.RecipeGuiLayouts;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;

/** Exercises pinned tooltip bookmarking and scrolling through Minecraft's input callbacks. */
@SuppressWarnings("UnstableApiUsage")
public class InteractiveIngredientTooltipClientGameTest {
	static void run() {
		FabricClientTestContext context = new FabricClientTestContext();
		RecipeFocusSourceClientTest.run(context);
		runScrollChecks(context);
		runBookmarkChecks(context);
	}

	private static void runScrollChecks(FabricClientTestContext context) {
		var originalScreen = context.computeOnClient(client -> client.screen);
		var smoothScrolling = Internal.getClientConfigs().getClientConfig().smoothScrollingEnabled();
		boolean originalSmoothScrolling = smoothScrolling.get();
		try {
			context.runOnClient(client -> {
				smoothScrolling.set(false);
				client.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
				var manager = Internal.getJeiRuntime().getRecipeManager();
				var category = new ScrollCategory<>(manager.getRecipeCategory(RecipeTypes.SMELTING));
				gui().showRecipes(category, manager.createRecipeLookup(RecipeTypes.SMELTING).get().limit(1).toList(), List.of());
			});
			context.waitTicks(3);
			Point source = context.computeOnClient(client -> findPoint((x, y) -> layouts().getRecipeLayoutUnderMouse(x, y)
				.flatMap(layout -> layout.getRecipeLayout().getSlotUnderMouse(x, y))
				.filter(slot -> slot.slot().getDisplayedIngredients().count() == 80)
				.isPresent()));
			context.getInput().holdKey(InputConstants.KEY_LSHIFT);
			move(context, source);
			context.waitTicks(3);
			Point ingredient = context.computeOnClient(client -> findPoint((x, y) -> gui().getForegroundInputLayer().containsMouse(x, y) &&
				gui().getIngredientUnderMouse(x, y).findAny().isPresent() &&
				layouts().getRecipeLayoutUnderMouse(x, y).flatMap(layout -> layout.getRecipeLayout().getSlotUnderMouse(x, y)).isEmpty()));
			move(context, ingredient);
			String originalIngredient = tooltipIngredient(context, ingredient);

			// Minecraft can report Shift-wheel as horizontal scrolling while the pin key is held.
			context.getInput().scroll(-1, 0);
			context.waitTicks(2);
			String horizontallyScrolled = tooltipIngredient(context, ingredient);
			context.getInput().scroll(1, 0);
			context.waitTicks(2);
			String restoredIngredient = tooltipIngredient(context, ingredient);
			context.getInput().scroll(0, -1);
			context.waitTicks(2);
			String verticallyScrolled = tooltipIngredient(context, ingredient);
			context.takeScreenshot("jei-pinned-grid-shift-scroll");

			if (originalIngredient.equals(horizontallyScrolled) || !originalIngredient.equals(restoredIngredient) || !horizontallyScrolled.equals(verticallyScrolled)) {
				throw new AssertionError("Pinned grids must scroll in both wheel axes without changing the underlying recipe: original=" + originalIngredient +
					", horizontal=" + horizontallyScrolled + ", restored=" + restoredIngredient + ", vertical=" + verticallyScrolled);
			}
		} finally {
			context.getInput().releaseKey(InputConstants.KEY_LSHIFT);
			context.runOnClient(client -> {
				gui().onClose();
				client.setScreen(originalScreen);
				smoothScrolling.set(originalSmoothScrolling);
			});
		}
	}

	private static String tooltipIngredient(FabricClientTestContext context, Point point) {
		return context.computeOnClient(client -> gui().getIngredientUnderMouse(point.x(), point.y()).findFirst().orElseThrow()
			.getTypedIngredient().getItemStack().orElseThrow().getHoverName().getString());
	}

	private static final class ScrollCategory<T> extends AbstractRecipeCategory<T> {
		private ScrollCategory(IRecipeCategory<T> delegate) {
			super(delegate.getRecipeType(), delegate.getTitle(), Internal.getJeiRuntime().getJeiHelpers().getGuiHelper().getSlotDrawable(), 160, 40);
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
			var candidates = IntStream.range(0, 80).mapToObj(index -> {
					ItemStack stack = new ItemStack(Items.STONE);
					stack.setHoverName(Component.literal("Pinned candidate " + index));
					return stack;
				})
				.toList();
			builder.addInputSlot(4, 4).addItemStacks(candidates);
		}
	}

	private static void runBookmarkChecks(FabricClientTestContext context) {
		List<Runnable> cleanup = new ArrayList<>();
		try {
			context.runOnClient(client -> {
				var runtime = Internal.getJeiRuntime();
				var config = Internal.getClientConfigs().getClientConfig();
				var recipeWidth = config.recipeGuiWidth();
				int originalRecipeWidth = recipeWidth.get();
				cleanup.add(() -> recipeWidth.set(originalRecipeWidth));
				// Leave room for the bookmark panel at 1.20.1's automatic GUI scale.
				recipeWidth.set(200);
				var columns = config.maxRecipeGuiColumns();
				int originalColumns = columns.get();
				cleanup.add(() -> columns.set(originalColumns));
				columns.set(1);
				var sortBookmarks = config.recipeSortingBookmarksEnabled();
				boolean originalSortBookmarks = sortBookmarks.get();
				cleanup.add(() -> sortBookmarks.set(originalSortBookmarks));
				sortBookmarks.set(false);
				var originalBookmarks = bookmarks().getElements().stream()
					.flatMap(element -> element.getBookmark().stream())
					.toList();
				cleanup.add(() -> replaceBookmarks(originalBookmarks));
				replaceBookmarks(List.of());
				client.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
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
				return foreground.containsMouse(x, y) &&
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
				return foreground.containsMouse(x, y) &&
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
			context.runOnClient(client -> client.setScreen(new InventoryScreen(Objects.requireNonNull(client.player))));
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
				client.setScreen(null);
				for (int i = cleanup.size() - 1; i >= 0; i--) {
					cleanup.get(i).run();
				}
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

	private static boolean checkObscuredBookmark(FabricClientTestContext context, Point source) {
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
					stack.setHoverName(Component.literal("Wide interactive tooltip for bookmark occlusion regression"));
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
		context.takeScreenshot("jei-interactive-tooltip-obscured-bookmark");
		context.runOnClient(client -> {
			BookmarkOverlay overlay = (BookmarkOverlay) Internal.getJeiRuntime().getBookmarkOverlay();
			if (!overlay.isListDisplayed()) {
				throw new AssertionError("Expected visible bookmarks for tooltip occlusion: hasRoom=" + overlay.hasRoom() +
					", bookmarks=" + bookmarks().getElements().size() + ", overlayEnabled=" + Internal.getWorldConfig().isOverlayEnabled() +
					", bookmarksEnabled=" + Internal.getWorldConfig().isBookmarkEnabled());
			}
		});
		Point covered = context.computeOnClient(client -> findPoint(
			new ImmutableRect2i(0, 0, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight()),
			(x, y) -> gui().getForegroundInputLayer().containsMouse(x, y) &&
				gui().getIngredientUnderMouse(x, y).findAny().isEmpty() &&
				((BookmarkOverlay) Internal.getJeiRuntime().getBookmarkOverlay()).getIngredientUnderMouse(x, y).findAny().isPresent()
		));
		IBookmark hiddenBookmark = context.computeOnClient(client -> ((BookmarkOverlay) Internal.getJeiRuntime().getBookmarkOverlay())
			.getIngredientUnderMouse(covered.x(), covered.y()).findFirst().orElseThrow().getElement().getBookmark().orElseThrow());
		move(context, covered);
		context.runOnClient(client -> RecipeFocusSourceClientTest.assertPointerBlocked(client.screen, covered.x(), covered.y(), true));
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

	private static void move(FabricClientTestContext context, Point point) {
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
			replacements = new java.util.ArrayList<>(replacements);
			java.util.Collections.reverse(replacements);
		}
		replacements.forEach(bookmarks::add);
	}

	private record Point(int x, int y) {}
}
