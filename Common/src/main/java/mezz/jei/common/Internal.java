package mezz.jei.common;

import com.google.common.base.Preconditions;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.runtime.JeiHelpers;
import mezz.jei.common.runtime.JeiRuntime;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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

	public static Optional<JeiRuntime> getRuntime() {
		return Optional.ofNullable(runtime);
	}

	public static void setRuntime(@Nullable JeiRuntime runtime) {
		Internal.runtime = runtime;
	}

	public static RegisteredIngredients getRegisteredIngredients() {
		Preconditions.checkState(registeredIngredients != null, "RegisteredIngredients has not been created yet.");
		return registeredIngredients;
	}

	public static void setRegisteredIngredients(RegisteredIngredients registeredIngredients) {
		Internal.registeredIngredients = registeredIngredients;
	}

	public static IConnectionToServer getServerConnection() {
		Preconditions.checkState(serverConnection != null, "Server Connection has not been created yet.");
		return serverConnection;
	}

	public static void setServerConnection(IConnectionToServer serverConnection) {
		Internal.serverConnection = serverConnection;
	}
}
