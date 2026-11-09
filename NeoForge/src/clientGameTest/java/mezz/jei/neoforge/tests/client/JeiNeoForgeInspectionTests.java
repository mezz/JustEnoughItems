package mezz.jei.neoforge.tests.client;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.time.Duration;
import java.util.List;

final class JeiNeoForgeInspectionTests {
	private static final Duration TIMEOUT = Duration.ofSeconds(60);

	private JeiNeoForgeInspectionTests() {
	}

	static void run() {
		ClientTestUtil.runOnClient(client -> {
			client.setScreen(new InventoryScreen(client.player));
		});
		waitForOverlay();
		ClientTestUtil.runOnClient(client -> {
			IJeiRuntime runtime = InspectionTestPlugin.runtime;
			IRecipesGui gui = runtime.getRecipesGui();
			var focus = runtime.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.INPUT, VanillaTypes.ITEM_STACK, new ItemStack(Items.OAK_LOG));
			gui.show(focus);
			check(gui.isOpen() && gui.getFocuses().size() == 1, "Current recipe focuses");
			check(gui.setRecipeCategory(InspectionTestPlugin.TYPE), "Select fixture category among current categories");
			check(gui.getSelectedRecipeCategory().orElseThrow().getRecipeType().equals(InspectionTestPlugin.TYPE), "Selected category");
			check(!gui.setRecipeCategory(RecipeType.create("jeiclienttests", "missing", Object.class)), "Unavailable category must fail");
			check(gui.getPageIndex() == 0 && gui.getPageCount() > 1, "Numeric recipe pagination");
			firstLayout(gui);
			client.setScreen(new InventoryScreen(client.player));
			check(gui.getFocuses().isEmpty() && gui.getRecipeCategories().isEmpty() && gui.getSelectedRecipeCategory().isEmpty() && gui.getPageCount() == 0 && gui.getPageIndex() == -1 && gui.getVisibleRecipeLayouts().isEmpty(), "Closed view must not expose stale state");
			gui.showTypes(List.of(InspectionTestPlugin.TYPE));
			check(gui.getFocuses().isEmpty(), "Category view has no focus");
			client.setScreen(null);
		});
	}

	private static IRecipeLayoutDrawable<?> firstLayout(IRecipesGui gui) {
		check(!gui.getVisibleRecipeLayouts().isEmpty(), "Expected visible recipe layouts");
		return gui.getVisibleRecipeLayouts().getFirst();
	}

	private static void waitForOverlay() {
		ClientTestUtil.waitUntil(() -> ClientTestUtil.computeOnClient(client -> InspectionTestPlugin.runtime.getIngredientListOverlay().isListDisplayed()),
			TIMEOUT, () -> "Expected ingredient overlay on inventory screen");
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
