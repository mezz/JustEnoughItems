package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.Internal;
import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.fabric.input.FabricKeyMapping;
import mezz.jei.gui.elements.IconButton;
import mezz.jei.gui.overlay.IngredientListOverlay;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/** Verifies both cheat shortcuts after rebinding them in the inventory. */
@SuppressWarnings("UnstableApiUsage")
public class CheatModeKeyClientTest {
	static void run() {
		run(new FabricClientTestContext());
	}

	private static void run(FabricClientTestContext context) {
		KeyMapping global = context.computeOnClient(client -> java.util.Arrays.stream(client.options.keyMappings).filter(key -> key.getName().equals("key.jei.toggleCheatMode")).findFirst().orElseThrow());
		KeyMapping hovered = context.computeOnClient(client -> java.util.Arrays.stream(client.options.keyMappings).filter(key -> key.getName().equals("key.jei.toggleCheatModeConfigButton")).findFirst().orElseThrow());
		InputConstants.Key originalGlobal = context.computeOnClient(client -> boundKey(global));
		InputConstants.Key originalHovered = context.computeOnClient(client -> boundKey(hovered));
		boolean originalCheat = context.computeOnClient(client -> Internal.getWorldConfig().isCheatItemsEnabled());
		boolean originalOverlay = context.computeOnClient(client -> Internal.getWorldConfig().isOverlayEnabled());
		try {
			context.runOnClient(client -> {
				Internal.getWorldConfig().setOverlayEnabled(true);
				client.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
			});
			context.waitTick();
			IconButton button = context.computeOnClient(client -> {
				var overlay = (IngredientListOverlay) Internal.getJeiRuntime().getIngredientListOverlay();
				check(overlay.isListDisplayed(), "The ingredient list must be visible in the inventory");
				return new ReflectionUtil().getFieldWithClass(overlay, IconButton.class).findFirst().orElseThrow();
			});
			for (int key : List.of(InputConstants.KEY_LCONTROL, InputConstants.KEY_N)) {
				bind(context, hovered, InputConstants.UNKNOWN);
				bind(context, global, InputConstants.Type.KEYSYM.getOrCreate(key));
				move(context, 0, 0);
				assertShortcut(context, () -> context.getInput().pressKey(key), true,
					"The global cheat shortcut must work away from the config button");
				move(context, button.getX() + 1, button.getY() + 1);
				assertShortcut(context, () -> context.getInput().pressKey(key), true,
					"The global cheat shortcut must also work over the config button");

				bind(context, global, InputConstants.UNKNOWN);
				bind(context, hovered, InputConstants.Type.KEYSYM.getOrCreate(key));
				move(context, 0, 0);
				assertShortcut(context, () -> context.getInput().pressKey(key), false,
					"The config-button shortcut must require hovering over that button");
				move(context, button.getX() + 1, button.getY() + 1);
				assertShortcut(context, () -> context.getInput().pressKey(key), true,
					"The rebound config-button shortcut must toggle cheat mode exactly once");
			}

			bind(context, hovered, InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT));
			assertShortcut(context, () -> {
				context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
				context.runOnClient(client -> check(Internal.getWorldConfig().isCheatItemsEnabled(),
					"A mouse shortcut must wait for release"));
				context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
			}, true, "The config-button shortcut must accept a rebound mouse button");
			assertShortcut(context, () -> {
				context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_RIGHT);
				move(context, 0, 0);
				context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
			}, false, "Releasing outside the config button must cancel the mouse shortcut");
		} finally {
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_RIGHT);
			context.getInput().releaseKey(InputConstants.KEY_LCONTROL);
			context.getInput().releaseKey(InputConstants.KEY_N);
			context.runOnClient(client -> {
				global.setKey(originalGlobal);
				hovered.setKey(originalHovered);
				KeyMapping.resetMapping();
				Internal.getWorldConfig().setCheatItemsEnabled(originalCheat);
				Internal.getWorldConfig().setOverlayEnabled(originalOverlay);
				client.setScreen(null);
			});
		}
	}

	private static void assertShortcut(FabricClientTestContext context, Runnable input, boolean toggles, String message) {
		AtomicInteger changes = new AtomicInteger();
		Runnable removeListener = context.computeOnClient(client -> {
			// Disable an already-enabled cheat mode so server permission cannot affect this input test.
			var state = Internal.getWorldConfig();
			state.setCheatItemsEnabled(true);
			return state.addCheatItemsEnabledListener(enabled -> changes.incrementAndGet());
		});
		try {
			input.run();
			context.runOnClient(client -> {
				check(Internal.getWorldConfig().isCheatItemsEnabled() != toggles, message);
				int expectedChanges = 0;
				if (toggles) {
					expectedChanges = 1;
				}
				check(changes.get() == expectedChanges, message);
				check(client.screen instanceof InventoryScreen, "The cheat shortcut must keep the inventory open");
			});
		} finally {
			context.runOnClient(client -> {
				removeListener.run();
				Internal.getWorldConfig().setCheatItemsEnabled(false);
			});
		}
	}

	private static void bind(FabricClientTestContext context, KeyMapping mapping, InputConstants.Key key) {
		context.runOnClient(client -> {
			mapping.setKey(InputConstants.UNKNOWN);
			mapping.setKey(key);
			KeyMapping.resetMapping();
		});
	}

	private static InputConstants.Key boundKey(KeyMapping mapping) {
		if (mapping instanceof FabricKeyMapping fabricMapping) {
			return InputConstants.getKey(fabricMapping.saveString());
		}
		return KeyBindingHelper.getBoundKeyOf(mapping);
	}

	private static void move(FabricClientTestContext context, int x, int y) {
		double scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
		context.getInput().setCursorPos(x * scale, y * scale);
		context.waitTick();
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
