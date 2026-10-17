package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.AbstractJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import mezz.jei.common.input.keys.IJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;
import net.minecraft.client.KeyMapping;
import net.fabricmc.loader.api.FabricLoader;

public class FabricJeiKeyMappingBuilder extends AbstractJeiKeyMappingBuilder {
	private final KeyMapping.Category category;
	private final String description;
	private JeiKeyConflictContext context = JeiKeyConflictContext.UNIVERSAL;
	private JeiKeyModifier modifier = JeiKeyModifier.NONE;

	public FabricJeiKeyMappingBuilder(KeyMapping.Category category, String description) {
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
		this.modifier = modifier;
		return this;
	}

	@Override
	protected IJeiKeyMappingInternal buildMouse(int mouseButton) {
		return build(InputConstants.Type.MOUSE, mouseButton);
	}

	@Override
	public IJeiKeyMappingInternal buildKeyboardKey(int key) {
		return build(InputConstants.Type.KEYBOARD, key);
	}

	private IJeiKeyMappingInternal build(InputConstants.Type type, int key) {
		if (FabricLoader.getInstance().isModLoaded("amecs_key_modifiers")) {
			return AmecsSupport.createMapping(description, type, key, category, context, modifier);
		}
		FabricKeyMapping keyMapping = new FabricKeyMapping(
			description,
			type,
			key,
			category,
			context,
			modifier
		);
		return new FabricJeiKeyMapping<>(keyMapping);
	}
}
