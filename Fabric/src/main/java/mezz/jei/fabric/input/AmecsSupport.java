package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifier;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifierCombination;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiers;
import de.siphalor.amecs.key_modifiers.impl.AmecsKeyMappingManager;
import de.siphalor.amecs.key_modifiers.impl.AmecsKeyMappingManagerLayer;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.InputQuirks;

import java.util.stream.Stream;

/** Optional AMECS integration, loaded only when AMECS is installed. */
@SuppressWarnings("UnstableApiUsage")
public final class AmecsSupport {
	private static final AmecsKeyModifier COMMAND = new AmecsKeyModifier("command") {
		@Override
		public boolean matches(int keyCode) {
			return keyCode == InputConstants.KEY_LGUI || keyCode == InputConstants.KEY_RGUI;
		}

		@Override
		public String getTranslationKey() {
			return "jei.key.command";
		}
	};

	private AmecsSupport() {}

	public static void init() {
		AmecsKeyModifiers.register(COMMAND);
		// Isolate JEI shortcuts so inactive GUI bindings cannot suppress ordinary in-game bindings.
		AmecsKeyMappingManager.prependLayer(new AmecsKeyMappingManagerLayer() {
			@Override
			public boolean accepts(KeyMapping mapping) {
				return mapping instanceof AmecsKeyMapping;
			}

			@Override
			public Stream<KeyMapping> getAllMappings() {
				return super.getAllMappings().filter(mapping -> ((AmecsKeyMapping) mapping).isContextActive());
			}

			@Override
			public Stream<KeyMapping> getMappingsForInput(InputConstants.Key input) {
				return super.getMappingsForInput(input).filter(mapping -> ((AmecsKeyMapping) mapping).isContextActive());
			}
		});
	}

	static IJeiKeyMappingInternal createMapping(String description, InputConstants.Type type, int key, KeyMapping.Category category, JeiKeyConflictContext context, JeiKeyModifier modifier) {
		AmecsKeyModifierCombination modifiers = new AmecsKeyModifierCombination();
		switch (modifier) {
			case SHIFT -> modifiers.setShift(true);
			case ALT -> modifiers.setAlt(true);
			case CONTROL -> modifiers.setControl(true);
			case CONTROL_OR_COMMAND -> {
				if (InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY) {
					modifiers.set(COMMAND, true);
				} else {
					modifiers.setControl(true);
				}
			}
			case NONE -> {}
		}
		return new FabricJeiKeyMapping<>(new AmecsKeyMapping(description, type, key, category, modifiers, context));
	}
}
