package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyMappingWithKeyModifiers;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifierCombination;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiersApi;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class AmecsKeyMappingWithContext extends AmecsKeyMappingWithKeyModifiers {
	private InputConstants.Key realKey;
	private final JeiKeyConflictContext context;

	public AmecsKeyMappingWithContext(String id, InputConstants.Type type, int code, Category category, AmecsKeyModifierCombination defaultModifiers, JeiKeyConflictContext context) {
		super(id, type, code, category, defaultModifiers);
		this.realKey = KeyMappingHelper.getBoundKeyOf(this);
		this.context = context;
		hideFromMinecraftClickDispatch();
	}

	@Override
	public boolean same(KeyMapping binding) {
		// Special implementation which is aware of the key conflict context.
		if (binding instanceof AmecsKeyMappingWithContext other) {
			return realKey.equals(other.realKey) &&
				(context.conflicts(other.context) || other.context.conflicts(context));
		} else {
			// This ensures symmetry between conflicts, as regular keybinds see this one as
			// being unbound and not conflicting.
			return false;
		}
	}

	@Override
	public void setKey(InputConstants.Key key) {
		this.realKey = key;
		if (this.realKey.equals(InputConstants.UNKNOWN)) {
			AmecsKeyModifiersApi.getBoundModifiers(this).unset();
		}
		hideFromMinecraftClickDispatch();
	}

	@Override
	public boolean isUnbound() {
		return this.realKey.equals(InputConstants.UNKNOWN);
	}

	@Override
	public boolean matches(KeyEvent keyEvent) {
		int keyCode = keyEvent.key();
		if (keyCode != InputConstants.UNKNOWN.getValue()) {
			return this.realKey.getType() == InputConstants.Type.KEYSYM &&
				this.realKey.getValue() == keyCode;
		} else {
			return this.realKey.getType() == InputConstants.Type.SCANCODE &&
				this.realKey.getValue() == keyEvent.scancode();
		}
	}

	@Override
	public boolean matchesMouse(MouseButtonEvent mouseButtonEvent) {
		return this.realKey.getType() == InputConstants.Type.MOUSE &&
			this.realKey.getValue() == mouseButtonEvent.button();
	}

	@Override
	public Component getTranslatedKeyMessage() {
		AmecsKeyModifierCombination combination = AmecsKeyModifiersApi.getBoundModifiers(this);
		return AmecsHelper.getCombinedName(combination, this.realKey);
	}

	@Override
	public boolean isDefault() {
		return AmecsKeyModifiersApi.getBoundModifiers(this).equals(getDefaultAmecsKeyModifiers()) &&
			this.realKey.equals(getDefaultKey());
	}

	@Override
	public String saveString() {
		return this.realKey.getName();
	}

	public boolean isContextActive() {
		return context.isActive();
	}

	public InputConstants.Key getRealKey() {
		return realKey;
	}

	private void hideFromMinecraftClickDispatch() {
		// Keep the parent binding unbound so AMECS and vanilla click dispatch do not prioritize
		// inactive GUI-only JEI mappings over vanilla attack/use. JEI uses realKey for its own matching.
		this.key = InputConstants.UNKNOWN;
		KeyMapping.resetMapping();
	}
}
