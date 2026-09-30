package mezz.jei.common.gui;

import net.mezzdev.config.api.value.IConfigValue;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Selects GUI sprites for the configured light or dark theme.
 */
public final class JeiTheme {
	private static @Nullable IConfigValue<Boolean> darkModeEnabled;

	private JeiTheme() {

	}

	public static void setConfigValue(IConfigValue<Boolean> darkModeEnabled) {
		JeiTheme.darkModeEnabled = Objects.requireNonNull(darkModeEnabled);
	}

	public static boolean isDarkModeEnabled() {
		IConfigValue<Boolean> config = darkModeEnabled;
		return config != null && config.get();
	}

	public static void setDarkModeEnabled(boolean enabled) {
		Objects.requireNonNull(darkModeEnabled, "JEI's theme config is not registered").set(enabled);
	}

	public static TextureAtlasSprite getSprite(TextureAtlas atlas, Identifier light, Identifier dark) {
		if (isDarkModeEnabled()) {
			TextureAtlasSprite sprite = atlas.getSprite(dark);
			if (sprite != atlas.missingSprite()) {
				return sprite;
			}
		}
		return atlas.getSprite(light);
	}
}
