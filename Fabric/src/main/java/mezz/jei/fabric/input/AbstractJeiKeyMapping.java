package mezz.jei.fabric.input;

import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public abstract class AbstractJeiKeyMapping implements IJeiKeyMappingInternal {
	protected final JeiKeyConflictContext context;

	public AbstractJeiKeyMapping(JeiKeyConflictContext context) {
		this.context = context;
	}

	protected abstract KeyMapping getMapping();

	@Override
	public boolean isUnbound() {
		return this.getMapping().isUnbound();
	}

	@Override
	public Component getTranslatedKeyMessage() {
		return this.getMapping().getTranslatedKeyMessage();
	}

	@Override
	public IJeiKeyMappingInternal register(Consumer<KeyMapping> registerMethod) {
		registerMethod.accept(this.getMapping());
		return this;
	}
}
