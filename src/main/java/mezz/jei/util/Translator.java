package mezz.jei.util;

import java.util.IllegalFormatException;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.translation.I18n;

public final class Translator {
	private Translator() {
	}

	public static String translateToLocal(String key) {
		if (I18n.canTranslate(key)) {
			return I18n.translateToLocal(key);
		} else {
			return I18n.translateToFallback(key);
		}
	}

	public static String translateToLocalFormatted(String key, Object... format) {
		String s = translateToLocal(key);
		try {
			return String.format(s, format);
		} catch (IllegalFormatException e) {
			String errorMessage = "Format error: " + s;
			Log.error(errorMessage, e);
			return errorMessage;
		}
	}

	public static String toLowercaseWithLocale(String string) {

		return string.toLowerCase(getLocale());
	}

	private static Locale getLocale() {
		Minecraft minecraft = Minecraft.getMinecraft();
		//noinspection ConstantConditions
		if (minecraft != null) {
			return minecraft.getLanguageManager().getCurrentLanguage().getJavaLocale();
		} else {
			return Locale.getDefault();
		}
	}
}
