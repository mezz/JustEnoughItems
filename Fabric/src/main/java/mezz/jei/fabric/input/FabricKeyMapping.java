package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class FabricKeyMapping extends KeyMapping {
	protected InputConstants.Key realKey;
	protected final JeiKeyConflictContext context;

	public FabricKeyMapping(
		String description,
		InputConstants.Type type,
		int keyCode,
		Category category,
		JeiKeyConflictContext context
	) {
		super(description, type, keyCode, category);
		this.realKey = KeyMappingHelper.getBoundKeyOf(this);
		this.context = context;
		hideFromMinecraftClickDispatch();
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
			return realKey.equals(other.realKey) &&
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

	private void hideFromMinecraftClickDispatch() {
		// Keep the parent binding unbound and rebuild the static key map so hidden JEI mappings
		// do not steal clicks from vanilla mappings like attack/use.
		super.setKey(InputConstants.UNKNOWN);
		KeyMapping.resetMapping();
	}
}
