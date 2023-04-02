package mezz.jei.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.LanguageManager;

import javax.annotation.Nullable;
import java.util.Locale;

public final class Translator {
	@Nullable
	private static String cachedLocaleCode;
	@Nullable
	private static Locale cachedLocale;

	private Translator() {
	}

	public static String translateToLocal(String key) {
		return I18n.get(key);
	}

	public static String translateToLocalFormatted(String key, Object... format) {
		return I18n.get(key, format);
	}

	public static String toLowercaseWithLocale(String string) {
		return string.toLowerCase(getLocale());
	}

	@SuppressWarnings("ConstantConditions")
	private static Locale getLocale() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null) {
			return Locale.getDefault();
		}
		LanguageManager languageManager = minecraft.getLanguageManager();
		String code = languageManager.getSelected();
		if (cachedLocale == null || !code.equals(cachedLocaleCode)) {
			cachedLocaleCode = code;
			String[] splitLangCode = code.split("_", 2);
			if (splitLangCode.length == 1) { // Vanilla has some languages without underscores
				cachedLocale = new Locale(code);
			} else {
				cachedLocale = new Locale(splitLangCode[0], splitLangCode[1]);
			}
		}
		return cachedLocale;
	}
}
