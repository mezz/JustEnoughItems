package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.Internal;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.test.client.ReflectionUtil;
import mezz.jei.gui.elements.IconButton;
import mezz.jei.fabric.events.JeiScreenEvents;
import java.util.concurrent.atomic.AtomicBoolean;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.ConfigButtonController;
import mezz.jei.gui.input.GuiTextFieldFilter;
import mezz.jei.test.client.ImeTextInputTestUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.PreeditEvent;
import mezz.jei.gui.config.JeiConfigGuiPlugin;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.mezzdev.config.gui.ConfigContentPage;
import net.mezzdev.config.gui.ConfigScreen;
import net.mezzdev.config.gui.api.ConfigInfo;
import net.mezzdev.config.gui.api.IConfigScreenValue;
import net.mezzdev.config.gui.api.IQuickConfigMenu;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.sounds.SoundSource;

import java.util.List;
import java.util.Objects;

/** Verifies JEI's setting references, runtime toggles, and wrench integration. */
@SuppressWarnings("UnstableApiUsage")
public class QuickConfigClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		JUnitXmlTestReporter.runAndReport("fabric-client-gametest", getClass().getSimpleName(), () -> {
			try (TestSingleplayerContext ignored = context.worldBuilder().create()) {
				JeiFabricClientGameTestAssertions.assertJeiStartedWithSyncedRecipes(context);
				context.runOnClient(client -> {
					check(client.options.getSoundSourceVolume(SoundSource.MASTER) == 0.0F, "Game audio must be muted");
					client.gui.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
				});
				context.waitFor(client -> overlay().isListDisplayed());
				var controller = context.computeOnClient(client -> controller());
				AtomicBoolean observe = new AtomicBoolean(true);
				AtomicBoolean clipped = new AtomicBoolean();
				AtomicBoolean foregroundRendered = new AtomicBoolean();
				context.runOnClient(client -> JeiScreenEvents.DRAW_FOREGROUND.register((screen, graphics, x, y) -> {
					if (observe.get() && controller.isQuickMenuOpen()) {
						foregroundRendered.set(true);
						var wrench = wrenchArea();
						if (!graphics.containsPointInScissor(wrench.x() + wrench.width() / 2, wrench.y() + wrench.height() / 2)) {
							clipped.set(true);
						}
					}
				}));
				// Creating the menu resolves every default and additional reference, including nested/custom values.
				context.runOnClient(client -> check(controller.openQuickMenu(), "The JEI wrench menu must open"));
				var selection = context.computeOnClient(client -> selection(controller));
				var original = context.computeOnClient(client -> selection.getValue());
				boolean bookmarksEnabled = context.computeOnClient(client -> Internal.getClientToggleState().isBookmarkEnabled());
				try {
					context.runOnClient(client -> {
						quickMenu(controller).close();
						selection.set(selection.getDefaultValue());
					});
					click(context, context.computeOnClient(client -> wrenchArea()));
					context.runOnClient(client -> {
						check(controller.isQuickMenuOpen(), "Clicking the wrench must open the menu");
						check(selection.getDefaultValue().equals(List.of(
							"lists.bookmarkList.bookmarkOverlayEnabled",
							"recipes.lookups.history.enabled",
							"cheating.cheatModeEnabled",
							"lists.ingredientList.hideIngredientsWhenNotSearching",
							"search.completion.searchCompletionEnabled"
						)), "The quick menu must show the intended defaults in order");
						var options = (java.util.Map<?, ?>) field(field(quickMenu(controller), "model"), "values");
						check(!options.containsKey("advanced.editModeEnabled"), "Edit mode must not be available in the quick menu");
						check(List.copyOf(options.keySet()).subList(5, options.size()).equals(List.of(
							"lists.bookmarks.bookmarkPreviewMode",
							"search.centerSearch",
							"recipes.lookups.history.maxRows",
							"lists.ingredientList.ingredientSorting.ingredientSortStages"
						)), "The quick menu must offer the intended additional settings in order");
					});
					var filterBefore = context.computeOnClient(client -> Internal.getJeiRuntime().getIngredientFilter().getFilterText());
					context.runOnClient(client -> {
						// Even a previously focused JEI field must not receive typing through the modal menu.
						var search = (mezz.jei.gui.input.GuiTextFieldFilter) field(overlay(), "searchField");
						search.setFocused(true);
						mezz.jei.test.client.ImeTextInputTestUtil.invokeCharacterCallback(client.keyboardHandler, client.getWindow().handle(), new net.minecraft.client.input.CharacterEvent('x'));
						check(Internal.getJeiRuntime().getIngredientFilter().getFilterText().equals(filterBefore), "Typing in the quick menu must not edit JEI search");
					});
					checkShortcutIsolation(context, controller);
					checkImeIsolation(context, controller);
					checkHeldKeyRelease(context, controller);
					var row = context.computeOnClient(client -> {
						var page = (ConfigContentPage) field(quickMenu(controller), "page");
						return rectangle(page.getEntries().getFirst().getArea());
					});
					int initialScale = context.computeOnClient(client -> client.getWindow().getGuiScale());
					context.getInput().setCursorPos((row.x() + 5) * initialScale, (row.y() + row.height() / 2.0) * initialScale);
					context.waitTicks(2);
					context.runOnClient(client -> check(field(field(field(quickMenu(controller), "view"), "infoPanel"), "info") != null,
						"Hovering a setting must populate the bottom information panel"));
					context.takeScreenshot("jei-quick-menu");
					check(foregroundRendered.get(), "JEI must render while the quick menu is open");
					check(!clipped.get(), "The quick menu must not clip away JEI's foreground or wrench");
					var bookmarkControl = context.computeOnClient(client -> {
						var page = (ConfigContentPage) field(quickMenu(controller), "page");
						return rectangle(field(page.getEntries().getFirst(), "valueArea"));
					});
					click(context, bookmarkControl);
					context.runOnClient(client -> check(Internal.getClientToggleState().isBookmarkEnabled() != bookmarksEnabled,
						"The menu must use JEI's registered bookmark toggle"));
					var wrench = context.computeOnClient(client -> wrenchArea());
					int scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
					context.getInput().setCursorPos((wrench.x() + wrench.width() / 2.0) * scale, (wrench.y() + wrench.height() / 2.0) * scale);
					context.waitTicks(2);
					context.runOnClient(client -> {
						var info = (ConfigInfo) field(field(field(quickMenu(controller), "view"), "infoPanel"), "info");
						check(info != null && info.lines().stream().anyMatch(line -> line.getString().equals(net.minecraft.network.chat.Component.translatable("jei.tooltip.config.open.full").getString())),
							"Hovering the wrench must explain how to open the full settings screen");
					});
					context.takeScreenshot("jei-quick-menu-wrench-help");
					var inventory = context.computeOnClient(client -> client.gui.screen());
					click(context, wrench);
					context.runOnClient(client -> check(!controller.isQuickMenuOpen() && client.gui.screen() instanceof ConfigScreen,
						"Clicking the wrench again must open the full config screen"));
					context.setScreen(() -> inventory);
					context.runOnClient(client -> {
						selection.set(List.of("lists.bookmarks.bookmarkPreviewMode", "search.centerSearch"));
						var recreated = JeiConfigGuiPlugin.createQuickMenu().orElseThrow();
						check(recreated.open(() -> wrenchArea().toMutable(), () -> true), "A new handle must open with saved choices");
						var page = (ConfigContentPage) field(recreated, "page");
						check(page.getEntries().size() == 2, "New menu handles must retain the saved selection");
						check(page.getEntries().getFirst().getConfigValue().getName().equals("bookmarkPreviewMode"),
							"Additional settings must reuse the full screen's custom editor value");
						recreated.close();
					});
					context.runOnClient(client -> check(controller.openQuickMenu(), "The quick menu must reopen"));
					var fullConfigButton = context.computeOnClient(client -> {
						Object sharedMenu = field(quickMenu(controller), "layout");
						try {
							var method = sharedMenu.getClass().getDeclaredMethod("fullConfigButtonArea");
							method.setAccessible(true);
							return rectangle(method.invoke(sharedMenu));
						} catch (ReflectiveOperationException e) {
							throw new AssertionError(e);
						}
					});
					context.takeScreenshot("jei-quick-menu-full-config-button");
					click(context, fullConfigButton);
					context.runOnClient(client -> check(client.gui.screen() instanceof ConfigScreen && !controller.isQuickMenuOpen(),
						"The quick-menu header button must open JEI's full config screen"));

				} finally {
					observe.set(false);
					context.runOnClient(client -> {
						quickMenu(controller).close();
						selection.set(original);
						Internal.getClientToggleState().setBookmarkEnabled(bookmarksEnabled);
					});
				}
			}
		});
	}

	private static void checkImeIsolation(ClientGameTestContext context, ConfigButtonController controller) {
		context.runOnClient(client -> {
			var search = (GuiTextFieldFilter) field(overlay(), "searchField");
			String original = search.getValue();
			long window = client.getWindow().handle();
			try {
				quickMenu(controller).close();
				search.setValue("");
				search.focus();
				ImeTextInputTestUtil.typeKoreanText(client.keyboardHandler, window, search);
				search.setValue("");
				client.keyboardHandler.textEditing(window, new PreeditEvent("ㅎ", 1, List.of("ㅎ"), 0));
				check(controller.openQuickMenu(), "The menu must open during an IME composition");
				check(!search.isFocused(), "Opening the menu must release the search field's IME focus");
				for (String stage : List.of("하", "한")) {
					client.keyboardHandler.textEditing(window, new PreeditEvent(stage, 1, List.of(stage), 0));
				}
				client.keyboardHandler.textInput(window, "한글");
				client.keyboardHandler.textEditing(window, null);
				check(search.getValue().isEmpty(), "IME composition and commits must not edit host search while the menu is open");
				quickMenu(controller).close();
				search.focus();
				ImeTextInputTestUtil.typeKoreanText(client.keyboardHandler, window, search);
			} finally {
				client.keyboardHandler.textEditing(window, null);
				search.setValue(original);
				search.clearFocus();
				check(controller.openQuickMenu(), "The menu must reopen after IME checks");
			}
		});
	}

	private static void checkHeldKeyRelease(ClientGameTestContext context, ConfigButtonController controller) {
		context.runOnClient(client -> {
			var search = (GuiTextFieldFilter) field(overlay(), "searchField");
			String original = search.getValue();
			var focusSearch = Objects.requireNonNull(KeyMapping.get("key.jei.focusSearch"));
			Runnable restore = KeyMappingTestHelper.rebindUnmodified(focusSearch, InputConstants.Type.KEYBOARD.getOrCreate(InputConstants.KEY_F));
			var key = new KeyEvent(InputConstants.KEY_F, 'f', 0);
			long window = client.getWindow().handle();
			try {
				quickMenu(controller).close();
				search.setValue("");
				search.clearFocus();
				ImeTextInputTestUtil.invokeKeyPress(client.keyboardHandler, window, key);
				check(search.isFocused(), "The held key must first activate JEI's focus-search shortcut");
				check(controller.openQuickMenu(), "The menu must open while the focus-search key is held");
				ImeTextInputTestUtil.invokeKeyRelease(client.keyboardHandler, window, key);
				quickMenu(controller).close();
				search.focus();
				// IME commits need no new key press to clear stale shortcut suppression.
				client.keyboardHandler.textInput(window, "苹果");
				check(search.getValue().equals("苹果"), "Releasing a host key during the menu must not leave later IME commits suppressed");
			} finally {
				quickMenu(controller).close();
				ImeTextInputTestUtil.invokeKeyRelease(client.keyboardHandler, window, key);
				restore.run();
				search.setValue(original);
				search.clearFocus();
				check(controller.openQuickMenu(), "The menu must reopen after held-key checks");
			}
		});
	}

	private static void checkShortcutIsolation(ClientGameTestContext context, ConfigButtonController controller) {
		var filter = context.computeOnClient(client -> Internal.getJeiRuntime().getIngredientFilter());
		var originalFilter = context.computeOnClient(client -> filter.getFilterText());
		var inventory = context.computeOnClient(client -> client.gui.screen());
		try {
			context.runOnClient(client -> {
				quickMenu(controller).close();
				filter.setFilterText("oak planks");
				((mezz.jei.gui.input.GuiTextFieldFilter) field(overlay(), "searchField")).setFocused(false);
			});
			context.waitTicks(2);
			var point = context.computeOnClient(client -> {
				for (int y = 0; y < inventory.height; y += 4) {
					for (int x = 0; x < inventory.width; x += 4) {
						if (overlay().getIngredientUnderMouse(x, y).anyMatch(ingredient -> ingredient.getTypedIngredient()
							.getIngredient(mezz.jei.api.constants.VanillaTypes.ITEM_STACK)
							.filter(stack -> stack.is(net.minecraft.world.item.Items.OAK_PLANKS)).isPresent())
						) {
							return new net.minecraft.client.gui.navigation.ScreenPosition(x, y);
						}
					}
				}
				throw new AssertionError("The shortcut test must hover oak planks in JEI");
			});
			for (int key : new int[]{InputConstants.KEY_R, InputConstants.KEY_U}) {
				int scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
				context.getInput().setCursorPos(point.x() * scale, point.y() * scale);
				context.waitTick();
				context.runOnClient(client -> {
					check(!overlay().hasKeyboardFocus(), "Search must not capture the shortcut");
					check(overlay().getIngredientUnderMouse().isPresent(), "The shortcut must target an ingredient");
				});
				context.getInput().pressKey(key);
				context.waitTick();
				context.runOnClient(client -> check(client.gui.screen() == Internal.getJeiRuntime().getRecipesGui(),
					"Key " + key + " must open recipes with the menu closed"));
				context.setScreen(() -> inventory);
				context.waitTick();
				context.runOnClient(client -> check(controller.openQuickMenu(), "The menu must reopen over the same ingredient"));
				context.getInput().setCursorPos(point.x() * scale, point.y() * scale);
				context.waitTick();
				context.runOnClient(client -> {
					check(!overlay().hasKeyboardFocus(), "Search must remain unfocused while testing isolation");
					check(overlay().getIngredientUnderMouse().isPresent(), "The ingredient must remain under the cursor while the menu is open");
				});
				context.getInput().pressKey(key);
				context.waitTick();
				context.runOnClient(client -> {
					check(controller.isQuickMenuOpen() && client.gui.screen() == inventory, "Key " + key + " must be blocked by the quick menu");
					quickMenu(controller).close();
				});
			}
		} finally {
			context.runOnClient(client -> {
				quickMenu(controller).close();
				filter.setFilterText(originalFilter);
				client.gui.setScreen(inventory);
				check(controller.openQuickMenu(), "The quick menu must reopen after the shortcut checks");
			});
		}
	}

	@SuppressWarnings("unchecked")
	private static IConfigScreenValue<List<String>> selection(ConfigButtonController controller) {
		return (IConfigScreenValue<List<String>>) field(field(field(quickMenu(controller), "model"), "selection"), "value");
	}

	private static IngredientListOverlay overlay() {
		return (IngredientListOverlay) Internal.getJeiRuntime().getIngredientListOverlay();
	}

	private static ConfigButtonController controller() {
		return new ReflectionUtil().getFieldWithClass(overlay(), ConfigButtonController.class).findFirst().orElseThrow();
	}

	private static IQuickConfigMenu quickMenu(ConfigButtonController controller) {
		return (IQuickConfigMenu) field(field(controller, "configGuiIntegration"), "quickMenu");
	}

	private static ImmutableRect2i wrenchArea() {
		return new ReflectionUtil().getFieldWithClass(overlay(), IconButton.class).findFirst().orElseThrow().getArea();
	}

	private static Object field(Object target, String name) {
		for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
			try {
				var field = type.getDeclaredField(name);
				field.setAccessible(true);
				return field.get(target);
			} catch (NoSuchFieldException ignored) {} catch (IllegalAccessException e) {
				throw new AssertionError(e);
			}
		}
		throw new AssertionError("Missing field: " + name);
	}

	private static ImmutableRect2i rectangle(Object value) {
		var area = (net.mezzdev.config.gui.util.ImmutableRect2i) value;
		return new ImmutableRect2i(area.getX(), area.getY(), area.getWidth(), area.getHeight());
	}

	private static void click(ClientGameTestContext context, ImmutableRect2i area) {
		int scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
		context.getInput().setCursorPos((area.x() + area.width() / 2.0) * scale, (area.y() + area.height() / 2.0) * scale);
		context.waitTick();
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTicks(2);
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
