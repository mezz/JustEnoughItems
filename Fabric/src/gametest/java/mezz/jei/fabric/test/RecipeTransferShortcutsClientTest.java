package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.Internal;
import mezz.jei.gui.recipes.RecipesGui;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Checks recipe-GUI transfer shortcuts through Minecraft input and server-side crafting slots. */
@SuppressWarnings("UnstableApiUsage")
final class RecipeTransferShortcutsClientTest {

	private RecipeTransferShortcutsClientTest() {}

	static void run(ClientGameTestContext context, TestServerContext server) {
		assertShortcut(context, server, "key.jei.transferRecipeGui", 1);
		assertShortcut(context, server, "key.jei.maxTransferRecipeGui", 4);
	}

	private static void assertShortcut(ClientGameTestContext context, TestServerContext server, String keyName, int expectedCount) {
		List<Runnable> cleanup = new ArrayList<>();
		try {
			server.runOnServer(minecraft -> {
				var player = minecraft.getPlayerList().getPlayers().getFirst();
				player.getInventory().clearContent();
				for (int slot = 1; slot <= 4; slot++) {
					player.inventoryMenu.getSlot(slot).set(ItemStack.EMPTY);
				}
				player.getInventory().setItem(9, new ItemStack(Items.OAK_LOG, 4));
				player.inventoryMenu.broadcastChanges();
			});
			context.waitFor(client -> client.player.getInventory().countItem(Items.OAK_LOG) == 4);
			context.runOnClient(client -> {
				var mapping = Objects.requireNonNull(KeyMapping.get(keyName));
				cleanup.add(KeyMappingTestHelper.rebindUnmodified(mapping, InputConstants.Type.KEYBOARD.getOrCreate(InputConstants.KEY_P)));
				client.gui.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
				var focus = Internal.getJeiRuntime().getJeiHelpers().getFocusFactory().createFocus(
					RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, new ItemStack(Items.OAK_PLANKS));
				gui().show(List.of(focus));
			});
			context.waitTicks(2);
			context.takeScreenshot("jei-recipe-transfer-shortcuts-" + expectedCount);

			// Keep recipe shortcuts outside ingredient slots so ingredient actions retain their input.
			move(context, context.computeOnClient(client -> findPoint(true)));
			context.getInput().pressKey(InputConstants.KEY_P);
			context.waitTicks(5);
			server.runOnServer(minecraft -> check(craftingCount(minecraft.getPlayerList().getPlayers().getFirst().inventoryMenu) == 0,
				"A recipe shortcut over an ingredient slot must not transfer ingredients"));
			context.runOnClient(client -> check(client.gui.screen() == gui(), "The ingredient-slot shortcut must leave the recipe GUI open"));

			move(context, context.computeOnClient(client -> findPoint(false)));
			context.getInput().pressKey(InputConstants.KEY_P);
			server.waitFor(minecraft -> craftingCount(minecraft.getPlayerList().getPlayers().getFirst().inventoryMenu) == expectedCount);
			context.waitFor(client -> craftingCount(client.player.inventoryMenu) == expectedCount && client.gui.screen() instanceof InventoryScreen);
			context.runOnClient(client -> check(client.player.getInventory().countItem(Items.OAK_LOG) == 4 - expectedCount,
				"Recipe transfer must move the requested number of ingredients out of the inventory"));
		} finally {
			context.runOnClient(client -> {
				gui().onClose();
				client.gui.setScreen(null);
				cleanup.reversed().forEach(Runnable::run);
			});
		}
	}

	private static int craftingCount(InventoryMenu menu) {
		int count = 0;
		for (int slot = 1; slot <= 4; slot++) {
			ItemStack stack = menu.getSlot(slot).getItem();
			if (stack.is(Items.OAK_LOG)) {
				count += stack.getCount();
			}
		}
		return count;
	}

	private static Point findPoint(boolean ingredientSlot) {
		var area = gui().getArea();
		for (int y = area.y(); y < area.y() + area.height(); y++) {
			for (int x = area.x(); x < area.x() + area.width(); x++) {
				var layout = gui().getRecipeLayouts().getRecipeLayoutUnderMouse(x, y);
				if (layout.isPresent()) {
					var recipe = layout.get().getRecipeLayout();
					if (recipe.getRect().contains(x, y) && recipe.getSlotUnderMouse(x, y).isPresent() == ingredientSlot) {
						return new Point(x, y);
					}
				}
			}
		}
		throw new AssertionError("Expected a recipe layout with ingredientSlot=" + ingredientSlot);
	}

	private static void move(ClientGameTestContext context, Point point) {
		int scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
		context.getInput().setCursorPos(point.x() * scale, point.y() * scale);
		context.waitTick();
	}

	private static RecipesGui gui() {
		return (RecipesGui) Internal.getJeiRuntime().getRecipesGui();
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private record Point(int x, int y) {}
}
