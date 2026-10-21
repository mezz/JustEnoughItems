package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.common.Internal;
import mezz.jei.common.config.SearchBarPosition;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.test.client.ReflectionUtil;
import mezz.jei.gui.elements.IconButton;
import mezz.jei.gui.input.GuiTextFieldFilter;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.ingredients.IIngredientListOverlayContents;
import net.mezzdev.config.api.value.IConfigValue;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Checks ingredient visibility and search interaction through the live overlay and Minecraft input. */
@SuppressWarnings("UnstableApiUsage")
public class IngredientListVisibilityClientGameTest {
	static void run() {
		FabricClientTestContext context = new FabricClientTestContext();
		List<Runnable> cleanup = new ArrayList<>();
		try {
			context.runOnClient(client -> {
				var configs = Internal.getClientConfigs();
				var config = configs.getClientConfig();
				set(cleanup, config.hideIngredientsWhenNotSearching(), false);
				set(cleanup, config.lookupHistoryEnabled(), false);
				set(cleanup, config.searchBarPosition(), SearchBarPosition.STANDARD);
				KeyMapping shortcut = java.util.Arrays.stream(client.options.keyMappings).filter(key -> key.getName().equals("key.jei.toggleHideIngredientsWhenNotSearching")).findFirst().orElseThrow();
				cleanup.add(KeyMappingTestHelper.rebindUnmodified(shortcut, InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT)));
				set(cleanup, configs.getIngredientListConfig().maxColumns(), 6);
				set(cleanup, configs.getIngredientListConfig().maxRows(), 4);
				set(cleanup, configs.getIngredientListConfig().shrinkToFit(), false);
				int originalScale = client.options.guiScale().get();
				cleanup.add(() -> {
					client.options.guiScale().set(originalScale);
					client.resizeDisplay();
				});
				client.options.guiScale().set(2);
				client.resizeDisplay();
				var filter = Internal.getJeiRuntime().getIngredientFilter();
				String originalFilter = filter.getFilterText();
				cleanup.add(() -> filter.setFilterText(originalFilter));
				filter.setFilterText("");
			});
			for (SearchBarPosition position : SearchBarPosition.values()) {
				assertSearchVisibility(context, position);
			}
		} finally {
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
			context.runOnClient(client -> {
				client.setScreen(null);
				cleanup.reversed().forEach(Runnable::run);
			});
		}
	}

	private static void assertSearchVisibility(FabricClientTestContext context, SearchBarPosition position) {
		TrackingInventoryScreen screen = context.computeOnClient(client -> {
			var config = Internal.getClientConfigs().getClientConfig();
			config.searchBarPosition().set(position);
			config.hideIngredientsWhenNotSearching().set(false);
			Internal.getJeiRuntime().getIngredientFilter().setFilterText("");
			TrackingInventoryScreen inventory = new TrackingInventoryScreen(Objects.requireNonNull(client.player));
			client.setScreen(inventory);
			return inventory;
		});
		context.waitFor(client -> overlay().isListDisplayed());
		context.runOnClient(client -> screen.ingredientArea = new ReflectionUtil()
			.getFieldWithClass(overlay(), IIngredientListOverlayContents.class).findFirst().orElseThrow().getBackgroundArea());

		context.runOnClient(client -> Internal.getClientConfigs().getClientConfig().hideIngredientsWhenNotSearching().set(true));
		assertVisibility(context, false);
		context.runOnClient(client -> {
			check(!Internal.getJeiRuntime().getIngredientFilter().getFilteredItemStacks().isEmpty(), "Hiding ingredients must preserve filter results");
			check(!overlay().isMouseOver(screen.ingredientArea.x() + 5, screen.ingredientArea.y() + 5), "The hidden list must not intercept input");
		});
		click(context, context.computeOnClient(client -> screen.ingredientArea));
		context.runOnClient(client -> check(screen.ingredientAreaClicks == 1, "Clicks through the hidden list must reach the inventory screen"));

		click(context, context.computeOnClient(client -> searchField().getArea()));
		context.runOnClient(client -> {
			check(overlay().hasKeyboardFocus(), "The hidden list must leave the search field usable");
			searchField().setValue("oak slab");
		});
		assertVisibility(context, true);
		context.takeScreenshot("jei-ingredient-list-search-" + position.name().toLowerCase(java.util.Locale.ROOT));
		context.runOnClient(client -> searchField().setValue("jei-no-matching-ingredient-4531"));
		assertVisibility(context, false);
		context.runOnClient(client -> {
			check(overlay().hasKeyboardFocus(), "An unmatched search must retain keyboard focus");
			searchField().setValue(" \t ");
		});
		assertVisibility(context, false);
		context.runOnClient(client -> searchField().setValue(""));
		assertVisibility(context, false);
		context.takeScreenshot("jei-ingredient-list-not-searching-" + position.name().toLowerCase(java.util.Locale.ROOT));
		hover(context, context.computeOnClient(client -> configButtonArea()));
		context.takeScreenshot("jei-ingredient-list-not-searching-config-" + position.name().toLowerCase(java.util.Locale.ROOT));
		assertVisibilityHotkey(context, screen, position);

		context.runOnClient(client -> Internal.getClientConfigs().getClientConfig().hideIngredientsWhenNotSearching().set(false));
		assertVisibility(context, true);
		context.takeScreenshot("jei-ingredient-list-always-visible-" + position.name().toLowerCase(java.util.Locale.ROOT));
		context.runOnClient(client -> {
			Internal.getClientConfigs().getClientConfig().hideIngredientsWhenNotSearching().set(true);
			client.setScreen(null);
			client.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
		});
		assertVisibility(context, false);
	}

	private static void assertVisibilityHotkey(FabricClientTestContext context, TrackingInventoryScreen screen, SearchBarPosition position) {
		var setting = Internal.getClientConfigs().getClientConfig().hideIngredientsWhenNotSearching();
		context.runOnClient(client -> check(searchField().isFocused(), "The mouse shortcut must work while search has focus"));
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
		context.waitTick();
		context.runOnClient(client -> check(setting.get(), "The shortcut must wait for mouse release"));
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
		assertVisibility(context, true);
		context.runOnClient(client -> {
			check(!setting.get(), "The shortcut must disable hiding ingredients when not searching");
			check(client.screen == screen, "The shortcut must leave the inventory open");
			check(searchField().getValue().isEmpty(), "The shortcut must preserve the search text");
		});
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
		context.waitTick();
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
		assertVisibility(context, false);

		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
		context.waitTick();
		hover(context, new ImmutableRect2i(0, 0, 1, 1));
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
		context.runOnClient(client -> check(setting.get(), "Releasing outside the config button must cancel the shortcut"));

		KeyMapping shortcut = context.computeOnClient(client -> java.util.Arrays.stream(client.options.keyMappings).filter(candidate -> candidate.getName().equals("key.jei.toggleHideIngredientsWhenNotSearching")).findFirst().orElseThrow());
		var originalKey = context.computeOnClient(client -> InputConstants.getKey(shortcut.saveString()));
		try {
			context.runOnClient(client -> {
				searchField().clearFocus();
				shortcut.setKey(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_P));
			});
			context.getInput().pressKey(InputConstants.KEY_P);
			context.runOnClient(client -> check(setting.get(), "The rebound shortcut must require hovering over the config button"));
			hover(context, context.computeOnClient(client -> configButtonArea()));
			context.takeScreenshot("jei-ingredient-list-rebound-shortcut-" + position.name().toLowerCase(java.util.Locale.ROOT));
			context.getInput().pressKey(InputConstants.KEY_P);
			assertVisibility(context, true);
			context.getInput().pressKey(InputConstants.KEY_P);
			assertVisibility(context, false);
			context.runOnClient(client -> shortcut.setKey(InputConstants.UNKNOWN));
			context.takeScreenshot("jei-ingredient-list-unbound-shortcut-" + position.name().toLowerCase(java.util.Locale.ROOT));
			context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
			context.waitTick();
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
			context.runOnClient(client -> check(setting.get(), "An unbound shortcut must not toggle ingredient visibility"));
		} finally {
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
			context.runOnClient(client -> shortcut.setKey(originalKey));
		}
	}

	private static void assertVisibility(FabricClientTestContext context, boolean visible) {
		context.waitTicks(3);
		context.runOnClient(client -> {
			check(overlay().isListDisplayed() == visible, "Unexpected ingredient list visibility");
			check(overlay().getVisibleIngredients(VanillaTypes.ITEM_STACK).isEmpty() != visible, "The visible ingredient API must reflect the overlay");
		});
	}

	private static IngredientListOverlay overlay() {
		return (IngredientListOverlay) Internal.getJeiRuntime().getIngredientListOverlay();
	}

	private static GuiTextFieldFilter searchField() {
		return new ReflectionUtil().getFieldWithClass(overlay(), GuiTextFieldFilter.class).findFirst().orElseThrow();
	}

	private static ImmutableRect2i configButtonArea() {
		return new ReflectionUtil().getFieldWithClass(overlay(), IconButton.class).findFirst().orElseThrow().getArea();
	}

	private static void click(FabricClientTestContext context, ImmutableRect2i area) {
		hover(context, area);
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
	}

	private static void hover(FabricClientTestContext context, ImmutableRect2i area) {
		double scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
		context.getInput().setCursorPos((area.x() + area.width() / 2.0) * scale, (area.y() + area.height() / 2.0) * scale);
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

	private static class TrackingInventoryScreen extends InventoryScreen {
		private ImmutableRect2i ingredientArea = ImmutableRect2i.EMPTY;
		private int ingredientAreaClicks;

		TrackingInventoryScreen(LocalPlayer player) {
			super(player);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (ingredientArea.contains(mouseX, mouseY)) {
				ingredientAreaClicks++;
				return true;
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}
	}
}
