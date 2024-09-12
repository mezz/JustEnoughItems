package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public abstract class AbstractJeiKeyMapping implements IJeiKeyMappingInternal {
	protected final JeiKeyConflictContext context;

	public AbstractJeiKeyMapping(JeiKeyConflictContext context) {
		this.context = context;
	}

	protected abstract KeyMapping getMapping();

	protected abstract InputConstants.Key getMappedKey();

	@Override
	public boolean isActiveAndMatches(InputConstants.Key key) {
		if (isUnbound()) {
			return false;
		}
		if (!this.getMappedKey().equals(key)) {
			return false;
		}
		return context.isActive();
	}

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
