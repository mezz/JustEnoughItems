package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.AbstractJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import mezz.jei.common.input.keys.IJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;

public class FabricJeiKeyMappingBuilder extends AbstractJeiKeyMappingBuilder {
	protected final String category;
	protected final String description;
	protected JeiKeyConflictContext context = JeiKeyConflictContext.UNIVERSAL;

	public FabricJeiKeyMappingBuilder(String category, String description) {
		this.category = category;
		this.description = description;
	}

	@Override
	public IJeiKeyMappingBuilder setContext(JeiKeyConflictContext context) {
		this.context = context;
		return this;
	}

	@Override
	public IJeiKeyMappingBuilder setModifier(JeiKeyModifier modifier) {
		return this;
	}

	@Override
	protected IJeiKeyMappingInternal buildMouse(int mouseButton) {
		FabricKeyMapping keyMapping = new FabricKeyMapping(
			description,
			InputConstants.Type.MOUSE,
			mouseButton,
			category,
			context
		);
		return new FabricJeiKeyMapping(keyMapping, context);
	}

	@Override
	public IJeiKeyMappingInternal buildKeyboardKey(int key) {
		FabricKeyMapping keyMapping = new FabricKeyMapping(
			description,
			InputConstants.Type.KEYSYM,
			key,
			category,
			context
		);
		return new FabricJeiKeyMapping(keyMapping, context);
	}
}
