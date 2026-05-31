package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifiers;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public class AmecsKeyBindingWithContext extends AmecsKeyBinding {
	private final JeiKeyConflictContext context;

	public AmecsKeyBindingWithContext(String id, InputConstants.Type type, int code, String category, KeyModifiers defaultModifiers, JeiKeyConflictContext context) {
		super(id, type, code, category, defaultModifiers);
		this.context = context;
	}

	@Override
	public boolean same(KeyMapping binding) {
		// Special implementation which is aware of the key conflict context.
		if (binding instanceof AmecsKeyBindingWithContext other) {
			return KeyBindingHelper.getBoundKeyOf(this).equals(KeyBindingHelper.getBoundKeyOf(other)) &&
				(context.conflicts(other.context) || other.context.conflicts(context));
		} else {
			// This ensures symmetry between conflicts, as regular keybinds see this one as
			// being unbound and not conflicting.
			return false;
		}
	}
}
