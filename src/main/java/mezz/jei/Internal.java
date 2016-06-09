package mezz.jei;

import javax.annotation.Nonnull;

import mezz.jei.util.StackHelper;
import mezz.jei.util.color.ColorNamer;

/** For JEI internal use only, these are normally accessed from the API. */
public class Internal {
	@Nonnull
	private static JeiHelpers helpers = new JeiHelpers();
	private static JeiRuntime runtime;
	private static ItemRegistry itemRegistry;
	private static ColorNamer colorNamer;

	private Internal() {

	}

	@Nonnull
	public static JeiHelpers getHelpers() {
		return helpers;
	}

	public static void setHelpers(@Nonnull JeiHelpers helpers) {
		Internal.helpers = helpers;
	}

	@Nonnull
	public static StackHelper getStackHelper() {
		return helpers.getStackHelper();
	}

	public static JeiRuntime getRuntime() {
		return runtime;
	}

	public static void setRuntime(JeiRuntime runtime) {
		if (Internal.runtime != null) {
			Internal.runtime.close();
		}
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
