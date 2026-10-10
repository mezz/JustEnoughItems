package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.widgets.IButtonWidget;
import mezz.jei.api.gui.widgets.ICycleButtonWidget;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.elements.DrawableBlank;
import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.fabric.test.mixin.MouseHandlerAccessor;
import mezz.jei.gui.recipes.IRecipeLayoutWithButtons;
import mezz.jei.gui.recipes.RecipeGuiLayouts;
import mezz.jei.gui.recipes.RecipesGui;
import net.mezzdev.config.api.value.IConfigValue;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.time.Duration;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Exercises recipe buttons through Minecraft's mouse callbacks. */
@SuppressWarnings("UnstableApiUsage")
final class RecipeButtonsClientTest {
	public static FabricClientTestRunner.ClientTestCase getTestCase() {
		return new FabricClientTestRunner.ClientTestCase(
			"fabric-client-gametest", RecipeButtonsClientTest.class.getSimpleName(), RecipeButtonsClientTest::runTest);
	}

	private static void runTest() {
		List<Runnable> cleanup = new ArrayList<>();
		try (FabricClientTestWorld ignored = FabricClientTestWorld.create()) {
			ClientTestUtil.waitUntil(
				() -> ClientTestUtil.computeOnClient(client -> Internal.getOptionalJeiRuntime().isPresent()),
				Duration.ofSeconds(60), () -> "Timed out waiting for JEI before testing recipe buttons");
			try {
				TestCategory<?> category = ClientTestUtil.computeOnClient(client -> {
					var config = Internal.getClientConfigs().getClientConfig();
					set(cleanup, config.maxRecipeGuiHeight(), 320);
					set(cleanup, config.recipeSortingCraftableEnabled(), false);
					set(cleanup, config.recipeSortingBookmarksEnabled(), false);
					client.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
					var manager = Internal.getJeiRuntime().getRecipeManager();
					var testCategory = new TestCategory<>(manager.getRecipeCategory(RecipeTypes.SMELTING));
					var recipes = manager.createRecipeLookup(RecipeTypes.SMELTING).get().limit(2).toList();
					check(recipes.size() == 2, "Two recipes are required for the drag test");
					gui().showRecipes(testCategory, recipes, List.of());
					return testCategory;
				});
				waitTicks(3);
				assertRecipeButtons(category);
			} finally {
				releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
				FabricClientTestInput.releaseKey(InputConstants.KEY_LSHIFT);
				ClientTestUtil.runOnClient(client -> {
					client.setScreen(null);
					for (int i = cleanup.size() - 1; i >= 0; i--) {
						cleanup.get(i).run();
					}
				});
			}
		}
	}

