package mezz.jei;

import mezz.jei.util.StackHelper;
import mezz.jei.util.color.ColorNamer;

/** For JEI internal use only, these are normally accessed from the API. */
public class Internal {
	private static final JeiHelpers helpers = new JeiHelpers();
	private static JeiRuntime runtime;
	private static ItemRegistry itemRegistry;
	private static ColorNamer colorNamer;

	private Internal() {

	}

	public static JeiHelpers getHelpers() {
		return helpers;
	}

	public static StackHelper getStackHelper() {
		return helpers.getStackHelper();
	}

	public static JeiRuntime getRuntime() {
		return runtime;
	}

	public static void setRuntime(JeiRuntime runtime) {
		Internal.runtime = runtime;
	}

	public static ItemRegistry getItemRegistry() {
		return itemRegistry;
	}

	public static void setItemRegistry(ItemRegistry itemRegistry) {
		Internal.itemRegistry = itemRegistry;
	}

	public static ColorNamer getColorNamer() {
		return colorNamer;
	}

	public static void setColorNamer(ColorNamer colorNamer) {
		Internal.colorNamer = colorNamer;
	}
}
