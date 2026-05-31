package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifiers;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

import java.util.List;

public class AmecsJeiKeyMapping extends AbstractJeiKeyMapping {
	protected final KeyMapping amecsMapping;

	public AmecsJeiKeyMapping(AmecsKeyBindingWithContext amecsMapping, JeiKeyConflictContext context) {
		super(context);
		this.amecsMapping = amecsMapping;
	}

	@Override
	protected KeyMapping getMapping() {
		return this.amecsMapping;
	}

	@Override
	public boolean isActiveAndMatches(InputConstants.Key key) {
		if (isUnbound()) {
			return false;
		}
		if (!KeyBindingHelper.getBoundKeyOf(this.amecsMapping).equals(key)) {
			return false;
		}
		if (!context.isActive()) {
			return false;
		}

		KeyModifiers modifiers = KeyBindingUtils.getBoundModifiers(this.amecsMapping);
		List<JeiKeyModifier> jeiKeyModifiers = AmecsHelper.getJeiModifiers(modifiers);
		for (JeiKeyModifier jeiKeyModifier : jeiKeyModifiers) {
			if (!jeiKeyModifier.isActive(context)) {
				return false;
			}
		}
		return true;
	}
}
