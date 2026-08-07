package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public class FabricKeyMapping extends KeyMapping {
	protected final JeiKeyConflictContext context;

	public FabricKeyMapping(
		String description,
		InputConstants.Type type,
		int keyCode,
		Category category,
		JeiKeyConflictContext context
	) {
		// Ensure the default key is set correctly (it is final).
		super(description, type, keyCode, category);
		this.context = context;
	}

	@Override
	public boolean same(KeyMapping binding) {
		// Special implementation which is aware of the key conflict context.
		if (binding instanceof FabricKeyMapping other) {
			return key.equals(KeyBindingHelper.getBoundKeyOf(other)) &&
				(context.conflicts(other.context) || other.context.conflicts(context));
		} else {
			// Regular mappings do not expose a JEI conflict context, so use the vanilla
			// comparison and keep conflict checks symmetric in the Controls screen.
			return super.same(binding);
		}
	}

	public boolean isContextActive() {
		return context.isActive();
	}
}
