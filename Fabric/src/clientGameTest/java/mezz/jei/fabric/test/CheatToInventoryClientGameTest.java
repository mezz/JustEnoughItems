package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.Internal;
import mezz.jei.common.config.GiveMode;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.server.IntegratedServer;
import mezz.jei.fabric.input.FabricAmecsSupport;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Checks inventory-give shortcuts through Minecraft input and the integrated server. */
@SuppressWarnings("UnstableApiUsage")
final class CheatToInventoryClientGameTest {
	static void run() {
		FabricClientTestContext context = new FabricClientTestContext();
		IntegratedServer server = context.computeOnClient(client -> Objects.requireNonNull(client.getSingleplayerServer()));
		assertShortcut(context, server, "key.jei.cheatOneItemToInventory", 1);
		assertShortcut(context, server, "key.jei.cheatItemStackToInventory", 64);
	}

	private static void assertShortcut(FabricClientTestContext context, IntegratedServer server, String keyName, int stickCount) {
		List<Runnable> cleanup = new ArrayList<>();
		try {
			context.runOnClient(client -> {
				KeyMapping mapping = java.util.Arrays.stream(client.options.keyMappings).filter(key -> key.getName().equals(keyName)).findFirst().orElseThrow();
				if (FabricAmecsSupport.isEnabled()) {
					cleanup.add(AmecsKeyMappingClientTestHelper.clearModifiers(mapping));
				}
				var originalKey = net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.getBoundKeyOf(mapping);
				cleanup.add(() -> mapping.setKey(originalKey));
				mapping.setKey(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_P));
				var toggles = Internal.getClientToggleState();
				boolean originalCheatMode = toggles.isCheatItemsEnabled();
				cleanup.add(() -> toggles.setCheatItemsEnabled(originalCheatMode));
				toggles.setCheatItemsEnabled(true);
				var giveMode = Internal.getClientConfigs().getClientConfig().giveMode();
				GiveMode originalGiveMode = giveMode.get();
				cleanup.add(() -> giveMode.set(originalGiveMode));
				giveMode.set(GiveMode.MOUSE_PICKUP);
				client.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
				var runtime = Internal.getJeiRuntime();
				var focus = runtime.getJeiHelpers().getFocusFactory().createFocus(
					RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, new ItemStack(Items.IRON_PICKAXE));
				gui().show(List.of(focus));
			});
			context.waitTicks(2);
			move(context, context.computeOnClient(client -> findRecipeIngredient(Items.STICK)));
			giveAndCheck(context, server, Items.STICK, stickCount);
			move(context, context.computeOnClient(client -> findRecipeIngredient(Items.IRON_PICKAXE)));
			giveAndCheck(context, server, Items.IRON_PICKAXE, 1);
			context.runOnClient(client -> {
				check(client.screen == gui(), "Giving an ingredient must leave the recipe GUI open");
				check(Internal.getClientConfigs().getClientConfig().giveMode().get() == GiveMode.MOUSE_PICKUP,
					"The shortcut must not change the configured give mode");
			});

			Point inventoryItem = context.computeOnClient(client -> {
				var screen = new InventoryScreen(Objects.requireNonNull(client.player));
				client.setScreen(screen);
				var properties = Internal.getJeiRuntime().getScreenHelper().getGuiProperties(screen).orElseThrow();
				var slot = screen.getMenu().slots.stream().filter(s -> s.getItem().is(Items.STICK)).findFirst().orElseThrow();
				return new Point(properties.getGuiLeft() + slot.x + 8, properties.getGuiTop() + slot.y + 8);
			});
			move(context, inventoryItem);
			giveAndCheck(context, server, Items.STICK, stickCount);

			context.runOnClient(client -> Internal.getClientToggleState().setCheatItemsEnabled(false));
			int before = context.computeOnClient(client -> client.player.getInventory().countItem(Items.STICK));
			context.getInput().pressKey(InputConstants.KEY_P);
			context.waitTicks(5);
			server.executeBlocking(() -> check(server.getPlayerList().getPlayers().get(0).getInventory().countItem(Items.STICK) == before,
				"Disabled cheat mode must not give items on the server"));
			context.runOnClient(client -> check(client.player.getInventory().countItem(Items.STICK) == before,
				"The shortcut must respect disabled cheat mode"));
		} finally {
			context.runOnClient(client -> {
				try {
					gui().onClose();
					client.setScreen(null);
				} finally {
					for (int i = cleanup.size() - 1; i >= 0; i--) {
						cleanup.get(i).run();
					}
				}
			});
		}
	}

	private static void giveAndCheck(FabricClientTestContext context, IntegratedServer server, Item item, int amount) {
		int before = context.computeOnClient(client -> client.player.getInventory().countItem(item));
		int expected = before + amount;
		context.getInput().pressKey(InputConstants.KEY_P);
		context.waitFor(client -> client.player.getInventory().countItem(item) == expected);
		server.executeBlocking(() -> check(server.getPlayerList().getPlayers().get(0).getInventory().countItem(item) == expected,
			"The shortcut must give the expected amount on the server"));
		context.runOnClient(client -> check(client.player.containerMenu.getCarried().isEmpty(),
			"The inventory-give shortcut must leave the cursor empty"));
	}

	private static Point findRecipeIngredient(Item item) {
		var area = gui().getArea();
		for (int y = area.y(); y < area.y() + area.height(); y++) {
			for (int x = area.x(); x < area.x() + area.width(); x++) {
				boolean matches = gui().getIngredientUnderMouse(x, y)
					.anyMatch(clicked -> clicked.getTypedIngredient().getItemStack().filter(stack -> stack.is(item)).isPresent());
				if (matches) {
					return new Point(x, y);
				}
			}
		}
		throw new AssertionError("Expected a recipe slot containing " + item);
	}

	private static void move(FabricClientTestContext context, Point point) {
		double scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
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
