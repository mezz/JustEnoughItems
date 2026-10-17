package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;
import mezz.jei.common.input.KeyNameUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public class FabricKeyMapping extends KeyMapping implements ContextAwareKeyMapping {
	private InputConstants.Key realKey;
	private final JeiKeyConflictContext context;
	private final int defaultModifiers;
	private int modifiers;

	public FabricKeyMapping(String description, InputConstants.Type type, int keyCode, String category, JeiKeyConflictContext context, JeiKeyModifier modifier) {
		super(description, type, keyCode, category);
		this.realKey = getDefaultKey();
		this.context = context;
		this.defaultModifiers = FabricKeyModifiers.from(modifier);
		this.modifiers = defaultModifiers;
		hideFromMinecraftClickDispatch();
	}

	public int getModifiers() {
		return modifiers;
	}

	public void setModifiers(int modifiers) {
		this.modifiers = FabricKeyModifiers.normalize(modifiers);
	}

	/** Rebinds both the key and modifiers, using zero modifiers when unbound. */
	public void setKeyAndModifiers(InputConstants.Key key, int modifiers) {
		setKey(key);
		if (key.equals(InputConstants.UNKNOWN)) {
			modifiers = 0;
		}
		setModifiers(modifiers);
	}

	@Override
	public void resetModifiersToDefault() {
		modifiers = defaultModifiers;
	}

	@Override
	public void setKey(InputConstants.Key key) {
		this.realKey = key;
		hideFromMinecraftClickDispatch();
	}

	@Override
	public boolean same(KeyMapping binding) {
		// Special implementation which is aware of the key conflict context.
		if (binding instanceof FabricKeyMapping other) {
			return realKey.equals(other.realKey) && modifiers == other.modifiers &&
				(context.conflicts(other.context) || other.context.conflicts(context));
		} else {
			// This ensures symmetry between conflicts, as regular keybinds see this one as
			// being unbound and not conflicting.
			return false;
		}
	}

	@Override
	public boolean isUnbound() {
		return this.realKey.equals(InputConstants.UNKNOWN);
	}

	@Override
	public boolean matches(int keyCode, int scanCode) {
		return this.realKey.getType() == InputConstants.Type.KEYSYM &&
			this.realKey.getValue() == keyCode &&
			context.isActive() && matchesModifiers(FabricKeyModifiers.current(), false);
	}

	@Override
	public boolean matchesMouse(int button) {
		return this.realKey.getType() == InputConstants.Type.MOUSE &&
			this.realKey.getValue() == button &&
			context.isActive() && matchesModifiers(FabricKeyModifiers.current(), false);
	}

	@Override
	public Component getTranslatedKeyMessage() {
		Component keyName = KeyNameUtil.getKeyDisplayName(this.realKey);
		if (isUnbound()) {
			return keyName;
		}
		return FabricKeyModifiers.getCombinedName(modifiers, keyName);
	}

	@Override
	public boolean isDefault() {
		return this.realKey.equals(getDefaultKey()) && modifiers == defaultModifiers;
	}

	@Override
	public String saveString() {
		return this.realKey.getName();
	}

	@Override
	public boolean isActiveAndMatches(InputConstants.Key key, boolean allowExtraModifiers) {
		return !isUnbound() &&
			this.realKey.equals(key) &&
			context.isActive() && matchesModifiers(FabricKeyModifiers.current(), allowExtraModifiers);
	}

	private boolean matchesModifiers(int pressed, boolean allowExtraModifiers) {
		if (this.realKey.getType() == InputConstants.Type.KEYSYM) {
			pressed &= ~FabricKeyModifiers.forKey(this.realKey.getValue());
		}
		pressed = FabricKeyModifiers.normalize(pressed);
		if (allowExtraModifiers || (modifiers == 0 && context.conflicts(JeiKeyConflictContext.IN_GAME))) {
			return (pressed & modifiers) == modifiers;
		}
		return pressed == modifiers;
	}

	@Override
	public InputConstants.Key getBoundKey() {
		return realKey;
	}

	private void hideFromMinecraftClickDispatch() {
		// Keep the parent binding unbound and rebuild the static key map so hidden JEI mappings
		// do not steal clicks from vanilla mappings like attack/use.
		super.setKey(InputConstants.UNKNOWN);
		KeyMapping.resetMapping();
	}
}
