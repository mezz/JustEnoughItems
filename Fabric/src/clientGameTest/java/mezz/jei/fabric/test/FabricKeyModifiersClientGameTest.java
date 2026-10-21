package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.Internal;
import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.fabric.input.FabricKeyMapping;
import mezz.jei.gui.elements.IconButton;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.fabric.test.mixin.MouseHandlerAccessor;
import mezz.jei.fabric.test.mixin.KeyboardHandlerAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

/** Checks JEI modifier bindings with the active Fabric input backend. */
@SuppressWarnings("UnstableApiUsage")
public class FabricKeyModifiersClientGameTest {
	private static final boolean AMECS_LOADED = FabricLoader.getInstance().isModLoaded("amecs_key_modifiers");

	static void run() {
		FabricClientTestContext context = new FabricClientTestContext();
		if (AMECS_LOADED) {
			AmecsKeyModifiersClientGameTestHelper.assertControlsAndPersistence(context);
		} else {
			assertControlsInput(context);
			context.runOnClient(FabricKeyModifiersClientGameTest::assertControlsAndPersistence);
		}
		try (FabricClientTestWorld ignored = FabricClientTestWorld.create()) {
			context.waitFor(client -> Internal.getOptionalJeiRuntime().isPresent());
			assertHeldKeyMapping(context);
			if (AMECS_LOADED) {
				AmecsKeyModifiersClientGameTestHelper.assertShortcutModifiers(context);
			} else {
				assertConfigButtonShortcut(context);
			}
		}
	}

	private static void assertControlsInput(FabricClientTestContext context) {
		FabricKeyMapping mapping = context.computeOnClient(client -> mapping("key.jei.toggleOverlay"));
		var originalKey = context.computeOnClient(client -> mapping.getBoundKey());
		int originalModifiers = context.computeOnClient(client -> mapping.getModifiers());
		var originalScreen = context.computeOnClient(client -> client.screen);
		try {
			context.runOnClient(client -> {
				KeyBindsScreen screen = new KeyBindsScreen(originalScreen, client.options);
				client.setScreen(screen);
				screen.selectedKey = mapping;
			});
			pressShiftModifiedKey(context, InputConstants.KEY_P);
			context.runOnClient(client -> {
				KeyBindsScreen screen = (KeyBindsScreen) client.screen;
				check(screen.selectedKey == null, "Keyboard callbacks must finish recording a JEI shortcut");
				check(ModifierTestInput.matches(mapping, InputConstants.KEY_P, 'p', GLFW.GLFW_MOD_SHIFT), "Keyboard callbacks must record modified JEI bindings");
				screen.selectedKey = mapping;
			});
			context.getInput().pressKey(InputConstants.KEY_LSHIFT);
			context.runOnClient(client -> {
				KeyBindsScreen screen = (KeyBindsScreen) client.screen;
				check(screen.selectedKey == null, "Releasing a standalone modifier must finish recording");
				check(mapping.getBoundKey().getValue() == InputConstants.KEY_LSHIFT && mapping.getModifiers() == 0, "Keyboard callbacks must record standalone modifier bindings");
			});
		} finally {
			context.runOnClient(client -> {
				mapping.setKeyAndModifiers(originalKey, originalModifiers);
				KeyMapping.resetMapping();
				client.setScreen(originalScreen);
			});
		}
	}

	static void pressShiftModifiedKey(FabricClientTestContext context, int key) {
		pressModifiedKey(context, key, InputConstants.KEY_LSHIFT, GLFW.GLFW_MOD_SHIFT);
	}

	static void pressAltModifiedKey(FabricClientTestContext context, int key) {
		pressModifiedKey(context, key, InputConstants.KEY_LALT, GLFW.GLFW_MOD_ALT);
	}

