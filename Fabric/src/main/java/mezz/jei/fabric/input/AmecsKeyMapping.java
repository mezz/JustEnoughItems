package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyMappingWithKeyModifiers;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifier;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifierCombination;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiers;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiersApi;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

/** An AMECS binding that observes JEI's input contexts. */
public class AmecsKeyMapping extends AmecsKeyMappingWithKeyModifiers implements ContextAwareKeyMapping {
	private final JeiKeyConflictContext context;

	public AmecsKeyMapping(String description, InputConstants.Type type, int code, String category, AmecsKeyModifierCombination modifiers, JeiKeyConflictContext context) {
		super(description, type, code, category, modifiers);
		this.context = context;
	}

	@Override
	public InputConstants.Key getBoundKey() {
		return KeyBindingHelper.getBoundKeyOf(this);
	}

	boolean isContextActive() {
		return context.isActive();
	}

	@Override
	public void resetModifiersToDefault() {
		resetKeyBinding();
	}

	@Override
	public boolean same(KeyMapping other) {
		return super.same(other) && (!(other instanceof AmecsKeyMapping mapping) ||
			context.conflicts(mapping.context) || mapping.context.conflicts(context));
	}

	@Override
	public boolean matches(int keyCode, int scanCode) {
		return isActiveAndMatches(InputConstants.getKey(keyCode, scanCode), false);
	}

	@Override
	public boolean matchesMouse(int button) {
		return isActiveAndMatches(InputConstants.Type.MOUSE.getOrCreate(button), false);
	}

	@Override
	public boolean isActiveAndMatches(InputConstants.Key input, boolean allowExtraModifiers) {
		if (isUnbound() || !getBoundKey().equals(input) || !context.isActive()) {
			return false;
		}
		AmecsKeyModifierCombination pressed = new AmecsKeyModifierCombination();
		pressed.copyFrom(AmecsKeyModifierCombination.getCurrentlyPressed());
		AmecsKeyModifier boundKeyModifier = AmecsKeyModifiers.fromKey(getBoundKey());
		if (boundKeyModifier != null) {
			pressed.set(boundKeyModifier, false);
		}
		AmecsKeyModifierCombination modifiers = AmecsKeyModifiersApi.getBoundModifiers(this);
		if (allowExtraModifiers || (modifiers.isUnset() && context.conflicts(JeiKeyConflictContext.IN_GAME))) {
			// AMECS 1.0.2's contains compares the complete combination.
			for (AmecsKeyModifier modifier : AmecsKeyModifiers.getAll()) {
				if (modifiers.get(modifier) && !pressed.get(modifier)) {
					return false;
				}
			}
			return true;
		}
		return pressed.equals(modifiers);
	}
}
