package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifiers;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.network.chat.Component;

public class AmecsJeiKeyMapping extends FabricJeiKeyMapping<AmecsKeyBindingWithContext> {
	public AmecsJeiKeyMapping(AmecsKeyBindingWithContext mapping) {
		super(mapping);
	}

	@Override
	public Component getTranslatedKeyMessage() {
		InputConstants.Key key = KeyBindingHelper.getBoundKeyOf(this.mapping);
		KeyModifiers modifiers = KeyBindingUtils.getBoundModifiers(this.mapping);
		return AmecsHelper.getCombinedName(modifiers, key);
	}
}
