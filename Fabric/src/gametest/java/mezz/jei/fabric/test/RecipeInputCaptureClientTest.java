package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IScrollBoxWidget;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.config.RecipeGuiNavigationMode;
import mezz.jei.common.gui.elements.DrawableBlank;
import mezz.jei.common.gui.elements.Scrollbar;
import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.gui.recipes.IRecipeLayoutWithButtons;
import mezz.jei.gui.recipes.RecipeGuiLayouts;
import mezz.jei.gui.recipes.RecipesGui;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.mezzdev.config.api.value.IConfigValue;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Exercises mod controls and JEI scrollbars through Minecraft's mouse callbacks. */
@SuppressWarnings("UnstableApiUsage")
final class RecipeInputCaptureClientTest {
	static void run(ClientGameTestContext context) {
		List<Runnable> cleanup = new ArrayList<>();
		try {
			TestCategory<?> category = context.computeOnClient(client -> {
				var config = Internal.getClientConfigs().getClientConfig();
				set(cleanup, config.recipeGuiNavigationMode(), RecipeGuiNavigationMode.PAGED);
				set(cleanup, config.recipeGuiWidth(), 240);
				set(cleanup, config.maxRecipeGuiHeight(), 320);
				set(cleanup, config.maxRecipeGuiColumns(), 1);
				set(cleanup, config.recipeSortingCraftableEnabled(), false);
				set(cleanup, config.recipeSortingBookmarksEnabled(), false);
				client.gui.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
				var manager = Internal.getJeiRuntime().getRecipeManager();
				var testCategory = new TestCategory<>(manager.getRecipeCategory(RecipeTypes.SMELTING));
				var recipes = manager.createRecipeLookup(RecipeTypes.SMELTING).get().limit(2).toList();
				check(recipes.size() == 2, "Two recipes are required for the drag test");
				gui().showRecipes(testCategory, recipes, List.of());
				return testCategory;
			});
			context.waitTicks(3);
			List<IRecipeLayoutDrawable<?>> layouts = context.computeOnClient(client -> visibleLayouts());
			check(layouts.size() == 2, "Both test recipes must be visible");
			IRecipeLayoutDrawable<?> first = layouts.getFirst();
			IRecipeLayoutDrawable<?> second = layouts.getLast();
			Controls a = context.computeOnClient(client -> Objects.requireNonNull(category.controls.get(first.getRecipe())));
			Controls b = context.computeOnClient(client -> Objects.requireNonNull(category.controls.get(second.getRecipe())));

			// Public drag callbacks work even when every control rejects the press.
			context.runOnClient(client -> a.first.acceptPress = false);
			move(context, first, 12, 12);
			context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
			move(context, first, 16, 12);
			context.runOnClient(client -> {
				check(a.first.drags > 0 && a.first.lastX == 8, "A drag-only API handler must receive movement in local coordinates");
				check(a.fallback.drags > 0, "Returning false must allow the next overlapping API handler to receive movement");
			});
			int fallbackDrags = context.computeOnClient(client -> a.fallback.drags);
			context.runOnClient(client -> a.first.consumeDrag = true);
			move(context, first, 20, 12);
			context.runOnClient(client -> check(a.fallback.drags == fallbackDrags, "Returning true must stop delivery to later API handlers"));
			int firstDrags = context.computeOnClient(client -> a.first.drags);
			move(context, first, 44, 12);
			context.runOnClient(client -> {
				check(a.first.drags == firstDrags, "API handlers must not receive movement outside their area");
				check(a.second.drags > 0 && a.second.lastX == 4, "Movement must reach the API control currently under the pointer");
			});
			move(context, second, 12, 12);
			context.runOnClient(client -> check(b.first.drags > 0 && b.fallback.drags > 0, "Public drag callbacks must also work over another recipe"));
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.runOnClient(client -> check(a.first.actions == 0 && b.first.actions == 0, "A rejected press must not execute a click on release"));

			context.runOnClient(client -> a.first.acceptPress = true);
			move(context, first, 12, 12);
			context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
			move(context, first, 44, 12);
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.runOnClient(client -> check(a.first.actions == 0 && a.second.actions == 1, "An accepted API click must check the controls under the release position in the same recipe"));
			move(context, first, 12, 12);
			context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
			move(context, second, 12, 12);
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.runOnClient(client -> check(a.first.actions == 0 && b.first.actions == 0, "Releasing outside the recipe that accepted the press must not execute a click"));
			move(context, first, 12, 12);
			context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.runOnClient(client -> check(a.first.actions == 1, "A normal API click must execute once"));

			move(context, first, 76, 12);
			context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
			move(context, first, 80, 12);
			context.runOnClient(client -> check(a.listener.drags > 0, "The public listener must receive movement within its area"));
			int listenerDrags = context.computeOnClient(client -> a.listener.drags);
			move(context, second, 76, 12);
			context.runOnClient(client -> check(a.listener.drags == listenerDrags && b.listener.drags > 0, "Public listener movement must follow the pointer's bounds"));
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.runOnClient(client -> check(a.listener.releases == 0 && b.listener.releases == 0, "Releasing outside the original recipe must not synthesize an API release"));
			move(context, first, 76, 12);
			context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.runOnClient(client -> check(a.listener.releases == 1, "Releasing inside the listener must call it exactly once"));

			move(context, first, 123, 12);
			context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.runOnClient(client -> check(scrollbar(a.scrollbox).isDragging(), "The recipe scrollbar must accept the press"));
			move(context, second, 123, 44);
			context.runOnClient(client -> check(scrollbar(a.scrollbox).isDragging() && !scrollbar(b.scrollbox).isDragging(), "Only the original recipe scrollbar must be dragging"));
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.runOnClient(client -> check(!scrollbar(a.scrollbox).isDragging(), "Releasing outside the recipe must stop its scrollbar"));

			move(context, first, 123, 12);
			context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.runOnClient(client -> {
				check(scrollbar(a.scrollbox).isDragging(), "The scrollbar must accept a new press after release");
				client.gui.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
			});
			context.waitTick();
			context.runOnClient(client -> check(!scrollbar(a.scrollbox).isDragging(), "Changing screens must stop the recipe scrollbar"));
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.runOnClient(client -> client.gui.setScreen(gui()));
			context.waitTicks(3);
			IRecipeLayoutDrawable<?> reopened = context.computeOnClient(client -> visibleLayouts().getFirst());
			Controls reopenedControls = context.computeOnClient(client -> Objects.requireNonNull(category.controls.get(reopened.getRecipe())));
			move(context, reopened, 76, 12);
			context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
			int releasesBeforeClose = context.computeOnClient(client -> reopenedControls.listener.releases);
			context.runOnClient(client -> {
				check(reopenedControls.listener.pressed, "The listener must accept the press");
				client.gui.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
			});
			context.waitTick();
			context.runOnClient(client -> check(reopenedControls.listener.releases == releasesBeforeClose, "Changing screens must not synthesize an API release"));
		} finally {
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.runOnClient(client -> {
				client.gui.setScreen(null);
				cleanup.reversed().forEach(Runnable::run);
			});
		}
	}