	private static void assertRecipeButtons(TestCategory<?> category) {
		List<IRecipeLayoutDrawable<?>> layouts = ClientTestUtil.computeOnClient(client -> visibleLayouts());
		IRecipeLayoutDrawable<?> first = layouts.get(0);
		IRecipeLayoutDrawable<?> second = layouts.get(layouts.size() - 1);
		ButtonControls a = ClientTestUtil.computeOnClient(client -> Objects.requireNonNull(category.controls.get(first.getRecipe())));
		ButtonControls b = ClientTestUtil.computeOnClient(client -> Objects.requireNonNull(category.controls.get(second.getRecipe())));

		move(first, 12, 40);
		holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		ClientTestUtil.runOnClient(client -> check(a.actions == 0, "Recipe buttons must wait for release before running their action"));
		takeScreenshot("jei-recipe-button-pressed");
		move(first, 44, 40);
		releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		ClientTestUtil.runOnClient(client -> check(a.actions == 0 && a.cycle.getValue() == -1,
			"Releasing over a different button must not activate either button"));
		click(first, 12, 40);
		ClientTestUtil.runOnClient(client -> check(a.actions == 1, "A complete recipe button click must run exactly once"));

		move(first, 12, 40);
		holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		move(second, 12, 40);
		releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		ClientTestUtil.runOnClient(client -> check(a.actions == 1 && b.actions == 0, "A captured recipe button click must not move to another recipe"));

		ClientTestUtil.runOnClient(client -> a.button.setActive(false));
		move(first, 12, 40);
		holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		move(first, 76, 40);
		releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		ClientTestUtil.runOnClient(client -> check(a.iconActions == 0,
			"A rejected button press must not activate the button under the release position"));
		click(first, 12, 40);
		ClientTestUtil.runOnClient(client -> check(a.actions == 1, "Disabled recipe buttons must reject clicks"));
		takeScreenshot("jei-recipe-button-disabled");
		ClientTestUtil.runOnClient(client -> a.button.setActive(true));
		move(first, 12, 40);
		holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		ClientTestUtil.runOnClient(client -> {
			a.button.setActive(false);
			a.button.setActive(true);
		});
		releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		ClientTestUtil.runOnClient(client -> check(a.actions == 1, "Deactivating and reactivating a held button must cancel its click"));

		holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		ClientTestUtil.runOnClient(client -> {
			a.button.setVisible(false);
			JeiTooltip tooltip = new JeiTooltip();
			((IRecipeWidget) a.button).getTooltip(tooltip, 4, 4);
			check(tooltip.isEmpty(), "Hidden buttons must not contribute tooltips");
			a.button.setVisible(true);
		});
		releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		ClientTestUtil.runOnClient(client -> check(a.actions == 1, "Hiding and showing a held button must cancel its click"));

		assertIconButtons(first, second, a, b);
		assertIconCycleButton(first, a);

		for (int expected : List.of(0, 1, -1)) {
			move(first, 44, 40);
			holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
			int changes = ClientTestUtil.computeOnClient(client -> a.selected.size());
			releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
			ClientTestUtil.runOnClient(client -> {
				check(a.cycle.getValue() == expected, "Cycle buttons must use a copy of their values and wrap to the first value");
				check(a.selected.size() == changes + 1 && a.selected.get(a.selected.size() - 1) == expected, "Cycling must notify exactly once, after selecting the new value");
			});
		}
		FabricClientTestInput.holdKey(InputConstants.KEY_LSHIFT);
		waitTicks(1);
		move(first, 44, 40);
		ClientTestUtil.runOnClient(client -> check(Screen.hasShiftDown(), "Shift must be held for backward cycling"));
		click(first, 44, 40);
		ClientTestUtil.runOnClient(client -> check(a.cycle.getValue() == 1, "Shift + click must cycle backward and wrap to the last value"));
		FabricClientTestInput.releaseKey(InputConstants.KEY_LSHIFT);
		int changesBeforeKeys = ClientTestUtil.computeOnClient(client -> a.selected.size());
		int iconChangesBeforeKeys = ClientTestUtil.computeOnClient(client -> a.iconSelected.size());
		for (int x : List.of(12, 44, 76, 140)) {
			move(first, x, 40);
			for (int key : List.of(InputConstants.KEY_RETURN, InputConstants.KEY_NUMPADENTER, InputConstants.KEY_SPACE)) {
				FabricClientTestInput.pressKey(key);
				waitTicks(1);
				ClientTestUtil.runOnClient(client -> {
					check(a.actions == 1 && b.actions == 0, "Enter and Space must not activate recipe buttons while hovered");
					check(a.iconActions == 1 && b.iconActions == 0, "Enter and Space must not activate hovered icon buttons");
					check(a.cycle.getValue() == 1 && a.selected.size() == changesBeforeKeys,
						"Enter and Space must not change hovered cycle buttons or notify their callback");
					check(a.iconCycle.getValue() == 0 && a.iconSelected.size() == iconChangesBeforeKeys,
						"Enter and Space must not change hovered icon cycle buttons");
				});
			}
		}
		ClientTestUtil.runOnClient(client -> {
			int changes = a.selected.size();
			a.cycle.setValue(0);
			check(a.selected.size() == changes, "Programmatic selection must not call the user-input callback");
			a.button.setMessage(Component.literal("Long action label"));
			JeiTooltip tooltip = new JeiTooltip();
			((IRecipeWidget) a.button).getTooltip(tooltip, 4, 4);
			check(tooltip.getLines().size() == 2, "A truncated button label must precede its configured tooltip");
		});
		move(first, 12, 40);
		takeScreenshot("jei-recipe-button-tooltip");
		holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		ClientTestUtil.runOnClient(client -> client.setScreen(new InventoryScreen(Objects.requireNonNull(client.player))));
		waitTicks(1);
		releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		ClientTestUtil.runOnClient(client -> check(a.actions == 1, "Changing screens must cancel a recipe button click"));
	}

