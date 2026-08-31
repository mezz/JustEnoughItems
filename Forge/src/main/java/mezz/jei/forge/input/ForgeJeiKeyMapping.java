package mezz.jei.forge.input;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;

import mezz.jei.common.input.KeyNameUtil;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.settings.KeyModifier;

public class ForgeJeiKeyMapping implements IJeiKeyMappingInternal {
	private final KeyMapping keyMapping;

	public ForgeJeiKeyMapping(KeyMapping keyMapping) {
		this.keyMapping = keyMapping;
	}

	@Override
	public boolean isActiveAndMatches(InputConstants.Key key) {
		return keyMapping.isActiveAndMatches(key);
	}

	@Override
	public boolean isActiveAndMatchesAllowingExtraModifiers(InputConstants.Key key) {
		if (keyMapping.isUnbound() || !keyMapping.getKey().equals(key)) {
			return false;
		}
		if (!keyMapping.getKeyConflictContext().isActive()) {
			return false;
		}
		KeyModifier keyModifier = keyMapping.getKeyModifier();
		return keyModifier == KeyModifier.NONE || keyModifier.isActive(keyMapping.getKeyConflictContext());
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
		return IJeiKeyMappingInternal.isKeyDown(keyMapping.getKey());
	}

	@Override
	public IJeiKeyMappingInternal register(Consumer<KeyMapping> registerMethod) {
		registerMethod.accept(keyMapping);
		return this;
	}
}
