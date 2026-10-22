package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.ModIds;
import mezz.jei.common.platform.Services;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;
import mezz.jei.test.client.ReflectionUtil;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.mezzdev.config.gui.ConfigGui;
import net.mezzdev.config.gui.ConfigScreen;
import net.mezzdev.config.api.value.editor.ConfigValueRestartRequirement;
import net.mezzdev.config.gui.api.ConfigValueApplyMode;
import net.mezzdev.config.gui.api.IConfigScreenValue;
import net.mezzdev.config.gui.api.keybindings.IKeyMappingAdapter;
import net.mezzdev.config.gui.entries.ConfigEntryWidgetFactory;
import net.mezzdev.config.gui.api.keybindings.KeyMappingModifier;
import net.mezzdev.config.gui.fabric.ConfigGuiFabricPluginFinder;
import net.mezzdev.config.gui.input.InputType;
import net.mezzdev.config.gui.input.UserInput;
import net.mezzdev.config.gui.keybindings.ConfigKeyMappingHelper;
import net.mezzdev.config.gui.keybindings.KeyMappingConfigEntry;
import net.mezzdev.config.gui.keybindings.KeyMappingConfigValue;
import net.mezzdev.config.gui.keybindings.KeyMappingConfigValues;
import net.mezzdev.config.gui.keybindings.KeyMappingValue;
import net.mezzdev.config.gui.model.ConfigValueChange;
import net.mezzdev.config.gui.util.ImmutableRect2i;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static net.mezzdev.config.gui.api.keybindings.KeyMappingModifier.ALT;
import static net.mezzdev.config.gui.api.keybindings.KeyMappingModifier.COMMAND;
import static net.mezzdev.config.gui.api.keybindings.KeyMappingModifier.CONTROL;
import static net.mezzdev.config.gui.api.keybindings.KeyMappingModifier.SHIFT;

