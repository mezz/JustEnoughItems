package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IScrollGridWidget;
import mezz.jei.api.gui.widgets.ScrollbarVisibility;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.common.Internal;
import mezz.jei.common.config.NavigationVisibility;
import mezz.jei.common.gui.elements.DrawableBlank;
import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.InputPhase;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.overlay.elements.RecipeBookmarkElement;
import mezz.jei.library.focus.FocusGroup;
import net.minecraft.client.Screenshot;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.common.util.ImmutablePoint2i;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Checks grid contents, clipping, and scrolling through real recipe layouts and pinned previews. */
@SuppressWarnings("UnstableApiUsage")
final class RecipeScrollGridClientTest {
	private RecipeScrollGridClientTest() {}

	static void run() {
		var context = new FabricClientTestContext();
		context.runOnClient(client -> {
			var originalScreen = client.screen;
			var runtime = Internal.getJeiRuntime();
			var navigation = Internal.getClientConfigs().getIngredientListConfig().navigationVisibility();
			var smoothScrolling = Internal.getClientConfigs().getClientConfig().smoothScrollingEnabled();
			var scrollRate = Internal.getClientConfigs().getClientConfig().smoothScrollRate();
			var originalNavigation = navigation.get();
			boolean originalSmoothScrolling = smoothScrolling.get();
			int originalScrollRate = scrollRate.get();
			try {
				var screen = new InventoryScreen(Objects.requireNonNull(client.player));
				client.setScreen(screen);
				navigation.set(NavigationVisibility.ENABLED);
				smoothScrolling.set(false);
				IGuiHelper helper = runtime.getJeiHelpers().getGuiHelper();
				TestCategory category = new TestCategory(helper);
				var layout = runtime.getRecipeManager().createRecipeLayoutDrawable(category, "test", FocusGroup.EMPTY).orElseThrow();
				var initialSlot = layout.getSlotUnderMouse(5, 5).orElseThrow();
				IScrollGridWidget grid = Objects.requireNonNull(category.grid);
				IJeiInputHandler input = (IJeiInputHandler) grid;
				IInputTarget target = (IInputTarget) grid;
				check(grid.getWidth() == 70, "The default grid must follow enabled navigation");
				check(initialSlot.slot() == category.slots.get(0), "The layout must expose the grid's first slot");

				navigation.set(NavigationVisibility.DISABLED);
				check(grid.getWidth() == 54 && grid.getArea().getWidth() == 54, "Disabling navigation must remove the scrollbar gutter from the grid bounds");
				check(!input.handleInput(62, 1, press(62, 1)), "A hidden scrollbar must not accept a press");
				check(target.beginInput(screen, press(62, 1), Internal.getKeyMappings()).isEmpty(), "A hidden scrollbar must not capture mouse input");
				input.handleMouseScrolled(1, 1, -100);
				check(grid.getSlotUnderMouse(1, 1).orElseThrow().slot() == category.slots.get(6), "Hidden navigation must still allow scrolling to the last page");

				navigation.set(NavigationVisibility.ENABLED);
				var drag = target.beginInput(screen, press(62, 1), Internal.getKeyMappings()).orElseThrow();
				drag.drag(62, 35, InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT), 0, 34);
				int firstSlotIndex = category.slots.indexOf(grid.getSlotUnderMouse(1, 1).orElseThrow().slot());
				check(firstSlotIndex == 6, "Dragging the visible scrollbar must reach the last page: first slot=" + firstSlotIndex);
				grid.setSlots(category.slots);
				drag.drag(62, 35, InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT), 0, 34);
				check(!input.handleMouseDragged(62, 35, InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT), 0, 34), "Replacing overflowing contents must cancel an active scrollbar drag");
				check(grid.getSlotUnderMouse(1, 1).orElseThrow().slot() == category.slots.get(0), "Replacing contents must reset to the first row");

				navigation.set(NavigationVisibility.AUTO_HIDE);
				grid.setSlots(category.slots.subList(0, 6));
				check(grid.getWidth() == 54, "Auto-hide must hide navigation when the last row fits exactly");
				grid.setSlots(category.slots.subList(0, 7));
				check(grid.getWidth() == 70, "Adding an overflowing row must restore auto-hidden navigation");
				input.handleMouseScrolled(1, 1, -100);
				check(grid.getSlotUnderMouse(1, 1).orElseThrow().slot() == category.slots.get(3), "Scroll limits must use the replacement slot count");

				IRecipeSlotDrawable standalone = helper.createRecipeSlotDrawable(RecipeIngredientRole.INPUT,
					acceptor -> acceptor.addItemStack(new ItemStack(Items.COAL, 32)), Set.of(), 0);
				List<IRecipeSlotDrawable> replacement = new ArrayList<>(List.of(standalone));
				grid.setSlots(replacement);
				replacement.clear();
				check(layout.getSlotUnderMouse(5, 5).orElseThrow().slot() == standalone, "Replacement lists must be copied and standalone slots must support layout lookup");
				check(grid.getWidth() == 54 && grid.getSlotUnderMouse(19, 1).isEmpty(), "Shrinking contents must remove old slots and hide navigation");
				grid.setSlots(List.of());
				check(grid.getSlotUnderMouse(1, 1).isEmpty() && grid.getWidth() == 54, "An empty replacement must have no hovered slot or auto-hidden scrollbar");

				IRecipeSlotDrawable output = Objects.requireNonNull(category.output);
				grid.setSlots(List.of(output));
				check(layout.getSlotUnderMouse(85, 5).isEmpty(), "A newly claimed recipe slot must no longer be handled at its original layout position");
				check(layout.getSlotUnderMouse(5, 5).orElseThrow().slot() == output, "The grid must handle the newly claimed slot");

				grid.setScrollbarVisibility(ScrollbarVisibility.ENABLED);
				check(grid.getWidth() == 70, "Explicitly enabled navigation must show for contents that fit");
				grid.setScrollbarVisibility(ScrollbarVisibility.AUTO_HIDE);
				navigation.set(NavigationVisibility.ENABLED);
				check(grid.getWidth() == 54, "Explicit auto-hide must override the shared setting");
				grid.setSlots(category.slots);
				grid.setScrollbarVisibility(ScrollbarVisibility.DISABLED);
				check(grid.getWidth() == 54, "Explicitly disabled navigation must hide for overflowing contents");
				grid.setScrollbarVisibility(ScrollbarVisibility.DEFAULT);
				check(grid.getWidth() == 70, "Default navigation must resume following the shared setting");
				grid.setPosition(10, 20, 100, 50, HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM);
				check(grid.getArea().getX() == 40 && grid.getArea().getY() == 34, "The visible grid must align inside its placement area");
				navigation.set(NavigationVisibility.DISABLED);
				check(grid.getArea().getX() == 56, "Hiding navigation must preserve the grid's right alignment and hover bounds");
				check(layout.getSlotUnderMouse(57, 35).orElseThrow().slot() == category.slots.get(0), "Aligned grids must keep recipe slot lookup synchronized with their position");
				navigation.set(NavigationVisibility.ENABLED);
				check(grid.getArea().getX() == 40, "Restoring navigation must preserve the grid's alignment");
				grid.setPosition(4, 4);
				navigation.set(NavigationVisibility.DISABLED);
				check(grid.getArea().getX() == 4, "An absolute position must clear the previous alignment");
				navigation.set(NavigationVisibility.ENABLED);

				smoothScrolling.set(true);
				scrollRate.set(18);
				input.handleMouseScrolled(1, 1, -0.5);
				check(grid.getSlotUnderMouse(1, 0).orElseThrow().slot() == category.slots.get(0), "A partially visible top row must remain interactive");
				check(grid.getSlotUnderMouse(1, 35).orElseThrow().slot() == category.slots.get(6), "A partially visible bottom row must remain interactive");
				check(grid.getSlotUnderMouse(1, -1).isEmpty() && grid.getSlotUnderMouse(1, 36).isEmpty(), "Clipped rows must not be interactive outside the grid");

				var scrollbox = helper.createScrollBoxWidget(72, 36, 0, 0).setContents(new DrawableBlank(56, 120));
				navigation.set(NavigationVisibility.DISABLED);
				check(!scrollbox.handleInput(65, 1, press(65, 1)), "Scroll boxes must also inherit hidden navigation");
				scrollbox.setScrollbarVisibility(ScrollbarVisibility.ENABLED);
				((IInputTarget) scrollbox).beginInput(screen, press(65, 1), Internal.getKeyMappings()).orElseThrow();
				check(scrollbox.handleMouseDragged(65, 35, InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT), 0, 34), "A scroll box's override must enable scrollbar dragging");
				scrollbox.setScrollbarVisibility(ScrollbarVisibility.DISABLED);
				check(!scrollbox.handleMouseDragged(65, 35, InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT), 0, 34), "Hiding a scroll box's scrollbar must cancel its drag");
			} finally {
				client.setScreen(originalScreen);
				navigation.set(originalNavigation);
				smoothScrolling.set(originalSmoothScrolling);
				scrollRate.set(originalScrollRate);
			}
		});
		assertClippedRendering(context);
		assertPinnedRecipeScrolling(context);
	}

	private static void assertPinnedRecipeScrolling(FabricClientTestContext context) {
		var originalScreen = context.computeOnClient(client -> client.screen);
		var runtime = Internal.getJeiRuntime();
		var overlay = (BookmarkOverlay) runtime.getBookmarkOverlay();
		var bookmarks = overlay.getBookmarkList();
		var originalBookmarks = context.computeOnClient(client -> bookmarks.getElements().stream().flatMap(element -> element.getBookmark().stream()).toList());
		var config = Internal.getClientConfigs().getClientConfig();
		boolean originalSmoothScrolling = config.smoothScrollingEnabled().get();
		boolean originalPreview = config.bookmarkTooltipPreviewEnabled().get();
		try {
			var element = context.computeOnClient(client -> {
				config.smoothScrollingEnabled().set(false);
				config.bookmarkTooltipPreviewEnabled().set(true);
				client.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
				var category = new TestCategory(runtime.getJeiHelpers().getGuiHelper());
				var layout = runtime.getRecipeManager().createRecipeLayoutDrawable(category, "pinned", FocusGroup.EMPTY).orElseThrow();
				var bookmark = Objects.requireNonNull(RecipeBookmark.create(layout, runtime.getIngredientManager(), runtime.getRecipeManager(), runtime.getJeiHelpers().getGuiHelper(), new RecipeTransferService(runtime.getRecipeTransferManager())).orElseThrow());
				bookmarks.setFromConfigFile(List.of());
				bookmarks.add(bookmark);
				return (RecipeBookmarkElement<?, ?>) bookmark.getElement();
			});
			context.waitTicks(3);
			ImmutablePoint2i source = context.computeOnClient(client -> {
				for (int y = 0; y < client.getWindow().getGuiScaledHeight(); y++) {
					for (int x = 0; x < client.getWindow().getGuiScaledWidth(); x++) {
						if (overlay.getIngredientUnderMouse(x, y).findAny().isPresent()) {
							return new ImmutablePoint2i(x + 4, y + 4);
						}
					}
				}
				throw new AssertionError("The recipe bookmark must be visible");
			});
			context.getInput().holdKey(InputConstants.KEY_LSHIFT);
			double scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
			context.getInput().setCursorPos(source.x() * scale, source.y() * scale);
			context.waitTicks(3);
			var pinnedLayout = context.computeOnClient(client -> element.getInteractivePreview().orElseThrow().getRecipeLayout());
			ImmutablePoint2i point = context.computeOnClient(client -> new ImmutablePoint2i(pinnedLayout.getRect().getX() + 12, pinnedLayout.getRect().getY() + 12));
			context.getInput().setCursorPos(point.x() * scale, point.y() * scale);
			int original = pinnedSlotCount(context, pinnedLayout, point);
			context.getInput().scroll(-1, 0);
			context.waitTicks(2);
			int horizontal = pinnedSlotCount(context, pinnedLayout, point);
			context.getInput().scroll(1, 0);
			context.waitTicks(2);
			int restored = pinnedSlotCount(context, pinnedLayout, point);
			context.getInput().scroll(0, -1);
			context.waitTicks(2);
			int vertical = pinnedSlotCount(context, pinnedLayout, point);
			check(original == 1 && horizontal == 4 && restored == 1 && vertical == 4,
				"Pinned recipe widgets must receive both wheel axes: original=" + original + ", horizontal=" + horizontal + ", restored=" + restored + ", vertical=" + vertical);
		} finally {
			context.getInput().releaseKey(InputConstants.KEY_LSHIFT);
			context.runOnClient(client -> {
				bookmarks.setFromConfigFile(originalBookmarks);
				client.setScreen(originalScreen);
				config.smoothScrollingEnabled().set(originalSmoothScrolling);
				config.bookmarkTooltipPreviewEnabled().set(originalPreview);
			});
		}
	}

	private static int pinnedSlotCount(FabricClientTestContext context, IRecipeLayoutDrawable<?> layout, ImmutablePoint2i point) {
		return context.computeOnClient(client -> layout.getSlotUnderMouse(point.x(), point.y()).orElseThrow().slot()
			.getDisplayedIngredient(VanillaTypes.ITEM_STACK).orElseThrow().getCount());
	}

	private static void assertClippedRendering(FabricClientTestContext context) {
		var originalScreen = context.computeOnClient(client -> client.screen);
		var smoothScrolling = Internal.getClientConfigs().getClientConfig().smoothScrollingEnabled();
		var scrollRate = Internal.getClientConfigs().getClientConfig().smoothScrollRate();
		boolean originalSmoothScrolling = smoothScrolling.get();
		int originalScrollRate = scrollRate.get();
		try {
			TestCategory category = context.computeOnClient(client -> {
				smoothScrolling.set(true);
				scrollRate.set(18);
				var runtime = Internal.getJeiRuntime();
				var testCategory = new TestCategory(runtime.getJeiHelpers().getGuiHelper());
				var layout = runtime.getRecipeManager().createRecipeLayoutDrawable(testCategory, "render", FocusGroup.EMPTY).orElseThrow();
				layout.setPosition(40, 50);
				client.setScreen(new GridScreen(layout));
				return testCategory;
			});
			context.waitTicks(3);
			assertGridPixels(context, false);
			context.runOnClient(client -> ((IJeiInputHandler) Objects.requireNonNull(category.grid)).handleMouseScrolled(1, 1, -0.5));
			context.waitTicks(3);
			context.takeScreenshot("jei-scroll-grid-partial-row");
			assertGridPixels(context, true);
		} finally {
			context.runOnClient(client -> {
				client.setScreen(originalScreen);
				smoothScrolling.set(originalSmoothScrolling);
				scrollRate.set(originalScrollRate);
			});
		}
	}

	private static void assertGridPixels(FabricClientTestContext context, boolean clipped) {
		context.runOnClient(client -> {
			try (NativeImage image = Screenshot.takeScreenshot(client.getMainRenderTarget())) {
				double scale = client.getWindow().getGuiScale();
				int visiblePixels = 0;
				for (int y = 63; y < 81; y++) {
					for (int x = 45; x < 97; x++) {
						if ((image.getPixelRGBA((int) (x * scale), (int) (y * scale)) & 0xFFFFFF) != 0) {
							visiblePixels++;
						}
					}
				}
				check(visiblePixels > 100, "Grid items must remain visible at their translated position, clipped=" + clipped + ", visible pixels=" + visiblePixels);
				if (clipped) {
					for (int y : new int[]{52, 91}) {
						for (int x = 45; x < 97; x++) {
							check((image.getPixelRGBA((int) (x * scale), (int) (y * scale)) & 0xFFFFFF) == 0,
								"Partially visible rows must be clipped outside the grid at " + x + "," + y);
						}
					}
				}
			}
		});
	}

	private static final class GridScreen extends Screen {
		private final IRecipeLayoutDrawable<?> layout;

		private GridScreen(IRecipeLayoutDrawable<?> layout) {
			super(Component.literal("Grid rendering regression"));
			this.layout = layout;
		}

		@Override
		public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
			net.minecraft.client.gui.GuiComponent.fill(poseStack, 0, 0, width, height, 0xFF000000);
			layout.drawRecipe(poseStack, -1, -1);
		}
	}

	private static UserInput press(double x, double y) {
		return UserInput.fromVanilla(x, y, InputConstants.MOUSE_BUTTON_LEFT, InputPhase.PRESS).orElseThrow();
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static final class TestCategory extends AbstractRecipeCategory<String> {
		private @Nullable IScrollGridWidget grid;
		private List<IRecipeSlotDrawable> slots = List.of();
		private @Nullable IRecipeSlotDrawable output;

		private TestCategory(IGuiHelper helper) {
			super(RecipeType.create("jei", "scroll_grid_test", String.class), Component.literal("Scroll grid test"), helper.getSlotDrawable(), 160, 80);
		}

		@Override
		public ResourceLocation getRegistryName(String recipe) {
			return new ResourceLocation("jei", "scroll_grid_test/" + recipe);
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, String recipe, IFocusGroup focuses) {
			for (int i = 1; i <= 12; i++) {
				builder.addInputSlot().addItemStack(new ItemStack(Items.STONE, i));
			}
			builder.addOutputSlot(84, 4).addItemStack(new ItemStack(Items.DIAMOND));
		}

		@Override
		public void draw(String recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
			net.minecraft.client.gui.GuiComponent.fill(poseStack, 0, 0, getWidth(), getHeight(), 0xFF000000);
		}

		@Override
		public void createRecipeExtras(IRecipeExtrasBuilder builder, String recipe, IFocusGroup focuses) {
			slots = builder.getRecipeSlots().getSlots(RecipeIngredientRole.INPUT);
			output = builder.getRecipeSlots().getSlots(RecipeIngredientRole.OUTPUT).get(0);
			grid = builder.addScrollGridWidget(slots, 3, 2).setPosition(4, 4);
		}
	}
}
