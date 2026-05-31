package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.api.KeyModifiers;
import mezz.jei.common.input.keys.IJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import mezz.jei.common.input.keys.JeiKeyModifier;

public class AmecsJeiKeyMappingBuilder extends FabricJeiKeyMappingBuilder {
	private final KeyModifiers modifier = new KeyModifiers();

	public AmecsJeiKeyMappingBuilder(String category, String description) {
		super(category, description);
	}

	@Override
	public IJeiKeyMappingBuilder setModifier(JeiKeyModifier modifier) {
		var amecsModifier = AmecsHelper.getJeiModifier(modifier);
		this.modifier.set(amecsModifier, true);
		return this;
	}

	@Override
	protected IJeiKeyMappingInternal buildMouse(int mouseButton) {
		var keyMapping = new AmecsKeyBindingWithContext(description, InputConstants.Type.MOUSE, mouseButton, category, modifier, context);
		return new AmecsJeiKeyMapping(keyMapping, context);
	}

	@Override
	public IJeiKeyMappingInternal buildKeyboardKey(int key) {
		var keyMapping = new AmecsKeyBindingWithContext(description, InputConstants.Type.KEYSYM, key, category, modifier, context);
		return new AmecsJeiKeyMapping(keyMapping, context);
	}
}
