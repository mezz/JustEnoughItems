package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
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
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.common.Internal;
import mezz.jei.common.config.NavigationVisibility;
import mezz.jei.common.gui.elements.DrawableBlank;
import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.InputPhase;
import mezz.jei.common.input.UserInput;
import mezz.jei.library.focus.FocusGroup;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Checks changing grid contents and navigation preferences against real recipe layouts and slots. */
@SuppressWarnings("UnstableApiUsage")
final class RecipeScrollGridClientTest {
	private RecipeScrollGridClientTest() {}

	static void run(ClientGameTestContext context) {
		context.runOnClient(client -> {
			var originalScreen = client.gui.screen();
			var runtime = Internal.getJeiRuntime();
			var navigation = Internal.getClientConfigs().getIngredientListConfig().navigationVisibility();
			var smoothScrolling = Internal.getClientConfigs().getClientConfig().smoothScrollingEnabled();
			var scrollRate = Internal.getClientConfigs().getClientConfig().smoothScrollRate();
			var originalNavigation = navigation.get();
			boolean originalSmoothScrolling = smoothScrolling.get();
			int originalScrollRate = scrollRate.get();
			try {
				var screen = new InventoryScreen(Objects.requireNonNull(client.player));
				client.gui.setScreen(screen);
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
				check(initialSlot.slot() == category.slots.getFirst(), "The layout must expose the grid's first slot");

				navigation.set(NavigationVisibility.DISABLED);
				check(grid.getWidth() == 54 && grid.getScreenRectangle().width() == 54, "Disabling navigation must remove the scrollbar gutter from the grid bounds");
				check(!input.handleInput(62, 1, press(62, 1)), "A hidden scrollbar must not accept a press");
				check(target.beginInput(screen, press(62, 1), Internal.getKeyMappings()).isEmpty(), "A hidden scrollbar must not capture mouse input");
				input.handleMouseScrolled(1, 1, 0, -100);
				check(grid.getSlotUnderMouse(1, 1).orElseThrow().slot() == category.slots.get(6), "Hidden navigation must still allow scrolling to the last page");

				navigation.set(NavigationVisibility.ENABLED);
				var drag = target.beginInput(screen, press(62, 1), Internal.getKeyMappings()).orElseThrow();
				drag.drag(62, 35, InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT), 0, 34);
				int firstSlotIndex = category.slots.indexOf(grid.getSlotUnderMouse(1, 1).orElseThrow().slot());
				check(firstSlotIndex == 6, "Dragging the visible scrollbar must reach the last page: first slot=" + firstSlotIndex);
				grid.setSlots(category.slots);
				drag.drag(62, 35, InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT), 0, 34);
				check(!input.handleMouseDragged(62, 35, InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT), 0, 34), "Replacing overflowing contents must cancel an active scrollbar drag");
				check(grid.getSlotUnderMouse(1, 1).orElseThrow().slot() == category.slots.getFirst(), "Replacing contents must reset to the first row");

				navigation.set(NavigationVisibility.AUTO_HIDE);
				grid.setSlots(category.slots.subList(0, 6));
				check(grid.getWidth() == 54, "Auto-hide must hide navigation when the last row fits exactly");
				grid.setSlots(category.slots.subList(0, 7));
				check(grid.getWidth() == 70, "Adding an overflowing row must restore auto-hidden navigation");
				input.handleMouseScrolled(1, 1, 0, -100);
				check(grid.getSlotUnderMouse(1, 1).orElseThrow().slot() == category.slots.get(3), "Scroll limits must use the replacement slot count");

				IRecipeSlotDrawable standalone = helper.createRecipeSlotDrawable(RecipeIngredientRole.INPUT,
					acceptor -> acceptor.add(new ItemStack(Items.COAL, 32)), Set.of(), 0);
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
				check(grid.getPosition().x() == 40 && grid.getPosition().y() == 34, "The visible grid must align inside its placement area");
				navigation.set(NavigationVisibility.DISABLED);
				check(grid.getPosition().x() == 56 && grid.getScreenRectangle().left() == 56, "Hiding navigation must preserve the grid's right alignment and hover bounds");
				check(layout.getSlotUnderMouse(57, 35).orElseThrow().slot() == category.slots.getFirst(), "Aligned grids must keep recipe slot lookup synchronized with their position");
				navigation.set(NavigationVisibility.ENABLED);
				check(grid.getPosition().x() == 40, "Restoring navigation must preserve the grid's alignment");
				grid.setPosition(4, 4);
				navigation.set(NavigationVisibility.DISABLED);
				check(grid.getPosition().x() == 4, "An absolute position must clear the previous alignment");
				navigation.set(NavigationVisibility.ENABLED);

				smoothScrolling.set(true);
				scrollRate.set(18);
				input.handleMouseScrolled(1, 1, 0, -0.5);
				check(grid.getSlotUnderMouse(1, 0).orElseThrow().slot() == category.slots.getFirst(), "A partially visible top row must remain interactive");
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
				client.gui.setScreen(originalScreen);
				navigation.set(originalNavigation);
				smoothScrolling.set(originalSmoothScrolling);
				scrollRate.set(originalScrollRate);
			}
		});
	}

	private static UserInput press(double x, double y) {
		MouseButtonEvent event = new MouseButtonEvent(x, y, new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0));
		return UserInput.fromVanilla(event, false, InputPhase.PRESS).orElseThrow();
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
			super(IRecipeType.create("jei", "scroll_grid_test", String.class), Component.literal("Scroll grid test"), helper.getSlotDrawable(), 160, 80);
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, String recipe, IFocusGroup focuses) {
			for (int i = 1; i <= 12; i++) {
				builder.addInputSlot().add(new ItemStack(Items.STONE, i));
			}
			builder.addOutputSlot(84, 4).add(new ItemStack(Items.DIAMOND));
		}

		@Override
		public void createRecipeExtras(IRecipeExtrasBuilder builder, String recipe, IFocusGroup focuses) {
			slots = builder.getRecipeSlots().getSlots(RecipeIngredientRole.INPUT);
			output = builder.getRecipeSlots().getSlots(RecipeIngredientRole.OUTPUT).getFirst();
			grid = builder.addScrollGridWidget(slots, 3, 2).setPosition(4, 4);
		}
	}
}
