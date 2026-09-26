package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.config.RecipeGuiNavigationMode;
import mezz.jei.common.gui.elements.DrawableBlank;
import mezz.jei.common.gui.elements.Scrollbar;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.gui.recipes.IRecipeGuiLogic;
import mezz.jei.gui.recipes.RecipeGuiLayouts;
import mezz.jei.gui.recipes.RecipesGui;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.mezzdev.config.api.value.IConfigValue;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Verifies scrolling through real layouts without constructing an entire category. */
@SuppressWarnings("UnstableApiUsage")
final class RecipeGuiScrollingClientTest {
	static void run(ClientGameTestContext context) {
		List<Runnable> cleanup = new ArrayList<>();
		try {
			CountingCategory<?> category = context.computeOnClient(client -> {
				var config = Internal.getClientConfigs().getClientConfig();
				set(cleanup, config.recipeGuiNavigationMode(), RecipeGuiNavigationMode.SCROLLING);
				set(cleanup, config.smoothScrollRate(), 9);
				set(cleanup, config.recipeSortingCraftableEnabled(), false);
				set(cleanup, config.recipeSortingBookmarksEnabled(), true);
				set(cleanup, config.recipeGuiWidth(), 400);
				set(cleanup, config.maxRecipeGuiHeight(), 260);
				set(cleanup, config.maxRecipeGuiColumns(), 2);
				client.gui.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
				var manager = Internal.getJeiRuntime().getRecipeManager();
				var counted = new CountingCategory<>(manager.getRecipeCategory(RecipeTypes.SMELTING));
				var recipe = manager.createRecipeLookup(RecipeTypes.SMELTING).get().findFirst().orElseThrow();
				gui().showRecipes(counted, Collections.nCopies(10_000, recipe), List.of());
				check(counted.layoutsCreated > 0 && counted.layoutsCreated < 30, "Opening must construct only visible layouts");
				return counted;
			});
			int initialCount = context.computeOnClient(client -> category.layoutsCreated);
			context.waitTicks(20);
			context.runOnClient(client -> check(category.layoutsCreated == initialCount, "Idle ticks must not construct off-screen layouts"));

			ImmutableRect2i viewport = context.computeOnClient(client -> viewport());
			int initialRecipeY = context.computeOnClient(client -> logic().getScrollState().getRecipeY(0));
			move(context, viewport.x() + 1, viewport.y() + 10);
			context.getInput().scroll(-1);
			context.waitTick();
			context.runOnClient(client -> {
				check(initialRecipeY - logic().getScrollState().getRecipeY(0) == 9, "Recipes must use the same configured pixel distance as the ingredient list");
				check(gui().getRecipeLayoutUnderMouse(viewport.x() + viewport.width() / 2.0, viewport.y() - 1).isEmpty(), "Clipped recipes must not intercept header input");
				check(gui().getIngredientUnderMouse(viewport.x() + viewport.width() / 2.0, viewport.y() - 1).findAny().isEmpty(), "Clipped ingredients must not intercept header input");
			});
			context.runOnClient(client -> Internal.getClientConfigs().getClientConfig().smoothScrollRate().set(18));
			context.getInput().scroll(-1);
			context.waitTick();
			context.runOnClient(client -> check(initialRecipeY - logic().getScrollState().getRecipeY(0) == 27, "Changing the scroll distance must apply to the next wheel event"));
			context.takeScreenshot("jei-recipes-scrolled-partial-row");

			double scrollbarX = viewport.x() + viewport.width() + 3 + Scrollbar.WIDTH / 2.0;
			move(context, scrollbarX, viewport.y() + 8);
			context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.waitTick();
			move(context, scrollbarX, viewport.y() + viewport.height() + 20);
			context.waitTick();
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.runOnClient(client -> {
				var scroll = logic().getScrollState();
				check(scroll.getScrollOffset() == 1, "Dragging the scrollbar must reach the bottom");
				check(scroll.getEndRecipeIndex() == 10_000, "The final recipe must be visible");
				check(category.layoutsCreated < 60, "Jumping to the bottom must not construct the intervening recipes: " + category.layoutsCreated);
			});
			context.takeScreenshot("jei-recipes-scrolled-bottom");

			context.runOnClient(client -> {
				var config = Internal.getClientConfigs().getClientConfig();
				config.recipeGuiWidth().set(240);
				check(logic().getRecipeGuiGrid().columns() == 1, "Narrowing must update the column count");
				check(logic().getScrollState().getFirstRecipeIndex() > 9_900, "Resizing must keep the visible recipes nearby");
				config.recipeGuiNavigationMode().set(RecipeGuiNavigationMode.PAGED);
				check(!logic().isScrolling() && !logic().getPageString().startsWith("1/"), "Switching to pagination must retain the current position");
				config.recipeGuiNavigationMode().set(RecipeGuiNavigationMode.SCROLLING);
				check(logic().getScrollState().getFirstRecipeIndex() > 9_900, "Switching back must retain the current position");
				gui().showTypes(List.of(RecipeTypes.SMELTING, RecipeTypes.CRAFTING));
				var firstCategory = logic().getSelectedRecipeCategory();
				logic().nextPage();
				check(logic().getSelectedRecipeCategory() == firstCategory, "Scrolling recipes must stay within the category");
				check(logic().nextRecipeCategory(), "Category pagination must remain available");
				check(logic().getSelectedRecipeCategory() != firstCategory, "Category pagination must change category");
				check(logic().getScrollState().getScrollOffset() == 0, "Changing categories must start at the top");
				gui().back();
				check(logic().getScrollState().getFirstRecipeIndex() > 9_900, "Back must restore the scrolled lookup");
			});
		} finally {
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.runOnClient(client -> {
				gui().onClose();
				client.gui.setScreen(null);
				cleanup.reversed().forEach(Runnable::run);
			});
		}
	}

	private static RecipesGui gui() {
		return (RecipesGui) Internal.getJeiRuntime().getRecipesGui();
	}

	private static IRecipeGuiLogic logic() {
		return new ReflectionUtil().getFieldWithClass(gui(), IRecipeGuiLogic.class).findFirst().orElseThrow();
	}

	private static ImmutableRect2i viewport() {
		RecipeGuiLayouts layouts = new ReflectionUtil().getFieldWithClass(gui(), RecipeGuiLayouts.class).findFirst().orElseThrow();
		return new ReflectionUtil().getFieldWithClass(layouts, ImmutableRect2i.class).findFirst().orElseThrow();
	}

	private static void move(ClientGameTestContext context, double x, double y) {
		double scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
		context.getInput().setCursorPos(x * scale, y * scale);
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

	private static final class CountingCategory<T> extends AbstractRecipeCategory<T> {
		private final IRecipeCategory<T> delegate;
		private int layoutsCreated;

		private CountingCategory(IRecipeCategory<T> delegate) {
			super(delegate.getRecipeType(), delegate.getTitle(), new DrawableBlank(16, 16), delegate.getWidth(), delegate.getHeight());
			this.delegate = delegate;
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
			layoutsCreated++;
			delegate.setRecipe(builder, recipe, focuses);
		}
	}
}
