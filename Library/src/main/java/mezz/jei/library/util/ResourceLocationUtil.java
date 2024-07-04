package mezz.jei.library.util;

import net.minecraft.resources.ResourceLocation;

public class ResourceLocationUtil {
	public static String sanitizePath(String path) {
		StringBuilder s = new StringBuilder(path.length());
		for (char c : path.toCharArray()) {
			if (ResourceLocation.validPathChar(c)) {
				s.append(c);
			} else {
				s.append('_');
			}
		}
		return s.toString();
	}
}
