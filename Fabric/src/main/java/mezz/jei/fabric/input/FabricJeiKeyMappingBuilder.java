package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.AbstractJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import mezz.jei.common.input.keys.IJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;
import net.minecraft.client.KeyMapping;

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
		KeyMapping keyMapping = new KeyMapping(description, InputConstants.Type.MOUSE, mouseButton, category);
		return new FabricJeiKeyMapping(keyMapping, context);
	}

	@Override
	public IJeiKeyMappingInternal buildKeyboardKey(int key) {
		KeyMapping keyMapping = new KeyMapping(description, InputConstants.Type.KEYSYM, key, category);
		return new FabricJeiKeyMapping(keyMapping, context);
	}
}
