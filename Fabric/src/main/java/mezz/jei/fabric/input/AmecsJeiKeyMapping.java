package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifierCombination;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiersApi;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.network.chat.Component;

public class AmecsJeiKeyMapping extends FabricJeiKeyMapping<AmecsKeyMappingWithContext> {
	public AmecsJeiKeyMapping(AmecsKeyMappingWithContext mapping) {
		super(mapping);
	}

	@Override
	public Component getTranslatedKeyMessage() {
		InputConstants.Key key = KeyBindingHelper.getBoundKeyOf(this.mapping);
		AmecsKeyModifierCombination combination = AmecsKeyModifiersApi.getBoundModifiers(this.mapping);
		return AmecsHelper.getCombinedName(combination, key);
	}
}
