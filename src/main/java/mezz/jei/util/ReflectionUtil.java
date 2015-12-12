package mezz.jei.util;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for reflection things
 * @author shadowfacts
 */
public class ReflectionUtil {

	private static Map<String, Field> fieldMap = new HashMap<>();
	private static Map<String, Method> methodMap = new HashMap<>();

	public static void initCommon() {

	}

	public static void initClient() {
		try {
			putField(GuiContainer.class.getName(), "guiLeft");
			putField(GuiContainer.class.getName(), "xSize");

			putMethod(GuiScreen.class.getName(), "renderToolTip", ItemStack.class, int.class, int.class);
		} catch (ReflectiveOperationException e) {
			System.err.println("There was a problem initializing the client-side reflection util");
			e.printStackTrace();
			FMLCommonHandler.instance().exitJava(-1, false);
		}
	}

	private static void putField(String owner, String name) throws ClassNotFoundException, NoSuchFieldException {
		Field f = Class.forName(owner).getDeclaredField(FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(owner, name, null));
		f.setAccessible(true);
		fieldMap.put(owner + "." + name, f);
	}

	private static void putMethod(String owner, String name, Class... params) throws ClassNotFoundException, NoSuchMethodException {
		Method m = Class.forName(owner).getDeclaredMethod(name, params);
		m.setAccessible(true);
		methodMap.put(owner + "." + name, m);
	}

	/**
	 * Get field value
	 * @param owner Full owner class name
	 * @param name Unobfuscated field name
	 * @param instance The instance of the owner class
	 * @return The field value
	 */
	public static Object get(String owner, String name, Object instance) {
		try {
			return fieldMap.get(owner + "." + name).get(instance);
		} catch (IllegalAccessException ignored) {} // never happens, we allow access in putField
		return null;
	}

	/**
	 * Get {@code int} field value
	 * @param owner Full owner class name
	 * @param name Unobfuscated field name
	 * @param instance The instance of the owner class
	 * @return The field value
	 */
	public static int getInt(String owner, String name, Object instance) {
		try {
			return fieldMap.get(owner + "." + name).getInt(instance);
		} catch (IllegalAccessException ignored) {} // never happens, we allow access in putField
		return Integer.MIN_VALUE;
	}

	public static Object invokeMethod(String owner, String name, Object instance, Object... args) throws InvocationTargetException {
		try {
			return methodMap.get(owner + "." + name).invoke(instance, args);
		} catch (IllegalAccessException ignored) {} // never happens, we allow access in putField
		return null;
	}

}