	private static void pressModifiedKey(FabricClientTestContext context, int key, int modifierKey, int modifiers) {
		context.getInput().holdKey(modifierKey);
		try {
			context.runOnClient(client -> {
				// Fabric's input helper omits modifier flags from keyboard callbacks.
				KeyboardHandlerAccessor keyboard = (KeyboardHandlerAccessor) client.keyboardHandler;
				keyboard.jei$invokeKeyPress(client.getWindow().getWindow(), key, GLFW.glfwGetKeyScancode(key), InputConstants.PRESS, modifiers);
				keyboard.jei$invokeKeyPress(client.getWindow().getWindow(), key, GLFW.glfwGetKeyScancode(key), InputConstants.RELEASE, modifiers);
			});
		} finally {
			context.getInput().releaseKey(modifierKey);
		}
	}

	private static void assertControlsAndPersistence(Minecraft client) {
		FabricKeyMapping mapping = mapping("key.jei.toggleOverlay");
		var originalKey = mapping.getBoundKey();
		int originalModifiers = mapping.getModifiers();
		var originalJump = client.options.keyJump.saveString();
		var originalScreen = client.screen;
		KeyBindsScreen screen = new KeyBindsScreen(originalScreen, client.options);
		client.setScreen(screen);
		try {
			assertVanillaRebinding(screen, client.options.keyJump);
			resetMapping(screen, mapping);
			check(!mapping.isUnbound() && mapping.getModifiers() == shortcutModifier(), "JEI's default modifier shortcut must be bound without AMECS");
			screen.selectedKey = mapping;
			screen.keyPressed(InputConstants.KEY_LCONTROL, 0, GLFW.GLFW_MOD_CONTROL);
			check(screen.selectedKey == mapping, "A modifier press must wait for the rest of a JEI shortcut");
			screen.keyPressed(InputConstants.KEY_P, 'p', GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SHIFT);
			check(screen.selectedKey == null, "Recording a modified keyboard shortcut must finish selection");
			check(ModifierTestInput.matches(mapping, InputConstants.KEY_P, 'p', GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SHIFT), "The recorded combination must match");
			check(!ModifierTestInput.matches(mapping, InputConstants.KEY_P, 'p', GLFW.GLFW_MOD_CONTROL), "A shortcut must require every recorded modifier");
			check(!ModifierTestInput.matches(mapping, InputConstants.KEY_P, 'p', 0), "The bare key must not activate a modified shortcut");
			check(mapping.getTranslatedKeyMessage().getString().contains("SHIFT"), "Controls must display the recorded modifiers");

			client.options.save();
			mapping.setKeyAndModifiers(InputConstants.UNKNOWN, 0);
			client.options.load();
			check(ModifierTestInput.matches(mapping, InputConstants.KEY_P, 'p', GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SHIFT), "Saving and loading options must preserve the key and its modifiers");
			resetMapping(screen, mapping);
			client.options.save();
			var optionsFile = client.gameDirectory.toPath().resolve("options.txt");
			try {
				var oldOptions = Files.readAllLines(optionsFile).stream()
					.filter(line -> !line.startsWith("jei_modifiers_" + mapping.getName() + ":"))
					.toList();
				Files.write(optionsFile, oldOptions);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			client.options.load();
			check(mapping.getModifiers() == 0, "Loading an existing unmodified binding must not add default modifiers, even when its key matches the new default");

			screen.selectedKey = mapping;
			ModifierTestInput.click(screen, InputConstants.MOUSE_BUTTON_RIGHT, GLFW.GLFW_MOD_ALT);
			check(ModifierTestInput.matchesMouse(mapping, InputConstants.MOUSE_BUTTON_RIGHT, GLFW.GLFW_MOD_ALT), "Controls must record modifiers on mouse bindings");
			check(!ModifierTestInput.matchesMouse(mapping, InputConstants.MOUSE_BUTTON_RIGHT, 0), "An unmodified mouse click must not match");
			assertVanillaMouseDispatch(client);

			screen.selectedKey = mapping;
			screen.keyPressed(InputConstants.KEY_ESCAPE, 0, 0);
			check(mapping.isUnbound() && mapping.getModifiers() == 0, "Escape must unbind the key and its modifiers");
			assertVanillaMouseDispatch(client);
			resetMapping(screen, mapping);
			check(mapping.isDefault() && mapping.getModifiers() == shortcutModifier(), "The row's Reset button must restore default modifiers");

			screen.selectedKey = mapping;
			screen.keyPressed(InputConstants.KEY_LSHIFT, 0, GLFW.GLFW_MOD_SHIFT);
			screen.keyReleased(InputConstants.KEY_LSHIFT, 0, 0);
			check(mapping.getBoundKey().getValue() == InputConstants.KEY_LSHIFT && mapping.getModifiers() == 0, "A modifier key must also be usable as a standalone binding");
			screen.children().stream().filter(e -> e instanceof Button b && b.getMessage().equals(Component.translatable("controls.resetAll")))
				.map(e -> (Button) e).findFirst().orElseThrow().onPress();
			check(mapping.isDefault(), "Reset All must restore JEI's complete default binding");
			check(client.options.keyJump.isDefault(), "Reset All must still restore vanilla bindings");
		} finally {
			mapping.setKeyAndModifiers(originalKey, originalModifiers);
			client.options.keyJump.setKey(InputConstants.getKey(originalJump));
			KeyMapping.resetMapping();
			client.setScreen(originalScreen);
			client.options.save();
		}
	}

	private static void assertVanillaMouseDispatch(Minecraft client) {
		assertVanillaMouseClick(client.options.keyAttack, InputConstants.MOUSE_BUTTON_LEFT);
		assertVanillaMouseClick(client.options.keyUse, InputConstants.MOUSE_BUTTON_RIGHT);
	}

	private static void assertVanillaMouseClick(KeyMapping mapping, int button) {
		while (mapping.consumeClick()) {
			// Discard clicks left over from setup.
		}
		KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(button));
		check(mapping.consumeClick(), "JEI bindings must not steal vanilla attack/use clicks");
		check(!mapping.consumeClick(), "Vanilla must receive exactly one click");
	}

