package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifiers;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AmecsJeiKeyMapping extends AbstractJeiKeyMapping {
	protected final AmecsKeyBindingWithContext amecsMapping;

	public AmecsJeiKeyMapping(AmecsKeyBindingWithContext amecsMapping, JeiKeyConflictContext context) {
		super(context);
		this.amecsMapping = amecsMapping;
	}

	@Override
	protected KeyMapping getMapping() {
		return this.amecsMapping;
	}

	@Override
	protected InputConstants.Key getBoundKey() {
		return this.amecsMapping.getRealKey();
	}

	@Override
	public boolean isActiveAndMatches(InputConstants.Key key) {
		if (isUnbound()) {
			return false;
		}
		if (!this.amecsMapping.getRealKey().equals(key)) {
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

	@Override
	public Component getTranslatedKeyMessage() {
		InputConstants.Key key = this.amecsMapping.getRealKey();
		KeyModifiers modifiers = KeyBindingUtils.getBoundModifiers(this.amecsMapping);
		return AmecsHelper.getCombinedName(modifiers, key);
	}
}
