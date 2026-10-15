package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.Internal;
import mezz.jei.common.config.GiveMode;
import mezz.jei.fabric.input.FabricKeyMapping;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Checks inventory-give shortcuts through Minecraft input and the integrated server. */
@SuppressWarnings("UnstableApiUsage")
public class CheatToInventoryClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		JUnitXmlTestReporter.runAndReport("fabric-client-gametest", getClass().getSimpleName(), () -> {
			try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(settings -> settings.setAllowCommands(true))
				.create()
			) {
				JeiFabricClientGameTestAssertions.assertJeiStartedWithSyncedRecipes(context);
				JeiFabricClientGameTestAssertions.assertServerHasJei(context);
				assertShortcut(context, singleplayer.getServer(), "key.jei.cheatOneItemToInventory", 1);
				assertShortcut(context, singleplayer.getServer(), "key.jei.cheatItemStackToInventory", 64);
			}
		});
	}

	private static void assertShortcut(ClientGameTestContext context, TestServerContext server, String keyName, int stickCount) {
		List<Runnable> cleanup = new ArrayList<>();
		try {
			context.runOnClient(client -> {
				var mapping = (FabricKeyMapping) Objects.requireNonNull(KeyMapping.get(keyName));
				check(mapping.isUnbound(), "Modified shortcuts must default to unbound on Fabric without modifier support");
				var originalKey = net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.getBoundKeyOf(mapping);
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
				client.gui.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
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
				check(client.gui.screen() == gui(), "Giving an ingredient must leave the recipe GUI open");
				check(Internal.getClientConfigs().getClientConfig().giveMode().get() == GiveMode.MOUSE_PICKUP,
					"The shortcut must not change the configured give mode");
			});

			Point inventoryItem = context.computeOnClient(client -> {
				var screen = new InventoryScreen(Objects.requireNonNull(client.player));
				client.gui.setScreen(screen);
				var properties = Internal.getJeiRuntime().getScreenHelper().getGuiProperties(screen).orElseThrow();
				var slot = screen.getMenu().slots.stream().filter(s -> s.getItem().is(Items.STICK)).findFirst().orElseThrow();
				return new Point(properties.guiLeft() + slot.x + 8, properties.guiTop() + slot.y + 8);
			});
			move(context, inventoryItem);
			giveAndCheck(context, server, Items.STICK, stickCount);

			context.runOnClient(client -> Internal.getClientToggleState().setCheatItemsEnabled(false));
			int before = context.computeOnClient(client -> client.player.getInventory().countItem(Items.STICK));
			context.getInput().pressKey(InputConstants.KEY_P);
			context.waitTicks(5);
			server.runOnServer(minecraft -> check(minecraft.getPlayerList().getPlayers().getFirst().getInventory().countItem(Items.STICK) == before,
				"Disabled cheat mode must not give items on the server"));
			context.runOnClient(client -> check(client.player.getInventory().countItem(Items.STICK) == before,
				"The shortcut must respect disabled cheat mode"));
		} finally {
			context.runOnClient(client -> {
				try {
					gui().onClose();
					client.gui.setScreen(null);
				} finally {
					cleanup.reversed().forEach(Runnable::run);
				}
			});
		}
	}

	private static void giveAndCheck(ClientGameTestContext context, TestServerContext server, Item item, int amount) {
		int before = context.computeOnClient(client -> client.player.getInventory().countItem(item));
		int expected = before + amount;
		context.getInput().pressKey(InputConstants.KEY_P);
		server.waitFor(minecraft -> minecraft.getPlayerList().getPlayers().getFirst().getInventory().countItem(item) == expected);
		context.waitFor(client -> client.player.getInventory().countItem(item) == expected);
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
