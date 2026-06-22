package mezz.jei.fabric.input;

import de.siphalor.amecs.key_modifiers.impl.DefaultKeyModifier;
import de.siphalor.amecs.key_modifiers.impl.ModifierPrefixTextProvider;
import org.jetbrains.annotations.Nullable;

public class AmecsJeiKeyModifier extends DefaultKeyModifier {
	private final String translationKey;

	public AmecsJeiKeyModifier(String translationKey, @Nullable Integer legacyId, int... keyCodes) {
		super(null, legacyId, keyCodes);
		this.translationKey = translationKey;
	}

	@Override
	public String getTranslationKey() {
		return translationKey;
	}

	@Override
	public ModifierPrefixTextProvider getTextProvider() {
		return new ModifierPrefixTextProvider(this);
	}
}
