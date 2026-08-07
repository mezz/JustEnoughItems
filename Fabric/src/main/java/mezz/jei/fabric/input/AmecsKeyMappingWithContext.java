package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyMappingWithKeyModifiers;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifierCombination;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiersApi;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public class AmecsKeyMappingWithContext extends AmecsKeyMappingWithKeyModifiers {
	private final JeiKeyConflictContext context;

	public AmecsKeyMappingWithContext(String id, InputConstants.Type type, int code, Category category, AmecsKeyModifierCombination defaultModifiers, JeiKeyConflictContext context) {
		super(id, type, code, category, defaultModifiers);
		this.context = context;
	}

	@Override
	public boolean same(KeyMapping binding) {
		// Special implementation which is aware of the key conflict context.
		if (binding instanceof AmecsKeyMappingWithContext other) {
			return key.equals(KeyBindingHelper.getBoundKeyOf(other)) &&
				(context.conflicts(other.context) || other.context.conflicts(context));
		} else {
			// Regular mappings do not expose a JEI conflict context, so use the vanilla
			// comparison and keep conflict checks symmetric in the Controls screen.
			return super.same(binding);
		}
	}

	@Override
	public void setKey(InputConstants.Key key) {
		super.setKey(key);
		if (key.equals(InputConstants.UNKNOWN)) {
			AmecsKeyModifiersApi.getBoundModifiers(this).unset();
		}
	}

	public boolean isContextActive() {
		return context.isActive();
	}
}
