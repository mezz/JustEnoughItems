package mezz.jei.library.util;

import net.minecraft.resources.Identifier;

public class ResourceLocationUtil {
	public static String sanitizePath(String path) {
		char[] charArray = path.toCharArray();
		boolean valid = true;
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];
			if (!Identifier.validPathChar(c)) {
				charArray[i] = '.';
				valid = false;
			}
		}
		if (valid) {
			return path;
		}
		return new String(charArray);
	}
}
