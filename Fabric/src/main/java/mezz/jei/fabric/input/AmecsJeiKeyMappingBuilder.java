package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifierCombination;
import mezz.jei.common.input.keys.IJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import mezz.jei.common.input.keys.JeiKeyModifier;
import net.minecraft.client.KeyMapping;

public class AmecsJeiKeyMappingBuilder extends FabricJeiKeyMappingBuilder {
	private final AmecsKeyModifierCombination combination = new AmecsKeyModifierCombination();

	public AmecsJeiKeyMappingBuilder(KeyMapping.Category category, String description) {
		super(category, description);
	}

	@Override
	public IJeiKeyMappingBuilder setModifier(JeiKeyModifier modifier) {
		AmecsHelper.setJeiModifier(this.combination, modifier);
		return this;
	}

	@Override
	protected IJeiKeyMappingInternal buildMouse(int mouseButton) {
		var keyMapping = new AmecsKeyMappingWithContext(description, InputConstants.Type.MOUSE, mouseButton, category, combination, context);
		return new AmecsJeiKeyMapping(keyMapping, context);
	}

	@Override
	public IJeiKeyMappingInternal buildKeyboardKey(int key) {
		var keyMapping = new AmecsKeyMappingWithContext(description, InputConstants.Type.KEYSYM, key, category, combination, context);
		return new AmecsJeiKeyMapping(keyMapping, context);
	}
}
