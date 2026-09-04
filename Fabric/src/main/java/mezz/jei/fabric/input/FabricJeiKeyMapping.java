package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class FabricJeiKeyMapping<T extends KeyMapping & ContextAwareKeyMapping> implements IJeiKeyMappingInternal {
	protected final T mapping;

	public FabricJeiKeyMapping(T mapping) {
		this.mapping = mapping;
	}

	@Override
	public boolean isActiveAndMatches(InputConstants.Key key) {
		return this.mapping.isActiveAndMatches(key);
	}

	@Override
	public boolean isUnbound() {
		return this.mapping.isUnbound();
	}

	@Override
	public Component getTranslatedKeyMessage() {
		return this.mapping.getTranslatedKeyMessage();
	}

	@Override
	public boolean isDown() {
		return this.mapping.isContextActive() &&
			IJeiKeyMappingInternal.isKeyDown(KeyBindingHelper.getBoundKeyOf(this.mapping));
	}

	@Override
	public IJeiKeyMappingInternal register(Consumer<KeyMapping> registerMethod) {
		registerMethod.accept(this.mapping);
		return this;
	}
}
