package mezz.jei.neoforge.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.KeyNameUtil;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.settings.KeyModifier;

import java.util.function.Consumer;

public class NeoForgeJeiKeyMapping implements IJeiKeyMappingInternal {
	private final KeyMapping keyMapping;

	public NeoForgeJeiKeyMapping(KeyMapping keyMapping) {
		this.keyMapping = keyMapping;
	}

	@Override
	public boolean isActiveAndMatches(InputConstants.Key key) {
		return keyMapping.isActiveAndMatches(key);
	}

	@Override
	public boolean isUnbound() {
		return keyMapping.isUnbound();
	}

	@Override
	public Component getTranslatedKeyMessage() {
		InputConstants.Key key = keyMapping.getKey();
		return keyMapping.getKeyModifier().getCombinedName(key, () -> KeyNameUtil.getKeyDisplayName(key));
	}

	@Override
	public boolean isDown() {
		InputConstants.Key key = keyMapping.getKey();
		return IJeiKeyMappingInternal.isKeyDown(key) &&
			keyMapping.getKeyConflictContext().isActive() &&
			isKeyModifierActive(key);
	}

	private boolean isKeyModifierActive(InputConstants.Key key) {
		KeyModifier keyModifier = keyMapping.getKeyModifier();
		// Work around NeoForge treating KeyModifier.NONE as inactive in GUI contexts
		// when the key binding itself is a modifier key, like Left Shift.
		if (keyModifier == KeyModifier.NONE && KeyModifier.isKeyCodeModifier(key)) {
			return true;
		}
		return keyModifier.isActive(keyMapping.getKeyConflictContext());
	}

	@Override
	public IJeiKeyMappingInternal register(Consumer<KeyMapping> registerMethod) {
		registerMethod.accept(keyMapping);
		return this;
	}
}
