package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.Internal;
import mezz.jei.gui.recipes.IRecipeGuiLogic;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.gui.recipes.lookups.ILookupState;
import mezz.jei.test.client.ReflectionUtil;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Objects;

/** Exercises ingredient shortcuts on container slots whose reported coordinates may be outside the screen. */
@SuppressWarnings("UnstableApiUsage")
public class ContainerIngredientShortcutsClientTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		JUnitXmlTestReporter.runAndReport("fabric-client-gametest", getClass().getSimpleName(), () -> {
			try (TestSingleplayerContext ignored = context.worldBuilder().create()) {
				JeiFabricClientGameTestAssertions.assertJeiStartedWithSyncedRecipes(context);
				try {
					for (boolean offscreen : new boolean[]{true, false}) {
						assertShortcuts(context, offscreen);
					}
				} finally {
					context.runOnClient(client -> client.gui.setScreen(null));
				}
			}
		});
	}

	private static void assertShortcuts(ClientGameTestContext context, boolean offscreen) {
		VirtualSlotScreen screen = context.computeOnClient(client -> {
			VirtualSlotScreen result = new VirtualSlotScreen(Objects.requireNonNull(client.player), offscreen);
			client.gui.setScreen(result);
			return result;
		});
		context.waitTicks(2);
		ScreenPosition point = context.computeOnClient(client -> screen.slotPoint());
		move(context, point);
		context.runOnClient(client -> {
			var ingredient = Internal.getJeiRuntime().getScreenHelper()
				.getClickableIngredientUnderMouse(screen, point.x(), point.y()).findFirst().orElseThrow();
			check(ingredient.getTypedIngredient().getIngredient(VanillaTypes.ITEM_STACK).orElseThrow().is(Items.OAK_PLANKS),
				"The container lookup must find the virtual slot's item");
			check(ingredient.getArea().contains(point.x(), point.y()) != offscreen,
				"The fixture must report the requested slot coordinates");
		});

		for (int key : new int[]{InputConstants.KEY_R, InputConstants.KEY_U}) {
			context.getInput().pressKey(key);
			context.waitTicks(2);
			context.runOnClient(client -> {
				check(client.gui.screen() == gui(), "Key " + key + " must open recipes for the hovered container ingredient (offscreen=" + offscreen + ")");
				var logic = new ReflectionUtil().getFieldWithClass(gui(), IRecipeGuiLogic.class).findFirst().orElseThrow();
				var focuses = new ReflectionUtil().getFieldWithClass(logic, ILookupState.class).findFirst().orElseThrow()
					.getFocuses().getItemStackFocuses().toList();
				List<RecipeIngredientRole> roles;
				if (key == InputConstants.KEY_R) {
					roles = List.of(RecipeIngredientRole.OUTPUT);
				} else {
					roles = List.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.CRAFTING_STATION);
				}
				check(focuses.size() == roles.size() && focuses.stream().allMatch(focus -> focus.getTypedValue().getIngredient().is(Items.OAK_PLANKS) && roles.contains(focus.getRole())),
					"The lookup must use the container ingredient and the requested recipe roles");
				gui().onClose();
			});
			move(context, point);
		}

		var ingredient = context.computeOnClient(client -> Internal.getJeiRuntime().getIngredientManager()
			.createTypedIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.OAK_PLANKS), false).orElseThrow());
		boolean bookmarked = context.computeOnClient(client -> Internal.getJeiRuntime().getBookmarkManager().contains(ingredient));
		var bookmarkKey = context.computeOnClient(client -> InputConstants.getKey(Objects.requireNonNull(KeyMapping.get("key.jei.bookmark")).saveString()));
		try {
			context.runOnClient(client -> Internal.getJeiRuntime().getBookmarkManager().remove(ingredient));
			context.getInput().pressKey(bookmarkKey.getValue());
			context.runOnClient(client -> check(Internal.getJeiRuntime().getBookmarkManager().contains(ingredient),
				"The bookmark shortcut must add the hovered container ingredient"));
		} finally {
			context.runOnClient(client -> {
				if (bookmarked) {
					Internal.getJeiRuntime().getBookmarkManager().add(ingredient);
				} else {
					Internal.getJeiRuntime().getBookmarkManager().remove(ingredient);
				}
			});
		}
	}

	private static void move(ClientGameTestContext context, ScreenPosition point) {
		double scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
		context.getInput().setCursorPos(point.x() * scale, point.y() * scale);
		context.waitTicks(2);
	}

	private static RecipesGui gui() {
		return (RecipesGui) Internal.getJeiRuntime().getRecipesGui();
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static final class VirtualSlotScreen extends InventoryScreen {
		private final Slot virtualSlot;

		private VirtualSlotScreen(LocalPlayer player, boolean offscreen) {
			super(player);
			// Storage mods can expose a hovered item through a slot that is not part of the menu layout.
			int slotX = 8;
			int slotY = 18;
			if (offscreen) {
				slotX = Integer.MIN_VALUE;
				slotY = Integer.MIN_VALUE;
			}
			virtualSlot = new Slot(new SimpleContainer(new ItemStack(Items.OAK_PLANKS)), 0, slotX, slotY);
		}

		private ScreenPosition slotPoint() {
			return new ScreenPosition(leftPos + 16, topPos + 26);
		}

		@Override
		public void extractRenderState(GuiGraphicsExtractor graphics) {
			super.extractRenderState(graphics);
			graphics.item(virtualSlot.getItem(), leftPos + 8, topPos + 18);
			if (isHovering(8, 18, 16, 16, graphics.mouseX(), graphics.mouseY())) {
				hoveredSlot = virtualSlot;
			}
		}
	}
}
