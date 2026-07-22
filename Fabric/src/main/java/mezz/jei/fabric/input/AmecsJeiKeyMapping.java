package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifierCombination;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiersApi;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AmecsJeiKeyMapping extends AbstractJeiKeyMapping {
	protected final AmecsKeyMappingWithContext amecsMapping;

	public AmecsJeiKeyMapping(AmecsKeyMappingWithContext amecsMapping, JeiKeyConflictContext context) {
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
		if (!this.amecsMapping.getRealKey().equals(key)) {
			return false;
		}
		if (!context.isActive()) {
			return false;
		}

		AmecsKeyModifierCombination combination = AmecsKeyModifiersApi.getBoundModifiers(this.amecsMapping);
		List<JeiKeyModifier> jeiKeyModifiers = AmecsHelper.getJeiModifiers(combination);
		for (JeiKeyModifier jeiKeyModifier : jeiKeyModifiers) {
			if (!jeiKeyModifier.isActive(context)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Component getTranslatedKeyMessage() {
		AmecsKeyModifierCombination combination = AmecsKeyModifiersApi.getBoundModifiers(this.amecsMapping);
		return AmecsHelper.getCombinedName(combination, this.amecsMapping.getRealKey());
	}
}
