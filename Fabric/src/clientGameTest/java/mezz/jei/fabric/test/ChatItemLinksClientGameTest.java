package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.Internal;
import mezz.jei.common.chat.JeiChatItemLinkHover;
import mezz.jei.common.chat.JeiChatItemLinks;
import mezz.jei.gui.recipes.IRecipeGuiLogic;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.gui.recipes.lookups.ILookupState;
import mezz.jei.gui.util.GuiEventListenerUtil;
import mezz.jei.test.client.ReflectionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.server.MinecraftServer;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicInteger;

/** Exercises native chat actions and ingredient shortcuts through real input callbacks. */
final class ChatItemLinksClientGameTest {
	static FabricClientTestRunner.ClientTestCase getTestCase() {
		return new FabricClientTestRunner.ClientTestCase("fabric-client-chat", "itemLinks", () -> {
			try (FabricClientTestWorld ignored = FabricClientTestWorld.create()) {
				FabricClientTestContext context = new FabricClientTestContext();
				context.waitFor(client -> Internal.getOptionalJeiRuntime().isPresent() && Internal.hasClientSyncedRecipes());
				run(context);
			}
		});
	}

	private static void run(FabricClientTestContext context) {
		List<Runnable> cleanup = new ArrayList<>();
		try {
			context.runOnClient(client -> {
				bind(cleanup, "key.jei.showRecipe", InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_R));
				bind(cleanup, "key.jei.showUses", InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_U));
				bind(cleanup, "key.jei.bookmark", InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_A));
				bind(cleanup, "key.jei.showRecipe2", InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT));
				bind(cleanup, "key.jei.showUses2", InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT));
			});
			assertSuggestCommand(context);
			assertServerCommand(context);
			assertHoverOnlyShortcuts(context);
			assertJeiLink(context);
			assertCancelledClicks(context);
		} finally {
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
			context.runOnClient(client -> {
				client.setScreen(null);
				cleanup.reversed().forEach(Runnable::run);
			});
		}
	}

	private static void assertServerCommand(FabricClientTestContext context) {
		AtomicInteger commandsReceived = new AtomicInteger();
		runOnServer(minecraft -> {
			minecraft.getCommands().getDispatcher().register(Commands.literal("jei4599")
				.executes(source -> commandsReceived.incrementAndGet()));
			minecraft.getCommands().sendCommands(minecraft.getPlayerList().getPlayers().getFirst());
		});
		context.waitTicks(5);
		Style style = itemStyle(Items.MACE).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/jei4599"));
		// Send the component over the server connection, as a server plugin would.
		runOnServer(minecraft -> minecraft.getPlayerList().broadcastSystemMessage(Component.literal("Server item link").setStyle(style), false));
		context.waitTicks(5);
		context.runOnClient(client -> client.setScreen(new ChatScreen("")));
		move(context, findPoint(context, style));
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitFor(client -> commandsReceived.get() == 1);
		runOnServer(minecraft -> check(commandsReceived.get() == 1,
			"A show_item hover must not prevent the server's run_command click event"));
	}

	private static void assertSuggestCommand(FabricClientTestContext context) {
		Style style = itemStyle(Items.OAK_PLANKS).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/help"));
		show(context, Component.literal("Suggest command").setStyle(style));
		move(context, findPoint(context, style));
		context.runOnClient(client -> {
			double x = client.mouseHandler.xpos() * client.getWindow().getGuiScaledWidth() / client.getWindow().getScreenWidth();
			double y = client.mouseHandler.ypos() * client.getWindow().getGuiScaledHeight() / client.getWindow().getScreenHeight();
			check(JeiChatItemLinkHover.getHoveredStyle(client.screen, x, y).filter(style::equals).isPresent(),
				"The cursor must point at the native chat action: " + x + ", " + y);
		});
		click(context);
		context.runOnClient(client -> {
			var values = GuiEventListenerUtil.getEditBoxes(Objects.requireNonNull(client.screen)).map(box -> box.getValue()).toList();
			check(values.contains("/help"), "Native suggest_command actions must retain priority: " + client.screen.getClass().getName() + " " + values);
		});
		// Explicit click events must not disable JEI's keyboard shortcuts.
		context.getInput().pressKey(InputConstants.KEY_R);
		assertLookupAndClose(context, RecipeIngredientRole.OUTPUT);
	}

	private static void assertHoverOnlyShortcuts(FabricClientTestContext context) {
		Style style = itemStyle(Items.OAK_PLANKS);
		show(context, Component.literal("Hover-only item").setStyle(style));
		ScreenPosition point = findPoint(context, style);
		move(context, point);
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		move(context, new ScreenPosition(point.x() + 2, point.y()));
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		assertLookupAndClose(context, RecipeIngredientRole.OUTPUT);
		move(context, point);
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
		assertLookupAndClose(context, RecipeIngredientRole.INPUT);
		for (int key : new int[]{InputConstants.KEY_R, InputConstants.KEY_U}) {
			move(context, point);
			context.getInput().pressKey(key);
			RecipeIngredientRole role;
			if (key == InputConstants.KEY_R) {
				role = RecipeIngredientRole.OUTPUT;
			} else {
				role = RecipeIngredientRole.INPUT;
			}
			assertLookupAndClose(context, role);
		}
		var ingredient = context.computeOnClient(client -> Internal.getJeiRuntime().getIngredientManager()
			.createTypedIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.OAK_PLANKS), false).orElseThrow());
		boolean original = context.computeOnClient(client -> Internal.getJeiRuntime().getBookmarkManager().contains(ingredient));
		try {
			context.runOnClient(client -> Internal.getJeiRuntime().getBookmarkManager().remove(ingredient));
			move(context, point);
			context.getInput().pressKey(InputConstants.KEY_A);
			context.runOnClient(client -> check(Internal.getJeiRuntime().getBookmarkManager().contains(ingredient),
				"The bookmark shortcut must work on chat item hovers"));
		} finally {
			context.runOnClient(client -> {
				var bookmarks = Internal.getJeiRuntime().getBookmarkManager();
				if (original) {
					bookmarks.add(ingredient);
				} else {
					bookmarks.remove(ingredient);
				}
			});
		}
	}

	private static void assertJeiLink(FabricClientTestContext context) {
		Component link = context.computeOnClient(client -> {
			var manager = Internal.getJeiRuntime().getIngredientManager();
			var ingredient = manager.createTypedIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.OAK_PLANKS), false).orElseThrow();
			return JeiChatItemLinks.parse(JeiChatItemLinks.createLinkMarker(ingredient, manager)).getSiblings().getFirst();
		});
		show(context, link);
		move(context, findPoint(context, link.getStyle()));
		click(context);
		assertLookupAndClose(context, RecipeIngredientRole.OUTPUT);
	}

	private static void assertCancelledClicks(FabricClientTestContext context) {
		Style first = itemStyle(Items.OAK_PLANKS).withColor(ChatFormatting.RED);
		Style second = itemStyle(Items.OAK_PLANKS).withColor(ChatFormatting.GREEN);
		show(context, Component.literal("First item").setStyle(first), Component.literal("Second item").setStyle(second));
		move(context, findPoint(context, first));
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		move(context, findPoint(context, second));
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		assertChatOpen(context, "Releasing on another link for the same item must cancel the recipe click");
		move(context, findPoint(context, first));
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		move(context, new ScreenPosition(300, 20));
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		assertChatOpen(context, "Releasing outside chat text must cancel the recipe click");

		show(context, Component.literal("Changing item").setStyle(first));
		move(context, findPoint(context, first));
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.runOnClient(client -> {
			client.gui.getChat().clearMessages(false);
			client.gui.getChat().addMessage(Component.literal("Changing item").setStyle(itemStyle(Items.STICK).withColor(ChatFormatting.RED)));
		});
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
		assertChatOpen(context, "Replacing the ingredient at the same position must cancel the recipe click");
	}

	private static Style itemStyle(Item item) {
		return Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(new ItemStack(item))));
	}

	private static void show(FabricClientTestContext context, Component... messages) {
		context.runOnClient(client -> {
			client.gui.getChat().clearMessages(false);
			for (Component message : messages) {
				client.gui.getChat().addMessage(message);
			}
			client.setScreen(new ChatScreen(""));
		});
		context.waitTicks(2);
	}

	private static ScreenPosition findPoint(FabricClientTestContext context, Style style) {
		return context.computeOnClient(client -> {
			for (int y = client.getWindow().getGuiScaledHeight() - 35; y > 20; y--) {
				var text = JeiChatItemLinkHover.getHoveredText(client.screen, 8, y);
				if (text.isPresent() && text.get().style().equals(style)) {
					var area = text.get().area();
					return new ScreenPosition(area.getX() + area.getWidth() / 2, area.getY() + area.getHeight() / 2);
				}
			}
			throw new AssertionError("Expected the chat component to be visible: " + style);
		});
	}

	private static void move(FabricClientTestContext context, ScreenPosition point) {
		var window = context.computeOnClient(Minecraft::getWindow);
		double x = (point.x() + 0.5) * window.getWidth() / window.getGuiScaledWidth();
		double y = (point.y() + 0.5) * window.getHeight() / window.getGuiScaledHeight();
		context.getInput().setCursorPos(x, y);
		context.waitTicks(2);
	}

	private static void click(FabricClientTestContext context) {
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
	}

	private static void assertLookupAndClose(FabricClientTestContext context, RecipeIngredientRole role) {
		context.runOnClient(client -> {
			RecipesGui gui = (RecipesGui) Internal.getJeiRuntime().getRecipesGui();
			check(client.screen == gui, "The chat shortcut must open recipes");
			var logic = new ReflectionUtil().getFieldWithClass(gui, IRecipeGuiLogic.class).findFirst().orElseThrow();
			var focuses = new ReflectionUtil().getFieldWithClass(logic, ILookupState.class).findFirst().orElseThrow()
				.getFocuses().getItemStackFocuses().toList();
			check(focuses.stream().anyMatch(focus -> focus.getRole() == role && focus.getTypedValue().getIngredient().is(Items.OAK_PLANKS)),
				"The lookup must use the linked item and requested role");
			gui.onClose();
		});
		assertChatOpen(context, "Closing recipes must return to chat");
	}

	private static void assertChatOpen(FabricClientTestContext context, String message) {
		context.runOnClient(client -> check(client.screen instanceof ChatScreen, message));
	}

	private static void bind(List<Runnable> cleanup, String name, InputConstants.Key key) {
		cleanup.add(KeyMappingTestHelper.rebindUnmodified(Objects.requireNonNull(KeyMappingTestHelper.find(name)), key));
	}

	private static void runOnServer(Consumer<MinecraftServer> action) {
		MinecraftServer server = Objects.requireNonNull(ClientTestUtil.computeOnClient(Minecraft::getSingleplayerServer));
		server.executeBlocking(() -> action.accept(server));
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
