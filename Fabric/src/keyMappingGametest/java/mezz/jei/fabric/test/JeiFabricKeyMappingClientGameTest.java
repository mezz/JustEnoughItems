package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.Internal;
import mezz.jei.common.input.keys.IJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;
import mezz.jei.fabric.input.FabricAmecsSupport;
import mezz.jei.fabric.input.FabricJeiKeyMapping;
import mezz.jei.fabric.input.FabricJeiKeyMappingCategoryBuilder;
import mezz.jei.fabric.input.FabricKeyMapping;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.TestInput;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Verifies JEI's Fabric key-mapping wrappers with and without AMECS support.
 *
 * <p>This test class is run by two Fabric client launches:
 * <ul>
 *     <li>{@code :Fabric:runClientGameTest} runs with AMECS and its JEI integration enabled.</li>
 *     <li>{@code :Fabric:runClientGameTestWithoutAmecs} removes AMECS from the runtime classpath and exercises
 *     JEI's plain Fabric key-mapping path.</li>
 * </ul>
 */
@SuppressWarnings("UnstableApiUsage")
public class JeiFabricKeyMappingClientGameTest implements FabricClientGameTest {
	private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath("jei-test", "key_mapping"));
	private static final Identifier GUI_BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath("jei", "textures/jei/atlas/gui/gui_background.png");
	private static final String FOCUS_SEARCH_TRANSLATION_KEY = "key.jei.focusSearch";

	@Override
	public void runTest(ClientGameTestContext context) {
		boolean amecsLoaded = FabricLoader.getInstance()
			.isModLoaded(FabricAmecsSupport.AMECS_KEY_MODIFIERS_MOD_ID);
		String suiteName = "fabric-client-gametest";
		if (!amecsLoaded) {
			suiteName += "-without-amecs";
		}
		JUnitXmlTestReporter.runAndReport(
			suiteName,
			getClass().getSimpleName(),
			() -> {
				context.runOnClient(client -> {
					assertJeiResourcesAreLoaded(client);
					assertFabricKeyMappingConflictContexts();
					assertFabricJeiKeyMappingIsDiscoverableAndRebindable(
						"key.jei.test.fabricKeyMapping.keyboardR",
						InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_R),
						InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_T)
					);
					assertFabricJeiKeyMappingIsDiscoverableAndRebindable(
						"key.jei.test.fabricKeyMapping.keyboardU",
						InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_U),
						InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_Y)
					);
					assertFabricJeiKeyMappingIsDiscoverableAndRebindable(
						"key.jei.test.fabricKeyMapping.mouseLeft",
						InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT),
						InputConstants.Type.MOUSE.getOrCreate(3)
					);
					assertFabricJeiKeyMappingIsDiscoverableAndRebindable(
						"key.jei.test.fabricKeyMapping.mouseRight",
						InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT),
						InputConstants.Type.MOUSE.getOrCreate(2)
					);
					if (FabricAmecsSupport.isEnabled()) {
						AmecsKeyMappingClientGameTestHelper.assertKeyMappingConflictContexts(CATEGORY);
						AmecsKeyMappingClientGameTestHelper.assertJeiKeyMappingIsDiscoverableAndRebindable(CATEGORY);
					}
				});
				assertModifiedJeiKeyMappings(context);
				assertFocusSearchHotkeyDoesNotTypeItsCharacter(context);
				assertJeiMouseMappingsDoNotHideVanillaMouseClicks(context);
			}
		);
	}

	private static void assertJeiResourcesAreLoaded(Minecraft client) {
		if (client.getResourceManager().getResource(GUI_BACKGROUND_TEXTURE).isEmpty()) {
			throw new AssertionError("Expected the Fabric development mod to include JEI's Common textures.");
		}
		if (!Language.getInstance().has(FOCUS_SEARCH_TRANSLATION_KEY)) {
			throw new AssertionError("Expected the Fabric development mod to include JEI's Common translations.");
		}
	}

	private static void assertFocusSearchHotkeyDoesNotTypeItsCharacter(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			context.waitFor(
				client -> Internal.getOptionalJeiRuntime().isPresent(),
				ClientGameTestContext.DEFAULT_TIMEOUT
			);

			context.getInput().pressKey(options -> options.keyInventory);
			context.waitFor(
				client -> client.screen instanceof InventoryScreen &&
					Internal.getJeiRuntime().getIngredientListOverlay().isListDisplayed(),
				ClientGameTestContext.DEFAULT_TIMEOUT
			);
			context.runOnClient(client -> {
				KeyMapping focusSearch = KeyMapping.get("key.jei.focusSearch");
				if (focusSearch == null) {
					throw new AssertionError("Expected the focus-search key mapping to be registered.");
				}
				// Match the controls screen, which unbinds the selected mapping before recording its new key.
				focusSearch.setKey(InputConstants.UNKNOWN);
				focusSearch.setKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_F));
				KeyMapping.resetMapping();
				Internal.getJeiRuntime().getIngredientFilter().setFilterText("");
			});

			TestInput input = context.getInput();
			input.pressKey(GLFW.GLFW_KEY_F);
			context.waitFor(
				client -> Internal.getJeiRuntime().getIngredientListOverlay().hasKeyboardFocus(),
				ClientGameTestContext.DEFAULT_TIMEOUT
			);

			input.typeChar('f');
			String filterText = context.computeOnClient(client -> Internal.getJeiRuntime().getIngredientFilter().getFilterText());
			if (!filterText.isEmpty()) {
				throw new AssertionError("Expected the focus-search hotkey character to be consumed, got: " + filterText);
			}

			input.pressKey(GLFW.GLFW_KEY_X);
			input.typeChar('x');
			filterText = context.computeOnClient(client -> Internal.getJeiRuntime().getIngredientFilter().getFilterText());
			if (!filterText.equals("x")) {
				throw new AssertionError("Expected normal typing to resume after the focus-search character, got: " + filterText);
			}
		}
	}

	private static void assertFabricKeyMappingConflictContexts() {
		InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_G);
		FabricKeyMapping guiMapping = createFabricKeyMapping("conflicts.gui", key, JeiKeyConflictContext.GUI);
		FabricKeyMapping otherGuiMapping = createFabricKeyMapping("conflicts.otherGui", key, JeiKeyConflictContext.GUI);
		FabricKeyMapping inGameMapping = createFabricKeyMapping("conflicts.inGame", key, JeiKeyConflictContext.IN_GAME);
		FabricKeyMapping universalMapping = createFabricKeyMapping("conflicts.universal", key, JeiKeyConflictContext.UNIVERSAL);
		KeyMapping regularMapping = new KeyMapping("key.jei.test.fabricKeyMapping.conflicts.regular", key.getType(), key.getValue(), CATEGORY);

		try {
			assertConflictContextBehavior("Fabric", guiMapping, otherGuiMapping, inGameMapping, universalMapping, regularMapping);
		} finally {
			unbindMappings(guiMapping, otherGuiMapping, inGameMapping, universalMapping, regularMapping);
		}
	}

	private static FabricKeyMapping createFabricKeyMapping(String nameSuffix, InputConstants.Key key, JeiKeyConflictContext context) {
		return new FabricKeyMapping(
			"key.jei.test.fabricKeyMapping." + nameSuffix,
			key.getType(),
			key.getValue(),
			CATEGORY,
			context
		);
	}

	static void assertConflictContextBehavior(
		String implementationName,
		KeyMapping guiMapping,
		KeyMapping otherGuiMapping,
		KeyMapping inGameMapping,
		KeyMapping universalMapping,
		KeyMapping regularMapping
	) {
		if (!guiMapping.same(otherGuiMapping)) {
			throw new AssertionError("Expected " + implementationName + " mappings in the same context to conflict.");
		}
		if (guiMapping.same(inGameMapping) || inGameMapping.same(guiMapping)) {
			throw new AssertionError("Expected " + implementationName + " GUI and in-game mappings not to conflict.");
		}
		if (!guiMapping.same(universalMapping) || !universalMapping.same(guiMapping)) {
			throw new AssertionError("Expected " + implementationName + " universal mappings to conflict with every JEI context.");
		}
		if (!guiMapping.same(regularMapping) || !regularMapping.same(guiMapping)) {
			throw new AssertionError("Expected " + implementationName + " and regular mappings to report key conflicts symmetrically.");
		}
	}

	static void unbindMappings(KeyMapping... mappings) {
		for (KeyMapping mapping : mappings) {
			mapping.setKey(InputConstants.UNKNOWN);
		}
		KeyMapping.resetMapping();
	}

	private static void assertFabricJeiKeyMappingIsDiscoverableAndRebindable(
		String description,
		InputConstants.Key boundKey,
		InputConstants.Key reboundKey
	) {
		// Setup: build a plain Fabric-backed JEI mapping with a test-only translation key.
		FabricKeyMapping fabricMapping = new FabricKeyMapping(
			description,
			boundKey.getType(),
			boundKey.getValue(),
			CATEGORY,
			JeiKeyConflictContext.UNIVERSAL
		);
		FabricJeiKeyMapping jeiMapping = new FabricJeiKeyMapping(fabricMapping, JeiKeyConflictContext.UNIVERSAL);
		assertJeiKeyMappingIsDiscoverableAndRebindable("Fabric", fabricMapping, jeiMapping, boundKey, reboundKey);
	}

	static void assertJeiKeyMappingIsDiscoverableAndRebindable(
		String implementationName,
		KeyMapping platformMapping,
		IJeiKeyMappingInternal jeiMapping,
		InputConstants.Key boundKey,
		InputConstants.Key reboundKey
	) {
		// Operation and assertions: Fabric and other mods must see the same key that JEI matches.
		assertBoundKeyEquals(implementationName, platformMapping, boundKey, "initial binding");
		if (!jeiMapping.isActiveAndMatches(boundKey)) {
			throw new AssertionError("Expected " + implementationName + " JEI mapping to match its initial key: " + boundKey.getName());
		}
		if (jeiMapping.isActiveAndMatches(InputConstants.UNKNOWN)) {
			throw new AssertionError("Expected " + implementationName + " JEI mapping to reject the UNKNOWN key.");
		}

		// Rebind through the standard KeyMapping API, as third-party controls screens do, then rebuild dispatch maps.
		platformMapping.setKey(reboundKey);
		KeyMapping.resetMapping();
		assertBoundKeyEquals(implementationName, platformMapping, reboundKey, "rebound binding");
		if (!jeiMapping.isActiveAndMatches(reboundKey)) {
			throw new AssertionError("Expected " + implementationName + " JEI mapping to match its rebound key: " + reboundKey.getName());
		}
		if (jeiMapping.isActiveAndMatches(boundKey)) {
			throw new AssertionError("Expected " + implementationName + " JEI mapping to stop matching its previous key: " + boundKey.getName());
		}

		// Cleanup and verify that standard unbinding remains visible to both Fabric and JEI.
		platformMapping.setKey(InputConstants.UNKNOWN);
		KeyMapping.resetMapping();
		assertBoundKeyEquals(implementationName, platformMapping, InputConstants.UNKNOWN, "unbound binding");
		if (!jeiMapping.isUnbound()) {
			throw new AssertionError("Expected " + implementationName + " JEI mapping to report unbound after setting UNKNOWN.");
		}
	}

	private static void assertBoundKeyEquals(
		String implementationName,
		KeyMapping platformMapping,
		InputConstants.Key expectedKey,
		String operation
	) {
		InputConstants.Key exposedKey = KeyMappingHelper.getBoundKeyOf(platformMapping);
		if (!exposedKey.equals(expectedKey)) {
			throw new AssertionError(
				"Expected Fabric's key helper to expose the " + implementationName + " " + operation + ": " + expectedKey.getName()
			);
		}
	}

	private static void assertModifiedJeiKeyMappings(ClientGameTestContext context) {
		assertModifiedMapping(context, keyboardMapping("key.jei.test.modifiedKeyMapping.shiftKeyboardR", GLFW.GLFW_KEY_R), JeiKeyModifier.SHIFT);
		assertModifiedMapping(context, keyboardMapping("key.jei.test.modifiedKeyMapping.controlOrCommandKeyboardU", GLFW.GLFW_KEY_U), JeiKeyModifier.CONTROL_OR_COMMAND);
		assertModifiedMapping(context, mouseLeftMapping("key.jei.test.modifiedKeyMapping.shiftMouseLeft"), JeiKeyModifier.SHIFT);
		assertModifiedMapping(context, mouseRightMapping("key.jei.test.modifiedKeyMapping.shiftMouseRight"), JeiKeyModifier.SHIFT);
		assertModifiedMapping(context, mouseLeftMapping("key.jei.test.modifiedKeyMapping.controlOrCommandMouseLeft"), JeiKeyModifier.CONTROL_OR_COMMAND);
		assertModifiedMapping(context, mouseRightMapping("key.jei.test.modifiedKeyMapping.controlOrCommandMouseRight"), JeiKeyModifier.CONTROL_OR_COMMAND);
		assertModifiedMapping(context, mouseRightMapping("key.jei.test.modifiedKeyMapping.altMouseRight"), JeiKeyModifier.ALT);
	}

	private static void assertJeiMouseMappingsDoNotHideVanillaMouseClicks(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			context.runOnClient(client -> client.setScreen(null));
			context.waitFor(
				client -> client.level != null &&
					client.screen == null &&
					!JeiKeyConflictContext.GUI.isActive(),
				ClientGameTestContext.DEFAULT_TIMEOUT
			);

			// Setup: enter the in-game input state with no screen open and JEI's GUI context inactive.
			// JEI's GUI-only mouse mappings use the same buttons as vanilla attack/use, but vanilla
			// should remain responsible for those clicks while the player is in-world.
			assertJeiKeyMappingHeldState(context);
			assertBoundJeiMouseMappingsDoNotHideVanillaMouseClicks(context);
			assertUnboundJeiMouseMappingsDoNotHideVanillaMouseClicks(context);
			if (FabricAmecsSupport.isEnabled()) {
				context.runOnClient(client -> AmecsKeyMappingClientGameTestHelper.assertInactiveJeiMouseMappingsDoNotHideVanillaMouseMappings(CATEGORY));
			}
		}
	}

	private static void assertJeiKeyMappingHeldState(ClientGameTestContext context) {
		TestMapping mapping = context.computeOnClient(client -> createActiveTestMapping(
			"key.jei.test.heldKeyMapping.shift",
			GLFW.GLFW_KEY_LEFT_SHIFT,
			JeiKeyConflictContext.UNIVERSAL
		));

		if (context.computeOnClient(client -> mapping.jeiMapping().isDown())) {
			throw new AssertionError("Expected JEI's held-key mapping to start released.");
		}

		holdShiftForKeyMappingDispatch(context);
		try {
			if (!context.computeOnClient(client -> mapping.jeiMapping().isDown())) {
				throw new AssertionError("Expected JEI's held-key mapping to detect its standard bound key.");
			}
		} finally {
			releaseShiftForKeyMappingDispatch(context);
		}

		if (context.computeOnClient(client -> mapping.jeiMapping().isDown())) {
			throw new AssertionError("Expected JEI's held-key mapping to detect that its key was released.");
		}
		context.runOnClient(client -> {
			mapping.platformMapping().setKey(InputConstants.UNKNOWN);
			KeyMapping.resetMapping();
		});
	}

	private static TestMapping createActiveTestMapping(
		String description,
		int keyCode,
		JeiKeyConflictContext context
	) {
		IJeiKeyMappingInternal jeiMapping = new FabricJeiKeyMappingCategoryBuilder(CATEGORY)
			.createMapping(description)
			.setContext(context)
			.buildKeyboardKey(keyCode);
		AtomicReference<KeyMapping> platformMapping = new AtomicReference<>();
		jeiMapping.register(platformMapping::set);
		KeyMapping registeredMapping = platformMapping.get();
		if (registeredMapping == null) {
			throw new AssertionError("Expected JEI's Fabric key mapping to register its platform mapping.");
		}
		return new TestMapping(registeredMapping, jeiMapping);
	}

	private static void assertBoundJeiMouseMappingsDoNotHideVanillaMouseClicks(ClientGameTestContext context) {
		context.runOnClient(client -> {
			// Setup: create bound JEI mouse mappings that should be active only in JEI/GUI contexts.
			assertBoundFabricJeiMouseMapping(InputConstants.MOUSE_BUTTON_LEFT);
			assertBoundFabricJeiMouseMapping(InputConstants.MOUSE_BUTTON_RIGHT);
			if (FabricAmecsSupport.isEnabled()) {
				AmecsKeyMappingClientGameTestHelper.assertBoundJeiMouseMapping(CATEGORY, InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.NONE);
				AmecsKeyMappingClientGameTestHelper.assertBoundJeiMouseMapping(CATEGORY, InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.NONE);
				AmecsKeyMappingClientGameTestHelper.assertBoundJeiMouseMapping(CATEGORY, InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.SHIFT);
				AmecsKeyMappingClientGameTestHelper.assertBoundJeiMouseMapping(CATEGORY, InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.SHIFT);
			}

			// Operation and assertions: bound JEI GUI-only mouse mappings must not hide vanilla attack/use clicks
			// while the player is in-world.
			assertVanillaMouseClickIsConsumedBy(client.options.keyAttack, InputConstants.MOUSE_BUTTON_LEFT, "plain left-click with bound JEI mouse mappings");
			assertVanillaMouseClickIsConsumedBy(client.options.keyUse, InputConstants.MOUSE_BUTTON_RIGHT, "plain right-click with bound JEI mouse mappings");
		});
		holdShiftForKeyMappingDispatch(context);
		try {
			context.runOnClient(client -> {
				assertVanillaMouseClickIsConsumedBy(client.options.keyAttack, InputConstants.MOUSE_BUTTON_LEFT, "shift left-click with bound JEI mouse mappings");
				assertVanillaMouseClickIsConsumedBy(client.options.keyUse, InputConstants.MOUSE_BUTTON_RIGHT, "shift right-click with bound JEI mouse mappings");
			});
		} finally {
			releaseShiftForKeyMappingDispatch(context);
		}
	}

	private static void assertUnboundJeiMouseMappingsDoNotHideVanillaMouseClicks(ClientGameTestContext context) {
		context.runOnClient(client -> {
			// Setup: create JEI mouse mappings on the same buttons as vanilla attack/use, then unbind them as the
			// Controls screen would. Unbound JEI mappings must not remain registered for click dispatch.
			assertUnboundFabricJeiMouseMapping(InputConstants.MOUSE_BUTTON_LEFT);
			assertUnboundFabricJeiMouseMapping(InputConstants.MOUSE_BUTTON_RIGHT);
			if (FabricAmecsSupport.isEnabled()) {
				AmecsKeyMappingClientGameTestHelper.assertUnboundJeiMouseMapping(CATEGORY, InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.NONE);
				AmecsKeyMappingClientGameTestHelper.assertUnboundJeiMouseMapping(CATEGORY, InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.NONE);
				AmecsKeyMappingClientGameTestHelper.assertUnboundJeiMouseMapping(CATEGORY, InputConstants.MOUSE_BUTTON_LEFT, JeiKeyModifier.SHIFT);
				AmecsKeyMappingClientGameTestHelper.assertUnboundJeiMouseMapping(CATEGORY, InputConstants.MOUSE_BUTTON_RIGHT, JeiKeyModifier.SHIFT);
			}

			// Operation and assertions: setting JEI mouse bindings to UNKNOWN must remove them from vanilla/AMECS
			// click dispatch, so vanilla attack/use still receive the click.
			assertVanillaMouseClickIsConsumedBy(client.options.keyAttack, InputConstants.MOUSE_BUTTON_LEFT, "plain left-click after unbinding JEI mouse mappings");
			assertVanillaMouseClickIsConsumedBy(client.options.keyUse, InputConstants.MOUSE_BUTTON_RIGHT, "plain right-click after unbinding JEI mouse mappings");
		});
		holdShiftForKeyMappingDispatch(context);
		try {
			context.runOnClient(client -> {
				assertVanillaMouseClickIsConsumedBy(client.options.keyAttack, InputConstants.MOUSE_BUTTON_LEFT, "shift left-click after unbinding JEI mouse mappings");
				assertVanillaMouseClickIsConsumedBy(client.options.keyUse, InputConstants.MOUSE_BUTTON_RIGHT, "shift right-click after unbinding JEI mouse mappings");
			});
		} finally {
			releaseShiftForKeyMappingDispatch(context);
		}
	}

	private static void assertModifiedMapping(
		ClientGameTestContext context,
		ModifiedMapping mapping,
		JeiKeyModifier modifier
	) {
		// Setup: build the mapping through JEI's Fabric category builder so AMECS support is selected once for
		// this client startup, the same way it is during normal JEI initialization.
		IJeiKeyMappingInternal jeiMapping = context.computeOnClient(client -> {
			IJeiKeyMappingBuilder builder = new FabricJeiKeyMappingCategoryBuilder(CATEGORY)
				.createMapping(mapping.description())
				.setModifier(modifier);
			return mapping.mappingFactory().build(builder);
		});

		// Operation: ask JEI whether the mapping accepts the real input key before and after holding the modifier.
		boolean isUnbound = context.computeOnClient(client -> jeiMapping.isUnbound());
		boolean matchesWithoutModifier = context.computeOnClient(client -> jeiMapping.isActiveAndMatches(mapping.boundKey()));
		holdModifier(context.getInput(), modifier);
		boolean matchesWithModifier;
		boolean matchesUnknownKey;
		try {
			matchesWithModifier = context.computeOnClient(client -> jeiMapping.isActiveAndMatches(mapping.boundKey()));
			matchesUnknownKey = context.computeOnClient(client -> jeiMapping.isActiveAndMatches(InputConstants.UNKNOWN));
		} finally {
			releaseModifier(context.getInput(), modifier);
		}

		// Assertions: when AMECS support is enabled, the modified mapping stays bound and only matches with
		// its modifier held. When AMECS support is disabled, the modified mapping is unsupported and unbound.
		if (FabricAmecsSupport.isEnabled()) {
			if (isUnbound) {
				throw new AssertionError("Expected AMECS-backed modified key mapping to stay bound.");
			}
			if (matchesWithoutModifier) {
				throw new AssertionError("Expected modified key mapping to reject input when its modifier is not held.");
			}
			if (!matchesWithModifier) {
				throw new AssertionError("Expected modified key mapping to match with its modifier held: " + mapping.boundKey().getName());
			}
		} else {
			if (!isUnbound) {
				throw new AssertionError("Expected modified key mapping to be unbound when AMECS support is disabled.");
			}
			if (matchesWithoutModifier) {
				throw new AssertionError("Expected unsupported modified key mapping to reject input without its modifier.");
			}
			if (matchesWithModifier) {
				throw new AssertionError("Expected unsupported modified key mapping to reject input with its modifier held.");
			}
		}
		if (matchesUnknownKey) {
			throw new AssertionError("Expected modified key mapping to reject the UNKNOWN key.");
		}
	}

	private static ModifiedMapping keyboardMapping(String description, int keyCode) {
		return new ModifiedMapping(
			description,
			InputConstants.Type.KEYSYM.getOrCreate(keyCode),
			builder -> builder.buildKeyboardKey(keyCode)
		);
	}

	private static ModifiedMapping mouseLeftMapping(String description) {
		return new ModifiedMapping(
			description,
			InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT),
			IJeiKeyMappingBuilder::buildMouseLeft
		);
	}

	private static ModifiedMapping mouseRightMapping(String description) {
		return new ModifiedMapping(
			description,
			InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT),
			IJeiKeyMappingBuilder::buildMouseRight
		);
	}

	private static void assertBoundFabricJeiMouseMapping(int mouseButton) {
		InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(mouseButton);
		FabricKeyMapping fabricMapping = new FabricKeyMapping(
			"key.jei.test.fabricKeyMapping.boundMouse" + mouseButton,
			InputConstants.Type.MOUSE,
			mouseButton,
			CATEGORY,
			JeiKeyConflictContext.GUI
		);
		FabricJeiKeyMapping jeiMapping = new FabricJeiKeyMapping(fabricMapping, JeiKeyConflictContext.GUI);

		if (jeiMapping.isUnbound()) {
			throw new AssertionError("Expected bound Fabric-backed JEI mouse mapping to report bound: " + mouseKey.getName());
		}
		if (jeiMapping.isActiveAndMatches(mouseKey)) {
			throw new AssertionError("Expected bound Fabric-backed JEI mouse mapping to reject input while its GUI context is inactive: " + mouseKey.getName());
		}
	}

	private static void assertUnboundFabricJeiMouseMapping(int mouseButton) {
		InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(mouseButton);
		FabricKeyMapping fabricMapping = new FabricKeyMapping(
			"key.jei.test.fabricKeyMapping.unboundMouse" + mouseButton,
			InputConstants.Type.MOUSE,
			mouseButton,
			CATEGORY,
			JeiKeyConflictContext.GUI
		);
		FabricJeiKeyMapping jeiMapping = new FabricJeiKeyMapping(fabricMapping, JeiKeyConflictContext.GUI);

		fabricMapping.setKey(InputConstants.UNKNOWN);
		KeyMapping.resetMapping();

		if (!jeiMapping.isUnbound()) {
			throw new AssertionError("Expected unbound Fabric-backed JEI mouse mapping to report unbound: " + mouseKey.getName());
		}
		if (jeiMapping.isActiveAndMatches(mouseKey)) {
			throw new AssertionError("Expected unbound Fabric-backed JEI mouse mapping to reject input: " + mouseKey.getName());
		}
	}

	private static void assertVanillaMouseClickIsConsumedBy(KeyMapping vanillaMapping, int mouseButton, String inputDescription) {
		InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(mouseButton);

		drainClicks(vanillaMapping);
		KeyMapping.click(mouseKey);

		if (!vanillaMapping.consumeClick()) {
			throw new AssertionError(
				"Expected vanilla mapping " + vanillaMapping.getName() + " to consume " + inputDescription + ": " + mouseKey.getName()
			);
		}
		if (vanillaMapping.consumeClick()) {
			throw new AssertionError(
				"Expected vanilla mapping " + vanillaMapping.getName() + " to receive exactly one click for " + inputDescription + ": " + mouseKey.getName()
			);
		}
	}

	private static void drainClicks(KeyMapping keyMapping) {
		while (keyMapping.consumeClick()) {
			// Drain stale clicks from earlier setup so this assertion measures exactly one new click.
		}
	}

	private static void holdShiftForKeyMappingDispatch(ClientGameTestContext context) {
		context.getInput().holdShift();
		context.runOnClient(client -> KeyMapping.set(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_SHIFT), true));
	}

	private static void releaseShiftForKeyMappingDispatch(ClientGameTestContext context) {
		context.runOnClient(client -> KeyMapping.set(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_SHIFT), false));
		context.getInput().releaseShift();
	}

	private static void holdModifier(TestInput input, JeiKeyModifier modifier) {
		switch (modifier) {
			case CONTROL_OR_COMMAND -> input.holdControl();
			case SHIFT -> input.holdShift();
			case ALT -> input.holdAlt();
			default -> throw new IllegalArgumentException("Unsupported test modifier: " + modifier);
		}
	}

	private static void releaseModifier(TestInput input, JeiKeyModifier modifier) {
		switch (modifier) {
			case CONTROL_OR_COMMAND -> input.releaseControl();
			case SHIFT -> input.releaseShift();
			case ALT -> input.releaseAlt();
			default -> throw new IllegalArgumentException("Unsupported test modifier: " + modifier);
		}
	}

	private interface MappingFactory {
		IJeiKeyMappingInternal build(IJeiKeyMappingBuilder builder);
	}

	private record ModifiedMapping(String description, InputConstants.Key boundKey, MappingFactory mappingFactory) {
	}

	private record TestMapping(KeyMapping platformMapping, IJeiKeyMappingInternal jeiMapping) {
	}
}
