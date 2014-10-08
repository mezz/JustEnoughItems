package mezz.jei.util;

import net.minecraft.client.gui.inventory.GuiContainer;

import java.lang.reflect.Field;

public class Reflection {
	private static Object getDeclaredField(Class objClass, Object obj, String name) {
		try {
			Field f = objClass.getDeclaredField(name);
			if (f.isAccessible())
				throw new IllegalArgumentException("Attempting reflection on an accessible field: " + name + " on " + obj);

			f.setAccessible(true);
			Object fieldValue = f.get(obj);
			f.setAccessible(false);
			return fieldValue;
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException("Failed reflection on a field: " + name + " on " + obj, e);
		}
	}

	public static Integer[] getDimensions(GuiContainer guiContainer) {
		return new Integer[] {
				(Integer)getDeclaredField(GuiContainer.class, guiContainer, "guiLeft"),
				(Integer)getDeclaredField(GuiContainer.class, guiContainer, "guiTop"),
				(Integer)getDeclaredField(GuiContainer.class, guiContainer, "xSize"),
				(Integer)getDeclaredField(GuiContainer.class, guiContainer, "ySize"),
		};
	}
}