	private static void assertHeldKeyMapping(FabricClientTestContext context) {
		var mapping = context.computeOnClient(client -> Internal.getKeyMappings().getPauseRecipeCycling());
		context.runOnClient(client -> client.setScreen(new InventoryScreen(Objects.requireNonNull(client.player))));
		context.getInput().holdKey(InputConstants.KEY_LSHIFT);
		try {
			context.runOnClient(client -> {
				check(mapping.isDown(), "A standalone modifier binding must detect its held key");
				client.setScreen(null);
				check(!mapping.isDown(), "A held binding must respect its GUI context");
			});
		} finally {
			context.getInput().releaseKey(InputConstants.KEY_LSHIFT);
		}
		context.runOnClient(client -> {
			client.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
			check(!mapping.isDown(), "A modifier binding must detect key release");
			client.setScreen(null);
		});
	}

	private static void assertVanillaRebinding(KeyBindsScreen screen, KeyMapping mapping) {
		String originalKey = mapping.saveString();
		try {
			screen.selectedKey = mapping;
			screen.keyPressed(InputConstants.KEY_LCONTROL, 0, GLFW.GLFW_MOD_CONTROL);
			check(screen.selectedKey == null, "Vanilla bindings must still finish selection on a modifier press");
			check(ModifierTestInput.matches(mapping, InputConstants.KEY_LCONTROL, 0, 0), "Vanilla bindings must continue matching without JEI's modifier rules");

			screen.selectedKey = mapping;
			ModifierTestInput.click(screen, InputConstants.MOUSE_BUTTON_RIGHT, GLFW.GLFW_MOD_ALT);
			check(screen.selectedKey == null, "Vanilla mouse bindings must still finish selection");
			check(ModifierTestInput.matchesMouse(mapping, InputConstants.MOUSE_BUTTON_RIGHT, 0), "Editing a vanilla mouse binding must not add JEI modifiers");
			resetMapping(screen, mapping);
			check(mapping.isDefault(), "The row's Reset button must still reset vanilla bindings");
		} finally {
			mapping.setKey(InputConstants.getKey(originalKey));
			KeyMapping.resetMapping();
		}
	}

