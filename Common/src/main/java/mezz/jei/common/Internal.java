package mezz.jei.common;

import com.google.common.base.Preconditions;
import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.common.config.InternalKeyMappings;
import mezz.jei.common.config.WorldConfig;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.runtime.JeiRuntime;
import mezz.jei.core.config.IWorldConfig;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * For JEI internal use only, these are normally accessed from the API.
 */
public final class Internal {
	@Nullable
	private static JeiRuntime runtime;
	@Nullable
	private static IRegisteredIngredients registeredIngredients;
	@Nullable
	private static Textures textures;
	@Nullable
	private static IConnectionToServer serverConnection;
	@Nullable
	private static InternalKeyMappings keyMappings;
	@Nullable
	private static IWorldConfig worldConfig;

	private Internal() {

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

	public static IRegisteredIngredients getRegisteredIngredients() {
		Preconditions.checkState(registeredIngredients != null, "RegisteredIngredients has not been created yet.");
		return registeredIngredients;
	}

	public static void setRegisteredIngredients(IRegisteredIngredients registeredIngredients) {
		Internal.registeredIngredients = registeredIngredients;
	}

	public static IConnectionToServer getServerConnection() {
		Preconditions.checkState(serverConnection != null, "Server Connection has not been created yet.");
		return serverConnection;
	}

	public static void setServerConnection(IConnectionToServer serverConnection) {
		Internal.serverConnection = serverConnection;
	}

	public static InternalKeyMappings getKeyMappings() {
		Preconditions.checkState(keyMappings != null, "Key Mappings have not been created yet.");
		return keyMappings;
	}

	public static void setKeyMappings(InternalKeyMappings keyMappings) {
		Internal.keyMappings = keyMappings;
	}

	public static IWorldConfig getWorldConfig() {
		if (worldConfig == null) {
			worldConfig = new WorldConfig();
		}
		return worldConfig;
	}
}
