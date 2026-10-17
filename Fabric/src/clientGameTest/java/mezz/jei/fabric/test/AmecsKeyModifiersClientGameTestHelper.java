package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyMappingWithKeyModifiers;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifierCombination;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiers;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiersApi;
import mezz.jei.common.Internal;
import mezz.jei.common.input.keys.IJeiKeyMappingWithExtraModifiers;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Objects;

/** Exercises the native AMECS API, Controls editing, and AMECS settings for JEI bindings. */
@SuppressWarnings("UnstableApiUsage")
final class AmecsKeyModifiersClientGameTestHelper {
	private AmecsKeyModifiersClientGameTestHelper() {}

	static void assertControlsAndPersistence(FabricClientTestContext context) {
		KeyMapping mapping = context.computeOnClient(client -> java.util.Arrays.stream(client.options.keyMappings).filter(candidate -> candidate.getName().equals("key.jei.toggleOverlay")).findFirst().orElseThrow());
		var originalKey = context.computeOnClient(client -> InputConstants.getKey(mapping.saveString()));
		var originalModifiers = context.computeOnClient(client -> copyModifiers(mapping));
		var originalScreen = context.computeOnClient(client -> client.screen);
		var originalJump = context.computeOnClient(client -> KeyBindingHelper.getBoundKeyOf(client.options.keyJump));
		var originalJumpModifiers = context.computeOnClient(client -> copyModifiers(client.options.keyJump));
		try {
			context.runOnClient(client -> {
				check(mapping instanceof AmecsKeyMappingWithKeyModifiers, "JEI must use native AMECS mappings when AMECS is installed");
				check(originalKey.equals(mapping.getDefaultKey()), "Other mods must see JEI's real bound key");
				var defaults = AmecsKeyModifiersApi.getDefaultModifiers(mapping);
				var shortcut = AmecsKeyModifiers.CONTROL;
				if (Minecraft.ON_OSX) {
					shortcut = AmecsKeyModifiers.getByName("command");
				}
				check(shortcut != null && defaults.get(shortcut), "AMECS must expose JEI's default Ctrl/Command modifier");
				KeyBindsScreen screen = new KeyBindsScreen(originalScreen, client.options);
				client.setScreen(screen);
				FabricKeyModifiersClientGameTest.editMapping(screen, client.options.keyJump);
			});
			FabricKeyModifiersClientGameTest.pressShiftModifiedKey(context, InputConstants.KEY_V);
			context.runOnClient(client -> FabricKeyModifiersClientGameTest.editMapping((KeyBindsScreen) client.screen, mapping));
			FabricKeyModifiersClientGameTest.pressShiftModifiedKey(context, InputConstants.KEY_P);
			context.runOnClient(client -> {
				check(((KeyBindsScreen) client.screen).selectedKey == null, "AMECS must finish recording a JEI shortcut");
				check(InputConstants.getKey(mapping.saveString()).getValue() == InputConstants.KEY_P, "AMECS and Fabric must expose JEI's edited key");
				check(AmecsKeyModifiersApi.getBoundModifiers(mapping).equals(new AmecsKeyModifierCombination(AmecsKeyModifiers.SHIFT)), "AMECS must expose the edited JEI modifiers");
				client.options.save();
				var optionsFile = client.gameDirectory.toPath().resolve("options.amecs.key_modifiers.txt");
				try {
					check(Files.readString(optionsFile).contains("key_modifiers_" + mapping.getName() + ": shift"), "AMECS must save JEI's modifier combination");
					check(Files.readAllLines(client.gameDirectory.toPath().resolve("options.txt")).stream().noneMatch(line -> line.startsWith("jei_modifiers_" + mapping.getName() + ":")), "Native AMECS mappings must not also use JEI's fallback modifier settings");
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				mapping.setKey(InputConstants.UNKNOWN);
				client.options.load();
				check(InputConstants.getKey(mapping.saveString()).getValue() == InputConstants.KEY_P && AmecsKeyModifiersApi.getBoundModifiers(mapping).getShift(), "Options loading must restore native AMECS keys and modifiers");
			});
			context.getInput().holdKey(InputConstants.KEY_LSHIFT);
			try {
				context.runOnClient(client -> {
					check(Internal.getKeyMappings().getToggleOverlay().isActiveAndMatches(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_P)), "JEI must match the combination restored by AMECS");
					check(ModifierTestInput.matches(client.options.keyJump, InputConstants.KEY_V, 'v', GLFW.GLFW_MOD_SHIFT), "Editing JEI bindings must preserve ordinary AMECS bindings");
					// A Controls replacement or another mod can edit modifiers through the AMECS API.
					AmecsKeyModifiersApi.getBoundModifiers(mapping).setAlt(true);
					check(!Internal.getKeyMappings().getToggleOverlay().isActiveAndMatches(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_P)), "JEI must observe modifier changes made through the AMECS API");
				});
				context.getInput().holdKey(InputConstants.KEY_LALT);
				try {
					context.runOnClient(client -> check(Internal.getKeyMappings().getToggleOverlay().isActiveAndMatches(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_P)), "JEI must match every modifier supplied through AMECS"));
				} finally {
					context.getInput().releaseKey(InputConstants.KEY_LALT);
				}
			} finally {
				context.getInput().releaseKey(InputConstants.KEY_LSHIFT);
			}
			context.runOnClient(client -> FabricKeyModifiersClientGameTest.editMapping((KeyBindsScreen) client.screen, mapping));
			context.getInput().pressKey(InputConstants.KEY_LSHIFT);
			context.runOnClient(client -> {
				check(InputConstants.getKey(mapping.saveString()).getValue() == InputConstants.KEY_LSHIFT && AmecsKeyModifiersApi.getBoundModifiers(mapping).isUnset(), "AMECS must support standalone modifier bindings for JEI");
				FabricKeyModifiersClientGameTest.editMapping((KeyBindsScreen) client.screen, mapping);
			});
			context.getInput().holdKey(InputConstants.KEY_LALT);
			try {
				context.runOnClient(client -> {
					var screen = (KeyBindsScreen) client.screen;
					ModifierTestInput.click(screen, InputConstants.MOUSE_BUTTON_RIGHT, GLFW.GLFW_MOD_ALT);
					check(InputConstants.getKey(mapping.saveString()).equals(InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT)) && AmecsKeyModifiersApi.getBoundModifiers(mapping).getAlt(), "AMECS must record modified JEI mouse bindings");
				});
			} finally {
				context.getInput().releaseKey(InputConstants.KEY_LALT);
			}
			context.runOnClient(client -> {
				var screen = (KeyBindsScreen) client.screen;
				FabricKeyModifiersClientGameTest.editMapping(screen, mapping);
				screen.keyPressed(InputConstants.KEY_ESCAPE, 0, 0);
				check(mapping.isUnbound() && AmecsKeyModifiersApi.getBoundModifiers(mapping).isUnset(), "Unbinding through AMECS must clear JEI modifiers");
				FabricKeyModifiersClientGameTest.resetMapping(screen, mapping);
				check(mapping.isDefault() && AmecsKeyModifiersApi.getBoundModifiers(mapping).equals(AmecsKeyModifiersApi.getDefaultModifiers(mapping)), "AMECS Controls reset must restore JEI's complete default binding");
				AmecsKeyModifiersApi.getBoundModifiers(mapping).setAlt(true);
				screen.children().stream().filter(e -> e instanceof Button b && b.getMessage().equals(Component.translatable("controls.resetAll")))
					.map(e -> (Button) e).findFirst().orElseThrow().onPress();
				check(mapping.isDefault(), "Reset All must restore JEI's native AMECS modifiers");
			});
		} finally {
			context.runOnClient(client -> {
				mapping.setKey(originalKey);
				AmecsKeyModifiersApi.getBoundModifiers(mapping).copyFrom(originalModifiers);
				client.options.keyJump.setKey(originalJump);
				AmecsKeyModifiersApi.getBoundModifiers(client.options.keyJump).copyFrom(originalJumpModifiers);
				KeyMapping.resetMapping();
				client.setScreen(originalScreen);
				client.options.save();
			});
		}
	}

	static void assertShortcutModifiers(FabricClientTestContext context) {
		context.runOnClient(client -> client.setScreen(new InventoryScreen(Objects.requireNonNull(client.player))));
		context.runOnClient(client -> {
			var mapping = (IJeiKeyMappingWithExtraModifiers) Internal.getKeyMappings().getToggleCheatModeConfigButton();
			var mouse = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT);
			check(!mapping.isActiveAndMatchesAllowingExtraModifiers(mouse), "Allowing extra modifiers must still require the bound modifier");
		});
		int shortcutKey = InputConstants.KEY_LCONTROL;
		if (Minecraft.ON_OSX) {
			shortcutKey = org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SUPER;
		}
		context.getInput().holdKey(shortcutKey);
		try {
			context.runOnClient(client -> check(Internal.getKeyMappings().getToggleCheatModeConfigButton().isActiveAndMatches(InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT)), "The AMECS Ctrl/Command-click shortcut must work in a GUI"));
			context.getInput().holdKey(InputConstants.KEY_LALT);
			try {
				context.runOnClient(client -> {
					var mapping = (IJeiKeyMappingWithExtraModifiers) Internal.getKeyMappings().getToggleCheatModeConfigButton();
					var mouse = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT);
					check(!mapping.isActiveAndMatches(mouse), "An AMECS GUI shortcut must reject extra modifiers by default");
					check(mapping.isActiveAndMatchesAllowingExtraModifiers(mouse), "JEI must support extra modifiers with AMECS when requested");
					client.setScreen(null);
					check(!mapping.isActiveAndMatchesAllowingExtraModifiers(mouse), "AMECS JEI shortcuts must respect their GUI context");
					while (client.options.keyAttack.consumeClick()) {}
					KeyMapping.click(mouse);
					check(client.options.keyAttack.consumeClick(), "Inactive JEI shortcuts must not suppress ordinary AMECS attack bindings");
				});
			} finally {
				context.getInput().releaseKey(InputConstants.KEY_LALT);
			}
		} finally {
			context.getInput().releaseKey(shortcutKey);
			context.runOnClient(client -> client.setScreen(null));
		}
	}

	private static AmecsKeyModifierCombination copyModifiers(KeyMapping mapping) {
		var modifiers = new AmecsKeyModifierCombination();
		modifiers.copyFrom(AmecsKeyModifiersApi.getBoundModifiers(mapping));
		return modifiers;
	}

	static Runnable saveModifiers(KeyMapping mapping) {
		var modifiers = copyModifiers(mapping);
		return () -> AmecsKeyModifiersApi.getBoundModifiers(mapping).copyFrom(modifiers);
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