	private static RecipesGui gui() {
		return (RecipesGui) Internal.getJeiRuntime().getRecipesGui();
	}

	private static List<IRecipeLayoutDrawable<?>> visibleLayouts() {
		RecipeGuiLayouts layouts = new ReflectionUtil().getFieldWithClass(gui(), RecipeGuiLayouts.class).findFirst().orElseThrow();
		List<?> entries = new ReflectionUtil().getFieldWithClass(layouts, List.class).findFirst().orElseThrow();
		List<IRecipeLayoutDrawable<?>> result = new ArrayList<>();
		for (Object entry : entries) {
			result.add(((IRecipeLayoutWithButtons<?>) entry).getRecipeLayout());
		}
		return result;
	}

	private static Scrollbar scrollbar(IScrollBoxWidget widget) {
		return new ReflectionUtil().getFieldWithClass(widget, Scrollbar.class).findFirst().orElseThrow();
	}

	private static void move(ClientGameTestContext context, IRecipeLayoutDrawable<?> layout, int x, int y) {
		double scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
		var area = context.computeOnClient(client -> layout.getRect());
		context.getInput().setCursorPos((area.getX() + x) * scale, (area.getY() + y) * scale);
		context.waitTick();
	}

	private static <T> void set(List<Runnable> cleanup, IConfigValue<T> config, T value) {
		T original = config.get();
		cleanup.add(() -> config.set(original));
		config.set(value);
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static void checkCoordinates(double mouseX, double mouseY) {
		check(mouseX >= 0 && mouseX < 24 && mouseY >= 0 && mouseY < 20, "Public callbacks must only receive coordinates inside their own area: " + mouseX + "," + mouseY);
	}

	private static final class TestCategory<T> extends AbstractRecipeCategory<T> {
		private final Map<T, Controls> controls = new IdentityHashMap<>();

		private TestCategory(IRecipeCategory<T> delegate) {
			super(delegate.getRecipeType(), delegate.getTitle(), new DrawableBlank(16, 16), 160, 60);
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
		}

		@Override
		public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
			RecordingHandler first = new RecordingHandler(8);
			RecordingHandler second = new RecordingHandler(40);
			RecordingListener listener = new RecordingListener(72, true);
			RecordingListener fallback = new RecordingListener(8, false);
			builder.addInputHandler(first);
			builder.addInputHandler(second);
			builder.addGuiEventListener(listener);
			builder.addGuiEventListener(fallback);
			IScrollBoxWidget scrollbox = builder.addScrollBoxWidget(20, 40, 110, 8);
			scrollbox.setContents(new DrawableBlank(scrollbox.getContentAreaWidth(), 200));
			controls.put(recipe, new Controls(first, second, listener, fallback, scrollbox));
		}
	}