/** Exercises the config GUI editor against live JEI, vanilla, and optional AMECS mappings. */
@SuppressWarnings({"UnstableApiUsage", "DataFlowIssue"})
public class ConfigGuiKeyModifiersClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		JUnitXmlTestReporter.runAndReport("fabric-client-gametest", getClass().getSimpleName(), () -> {
			context.runOnClient(client -> {
				ConfigGui.createScreenFactories(ConfigGuiFabricPluginFinder.getPlugins());
				checkMapping(client, Objects.requireNonNull(KeyMapping.get("key.jei.toggleOverlay")), true);
				checkMapping(client, client.options.keyJump, FabricLoader.getInstance().isModLoaded("amecs_key_modifiers"));
			});
			checkConfigScreen(context);
		});
	}

	private static void checkConfigScreen(ClientGameTestContext context) {
		var originalScreen = context.computeOnClient(client -> client.gui.screen());
		var mapping = context.computeOnClient(client -> Objects.requireNonNull(KeyMapping.get("key.jei.toggleOverlay")));
		var value = context.computeOnClient(client -> (KeyMappingConfigValue) KeyMappingConfigValues.create(List.of(mapping)).getFirst());
		var original = context.computeOnClient(client -> value.getValue());
		try {
			context.setScreen(() -> Services.PLATFORM.getConfigHelper().getConfigScreen(ModIds.JEI_ID, originalScreen).orElseThrow());
			context.runOnClient(client -> {
				var screen = Objects.requireNonNull(client.gui.screen());
				ConfigScreenClientTest.assertCategoryLayout(screen);
				var search = screen.children().stream()
					.filter(EditBox.class::isInstance)
					.map(EditBox.class::cast)
					.findFirst().orElseThrow();
				search.setValue(Component.translatable(mapping.getName()).getString());
			});
			context.waitTick();
			double[] position = context.computeOnClient(client -> {
				var screen = Objects.requireNonNull(client.gui.screen());
				var entry = ConfigScreenClientTest.getScreenModel(screen).getVisibleEntryWidgets().stream()
					.filter(widget -> widget.getConfigValue().getName().equals(mapping.getName()))
					.map(KeyMappingConfigEntry.class::cast)
					.findFirst().orElseThrow();
				ImmutableRect2i button = getButtonArea(entry);
				return new double[]{
					(button.getX() + button.getWidth() / 2.0) * client.getWindow().getScreenWidth() / client.getWindow().getGuiScaledWidth(),
					(button.getY() + button.getHeight() / 2.0) * client.getWindow().getScreenHeight() / client.getWindow().getGuiScaledHeight()
				};
			});
			context.getInput().setCursorPos(position[0], position[1]);
			context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.runOnClient(client -> check(ConfigScreen.isCapturingKeyBinding(Objects.requireNonNull(client.gui.screen())), "Clicking the config row must start recording"));
			FabricKeyModifiersClientGameTest.pressShiftModifiedKey(context, InputConstants.KEY_P);
			context.runOnClient(client -> {
				check(!ConfigScreen.isCapturingKeyBinding(Objects.requireNonNull(client.gui.screen())), "The screen must finish recording the shortcut");
				check(value.getValue().binding().equals(ConfigKeyMappingHelper.INSTANCE.createBinding(InputConstants.Type.KEYBOARD.getOrCreate(InputConstants.KEY_P), Set.of(SHIFT))), "The config screen must apply the key and its modifier through the registered adapter");
				client.options.load();
				check(value.getValue().binding().getModifiers().equals(Set.of(SHIFT)), "The config screen must save the modified binding");
			});
		} finally {
			context.setScreen(() -> originalScreen);
			context.runOnClient(client -> value.set(original));
		}
	}

	private static ImmutableRect2i getButtonArea(KeyMappingConfigEntry entry) {
		try {
			var field = KeyMappingConfigEntry.class.getDeclaredField("buttonArea");
			field.setAccessible(true);
			return (ImmutableRect2i) field.get(entry);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Unable to find the key binding control", e);
		}
	}

	private static void checkMapping(Minecraft client, KeyMapping mapping, boolean supportsModifiers) {
		var helper = ConfigKeyMappingHelper.INSTANCE;
		KeyMappingConfigValue value = (KeyMappingConfigValue) KeyMappingConfigValues.create(List.of(mapping)).getFirst();
		var original = value.getValue();
		var adapter = original.configKeyMapping();
		var defaults = value.getDefaultValue();
		check(!original.binding().isUnbound(), "The GUI must see the real key, including hidden JEI keys");
		check(value.getValue().equals(original), "Reading defaults must not change the live binding");
		IConfigScreenValue<KeyMappingValue> screenValue = IConfigScreenValue.withApplyMode(
			IConfigScreenValue.withRestartRequirement(value, ConfigValueRestartRequirement.NONE),
			ConfigValueApplyMode.IMMEDIATE
		);
		var entryFactory = new ConfigEntryWidgetFactory(popup -> {}, () -> {}, null, Map.of());
		KeyMappingConfigEntry entry = (KeyMappingConfigEntry) entryFactory.create(screenValue);
		entry.setImmediateChangeHandler(ConfigValueChange::apply);
		entry.updateBounds(new ImmutableRect2i(0, 0, 400, 24));
		try {
			var registeredAdapter = new ReflectionUtil().getFieldWithClass(adapter, IKeyMappingAdapter.class).findFirst().orElseThrow();
			var input = helper.createBinding(InputConstants.getKey("key.keyboard.p"), Set.of(CONTROL, SHIFT), Set.of("jei_test:unsupported")).resolve();
			var captured = registeredAdapter.createBinding(input);
			check(value.getValue().equals(original), "Converting captured input must not change the live binding");
			registeredAdapter.setValue(captured);
			check(registeredAdapter.getValue().equals(captured), "The adapter must produce a binding it can retain unchanged, excluding unsupported modifiers");
			registeredAdapter.setValue(original.binding());

			start(entry);
			entry.keyPressed(InputConstants.KEY_LCONTROL, 0, InputConstants.MOD_CONTROL);
			entry.keyPressed(InputConstants.KEY_LSHIFT, 0, InputConstants.MOD_CONTROL | InputConstants.MOD_SHIFT);
			entry.keyPressed(InputConstants.KEY_P, 0, InputConstants.MOD_CONTROL | InputConstants.MOD_SHIFT);
			Set<KeyMappingModifier> expected = supported(supportsModifiers, CONTROL, SHIFT);
			check(value.getValue().binding().getModifiers().equals(expected), "Editing must preserve every supported modifier");
			check(!entry.isCapturingKeyboardInput(), "The base key must finish recording");
			var edited = value.getValue();
			adapter.getConflicts(edited.binding());
			check(value.getValue().equals(edited), "Conflict inspection must preserve the complete binding");
			client.options.save();
			adapter.set(helper.createBinding(InputConstants.UNKNOWN, Set.of()));
			client.options.load();
			check(value.getValue().equals(edited), "Options must restore the full binding saved by the GUI");

			start(entry);
			entry.keyPressed(InputConstants.KEY_LALT, 0, InputConstants.MOD_ALT);
			entry.createInputHandler().handleUserInput(null, UserInput.fromVanilla(340, 12, InputConstants.MOUSE_BUTTON_RIGHT, InputConstants.MOD_ALT, InputType.SIMULATE).orElseThrow());
			entry.keyReleased(InputConstants.KEY_LALT, 0, 0);
			check(entry.isCapturingKeyboardInput(), "Releasing a modifier during a mouse click must wait for the mouse release");
			entry.createInputHandler().handleUserInput(null, UserInput.fromVanilla(340, 12, InputConstants.MOUSE_BUTTON_RIGHT, 0, InputType.EXECUTE).orElseThrow());
			check(value.getValue().binding().getKey().equals(InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT)), "The editor must capture mouse buttons: " + value.getValue().binding());
			check(value.getValue().binding().getModifiers().equals(supported(supportsModifiers, ALT)), "Mouse bindings must retain modifiers from the press event");

			start(entry);
			entry.keyPressed(InputConstants.KEY_LSHIFT, 0, InputConstants.MOD_SHIFT);
			entry.keyReleased(InputConstants.KEY_LSHIFT, 0, 0);
			check(value.getValue().binding().getKey().equals(InputConstants.Type.KEYBOARD.getOrCreate(InputConstants.KEY_LSHIFT)) && value.getValue().binding().getModifiers().isEmpty(), "A standalone modifier must bind without requiring itself");

			start(entry);
			entry.keyPressed(InputConstants.KEY_ESCAPE, 0, 0);
			check(value.getValue().binding().isUnbound() && value.getValue().binding().getModifiers().isEmpty(), "Unbinding must clear every modifier");
			entry.resetToDefault();
			check(value.getValue().equals(defaults), "Reset must restore the key and every default modifier");

			List<KeyMappingValue> changes = new ArrayList<>();
			Runnable removeListener = value.addListener(changes::add);
			try {
				var current = value.getValue();
				var unsupported = helper.createBinding(current.binding().getKey(), current.binding().getModifiers(), Set.of("mezz_config_test:unsupported"));
				check(!value.set(current.withBinding(unsupported)), "Unsupported modifiers alone must not report a binding change");
				check(changes.isEmpty(), "An unchanged native binding must not notify listeners");
			} finally {
				removeListener.run();
			}

			if (supportsModifiers) {
				var portable = helper.createBinding(InputConstants.getKey("key.keyboard.p"), Set.of(KeyMappingModifier.CONTROL_OR_COMMAND));
				value.set(original.withBinding(portable));
				KeyMappingModifier platformModifier = CONTROL;
				if (InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY) {
					platformModifier = COMMAND;
				}
				check(value.getValue().binding().getModifiers().equals(Set.of(platformModifier)), "Portable modifiers must apply to physical Fabric bindings");
				var physical = helper.createBinding(portable.getKey(), Set.of(platformModifier));
				check(adapter.getValueName(portable).equals(adapter.getValueName(physical)), "Portable modifiers must display the platform modifier before applying the binding");
				value.set(original.withBinding(helper.createBinding(InputConstants.getKey("key.keyboard.p"), Set.of(CONTROL, COMMAND))));
				check(value.getValue().binding().getModifiers().equals(Set.of(CONTROL, COMMAND)), "Control and Command must remain distinct");
				var before = value.getValue();
				adapter.getConflicts(before.binding());
				check(value.getValue().equals(before), "Conflict checks must preserve custom AMECS Command modifiers");

				var existing = helper.createBinding(InputConstants.getKey("key.keyboard.left.control"), Set.of(CONTROL, SHIFT));
				adapter.set(existing);
				check(value.getValue().binding().equals(existing), "Reading an existing mapping must preserve even redundant modifier requirements");
				adapter.getConflicts(helper.createBinding(InputConstants.getKey("key.keyboard.p"), Set.of(ALT)));
				check(value.getValue().binding().equals(existing), "Conflict previews must restore the exact existing modifier requirements");
			}
		} finally {
			adapter.set(original.binding());
			KeyMapping.resetMapping();
			client.options.save();
		}
	}

	private static Set<KeyMappingModifier> supported(boolean supported, KeyMappingModifier... modifiers) {
		if (supported)
			return Set.of(modifiers);
		return Set.of();
	}

	private static void start(KeyMappingConfigEntry entry) {
		entry.createInputHandler().handleUserInput(null, UserInput.fromVanilla(340.0, 12.0, InputConstants.MOUSE_BUTTON_LEFT, InputType.IMMEDIATE).orElseThrow());
		check(entry.isCapturingKeyboardInput(), "Clicking the binding button must start recording");
	}

	private static void check(boolean result, String message) {
		if (!result)
			throw new AssertionError(message);
	}
}
