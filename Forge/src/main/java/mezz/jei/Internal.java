package mezz.jei;

import com.google.common.base.Preconditions;
import mezz.jei.color.ColorNamer;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.gui.textures.Textures;
import mezz.jei.ingredients.IngredientVisibility;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.runtime.JeiRuntime;
import org.jetbrains.annotations.Nullable;

/**
 * For JEI internal use only, these are normally accessed from the API.
 */
public final class Internal {
	@Nullable
	private static JeiHelpers helpers;
	@Nullable
	private static JeiRuntime runtime;
	@Nullable
	private static RegisteredIngredients registeredIngredients;
	@Nullable
	private static ColorNamer colorNamer;
	@Nullable
	private static IngredientVisibility ingredientVisibility;
	@Nullable
	private static Textures textures;
	@Nullable
	private static IConnectionToServer serverConnection;

	private Internal() {

	}

	public static JeiHelpers getHelpers() {
		Preconditions.checkState(helpers != null, "JeiHelpers has not been created yet.");
		return helpers;
	}

	public static void setHelpers(JeiHelpers helpers) {
		Internal.helpers = helpers;
	}

	public static Textures getTextures() {
		Preconditions.checkState(textures != null, "Textures has not been created yet.");
		return textures;
	}

	public static void setTextures(Textures textures) {
		Internal.textures = textures;
	}

	@Nullable
	public static JeiRuntime getRuntime() {
		return runtime;
	}

	public static void setRuntime(@Nullable JeiRuntime runtime) {
		Internal.runtime = runtime;
	}

	public static RegisteredIngredients getRegisteredIngredients() {
		Preconditions.checkState(registeredIngredients != null, "RegisteredIngredients has not been created yet.");
		return registeredIngredients;
	}

	public static void setIngredientManager(RegisteredIngredients registeredIngredients) {
		Internal.registeredIngredients = registeredIngredients;
	}

	public static IngredientVisibility getIngredientVisibility() {
		Preconditions.checkState(ingredientVisibility != null, "Ingredient Visibility has not been created yet.");
		return ingredientVisibility;
	}

	public static void setIngredientVisibility(IngredientVisibility ingredientVisibility) {
		Internal.ingredientVisibility = ingredientVisibility;
	}

	public static ColorNamer getColorNamer() {
		Preconditions.checkState(colorNamer != null, "Color Namer has not been created yet.");
		return colorNamer;
	}

	public static void setColorNamer(ColorNamer colorNamer) {
		Internal.colorNamer = colorNamer;
	}

	public static IConnectionToServer getServerConnection() {
		Preconditions.checkState(serverConnection != null, "Server Connection has not been created yet.");
		return serverConnection;
	}

	public static void setServerConnection(IConnectionToServer serverConnection) {
		Internal.serverConnection = serverConnection;
	}
}
