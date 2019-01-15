package mezz.jei.util;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;

public final class Translator {
	private Translator() {
	}

	public static String translateToLocal(String key) {
		return I18n.format(key);
	}

	public static String translateToLocalFormatted(String key, Object... format) {
		return I18n.format(key, format);
	}

	public static String toLowercaseWithLocale(String string) {
		return string.toLowerCase(getLocale());
	}

	@SuppressWarnings("ConstantConditions")
	private static Locale getLocale() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft != null) {
			LanguageManager languageManager = minecraft.getLanguageManager();
			if (languageManager != null) {
				Language currentLanguage = languageManager.getCurrentLanguage();
				if (currentLanguage != null) {
					return currentLanguage.getJavaLocale();
				}
			}
		}
		return Locale.getDefault();
	}
}
