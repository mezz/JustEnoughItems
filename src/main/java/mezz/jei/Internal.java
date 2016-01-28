package mezz.jei;

import mezz.jei.util.StackHelper;

/** For JEI internal use only, these are normally accessed from the API. */
public class Internal {
	private static JeiHelpers helpers;
	private static JeiRuntime runtime;
	private static ItemRegistry itemRegistry;

	private Internal() {

	}

	public static JeiHelpers getHelpers() {
		return helpers;
	}

	public static StackHelper getStackHelper() {
		return helpers.getStackHelper();
	}

	public static void setHelpers(JeiHelpers helpers) {
		Internal.helpers = helpers;
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
}
