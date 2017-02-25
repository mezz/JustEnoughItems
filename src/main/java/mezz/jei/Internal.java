package mezz.jei;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import mezz.jei.gui.recipes.IngredientLookupMemory;
import mezz.jei.util.ModIdUtil;
import mezz.jei.util.StackHelper;
import mezz.jei.util.color.ColorNamer;

/**
 * For JEI internal use only, these are normally accessed from the API.
 */
public class Internal {

	private static final ModIdUtil modIdUtil = new ModIdUtil();
	@Nullable
	private static StackHelper stackHelper;
	@Nullable
	private static JeiHelpers helpers;
	@Nullable
	private static JeiRuntime runtime;
	@Nullable
	private static IngredientRegistry ingredientRegistry;
	@Nullable
	private static ColorNamer colorNamer;
	@Nullable
	private static IngredientLookupMemory ingredientLookupMemory;

	private Internal() {

	}

	public static StackHelper getStackHelper() {
		Preconditions.checkState(stackHelper != null, "StackHelper has not been created yet.");
		return stackHelper;
	}

	public static void setStackHelper(StackHelper stackHelper) {
		Internal.stackHelper = stackHelper;
	}

	public static ModIdUtil getModIdUtil() {
		return modIdUtil;
	}

	public static JeiHelpers getHelpers() {
		Preconditions.checkState(helpers != null, "JeiHelpers has not been created yet.");
		return helpers;
	}

	public static void setHelpers(JeiHelpers helpers) {
		Internal.helpers = helpers;
	}

	@Nullable
	public static JeiRuntime getRuntime() {
		return runtime;
	}

	public static void setRuntime(JeiRuntime runtime) {
		JeiRuntime jeiRuntime = Internal.runtime;
		if (jeiRuntime != null) {
			jeiRuntime.close();
		}
		Internal.runtime = runtime;
	}

	public static IngredientRegistry getIngredientRegistry() {
		Preconditions.checkState(ingredientRegistry != null, "Ingredient Registry has not been created yet.");
		return ingredientRegistry;
	}

	public static void setIngredientRegistry(@Nullable IngredientRegistry ingredientRegistry) {
		Internal.ingredientRegistry = ingredientRegistry;
	}

	public static ColorNamer getColorNamer() {
		Preconditions.checkState(colorNamer != null, "Color Namer has not been created yet.");
		return colorNamer;
	}

	public static void setColorNamer(ColorNamer colorNamer) {
		Internal.colorNamer = colorNamer;
	}

	public static IngredientLookupMemory getIngredientLookupMemory() {
		Preconditions.checkState(ingredientLookupMemory != null, "Ingredient Lookup Memory has not been created yet.");
		return ingredientLookupMemory;
	}

	public static void setIngredientLookupMemory(IngredientLookupMemory ingredientLookupMemory) {
		Internal.ingredientLookupMemory = ingredientLookupMemory;
	}
}
