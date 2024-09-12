package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public class FabricKeyMapping extends KeyMapping {
	protected InputConstants.Key realKey;
	protected final JeiKeyConflictContext context;

	public FabricKeyMapping(
		String description,
		InputConstants.Type type,
		int keyCode,
		String category,
		JeiKeyConflictContext context
	) {
		// Ensure the default key is set correctly (it is final).
		super(description, type, keyCode, category);
		this.realKey = KeyBindingHelper.getBoundKeyOf(this);
		this.context = context;
		// Overwrite the parent's key variable so it doesn't block other keybinds.
		super.setKey(InputConstants.UNKNOWN);
	}

	// Override all methods that would otherwise interact with super.key so displaying
	// and rebinding work correctly. This cannot work for the static methods that
	// count clicks and monitor presses, but JEI doesn't use them anyway.

	@Override
	public void setKey(InputConstants.Key key) {
		this.realKey = key;
	}

	@Override
	public boolean same(KeyMapping binding) {
		// Special implementation which is aware of the key conflict context.
		if (binding instanceof FabricKeyMapping other) {
			return realKey.equals(KeyBindingHelper.getBoundKeyOf(other)) &&
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
		return this.realKey.getDisplayName();
	}

	@Override
	public boolean isDefault() {
		return this.realKey.equals(getDefaultKey());
	}

	@Override
	public String saveString() {
		return this.realKey.getName();
	}
}