	private static void assertIconButtons(IRecipeLayoutDrawable<?> first,
		IRecipeLayoutDrawable<?> second, ButtonControls a, ButtonControls b) {
		move(first, 76, 40);
		holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		ClientTestUtil.runOnClient(client -> check(a.iconActions == 0, "Icon buttons must wait for release before running their action"));
		takeScreenshot("jei-recipe-icon-button-pressed");
		move(first, 12, 40);
		releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		ClientTestUtil.runOnClient(client -> check(a.iconActions == 0 && a.actions == 1,
			"Releasing an icon button press over a text button must not activate either button"));
		click(first, 76, 40);
		ClientTestUtil.runOnClient(client -> check(a.iconActions == 1, "A complete icon button click must run exactly once"));

		holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		move(second, 76, 40);
		releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		ClientTestUtil.runOnClient(client -> check(a.iconActions == 1 && b.iconActions == 0,
			"A captured icon button click must not move to another recipe"));
		ClientTestUtil.runOnClient(client -> a.iconButton.setActive(false));
		click(first, 76, 40);
		ClientTestUtil.runOnClient(client -> {
			check(a.iconActions == 1, "Disabled icon buttons must reject clicks");
			JeiTooltip tooltip = new JeiTooltip();
			((IRecipeWidget) a.iconButton).getTooltip(tooltip, 4, 4);
			check(tooltip.getLines().size() == 1, "Icon buttons must show their configured tooltip while disabled");
			a.iconButton.setActive(true);
		});

		holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		ClientTestUtil.runOnClient(client -> {
			a.iconButton.setVisible(false);
			JeiTooltip tooltip = new JeiTooltip();
			((IRecipeWidget) a.iconButton).getTooltip(tooltip, 4, 4);
			check(tooltip.isEmpty(), "Hidden icon buttons must not contribute tooltips");
			a.iconButton.setVisible(true);
		});
		releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		ClientTestUtil.runOnClient(client -> {
			check(a.iconActions == 1, "Hiding and showing a held icon button must cancel its click");
			var guiHelper = Internal.getJeiRuntime().getJeiHelpers().getGuiHelper();
			a.iconButton.setIcon(guiHelper.createDrawableItemStack(new ItemStack(Items.GOLDEN_APPLE)));
		});
		takeScreenshot("jei-recipe-icon-button-tooltip");
	}

	private static void assertIconCycleButton(IRecipeLayoutDrawable<?> layout, ButtonControls controls) {
		move(layout, 140, 40);
		holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		ClientTestUtil.runOnClient(client -> check(controls.iconCycle.getValue() == -1 && controls.iconSelected.isEmpty(),
			"Icon cycle buttons must wait for release before changing their selected value"));
		releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		ClientTestUtil.runOnClient(client -> check(controls.iconCycle.getValue() == 0 && controls.iconSelected.equals(List.of(0)),
			"An icon cycle button click must select the next value and notify once"));
		click(layout, 140, 40);
		ClientTestUtil.runOnClient(client -> check(controls.iconCycle.getValue() == 1 && controls.iconSelected.equals(List.of(0, 1)),
			"Icon cycle buttons must cycle through the copied values"));
		ClientTestUtil.runOnClient(client -> {
			JeiTooltip tooltip = new JeiTooltip();
			((IRecipeWidget) controls.iconCycle).getTooltip(tooltip, 4, 4);
			check(tooltip.getLines().size() == 1, "Choosing icon contents must replace the cycle button's earlier text formatter");
		});
		waitTicks(1);
		takeScreenshot("jei-recipe-icon-cycle-button");
		ClientTestUtil.runOnClient(client -> controls.iconCycle.setValue(0));
		ClientTestUtil.runOnClient(client -> check(controls.iconSelected.equals(List.of(0, 1)),
			"Programmatic icon selection must update its contents without notifying the callback"));
		waitTicks(1);
		takeScreenshot("jei-recipe-icon-cycle-button-programmatic");
	}

	private static void click(IRecipeLayoutDrawable<?> layout, int x, int y) {
		move(layout, x, y);
		holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		waitTicks(1);
	}

	private static void holdMouse(int button) {
		mouseButton(button, GLFW.GLFW_PRESS);
	}

	private static void releaseMouse(int button) {
		mouseButton(button, GLFW.GLFW_RELEASE);
	}

	private static void mouseButton(int button, int action) {
		ClientTestUtil.runOnClient(client -> {
			var mouse = (MouseHandlerAccessor) client.mouseHandler;
			int modifiers = 0;
			if (Screen.hasShiftDown()) {
				modifiers = GLFW.GLFW_MOD_SHIFT;
			}
			mouse.jei$invokeOnPress(client.getWindow().getWindow(), button, action, modifiers);
		});
	}

