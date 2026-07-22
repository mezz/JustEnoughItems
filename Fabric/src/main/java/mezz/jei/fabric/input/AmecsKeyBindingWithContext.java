package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifiers;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public class AmecsKeyBindingWithContext extends AmecsKeyBinding {
	private InputConstants.Key realKey;
	private final JeiKeyConflictContext context;

	public AmecsKeyBindingWithContext(String id, InputConstants.Type type, int code, String category, KeyModifiers defaultModifiers, JeiKeyConflictContext context) {
		super(id, type, code, category, defaultModifiers);
		this.realKey = KeyBindingHelper.getBoundKeyOf(this);
		this.context = context;
		hideFromMinecraftClickDispatch();
	}

	@Override
	public boolean same(KeyMapping binding) {
		// Special implementation which is aware of the key conflict context.
		if (binding instanceof AmecsKeyBindingWithContext other) {
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
		hideFromMinecraftClickDispatch();
	}

	@Override
	public boolean isUnbound() {
		return this.realKey.equals(InputConstants.UNKNOWN);
	}

	@Override
	public boolean matches(int keyCode, int scanCode) {
		if (keyCode != InputConstants.UNKNOWN.getValue()) {
			return this.realKey.getType() == InputConstants.Type.KEYSYM &&
				this.realKey.getValue() == keyCode;
		} else {
			return this.realKey.getType() == InputConstants.Type.SCANCODE &&
				this.realKey.getValue() == scanCode;
		}
	}

	@Override
	public boolean matchesMouse(int button) {
		return this.realKey.getType() == InputConstants.Type.MOUSE &&
			this.realKey.getValue() == button;
	}

	@Override
	public Component getTranslatedKeyMessage() {
		KeyModifiers modifiers = KeyBindingUtils.getBoundModifiers(this);
		return AmecsHelper.getCombinedName(modifiers, this.realKey);
	}

	@Override
	public boolean isDefault() {
		return KeyBindingUtils.getBoundModifiers(this).equals(getDefaultModifiers()) &&
			this.realKey.equals(getDefaultKey());
	}

	@Override
	public String saveString() {
		return this.realKey.getName();
	}

	public InputConstants.Key getRealKey() {
		return realKey;
	}

	private void hideFromMinecraftClickDispatch() {
		// Keep the parent binding unbound so AMECS and vanilla click dispatch do not prioritize
		// inactive GUI-only JEI mappings over vanilla attack/use. JEI uses realKey for its own matching.
		super.setKey(InputConstants.UNKNOWN);
		KeyMapping.resetMapping();
	}
}
