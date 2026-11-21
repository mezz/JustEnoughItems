package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IngredientGridNavigationMode;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.fabric.events.JeiScreenEvents;
import mezz.jei.gui.elements.IconButton;
import mezz.jei.gui.input.GuiTextFieldFilter;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.ingredients.IIngredientGridSource;
import mezz.jei.gui.overlay.ingredients.IIngredientListOverlayContents;
import mezz.jei.gui.overlay.ingredients.IngredientGrid;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigation;
import mezz.jei.gui.overlay.ingredients.IngredientListSlot;
import mezz.jei.gui.recipes.IRecipeGuiLogic;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.gui.recipes.lookups.ILookupState;
import mezz.jei.test.client.ImeTextInputTestUtil;
import mezz.jei.test.client.ReflectionUtil;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.mezzdev.config.api.value.IConfigValue;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.GameType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Exercises keyboard selection and ingredient actions through Minecraft's input callbacks. */
@SuppressWarnings("UnstableApiUsage")
public class IngredientKeyboardNavigationClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		JUnitXmlTestReporter.runAndReport("fabric-client-gametest", getClass().getSimpleName(), () -> {
			try (TestSingleplayerContext ignored = context.worldBuilder().create()) {
				JeiFabricClientGameTestAssertions.assertJeiStartedWithSyncedRecipes(context);
				List<Runnable> cleanup = new ArrayList<>();
				try {
					context.runOnClient(client -> {
						var config = Internal.getClientConfigs();
						set(cleanup, config.getIngredientListConfig().maxColumns(), 4);
						set(cleanup, config.getIngredientListConfig().maxRows(), 3);
						set(cleanup, config.getIngredientListConfig().shrinkToFit(), false);
						set(cleanup, config.getIngredientListConfig().navigationMode(), IngredientGridNavigationMode.PAGED);
						set(cleanup, config.getClientConfig().smoothScrollingEnabled(), false);
						set(cleanup, config.getClientConfig().searchCompletionEnabled(), false);
						var filter = Internal.getJeiRuntime().getIngredientFilter();
						String original = filter.getFilterText();
						cleanup.add(() -> filter.setFilterText(original));
						bind(cleanup, "key.jei.focusSearch", InputConstants.KEY_F);
						bind(cleanup, "key.jei.showRecipe", InputConstants.KEY_R);
						bind(cleanup, "key.jei.showUses", InputConstants.KEY_U);
						bind(cleanup, "key.jei.bookmark", InputConstants.KEY_A);
					});
					assertMovement(context);
					assertEscapeClosesInventory(context);
					assertTooltipBounds(context);
					assertSearchTransitions(context);
					assertSearchHistory(context);
					assertKeyboardFocusBlocksHoveredControls(context);
					assertExcludedCell(context);
					assertActions(context);
					for (int rows : new int[]{1, 3}) {
						for (var mode : IngredientGridNavigationMode.values()) {
							assertBoundaries(context, mode, false, rows);
						}
						assertBoundaries(context, IngredientGridNavigationMode.SCROLLING, true, rows);
					}
					for (var mode : IngredientGridNavigationMode.values()) {
						assertTabNavigation(context, mode);
					}
					assertEmptyResultsAndCleanup(context);
					assertHiddenOverlayCleanup(context);
					assertCreativeTextFocus(context);
				} finally {
					context.runOnClient(client -> {
						client.gui.setScreen(null);
						cleanup.reversed().forEach(Runnable::run);
					});
				}
			}
		});
	}

	private static void open(ClientGameTestContext context, String filter) {
		context.runOnClient(client -> {
			client.gui.setScreen(null);
			Internal.getJeiRuntime().getIngredientFilter().setFilterText(filter);
			client.gui.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
		});
		context.waitTicks(2);
	}

	private static void focus(ClientGameTestContext context) {
		context.getInput().pressKey(InputConstants.KEY_F);
		context.runOnClient(client -> check(search().isFocused(), "Focus Search must select the search field"));
		context.getInput().pressKey(InputConstants.KEY_RETURN);
		context.runOnClient(client -> {
			check(!search().isFocused(), "Submitting search must leave text editing");
			check(grid().getFocusedSlot().isPresent(), "Submitting search must select an ingredient");
		});
	}

	private static void assertMovement(ClientGameTestContext context) {
		open(context, "");
		focus(context);
		var start = context.computeOnClient(client -> focused().getArea());
		context.getInput().pressKey(InputConstants.KEY_RIGHT);
		context.runOnClient(client -> check(focused().getArea().x() > start.x() && focused().getArea().y() == start.y(), "Right must move within the row"));
		context.getInput().pressKey(InputConstants.KEY_DOWN);
		context.runOnClient(client -> check(focused().getArea().x() > start.x() && focused().getArea().y() > start.y(), "Down must preserve the column"));
		context.getInput().pressKey(InputConstants.KEY_LEFT);
		context.getInput().pressKey(InputConstants.KEY_UP);
		context.runOnClient(client -> check(focused().getArea().equals(start), "Left then Up must return to the first ingredient"));
		context.getInput().pressKey(InputConstants.KEY_F);
		context.runOnClient(client -> {
			check(search().isFocused(), "Focus Search must return to search");
			check(grid().getFocusedSlot().isEmpty(), "Returning to search must clear ingredient focus");
		});
	}

	private static void assertEscapeClosesInventory(ClientGameTestContext context) {
		GameType original = context.computeOnClient(client -> client.gameMode.getPlayerMode());
		try {
			for (GameType mode : new GameType[]{GameType.SURVIVAL, GameType.CREATIVE}) {
				context.runOnClient(client -> client.gameMode.setLocalMode(mode));
				if (mode == GameType.CREATIVE) {
					context.runOnClient(client -> {
						client.gui.setScreen(null);
						Internal.getJeiRuntime().getIngredientFilter().setFilterText("planks");
						ImeTextInputTestUtil.openCreativeSearchWithTextInputFocused(client, search());
					});
					context.waitTicks(2);
				} else {
					open(context, "planks");
				}
				focus(context);
				context.getInput().pressKey(InputConstants.KEY_ESCAPE);
				context.runOnClient(client -> {
					check(client.gui.screen() == null, "Escape from ingredient navigation must close the " + mode + " inventory in one press");
					check(!search().isFocused(), "Closing the inventory must not focus search");
					check(grid().getFocusedSlot().isEmpty(), "Closing the inventory must clear ingredient selection");
				});
			}
		} finally {
			context.runOnClient(client -> {
				client.gui.setScreen(null);
				client.gameMode.setLocalMode(original);
			});
		}
	}

	private static void assertActions(ClientGameTestContext context) {
		context.runOnClient(client -> Internal.getClientConfigs().getIngredientListConfig().maxRows().set(1));
		open(context, "planks");
		var hovered = context.computeOnClient(client -> grid().getSlots().filter(slot -> slot.getOptionalElement().isPresent()).findFirst().orElseThrow());
		ITypedIngredient<?> hoveredIngredient = context.computeOnClient(client -> hovered.getOptionalElement().orElseThrow().getTypedIngredient());
		moveMouse(context, hovered.getArea());
		context.getInput().pressKey(InputConstants.KEY_R);
		assertLookupAndClose(context, hoveredIngredient, RecipeIngredientRole.OUTPUT);
		for (var mode : IngredientGridNavigationMode.values()) {
			context.runOnClient(client -> Internal.getClientConfigs().getIngredientListConfig().navigationMode().set(mode));
			for (int key : new int[]{InputConstants.KEY_R, InputConstants.KEY_U, InputConstants.KEY_RETURN}) {
				focus(context);
				context.getInput().pressKey(InputConstants.KEY_RIGHT);
				context.getInput().pressKey(InputConstants.KEY_DOWN);
				context.getInput().pressKey(InputConstants.KEY_DOWN);
				IElement<?> selected = context.computeOnClient(client -> focused().getOptionalElement().orElseThrow());
				var other = context.computeOnClient(client -> grid().getSlots().filter(slot -> slot.getOptionalElement().isPresent()).findFirst().orElseThrow().getArea());
				moveMouse(context, other);
				context.getInput().pressKey(key);
				RecipeIngredientRole expectedRole = switch (key) {
					case InputConstants.KEY_U -> RecipeIngredientRole.INPUT;
					default -> RecipeIngredientRole.OUTPUT;
				};
				assertLookupAndClose(context, selected.getTypedIngredient(), expectedRole);
				assertSelected(context, selected, "Returning from recipes in " + mode);
			}
		}
		focus(context);
		context.getInput().pressKey(InputConstants.KEY_RIGHT);
		ITypedIngredient<?> selected = context.computeOnClient(client -> focused().getOptionalElement().orElseThrow().getTypedIngredient());
		boolean bookmarked = context.computeOnClient(client -> Internal.getJeiRuntime().getBookmarkManager().contains(selected));
		try {
			context.getInput().pressKey(InputConstants.KEY_A);
			context.runOnClient(client -> check(Internal.getJeiRuntime().getBookmarkManager().contains(selected) != bookmarked, "Bookmark must act on the keyboard selection"));
		} finally {
			context.runOnClient(client -> {
				if (bookmarked) {
					Internal.getJeiRuntime().getBookmarkManager().add(selected);
				} else {
					Internal.getJeiRuntime().getBookmarkManager().remove(selected);
				}
			});
		}
	}

	private static void assertTooltipBounds(ClientGameTestContext context) {
		open(context, "planks");
		moveMouse(context, context.computeOnClient(client -> search().getArea()));
		focus(context);
		context.runOnClient(client -> {
			var searchArea = search().getArea();
			TooltipBoundsGraphics screenGraphics = new TooltipBoundsGraphics(client);
			JeiScreenEvents.DRAW_FOREGROUND.invoker().drawForeground(client.gui.screen(), screenGraphics, searchArea.x() + 4, searchArea.y() + 4);
			screenGraphics.extractDeferredElements(searchArea.x() + 4, searchArea.y() + 4, 0);
			check(!screenGraphics.tooltip.isEmpty(), "Keyboard tooltip must render while the mouse remains over search");
			ImmutableRect2i original = grid().getArea();
			int width = client.getWindow().getGuiScaledWidth();
			int height = client.getWindow().getGuiScaledHeight();
			try {
				for (int top : new int[]{0, height - 18}) {
					grid().updateBounds(new ImmutableRect2i(width - 18, top, 18, 18), Set.of(), null);
					contents().updateLayoutToFirstPage();
					TooltipBoundsGraphics graphics = new TooltipBoundsGraphics(client);
					grid().drawTooltips(client, graphics, -1, -1);
					graphics.extractDeferredElements(0, 0, 0);
					ImmutableRect2i tooltip = graphics.tooltip;
					check(!tooltip.isEmpty(), "Keyboard selection must render its tooltip");
					check(tooltip.x() >= TooltipRenderUtil.PADDING_LEFT && tooltip.y() >= TooltipRenderUtil.PADDING_TOP &&
						tooltip.x() + tooltip.width() <= width - TooltipRenderUtil.PADDING_RIGHT &&
						tooltip.y() + tooltip.height() <= height - TooltipRenderUtil.PADDING_BOTTOM,
						"Keyboard tooltip must stay inside the screen: " + tooltip);
				}
			} finally {
				grid().updateBounds(original, Set.of(), null);
				contents().updateLayoutToFirstPage();
			}
		});
	}

	private static final class TooltipBoundsGraphics extends GuiGraphicsExtractor {
		private ImmutableRect2i tooltip = ImmutableRect2i.EMPTY;

		private TooltipBoundsGraphics(Minecraft client) {
			super(client, new GuiRenderState(), -1, -1);
		}

		@Override
		public void tooltip(Font font, List<ClientTooltipComponent> lines, int x, int y, ClientTooltipPositioner positioner,
			@Nullable Identifier style, boolean showTooltip) {
			int width = lines.stream().mapToInt(line -> line.getWidth(font)).max().orElse(0);
			int height = lines.stream().mapToInt(line -> line.getHeight(font)).sum();
			if (lines.size() > 1) {
				height += 2;
			}
			var window = Minecraft.getInstance().getWindow();
			var position = positioner.positionTooltip(window.getGuiScaledWidth(), window.getGuiScaledHeight(), x, y, width, height);
			tooltip = new ImmutableRect2i(position.x(), position.y(), width, height);
		}
	}

	private static void assertSearchHistory(ClientGameTestContext context) {
		open(context, "oak planks");
		focus(context);
		context.getInput().pressKey(InputConstants.KEY_F);
		context.runOnClient(client -> search().setValue("birch planks"));
		context.getInput().pressKey(InputConstants.KEY_RETURN);
		context.getInput().pressKey(InputConstants.KEY_F);
		FabricKeyModifiersClientGameTest.pressAltModifiedKey(context, InputConstants.KEY_UP);
		context.runOnClient(client -> check(search().isFocused() && search().getValue().equals("oak planks"), "Alt-Up must recall history without selecting an ingredient"));
		FabricKeyModifiersClientGameTest.pressAltModifiedKey(context, InputConstants.KEY_DOWN);
		context.runOnClient(client -> check(search().getValue().equals("birch planks"), "Alt-Down must advance through history"));
		for (int key : new int[]{InputConstants.KEY_UP, InputConstants.KEY_DOWN}) {
			context.getInput().pressKey(key);
			context.runOnClient(client -> check(grid().getFocusedSlot().isPresent() && search().getValue().equals("birch planks"), "Plain arrows must select ingredients without changing the query"));
			context.getInput().pressKey(InputConstants.KEY_F);
		}
		FabricKeyModifiersClientGameTest.pressAltModifiedKey(context, InputConstants.KEY_DOWN);
		FabricKeyModifiersClientGameTest.pressAltModifiedKey(context, InputConstants.KEY_DOWN);
		context.runOnClient(client -> {
			check(search().isFocused() && search().getValue().isEmpty(), "The end of history must keep search focused");
			Internal.getClientConfigs().getClientConfig().searchCompletionEnabled().set(true);
		});
		context.waitTicks(3);
		context.runOnClient(client -> check(search().isCompletionVisible(), "Expected prefix suggestions"));
		FabricKeyModifiersClientGameTest.pressAltModifiedKey(context, InputConstants.KEY_UP);
		context.runOnClient(client -> check(search().isFocused() && search().getValue().equals("birch planks"), "History must remain available while suggestions are visible"));
		context.runOnClient(client -> search().setValue(""));
		context.waitTicks(3);
		context.getInput().pressKey(InputConstants.KEY_RETURN);
		String firstSuggestion = context.computeOnClient(client -> search().getValue());
		context.runOnClient(client -> search().setValue(""));
		context.waitTicks(3);
		context.getInput().pressKey(InputConstants.KEY_DOWN);
		context.runOnClient(client -> check(search().isFocused() && search().getValue().isEmpty(), "Suggestion navigation must preserve the query and text focus"));
		context.getInput().pressKey(InputConstants.KEY_RETURN);
		context.runOnClient(client -> check(!search().getValue().isEmpty() && !search().getValue().equals(firstSuggestion), "Down must choose a different suggestion"));
		context.runOnClient(client -> search().setValue(""));
		context.waitTicks(3);
		context.getInput().pressKey(InputConstants.KEY_UP);
		context.getInput().pressKey(InputConstants.KEY_DOWN);
		context.getInput().pressKey(InputConstants.KEY_RETURN);
		context.runOnClient(client -> {
			check(search().getValue().equals(firstSuggestion), "Up then Down must return to the original suggestion");
			Internal.getClientConfigs().getClientConfig().searchCompletionEnabled().set(false);
		});
	}

	private static void assertLookupAndClose(ClientGameTestContext context, ITypedIngredient<?> ingredient, RecipeIngredientRole role) {
		context.runOnClient(client -> {
			check(client.gui.screen() instanceof RecipesGui, "The ingredient shortcut must open recipes");
			var gui = (RecipesGui) Internal.getJeiRuntime().getRecipesGui();
			var logic = new ReflectionUtil().getFieldWithClass(gui, IRecipeGuiLogic.class).findFirst().orElseThrow();
			var focuses = new ReflectionUtil().getFieldWithClass(logic, ILookupState.class).findFirst().orElseThrow().getFocuses().getAllFocuses();
			check(focuses.stream().allMatch(f -> f.getTypedValue().getType() == ingredient.getType() && uid(f.getTypedValue()).equals(uid(ingredient))), "Lookup must use the selected ingredient");
			check(focuses.stream().anyMatch(f -> f.getRole() == role), "Lookup must use the requested recipe role");
		});
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitTick();
	}

	private static void moveMouse(ClientGameTestContext context, ImmutableRect2i area) {
		double scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
		context.getInput().setCursorPos((area.x() + 4) * scale, (area.y() + 4) * scale);
		context.waitTicks(2);
	}

	private static void assertSearchTransitions(ClientGameTestContext context) {
		open(context, "planks");
		focus(context);
		// Repeatedly move from results to search and back without closing the inventory.
		focus(context);
		context.getInput().pressKey(InputConstants.KEY_F);
		context.runOnClient(client -> {
			Internal.getClientConfigs().getClientConfig().searchCompletionEnabled().set(true);
			search().setValue("@mine");
		});
		context.waitTicks(3);
		context.runOnClient(client -> check(search().isCompletionVisible(), "Expected a mod-name suggestion"));
		context.getInput().pressKey(InputConstants.KEY_RETURN);
		context.runOnClient(client -> {
			check(search().isFocused() && grid().getFocusedSlot().isEmpty(), "Accepting a suggestion must keep text focus");
			Internal.getClientConfigs().getClientConfig().searchCompletionEnabled().set(false);
			search().setValue("planks");
		});
		context.getInput().pressKey(InputConstants.KEY_RETURN);
		context.runOnClient(client -> check(grid().getFocusedSlot().isPresent(), "Enter after completion must focus the results"));
		var properties = context.computeOnClient(client -> Internal.getJeiRuntime().getScreenHelper().getGuiProperties(client.gui.screen()).orElseThrow());
		double scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
		context.getInput().setCursorPos((properties.guiLeft() + 2) * scale, (properties.guiTop() + 2) * scale);
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.runOnClient(client -> check(grid().getFocusedSlot().isEmpty(), "Mouse input must leave keyboard navigation"));
		focus(context);
		context.runOnClient(client -> {
			var area = grid().getArea();
			ScreenMouseEvents.allowMouseScroll(client.gui.screen()).invoker()
				.allowMouseScroll(client.gui.screen(), area.x() + 2, area.y() + 2, 0, -1);
			check(grid().getFocusedSlot().isEmpty(), "Scrolling the list must leave keyboard navigation");
		});
	}

	private static <T> Object uid(ITypedIngredient<T> ingredient) {
		return Internal.getJeiRuntime().getIngredientManager().getIngredientHelper(ingredient.getType()).getUid(ingredient, UidContext.Ingredient);
	}

	private static void assertKeyboardFocusBlocksHoveredControls(ClientGameTestContext context) {
		open(context, "planks");
		List<Runnable> cleanup = new ArrayList<>();
		boolean original = context.computeOnClient(client -> {
			bind(cleanup, "key.jei.toggleHideIngredientsWhenNotSearching", InputConstants.KEY_F8);
			return Internal.getClientConfigs().getClientConfig().hideIngredientsWhenNotSearching().get();
		});
		try {
			focus(context);
			var area = context.computeOnClient(client -> new ReflectionUtil().getFieldWithClass(Internal.getJeiRuntime().getIngredientListOverlay(), IconButton.class).findFirst().orElseThrow().getArea());
			moveMouse(context, area);
			context.getInput().pressKey(InputConstants.KEY_F8);
			context.runOnClient(client -> check(Internal.getClientConfigs().getClientConfig().hideIngredientsWhenNotSearching().get() == original,
				"Keyboard navigation must not activate a different control under the mouse"));
			// Leave the grid, then the search editor, before trying a pointer shortcut.
			context.getInput().pressKey(InputConstants.KEY_F);
			context.getInput().pressKey(InputConstants.KEY_ESCAPE);
			context.getInput().pressKey(InputConstants.KEY_F8);
			context.runOnClient(client -> check(Internal.getClientConfigs().getClientConfig().hideIngredientsWhenNotSearching().get() != original,
				"The hovered control's shortcut must still work after leaving keyboard navigation"));
		} finally {
			context.runOnClient(client -> {
				Internal.getClientConfigs().getClientConfig().hideIngredientsWhenNotSearching().set(original);
				cleanup.reversed().forEach(Runnable::run);
			});
		}
	}

	private static void assertHiddenOverlayCleanup(ClientGameTestContext context) {
		open(context, "planks");
		focus(context);
		boolean enabled = context.computeOnClient(client -> Internal.getClientToggleState().isOverlayEnabled());
		try {
			context.runOnClient(client -> Internal.getClientToggleState().setOverlayEnabled(false));
			context.waitTicks(2);
			context.runOnClient(client -> check(grid().getFocusedSlot().isEmpty(), "Hiding the overlay must release keyboard focus before the next key press"));
		} finally {
			context.runOnClient(client -> Internal.getClientToggleState().setOverlayEnabled(enabled));
		}
	}

	private static void assertExcludedCell(ClientGameTestContext context) {
		open(context, "");
		var area = context.computeOnClient(client -> grid().getArea());
		try {
			context.runOnClient(client -> {
				var slots = grid().getAllSlots();
				var horizontal = slots.get(1).getArea();
				var vertical = slots.get(grid().getColumnCount() + 2).getArea();
				grid().updateBounds(area, Set.of(
					new ImmutableRect2i(horizontal.x() + 6, horizontal.y() + 6, 1, 1),
					new ImmutableRect2i(vertical.x() + 6, vertical.y() + 6, 1, 1)
				), null);
				contents().updateLayoutToFirstPage();
			});
			focus(context);
			var right = context.computeOnClient(client -> grid().getAllSlots().get(2).getOptionalElement().orElseThrow());
			var down = context.computeOnClient(client -> grid().getAllSlots().get(2 * grid().getColumnCount() + 2).getOptionalElement().orElseThrow());
			context.getInput().pressKey(InputConstants.KEY_RIGHT);
			assertSelected(context, right, "Right across an excluded cell");
			context.getInput().pressKey(InputConstants.KEY_DOWN);
			assertSelected(context, down, "Down across an excluded cell");
			context.getInput().pressKey(InputConstants.KEY_UP);
			assertSelected(context, right, "Up across an excluded cell");
		} finally {
			context.runOnClient(client -> {
				grid().updateBounds(area, Set.of(), null);
				contents().updateLayoutToFirstPage();
			});
		}
	}

	private static void assertBoundaries(ClientGameTestContext context, IngredientGridNavigationMode mode, boolean smooth, int rows) {
		context.runOnClient(client -> {
			Internal.getClientConfigs().getIngredientListConfig().navigationMode().set(mode);
			Internal.getClientConfigs().getIngredientListConfig().maxRows().set(rows);
			Internal.getClientConfigs().getClientConfig().smoothScrollingEnabled().set(smooth);
		});
		open(context, "");
		context.runOnClient(client -> contents().updateLayoutToFirstPage());
		focus(context);
		context.getInput().pressKey(InputConstants.KEY_RIGHT);
		context.getInput().pressKey(InputConstants.KEY_RIGHT);
		List<IElement<?>> ingredients = context.computeOnClient(client -> ingredients());
		int columns = context.computeOnClient(client -> grid().getColumnCount());
		int startX = context.computeOnClient(client -> focused().getArea().x());
		for (int row = 1; row <= 5; row++) {
			context.getInput().pressKey(InputConstants.KEY_DOWN);
			assertSelected(context, ingredients.get(row * columns + 2), "Down across a boundary in " + mode + " with " + rows + " rows");
			context.runOnClient(client -> check(focused().getArea().x() == startX, "Down must preserve the column across a boundary"));
		}
		for (int row = 4; row >= 0; row--) {
			context.getInput().pressKey(InputConstants.KEY_UP);
			assertSelected(context, ingredients.get(row * columns + 2), "Up across a boundary in " + mode + " with " + rows + " rows");
			context.runOnClient(client -> check(focused().getArea().x() == startX, "Up must preserve the column across a boundary"));
		}
	}

	private static void assertTabNavigation(ClientGameTestContext context, IngredientGridNavigationMode mode) {
		context.runOnClient(client -> {
			Internal.getClientConfigs().getIngredientListConfig().navigationMode().set(mode);
			Internal.getClientConfigs().getClientConfig().smoothScrollingEnabled().set(false);
		});
		open(context, "planks");
		context.runOnClient(client -> contents().updateLayoutToFirstPage());
		List<IElement<?>> ingredients = context.computeOnClient(client -> ingredients());
		int pageSize = context.computeOnClient(client -> grid().size());
		check(ingredients.size() > pageSize, "Tab navigation must exercise more than one page");
		context.getInput().pressKey(InputConstants.KEY_F);
		context.getInput().pressKey(InputConstants.KEY_TAB);
		assertSelected(context, ingredients.getFirst(), "Tab from search");
		for (int i = 1; i < ingredients.size(); i++) {
			context.getInput().pressKey(InputConstants.KEY_TAB);
			assertSelected(context, ingredients.get(i), "Tab forward in " + mode);
		}
		for (int i = ingredients.size() - 2; i >= 0; i--) {
			FabricKeyModifiersClientGameTest.pressShiftModifiedKey(context, InputConstants.KEY_TAB);
			context.waitTick();
			assertSelected(context, ingredients.get(i), "Shift-Tab backward in " + mode);
		}
		FabricKeyModifiersClientGameTest.pressShiftModifiedKey(context, InputConstants.KEY_TAB);
		context.runOnClient(client -> {
			check(search().isFocused(), "Shift-Tab before the first result must return to search");
			check(grid().getFocusedSlot().isEmpty(), "Returning to search must release the result selection");
		});
	}

	private static void assertSelected(ClientGameTestContext context, IElement<?> expected, String action) {
		context.runOnClient(client -> {
			IngredientListSlot selected = grid().getFocusedSlot().orElseThrow(() -> new AssertionError(action +
				" lost focus before ingredient " + ingredients().indexOf(expected) + " on page " + contents().getPageDelegate().getPageNumber() +
				", search focused: " + search().isFocused()));
			IElement<?> actual = selected.getOptionalElement().orElseThrow();
			check(actual == expected, action + " must select ingredient " + ingredients().indexOf(expected) +
				", selected " + ingredients().indexOf(actual) + " on page " + contents().getPageDelegate().getPageNumber());
			var slot = selected.getArea();
			var area = grid().getArea();
			check(slot.y() >= area.y() && slot.y() + slot.height() <= area.y() + area.height(), action + " must keep the selection fully visible");
		});
	}

	private static List<IElement<?>> ingredients() {
		return List.copyOf(new ReflectionUtil().getFieldWithClass(contents(), IIngredientGridSource.class).findFirst().orElseThrow().getElements());
	}

	private static void assertEmptyResultsAndCleanup(ClientGameTestContext context) {
		open(context, "jei_no_matching_ingredient_4540");
		context.getInput().pressKey(InputConstants.KEY_F);
		for (int key : new int[]{InputConstants.KEY_RETURN, InputConstants.KEY_UP, InputConstants.KEY_DOWN}) {
			context.getInput().pressKey(key);
			context.runOnClient(client -> check(search().isFocused() && grid().getFocusedSlot().isEmpty(), "Empty results must leave search focused for editing"));
		}
		open(context, "planks");
		focus(context);
		context.runOnClient(client -> Internal.getJeiRuntime().getIngredientFilter().setFilterText("jei_no_matching_ingredient_4540"));
		context.waitTicks(2);
		context.getInput().pressKey(InputConstants.KEY_RETURN);
		context.runOnClient(client -> check(!(client.gui.screen() instanceof RecipesGui), "A removed selection must not open stale recipes"));
		open(context, "planks");
		focus(context);
		context.getInput().pressKey(InputConstants.KEY_R);
		context.runOnClient(client -> Internal.getJeiRuntime().getIngredientFilter().setFilterText("stone"));
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.runOnClient(client -> check(grid().getFocusedSlot().isEmpty(), "Returning from recipes must not restore a filtered-out ingredient"));
		open(context, "planks");
		focus(context);
		context.runOnClient(client -> client.gui.setScreen(null));
		context.runOnClient(client -> check(grid().getFocusedSlot().isEmpty(), "Closing the screen must clear keyboard selection"));
	}

	private static IngredientGrid grid() {
		return new ReflectionUtil().getFieldWithClass(contents(), IngredientGrid.class).findFirst().orElseThrow();
	}

	private static void assertCreativeTextFocus(ClientGameTestContext context) {
		GameType original = context.computeOnClient(client -> client.gameMode.getPlayerMode());
		try {
			context.runOnClient(client -> {
				client.gameMode.setLocalMode(GameType.CREATIVE);
				Internal.getJeiRuntime().getIngredientFilter().setFilterText("planks");
				ImeTextInputTestUtil.openCreativeSearchWithTextInputFocused(client, search());
			});
			context.waitTicks(2);
			context.runOnClient(client -> check(client.gui.screen() instanceof CreativeModeInventoryScreen, "The creative fixture must remain open between key presses"));
			focus(context);
			context.getInput().pressKey(InputConstants.KEY_RIGHT);
			context.runOnClient(client -> check(grid().getFocusedSlot().isPresent(), "Creative search must not retain keyboard input while navigating results"));
			context.getInput().pressKey(InputConstants.KEY_F);
			context.runOnClient(client -> {
				check(search().isFocused(), "Focus Search must return from creative result navigation to search");
				ImeTextInputTestUtil.assertCreativeSearchFocusRestored(client, (CreativeModeInventoryScreen) client.gui.screen(), search());
			});
			focus(context);
			context.runOnClient(client -> {
				var screen = client.gui.screen();
				EditBox creativeSearch = new ReflectionUtil().getFieldWithClass(screen, EditBox.class).findFirst().orElseThrow();
				EditBox replacement = new EditBox(client.font, 0, 0, 80, 20, Component.literal("Replacement input"));
				screen.setFocused(replacement);
				check(replacement.isFocused() && screen.getFocused() == replacement, "Minecraft must be able to move focus to a different widget");
				check(!creativeSearch.isFocused(), "Moving focus to a different widget must not reactivate creative search");
				check(grid().getFocusedSlot().isEmpty(), "Moving focus to a different widget must clear ingredient selection");
			});
			focus(context);
			context.runOnClient(client -> {
				client.gui.setScreen(new ChatScreen("", false));
				EditBox chat = new ReflectionUtil().getFieldWithClass(client.gui.screen(), EditBox.class).findFirst().orElseThrow();
				check(chat.isFocused() && client.gui.screen().getFocused() == chat, "Screen cleanup must preserve the new chat field's focus");
				check(grid().getFocusedSlot().isEmpty(), "Screen cleanup must clear creative ingredient navigation");
			});
		} finally {
			context.runOnClient(client -> {
				client.gui.setScreen(null);
				client.gameMode.setLocalMode(original);
			});
		}
	}

	private static IngredientGridWithNavigation contents() {
		var overlay = Internal.getJeiRuntime().getIngredientListOverlay();
		return (IngredientGridWithNavigation) new ReflectionUtil().getFieldWithClass(overlay, IIngredientListOverlayContents.class).findFirst().orElseThrow();
	}

	private static GuiTextFieldFilter search() {
		return new ReflectionUtil().getFieldWithClass(Internal.getJeiRuntime().getIngredientListOverlay(), GuiTextFieldFilter.class).findFirst().orElseThrow();
	}

	private static IngredientListSlot focused() {
		return grid().getFocusedSlot().orElseThrow(() -> new AssertionError("Expected a keyboard-focused ingredient"));
	}

	private static <T> void set(List<Runnable> cleanup, IConfigValue<T> config, T value) {
		T original = config.get();
		cleanup.add(() -> config.set(original));
		config.set(value);
	}

	private static void bind(List<Runnable> cleanup, String name, int key) {
		KeyMapping mapping = Objects.requireNonNull(KeyMapping.get(name));
		cleanup.add(KeyMappingTestHelper.rebindUnmodified(mapping, InputConstants.Type.KEYBOARD.getOrCreate(key)));
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
