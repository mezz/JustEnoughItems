package mezz.jei.fabric.input;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifiers;
import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import mezz.jei.common.input.keys.IJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.JeiKeyModifier;

public class AmecsJeiKeyMappingBuilder extends FabricJeiKeyMappingBuilder {
	protected KeyModifiers modifier = new KeyModifiers();

	public AmecsJeiKeyMappingBuilder(String category, String description) {
		super(category, description);
		this.modifier = new KeyModifiers();
	}

	@Override
	public IJeiKeyMappingBuilder setModifier(JeiKeyModifier modifier) {
		this.modifier.unset();
		switch (modifier) {
			case CONTROL_OR_COMMAND:
				this.modifier.setControl(true);
				break;
			case SHIFT:
				this.modifier.setShift(true);
				break;
			case ALT:
				this.modifier.setAlt(true);
				break;
		}
		return this;
	}

	@Override
	protected IJeiKeyMappingInternal buildMouse(int mouseButton) {
		AmecsKeyBinding keyMapping = new AmecsKeyBinding(description, InputConstants.Type.MOUSE, mouseButton, category, modifier);
		return new AmecsJeiKeyMapping(keyMapping, context);
	}

	@Override
	public IJeiKeyMappingInternal buildKeyboardKey(int key) {
		AmecsKeyBinding keyMapping = new AmecsKeyBinding(description, InputConstants.Type.KEYSYM, key, category, modifier);
		return new AmecsJeiKeyMapping(keyMapping, context);
	}
}
