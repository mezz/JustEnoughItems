package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifiers;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public class AmecsKeyBindingWithContext extends AmecsKeyBinding implements ContextAwareKeyMapping {
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
			// Regular mappings do not expose a JEI conflict context, so use the vanilla
			// comparison and keep conflict checks symmetric in the Controls screen.
			return super.same(binding);
		}
	}

	@Override
	public void setKey(InputConstants.Key key) {
		super.setKey(key);
		if (key.equals(InputConstants.UNKNOWN)) {
			KeyBindingUtils.getBoundModifiers(this).unset();
		}
	}

	@Override
	public boolean isContextActive() {
		return context.isActive();
	}
}
