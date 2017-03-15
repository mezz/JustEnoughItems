package mezz.jei;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import mezz.jei.color.ColorNamer;
import mezz.jei.gui.ingredients.IngredientLookupMemory;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.startup.ModIdHelper;
import mezz.jei.startup.StackHelper;

/**
 * For JEI internal use only, these are normally accessed from the API.
 */
public final class Internal {

	private static final ModIdHelper MOD_ID_HELPER = new ModIdHelper();
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

	public static ModIdHelper getModIdHelper() {
		return MOD_ID_HELPER;
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

	public static void setIngredientRegistry(IngredientRegistry ingredientRegistry) {
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