	static void resetMapping(KeyBindsScreen screen, KeyMapping mapping) {
		pressMappingButton(screen, mapping, 1);
	}

	static void editMapping(KeyBindsScreen screen, KeyMapping mapping) {
		pressMappingButton(screen, mapping, 0);
	}

	private static void pressMappingButton(KeyBindsScreen screen, KeyMapping mapping, int buttonIndex) {
		KeyBindsList list = new ReflectionUtil().getFieldWithClass(screen, KeyBindsList.class).findFirst().orElseThrow();
		var entry = list.children().stream()
			.filter(e -> e instanceof KeyBindsList.KeyEntry && new ReflectionUtil().getFieldWithClass(e, KeyMapping.class).anyMatch(k -> k == mapping))
			.findFirst().orElseThrow();
		((Button) entry.children().get(buttonIndex)).onPress();
	}

	private static void assertConfigButtonShortcut(FabricClientTestContext context) {
		FabricKeyMapping shortcut = mapping("key.jei.toggleCheatModeConfigButton");
		var originalKey = shortcut.getBoundKey();
		int originalModifiers = shortcut.getModifiers();
		boolean originalCheatMode = context.computeOnClient(client -> Internal.getClientToggleState().isCheatItemsEnabled());
		int modifierKey = InputConstants.KEY_LCONTROL;
		if (Minecraft.ON_OSX) {
			modifierKey = org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SUPER;
		}
		try {
			context.runOnClient(client -> {
				shortcut.setKey(shortcut.getDefaultKey());
				shortcut.resetModifiersToDefault();
				Internal.getClientToggleState().setOverlayEnabled(true);
				Internal.getClientToggleState().setCheatItemsEnabled(false);
				client.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
			});
			context.waitTick();
			var area = context.computeOnClient(client -> {
				var overlay = (IngredientListOverlay) Internal.getJeiRuntime().getIngredientListOverlay();
				return new ReflectionUtil().getFieldWithClass(overlay, IconButton.class).findFirst().orElseThrow().getArea();
			});
			double scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
			context.getInput().setCursorPos((area.x() + area.width() / 2.0) * scale, (area.y() + area.height() / 2.0) * scale);
			context.getInput().holdKey(modifierKey);
			clickConfigButton(context);
			context.runOnClient(client -> check(Internal.getClientToggleState().isCheatItemsEnabled(), "Ctrl/Command-click on the wrench must toggle cheat mode without AMECS"));
			context.runOnClient(client -> shortcut.setKeyAndModifiers(InputConstants.UNKNOWN, 0));
			clickConfigButton(context);
			context.runOnClient(client -> check(Internal.getClientToggleState().isCheatItemsEnabled(), "Unbinding the wrench shortcut must disable Ctrl/Command-click"));
		} finally {
			context.getInput().releaseKey(modifierKey);
			context.runOnClient(client -> {
				shortcut.setKeyAndModifiers(originalKey, originalModifiers);
				Internal.getClientToggleState().setCheatItemsEnabled(originalCheatMode);
				client.setScreen(null);
			});
		}
	}

	private static void clickConfigButton(FabricClientTestContext context) {
		context.runOnClient(client -> {
			MouseHandlerAccessor mouse = (MouseHandlerAccessor) client.mouseHandler;
			mouse.jei$invokeOnPress(client.getWindow().getWindow(), InputConstants.MOUSE_BUTTON_LEFT, InputConstants.PRESS, shortcutModifier());
			mouse.jei$invokeOnPress(client.getWindow().getWindow(), InputConstants.MOUSE_BUTTON_LEFT, InputConstants.RELEASE, shortcutModifier());
		});
		context.waitTick();
	}

	private static int shortcutModifier() {
		if (Minecraft.ON_OSX) {
			return GLFW.GLFW_MOD_SUPER;
		}
		return GLFW.GLFW_MOD_CONTROL;
	}

	private static FabricKeyMapping mapping(String name) {
		return (FabricKeyMapping) KeyMappingTestHelper.find(name);
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