	/** Two separate mod controls, a listener, an overlapping fallback listener, and a JEI scrollbar in one recipe. */
	private record Controls(RecordingHandler first, RecordingHandler second, RecordingListener listener, RecordingListener fallback, IScrollBoxWidget scrollbox) {
	}

	private static final class RecordingHandler implements IJeiInputHandler {
		private final int x;
		private boolean acceptPress = true;
		private boolean consumeDrag;
		private int actions;
		private int drags;
		private double lastX;

		private RecordingHandler(int x) {
			this.x = x;
		}

		@Override
		public ScreenRectangle getArea() {
			return new ScreenRectangle(x, 8, 24, 20);
		}

		@Override
		public boolean handleInput(double mouseX, double mouseY, IJeiUserInput input) {
			checkCoordinates(mouseX, mouseY);
			if (input.isSimulate()) {
				return acceptPress;
			}
			actions++;
			return true;
		}

		@Override
		public boolean handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
			checkCoordinates(mouseX, mouseY);
			drags++;
			lastX = mouseX;
			return consumeDrag;
		}
	}

	private static final class RecordingListener implements IJeiGuiEventListener {
		private final int x;
		private final boolean acceptPress;
		private boolean pressed;
		private int drags;
		private int releases;

		private RecordingListener(int x, boolean acceptPress) {
			this.x = x;
			this.acceptPress = acceptPress;
		}

		@Override
		public ScreenRectangle getArea() {
			return new ScreenRectangle(x, 8, 24, 20);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			checkCoordinates(mouseX, mouseY);
			pressed = acceptPress;
			return acceptPress;
		}

		@Override
		public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
			checkCoordinates(mouseX, mouseY);
			drags++;
			return true;
		}

		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			checkCoordinates(mouseX, mouseY);
			pressed = false;
			releases++;
			return true;
		}
	}
}
