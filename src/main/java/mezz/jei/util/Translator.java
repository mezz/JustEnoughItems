package mezz.jei.util;

import java.util.IllegalFormatException;

import net.minecraft.util.StatCollector;

public class Translator {
	public static String translateToLocal(String key) {
		if (StatCollector.canTranslate(key)) {
			return StatCollector.translateToLocal(key);
		} else {
			return StatCollector.translateToFallback(key);
		}
	}

	public static String translateToLocalFormatted(String key, Object... format) {
		String s = translateToLocal(key);
		try {
			return String.format(s, format);
		} catch (IllegalFormatException var5) {
			return "Format error: " + s;
		}
	}
}
