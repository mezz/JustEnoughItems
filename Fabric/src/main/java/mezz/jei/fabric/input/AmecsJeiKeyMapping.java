package mezz.jei.fabric.input;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifiers;
import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class AmecsJeiKeyMapping extends AbstractJeiKeyMapping {
	protected final AmecsKeyBinding amecsMapping;

	public AmecsJeiKeyMapping(AmecsKeyBinding amecsMapping, JeiKeyConflictContext context) {
		super(context);
		this.amecsMapping = amecsMapping;
	}

	protected AmecsKeyBinding getMapping() {
		return this.amecsMapping;
	}

	protected InputConstants.Key getMappedKey() {
		return KeyBindingHelper.getBoundKeyOf(this.amecsMapping);
	}

	@Override
	public boolean isActiveAndMatches(InputConstants.Key key) {
		if (isUnbound()) {
			return false;
		}
		if (!this.getMappedKey().equals(key)) {
			return false;
		}
		if (!context.isActive()) {
			return false;
		}

		KeyModifiers modifier = KeyBindingUtils.getBoundModifiers(this.amecsMapping);
		if (modifier.getControl() && !JeiKeyModifier.CONTROL_OR_COMMAND.isActive(context)) {
			return false;
		}
		if (modifier.getShift() && !JeiKeyModifier.SHIFT.isActive(context)) {
			return false;
		}
		if (modifier.getAlt() && !JeiKeyModifier.ALT.isActive(context)) {
			return false;
		}
		if (modifier.isUnset() && !JeiKeyModifier.NONE.isActive(context)) {
			return false;
		}
		return true;
	}
}