	private static void move(IRecipeLayoutDrawable<?> layout, int x, int y) {
		ClientTestUtil.runOnClient(client -> {
			double scale = client.getWindow().getGuiScale();
			var area = layout.getRect();
			((MouseHandlerAccessor) client.mouseHandler).jei$invokeOnMove(client.getWindow().getWindow(),
				(area.getX() + x) * scale, (area.getY() + y) * scale);
		});
		waitTicks(1);
	}

	private static void waitTicks(int ticks) {
		int start = ClientTestUtil.computeOnClient(client -> Objects.requireNonNull(client.player).tickCount);
		ClientTestUtil.waitUntil(
			() -> ClientTestUtil.computeOnClient(client -> Objects.requireNonNull(client.player).tickCount >= start + ticks),
			Duration.ofSeconds(10), () -> "Timed out waiting for Minecraft client ticks");
	}

	private static void takeScreenshot(String name) {
		waitTicks(1);
		ClientTestUtil.runOnClient(client -> Screenshot.grab(client.gameDirectory, name + ".png", client.getMainRenderTarget(), message -> {}));
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
		check(result.size() == 2, "Both button test recipes must be visible");
		return result;
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

	private static final class TestCategory<T> extends AbstractRecipeCategory<T> {
		private final Map<T, ButtonControls> controls = new IdentityHashMap<>();

		private TestCategory(IRecipeCategory<T> delegate) {
			super(delegate.getRecipeType(), delegate.getTitle(), new DrawableBlank(16, 16), 160, 60);
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
		}

		@Override
		public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
			controls.put(recipe, new ButtonControls(builder));
		}
	}

	private static final class ButtonControls {
		private final IButtonWidget<?> button;
		private final IButtonWidget<?> iconButton;
		private final ICycleButtonWidget<Integer> cycle;
		private final ICycleButtonWidget<Integer> iconCycle;
		private final List<Integer> selected = new ArrayList<>();
		private final List<Integer> iconSelected = new ArrayList<>();
		private int actions;
		private int iconActions;

		private ButtonControls(IRecipeExtrasBuilder builder) {
			button = builder.addButton(24, 20, () -> actions++)
				.setMessage(Component.literal("Run"))
				.setPosition(8, 36).setTooltip(Component.literal("Action help"));
			var guiHelper = Internal.getJeiRuntime().getJeiHelpers().getGuiHelper();
			var icons = Map.of(
				-1, guiHelper.createDrawableItemStack(new ItemStack(Items.APPLE)),
				0, guiHelper.createDrawableItemStack(new ItemStack(Items.GOLDEN_APPLE)),
				1, guiHelper.createDrawableItemStack(new ItemStack(Items.DIAMOND))
			);
			iconButton = builder.addButton(24, 20, () -> iconActions++)
				.setMessage(Component.literal("Ignored"))
				.setIcon(Objects.requireNonNull(icons.get(-1)))
				.setPosition(72, 36).setTooltip(Component.literal("Icon action help"));
			List<Integer> values = new ArrayList<>(List.of(-1, 0, 1));
			cycle = builder.addCycleButton(values, -1, 24, 20, this::onValueChanged)
				.setIcon(value -> Objects.requireNonNull(icons.get(value)))
				.setMessage(ButtonControls::layerMessage)
				.setPosition(40, 36).setTooltip(this::addCycleTooltip);
			iconCycle = builder.addCycleButton(values, -1, 20, 20, this::onIconValueChanged)
				.setMessage(value -> Component.literal("Ignored cycle " + value))
				.setIcon(value -> Objects.requireNonNull(icons.get(value)))
				.setPosition(136, 36).setTooltip(Component.literal("Icon layer help"));
			values.clear();
		}

		private static Component layerMessage(Integer value) {
			if (value < 0) {
				return Component.literal("All");
			}
			return Component.literal(Integer.toString(value + 1));
		}

		private void onValueChanged(Integer value) {
			check(cycle.getValue().equals(value), "The cycle callback must observe the selected value");
			selected.add(value);
		}

		private void addCycleTooltip(ITooltipBuilder tooltip) {
			tooltip.add(Component.literal("Layer " + cycle.getValue()));
		}

		private void onIconValueChanged(Integer value) {
			check(iconCycle.getValue().equals(value), "The icon cycle callback must observe the selected value");
			iconSelected.add(value);
		}
	}

}
